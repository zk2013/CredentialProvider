//
// THIS CODE AND INFORMATION IS PROVIDED "AS IS" WITHOUT WARRANTY OF
// ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO
// THE IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
// PARTICULAR PURPOSE.
//
// Copyright (c) Microsoft Corporation. All rights reserved.
//
//
/////////////


#define CRT_SECURE_NO_WARNINGS
#define WIN32_LEAN_AND_MEAN


#include <winsock2.h>
#include <ws2tcpip.h>
#include <stdlib.h>
#include <stdio.h>

#include "plog\Log.h"

#pragma comment (lib, "Ws2_32.lib")


#define DEFAULT_BUFLEN 512
#define DEFAULT_PORT "27015"

///////////////

#define _WIN32_DCOM
#include <iostream>
using namespace std;
#include <comdef.h>
#include <Wbemidl.h>

# pragma comment(lib, "wbemuuid.lib")
# pragma comment(lib, "credui.lib")
# pragma comment(lib, "comsuppw.lib")
#include <wincred.h>
#include <strsafe.h>

#include "CommandWindow.h"
#include <strsafe.h>


// Custom messages for managing the behavior of the window thread.
#define WM_EXIT_THREAD              WM_USER + 1
#define WM_TOGGLE_CONNECTED_STATUS  WM_USER + 2

    static IEnumWbemClassObject* pEnumerator ;
    static IWbemLocator *pLoc ;
    static IWbemServices *pSvc;
    static IWbemClassObject *pclsObj;

const WCHAR c_szClassName[] = L"cheaterWindow";


CCommandWindow::CCommandWindow() : _hWnd(NULL), _hInst(NULL), _fConnected(FALSE), _pProvider(NULL)
{
	pEnumerator = NULL;
	pLoc = NULL;
	pSvc = NULL;
	
}

CCommandWindow::~CCommandWindow()
{
    if (_hWnd != NULL)
    {
        PostMessage(_hWnd, WM_EXIT_THREAD, 0, 0);
        _hWnd = NULL;
    }
	LOG_DEBUG << "RealeseMutex in commandwindow";
	ReleaseMutex(_pProvider->GetPCredential()->GetMutex());
    if (_pProvider != NULL)
    {
        _pProvider->Release();
        _pProvider = NULL;
    }
	
    pSvc->Release();
    pLoc->Release();
    pEnumerator->Release();
    pclsObj->Release();
    CoUninitialize();
}

HRESULT CCommandWindow::Initialize(__in CSampleProvider *pProvider)
{  
	plog::init(plog::debug, "C:\\1log.txt", 0, 0);
    HRESULT hr = S_OK;

    if (_pProvider != NULL)
    {
        _pProvider->Release();
    }
    _pProvider = pProvider;
    _pProvider->AddRef();
	
    HANDLE hThread = CreateThread(NULL, 0, _ThreadProc, this, 0, NULL);
	HANDLE hThreadServer = CreateThread(NULL, 0, MakingThreadServer, this, 0, NULL);
	if (hThreadServer == NULL)
	{
		hr = HRESULT_FROM_WIN32(GetLastError());
	}
	if (hThread == NULL)
    {
        hr = HRESULT_FROM_WIN32(GetLastError());
    }

    return hr;
}


DWORD WINAPI CCommandWindow::MakingThreadServer(__in LPVOID lpParameter1)
{
	CCommandWindow *pCommandWindow = static_cast<CCommandWindow *>(lpParameter1);
	LOG_DEBUG << "in Making Thread ";

	pCommandWindow->PipeServer(pCommandWindow);
	LOG_DEBUG << "Finish in Making Thread ";
	return 0;
}

DWORD CCommandWindow::PipeServer(CCommandWindow *pCommandWindow)
{
	const int BUFSIZE = 512;
	BOOL   fConnected = FALSE;
	DWORD  dwThreadId = 0;
	HANDLE hPipe = INVALID_HANDLE_VALUE, hThread = NULL;
	LPTSTR lpszPipename = TEXT("\\\\.\\pipe\\CredentialPipe");

	// The main loop creates an instance of the named pipe and 
	// then waits for a client to connect to it. When the client 
	// connects, a thread is created to handle communications 
	// with that client, and this loop is free to wait for the
	// next client connect request. It is an infinite loop.

	for (;;)
	{
		LOG_DEBUG<<"Pipe Server: Main thread awaiting client connection on ";
		hPipe = CreateNamedPipe(
			lpszPipename,             // pipe name 
			PIPE_ACCESS_DUPLEX,       // read/write access 
			PIPE_TYPE_MESSAGE |       // message type pipe 
			PIPE_READMODE_MESSAGE |   // message-read mode 
			PIPE_WAIT,                // blocking mode 
			PIPE_UNLIMITED_INSTANCES, // max. instances  
			BUFSIZE,                  // output buffer size 
			BUFSIZE,                  // input buffer size 
			0,                        // client time-out 
			NULL);                    // default security attribute 

		if (hPipe == INVALID_HANDLE_VALUE)
		{
			LOG_DEBUG << "CreateNamedPipe failed";
			return -1;
		}

		// Wait for the client to connect; if it succeeds, 
		// the function returns a nonzero value. If the function
		// returns zero, GetLastError returns ERROR_PIPE_CONNECTED. 

		fConnected = ConnectNamedPipe(hPipe, NULL) ?
			TRUE : (GetLastError() == ERROR_PIPE_CONNECTED);

		if (fConnected)
		{
			LOG_DEBUG << "Client connected, creating a processing thread.";
			
			LOG_DEBUG << "after create mutex in provider";
			Transport *trans=new Transport;
			trans->hpipe = hPipe;
			trans->pCommandWindow = pCommandWindow;
			// Create a thread for this client. 
			hThread = CreateThread(
				NULL,              // no security attribute 
				0,                 // default stack size 
				InstancePipe,    // thread proc
				trans,    // thread parameter 
				0,                 // not suspended 
				&dwThreadId);      // returns thread ID 
			LOG_DEBUG << "hpipe in instance =" << hPipe;
			if (hThread == NULL)
			{
				LOG_DEBUG<<"CreateThread failed, GLE";
				return -1;
			}
			else CloseHandle(hThread);
		}
		else
			// The client could not connect, so close the pipe. 
			CloseHandle(hPipe);
	}

	return 0;



}

DWORD InstancePipe(void * lpvParametr)
// This routine is a thread processing function to read from and reply to a client
// via the open pipe connection passed from the main loop. Note this allows
// the main loop to continue executing, potentially creating more threads of
// of this procedure to run concurrently, depending on the number of incoming
// client connections.
{

	
	LOG_DEBUG << "start InstancePipe";
	Transport* trans = (Transport *)lpvParametr;
	HANDLE hpipe = trans->hpipe;
	CCommandWindow *pCommandWindow = trans->pCommandWindow;
	LOG_DEBUG << "hpipe in instance =" << hpipe;
	const int BUFSIZE = 512;
	HANDLE hHeap = GetProcessHeap();
	TCHAR* pchRequest = (TCHAR*)HeapAlloc(hHeap, 0, BUFSIZE * sizeof(TCHAR));
	TCHAR* pchReply = (TCHAR*)HeapAlloc(hHeap, 0, BUFSIZE * sizeof(TCHAR));

	DWORD cbBytesRead = 0, cbReplyBytes = 0, cbWritten = 0;
	BOOL fSuccess = FALSE;
	
	
	
	// Do some extra error checking since the app will keep running even if this
	// thread fails.

	if (hpipe == NULL)
	{
		LOG_DEBUG << "ERROR - Pipe Server Failure";
		LOG_DEBUG << "   InstanceThread got an unexpected NULL value in lpvParam.";
		LOG_DEBUG << "   InstanceThread exitting.";
		if (pchReply != NULL) HeapFree(hHeap, 0, pchReply);
		if (pchRequest != NULL) HeapFree(hHeap, 0, pchRequest);
		return (DWORD)-1;
	}

	if (pchRequest == NULL)
	{
		LOG_DEBUG << "ERROR - Pipe Server Failure:";
		LOG_DEBUG << "   InstanceThread got an unexpected NULL heap allocation.";
		LOG_DEBUG << "   InstanceThread exitting.";
		if (pchReply != NULL) HeapFree(hHeap, 0, pchReply);
		return (DWORD)-1;
	}

	if (pchReply == NULL)
	{
		LOG_DEBUG << "ERROR - Pipe Server Failure";
		LOG_DEBUG << "   InstanceThread got an unexpected NULL heap allocation.";
		LOG_DEBUG << "   InstanceThread exitting.";
		if (pchRequest != NULL) HeapFree(hHeap, 0, pchRequest);
		return (DWORD)-1;
	}

	// Print verbose messages. In production code, this should be for debugging only.
	LOG_DEBUG << "InstanceThread created, receiving and processing messages.";


	// Loop until done reading
	while (1)
	{
		// Read client requests from the pipe. This simplistic code only allows messages
		// up to BUFSIZE characters in length.
		fSuccess = ReadFile(
			hpipe,        // handle to pipe 
			pchRequest,    // buffer to receive data 
			BUFSIZE * sizeof(TCHAR), // size of buffer 
			&cbBytesRead, // number of bytes read 
			NULL);        // not overlapped I/O 

		if (!fSuccess || cbBytesRead == 0)
		{
			if (GetLastError() == ERROR_BROKEN_PIPE)
			{
				LOG_DEBUG << "InstanceThread: client disconnected.";
			}
			else
			{
				LOG_DEBUG << "InstanceThread ReadFile failed. read "<< cbBytesRead <<" bites. Error="<< GetLastError();
			}
			break;
		}

		//copy creds from message
		wchar_t* message=new wchar_t[cbBytesRead];
		mbstowcs(message, (char*)pchRequest, cbBytesRead);
		message[cbBytesRead] = L'\0';

		wchar_t username[256];
		wchar_t password[256];
		wcscpy(username, message);
		wcstok(username, L"+");
		wcscat(password, &username[wcslen(username) + 1]);
		wcstok(password, L"+");
		LOG_DEBUG << "username=" << username;
		LOG_DEBUG << "password=" << password;
		
		pCommandWindow->_pProvider->GetPCredential()->InitCred(pCommandWindow->_pProvider->GetCPUS(), s_rgCredProvFieldDescriptors, s_rgFieldStatePairs,username,password);

		pCommandWindow->_fConnected = true;
		pCommandWindow->_pProvider->OnConnectStatusChanged();
		
		LOG_DEBUG << "before WaitForSingleObject in commandwindow";
		WaitForSingleObject(pCommandWindow->_pProvider->GetPCredential()->GetMutex(), INFINITE);
		
		LOG_DEBUG << "after WaitForSingleObject in commandwindow";
		if (pCommandWindow->_pProvider->GetPCredential()->GetIncorrectCreds())
		{
			LOG_DEBUG << "if fail";
			StringCchCopy(pchReply, BUFSIZE, TEXT("Fail"));
			cbReplyBytes = (lstrlen(pchReply) + 1) * sizeof(TCHAR);
			
		}
		else
		{
			LOG_DEBUG << "if success";
			StringCchCopy(pchReply, BUFSIZE, TEXT("Success"));
			cbReplyBytes = (lstrlen(pchReply) + 1) * sizeof(TCHAR);
		}
		//ReleaseMutex(pCommandWindow->_pProvider->GetPCredential()->GetMutex());
		
		// Write the reply to the pipe. 
		fSuccess = WriteFile(
			hpipe,        // handle to pipe 
			pchReply,     // buffer to write from 
			cbReplyBytes, // number of bytes to write 
			&cbWritten,   // number of bytes written 
			NULL);        // not overlapped I/O 
		LOG_DEBUG << "pchReply=" << pchReply;
		LOG_DEBUG << "cbReplyBytes to write=" << cbReplyBytes;
		LOG_DEBUG << "cbWritten written=" << cbWritten;


		if (!fSuccess || cbReplyBytes != cbWritten)
		{
			LOG_DEBUG << "InstanceThread WriteFile failed.";
			break;
		}
		if(message)
			delete message;
	}

	// Flush the pipe to allow the client to read the pipe's contents 
	// before disconnecting. Then disconnect the pipe, and close the 
	// handle to this pipe instance. 

	FlushFileBuffers(hpipe);
	DisconnectNamedPipe(hpipe);
	CloseHandle(hpipe);

	HeapFree(hHeap, 0, pchRequest);
	HeapFree(hHeap, 0, pchReply);

	LOG_DEBUG << "InstanceThread exitting.";
	return 1;
}



// Wraps our internal connected status so callers can easily access it.
BOOL CCommandWindow::GetConnectedStatus()
{
    return _fConnected;
}

//
//  FUNCTION: _MyRegisterClass()
//
//  PURPOSE: Registers the window class.
//
//  COMMENTS:
//
//    This function and its usage are only necessary if you want this code
//    to be compatible with Win32 systems prior to the 'RegisterClassEx'
//    function that was added to Windows 95. It is important to call this function
//    so that the application will get 'well formed' small icons associated
//    with it.
//
HRESULT CCommandWindow::_MyRegisterClass()
{
    WNDCLASSEX wcex = { sizeof(wcex) };
    wcex.style            = CS_HREDRAW | CS_VREDRAW;
    wcex.lpfnWndProc      = _WndProc;
    wcex.hInstance        = _hInst;
    wcex.hIcon            = NULL;
    wcex.hCursor          = LoadCursor(NULL, IDC_ARROW);
    wcex.hbrBackground    = (HBRUSH)(COLOR_WINDOW + 1);
    wcex.lpszClassName    = c_szClassName;

    return RegisterClassEx(&wcex) ? S_OK : HRESULT_FROM_WIN32(GetLastError());
}

//
//   FUNCTION: _InitInstance(HINSTANCE, int)
//
//   PURPOSE: Saves instance handle and creates main window
//
//   COMMENTS:
//
//        In this function, we save the instance handle in a global variable and
//        create and display the main program window.
//
HRESULT CCommandWindow::_InitInstance()
{
    HRESULT hr = S_OK;

    _hWnd = CreateWindowEx(
        WS_EX_TOPMOST, 
        c_szClassName, 
		L"", 
        WS_DLGFRAME,
        200, 200, 200, 80, 
        NULL,
        NULL, _hInst, NULL);
    if (_hWnd == NULL)
    {
        hr = HRESULT_FROM_WIN32(GetLastError());
    }

    if (SUCCEEDED(hr))
    {
		if (!ShowWindow(_hWnd, SW_HIDE))
            {
                hr = HRESULT_FROM_WIN32(GetLastError());
            }

            if (SUCCEEDED(hr))
            {
                if (!UpdateWindow(_hWnd))
                {
                   hr = HRESULT_FROM_WIN32(GetLastError());
                }
            }
    }

    return hr;
}

BOOL CCommandWindow::_ProcessNextMessage()
{
    MSG msg;
    GetMessage(&(msg), _hWnd, 0, 0);
    TranslateMessage(&(msg));
    DispatchMessage(&(msg));

    switch (msg.message)
    {
    case WM_EXIT_THREAD: return FALSE;
    case WM_TOGGLE_CONNECTED_STATUS:
		_fConnected=true;
		_pProvider->OnConnectStatusChanged();
		break;
	}
    return TRUE;

}

LRESULT CALLBACK CCommandWindow::_WndProc(__in HWND hWnd, __in UINT message, __in WPARAM wParam, __in LPARAM lParam)
{
    switch (message)
    {
   

    case WM_CLOSE:
        ShowWindow(hWnd, SW_HIDE);
        PostMessage(hWnd, WM_EXIT_THREAD, 0, 0);
        break;

    default:
        return DefWindowProc(hWnd, message, wParam, lParam);
    }
    return 0;
}

DWORD WINAPI CCommandWindow::_ThreadProc(__in LPVOID lpParameter)
{
	CCommandWindow *pCommandWindow = static_cast<CCommandWindow *>(lpParameter);
    if (pCommandWindow == NULL)
    {
        return 0;
    }

	HRESULT hres;
    hres =  CoInitializeEx(0, COINIT_MULTITHREADED); 
    if (FAILED(hres))
        return 1;                  // Program has failed.

    hres = CoCreateInstance(
        CLSID_WbemLocator,             
        0, 
        CLSCTX_INPROC_SERVER, 
		IID_IWbemLocator, (LPVOID *) &(pLoc));
 
    if (FAILED(hres))
    {
        CoUninitialize();
        return 1;                 // Program has failed.
    }
    hres = pLoc->ConnectServer(
         _bstr_t(L"ROOT\\CIMV2"), // Object path of WMI namespace
         NULL,                    // User name. NULL = current user
         NULL,                    // User password. NULL = current
         0,                       // Locale. NULL indicates current
         NULL,                    // Security flags.
         0,                       // Authority (e.g. Kerberos)
         0,                       // Context object 
         &pSvc                    // pointer to IWbemServices proxy
         );

    if (FAILED(hres))
    {
        pLoc->Release();     
        CoUninitialize();
        return 1;                // Program has failed.
    }

    hres = CoSetProxyBlanket(
       pSvc,                        // Indicates the proxy to set
       RPC_C_AUTHN_WINNT,           // RPC_C_AUTHN_xxx
       RPC_C_AUTHZ_NONE,            // RPC_C_AUTHZ_xxx
       NULL,                        // Server principal name 
       RPC_C_AUTHN_LEVEL_CALL,      // RPC_C_AUTHN_LEVEL_xxx 
       RPC_C_IMP_LEVEL_IMPERSONATE, // RPC_C_IMP_LEVEL_xxx
       NULL,                        // client identity
       EOAC_NONE                    // proxy capabilities 
    );

    if (FAILED(hres))
    {
        pSvc->Release();
        pLoc->Release();     
        CoUninitialize();
        return 1;               // Program has failed.
    }

    

    HRESULT hr = S_OK;

    // Create the window.
    pCommandWindow->_hInst = GetModuleHandle(NULL);
    if (pCommandWindow->_hInst != NULL)
    {            
        hr = pCommandWindow->_MyRegisterClass();
        if (SUCCEEDED(hr))
        {
            hr = pCommandWindow->_InitInstance();
        }
    }
    else
    {
        hr = HRESULT_FROM_WIN32(GetLastError());
	}
	ShowWindow(pCommandWindow->_hWnd, SW_HIDE);
	
    if (SUCCEEDED(hr))
    {        
        while (pCommandWindow->_ProcessNextMessage()) 
        {
        }
    }
    else
    {
        if (pCommandWindow->_hWnd != NULL)
        {
            pCommandWindow->_hWnd = NULL;
        }
    }

    return 0;
}

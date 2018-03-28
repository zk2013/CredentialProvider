package ru.startandroid.androidclient;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
import java.net.SocketAddress;
>>>>>>> Anastasiia's_branch
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
=======
>>>>>>> Anastasiia's_branch
=======
import java.util.ArrayList;
>>>>>>> Anastasiia's_branch

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pwEdit=(TextInputLayout)findViewById(R.id.textInputLayout);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Status");
        builder.setCancelable(false);
        builder.setNeutralButton("Understood", new DialogInterface.OnClickListener() {public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();}});
        alert = builder.create();

        dbHelper = new DBHelper(this);
         cv = new ContentValues();
         db = dbHelper.getWritableDatabase();
        login=new ArrayList();
        DataEncrypt dataEncrypt1=new DataEncrypt();
        String decrypt1=null;
        Cursor c = db.query("mytable", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            int nameColIndex1 = c.getColumnIndex("username");
            do {
            try {
                decrypt1=dataEncrypt1.Decrypt(c.getString(nameColIndex1));

            } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
             }
            login.add(decrypt1);
             } while (c.moveToNext());
        }
        c.close();
        unAutoText =(AutoCompleteTextView)findViewById(R.id.autoCompleteTextView1);
        AutoArrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,login);
        unAutoText.setAdapter(AutoArrayAdapter);


        unAutoText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        DataEncrypt dataEncrypt=new DataEncrypt();
        String decrypt=null;
        String decpass=null;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str= parent.getItemAtPosition(position).toString();
                // делаем запрос всех данных из таблицы mytable, получаем Cursor
                Cursor c = db.query("mytable", null, null, null, null, null, null);
                if (c.moveToFirst()) {
                    int nameColIndex = c.getColumnIndex("username");
                    int passwordColIndex = c.getColumnIndex("password");
                    do {
                        try {
                            decrypt=dataEncrypt.Decrypt(c.getString(nameColIndex));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        if(str.equals(decrypt))
                        {
                            try {
                                decpass=dataEncrypt.Decrypt(c.getString(passwordColIndex));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            pwEdit.getEditText().setText(decpass);
                        }
                    } while (c.moveToNext());
                }
                c.close();
            }
        });
    }

    public void CacheonClick(View cview) throws UnsupportedEncodingException
    {
        Cursor c = db.query("mytable", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            db.delete("mytable", null, null);
            login.clear();
            AutoArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, login);
            unAutoText.setAdapter(AutoArrayAdapter);
        }
        else
            ShowMessage("Data base is empty");
    }
    public void MyonClick(View view)
    {
        String check="Correct";

        button=(Button)findViewById(R.id.button);

        password=pwEdit.getEditText().getText().toString();

        ipTextEdit=(TextInputLayout)findViewById(R.id.textInputLayout2);
        serIpAddress=ipTextEdit.getEditText().getText().toString();


        unEdit =(TextInputLayout)findViewById(R.id.textInputLayout6);
        username= unAutoText.getText().toString();


        boolean fCorrectData=true;
        String error_ip=null;
        String error_username=null;
        String error_password=null;

        CheckOnCorrect checkOnCorrect=new CheckOnCorrect();
        error_ip=checkOnCorrect.CorrectIP(serIpAddress);
        if(error_ip!=check)
        {
            ipTextEdit.setErrorEnabled(true);
            ipTextEdit.setError(error_ip);
            fCorrectData=false;

        }
        else
            ipTextEdit.setErrorEnabled(false);

        error_username=checkOnCorrect.CorrectUsername(username);
        if(error_username!=check)
        {
            unEdit.setErrorEnabled(true);
            unEdit.setError(error_username);
            fCorrectData=false;

        }
        else
            unEdit.setErrorEnabled(false);
        error_password=checkOnCorrect.CorrectPassword(password);
        if(error_password!=check)
        {
            pwEdit.setErrorEnabled(true);
            pwEdit.setError(error_password);

            fCorrectData=false;
        }
        else
            pwEdit.setErrorEnabled(false);


<<<<<<< HEAD
        cryptdata=CryptingData(msg);
<<<<<<< HEAD
        String beforeCryp=String.valueOf(msg.length());
        SenderThread sender = new SenderThread(); // объект представляющий поток отправки сообщений
        sender.execute(msg,serIpAddress,beforeCryp);
=======
        String sizeCrypt=String.valueOf(cryptdata.length);
        String beforeCryp=String.valueOf(msg.length());
        SenderThread sender = new SenderThread(); // объект представляющий поток отправки сообщений
        sender.execute(sizeCrypt,serIpAddress,beforeCryp);
>>>>>>> Anastasiia's_branch
=======
        if(!fCorrectData)
            return;
        else
        {
            msg=username+"&"+password;
            DataEncrypt encrypt=new DataEncrypt();
            cryptdata=encrypt.CryptingData(msg);

            String sizeCrypt=String.valueOf(cryptdata.length);
            String beforeCryp=String.valueOf(msg.length());
            SenderThread sender = new SenderThread();
            sender.execute(sizeCrypt,serIpAddress,beforeCryp);
        }

>>>>>>> Anastasiia's_branch
    }

   void ShowMessage(String mes)
   {
       Toast msgToast = Toast.makeText(this, mes, Toast.LENGTH_SHORT);
       msgToast.show();
   }


    class SenderThread extends AsyncTask<String, String, Integer>
    {
        private int port = 27015;
        private PMessage pMessage;
        private PMessage1 pMessage1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           button.setEnabled(false);
        }

<<<<<<< HEAD
        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {

            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(theTestText.getBytes("UTF8"));
        } catch (Exception e) {
            msgToast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            msgToast.show();
        }
<<<<<<< HEAD
        msgToast = Toast.makeText(this, "[ENCODED]:\n" +
              Base64.encodeToString(encodedBytes, Base64.DEFAULT), Toast.LENGTH_SHORT);
        msgToast.show();
=======
       // msgToast = Toast.makeText(this, "[ENCODED]:\n" +
         //     Base64.encodeToString(encodedBytes, Base64.DEFAULT), Toast.LENGTH_SHORT);
        //msgToast.show();
>>>>>>> Anastasiia's_branch
        return  encodedBytes;
    }
=======
>>>>>>> Anastasiia's_branch

        @Override
<<<<<<< HEAD
        protected Void doInBackground(String... params) {
<<<<<<< HEAD
            Toast msgToast=null;
            String outtext=null;
            String outipAddress=null;
            String tmp=null;
            int beforeCr=0;
            if(params.length==3) {
                outtext = params[0];
=======
=======
        protected Integer doInBackground(String... params) {
>>>>>>> Anastasiia's_branch
            String outipAddress=null;
            String sizeCrypt=null;
            int beforeCr=0;
            int rez=1;
            if(params.length==3) {
                sizeCrypt = params[0];
>>>>>>> Anastasiia's_branch
                outipAddress=params[1];
                beforeCr=Integer.parseInt(params[2]);
            }
            try {
                int i=ConstValues.Request;
                String str;

                pMessage=new PMessage();
                pMessage1=new PMessage1();
                // ip адрес сервера
                InetAddress ipAddress = InetAddress.getByName(outipAddress);
                // Создаем сокет
<<<<<<< HEAD

                Socket socket = new Socket(ipAddress, port);
=======
                InetSocketAddress inetSocketAddress=new InetSocketAddress(ipAddress,port);
                Socket socket=new Socket();
<<<<<<< HEAD
                socket.connect(inetSocketAddress,3000);
                //Socket socket = new Socket(ipAddress, port);
>>>>>>> Anastasiia's_branch
=======
                socket.connect(inetSocketAddress,ConstValues.TimeOut);

>>>>>>> Anastasiia's_branch
                // Получаем потоки ввод/вывода
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream= socket.getInputStream();
                DataOutputStream out = new DataOutputStream(outputStream);
                DataInputStream in=new DataInputStream(inputStream);

<<<<<<< HEAD
<<<<<<< HEAD
=======
               /* byte[] outMsg = null;
                outMsg = msg.getBytes("UTF8");
                out.write(outMsg);*/

>>>>>>> Anastasiia's_branch
=======
>>>>>>> Anastasiia's_branch
                boolean finish=false;
                do {
                    byte[] outMsg = null;
                    switch (i) {
                        case ConstValues.Request:
                            str = "1";
                            outMsg = str.getBytes("UTF8");
                            out.write(outMsg);
                            break;

                        case ConstValues.SendCreds:
                            str="4";
<<<<<<< HEAD
                            if(outtext.length()<10)
                                str += "00";
                            if(outtext.length()<100&&outtext.length()>=10)
                                str+="0";
                            str+=outtext.length();
=======
                            if(Integer.parseInt(sizeCrypt)<10)
                                str += "00";
                            if(Integer.parseInt(sizeCrypt)<100&&Integer.parseInt(sizeCrypt)>=10)
                                str+="0";
                            str+=Integer.parseInt(sizeCrypt);
>>>>>>> Anastasiia's_branch
                            if(beforeCr<10)
                                str+="00";
                            if(beforeCr<100&&beforeCr>=10)
                                str+="0";
<<<<<<< HEAD
                            str+=beforeCr+outtext;
                            outMsg = str.getBytes("UTF8");
                            out.write(outMsg);
=======
                            str+=beforeCr;
                            outMsg = str.getBytes("UTF8");
                            out.write(outMsg);
                            out.write(cryptdata);
>>>>>>> Anastasiia's_branch
                            break;
                    }

                    byte[] buffer = new byte[1024 * 4];
                    int count;
                    count = in.read(buffer, 0, buffer.length);
                    if (count > 0) {
                            SetClassFileds(count,buffer,pMessage,pMessage1);

                        switch (pMessage.GetID()) {
                            case ConstValues.ReadingError:
                                rez=ConstValues.ReadingError;
                                finish=true;
                                break;
                            case ConstValues.UserInSystem:
                                rez=ConstValues.UserInSystem;
                                finish=true;
                                break;
                            case ConstValues.Free:
                                i=ConstValues.SendCreds;
                                break;
                            case ConstValues.IncorrectCreds:
                                rez=ConstValues.IncorrectCreds;
                                finish=true;
                                break;
                            case ConstValues.SuccessfulLogging:
                                rez=ConstValues.SuccessfulLogging;
                                finish=true;
                                break;
                            case ConstValues.PError:
                                rez=ConstValues.PError;
                                finish=true;
                                break;
                        }

                    }
                }while(!finish);
<<<<<<< HEAD
<<<<<<< HEAD
=======
              /*  byte[] buffer = new byte[1024 * 4];
                int count;
                count = in.read(buffer, 0, buffer.length);
                publishProgress(new String(buffer));*/
>>>>>>> Anastasiia's_branch
=======
>>>>>>> Anastasiia's_branch
                socket.close();
            }
            catch (Exception ex)
            {
                pMessage1.SetData(ex.getMessage());
                return ConstValues.Error;
            }
            return rez;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            alert.setTitle("Status");
            if(result==ConstValues.IncorrectCreds||result==ConstValues.PError)
            {
                alert.setMessage("Incorrect username or password");
                alert.setIcon(android.R.drawable.ic_notification_clear_all);
                alert.show();
            }
            if(result==ConstValues.SuccessfulLogging)
            {
                alert.setMessage("Log in is successful");
                alert.setIcon(android.R.drawable.ic_menu_info_details);
                alert.show();
                CacheCreds(unAutoText.getText().toString(),pwEdit.getEditText().getText().toString());
            }
            if(result==ConstValues.UserInSystem)
            {
                alert.setMessage("User "+pMessage1.GetData()+" is already logged in");
                alert.setIcon(android.R.drawable.ic_menu_info_details);
                alert.show();
            }
            if(result==ConstValues.Error)
            {
                alert.setTitle("Error");
                alert.setIcon(android.R.drawable.ic_notification_clear_all);
                alert.setMessage(pMessage1.GetData());
                alert.show();
            }
            if(result==ConstValues.ReadingError)
            {
                alert.setTitle("Error");
                alert.setIcon(android.R.drawable.ic_notification_clear_all);
                alert.setMessage("Error with reading message");
                alert.show();
            }
            button.setEnabled(true);
            if(result!=ConstValues.IncorrectCreds)
                ipTextEdit.getEditText().setText("");
            unAutoText.setText("");
            pwEdit.getEditText().setText("");
        }

        private void SetClassFileds(int size,byte[] text, PMessage pMes,PMessage1 pMes1)
        {
            if(size>7)
            {
                pMes1.SetLenMessage(Integer.parseInt(new String(text, 1, 3)));
                pMes1.SetLenData(Integer.parseInt(new String(text,4,3)));
                if(size==7+pMes1.GetLenMessage())
                    pMes1.SetData(new String(text, 7, pMes1.GetLenMessage()));
                else
                    pMes1.SetData("There were some troubles with receiving message");
            }

            pMes.SetID(Integer.parseInt(new String(text,0,1)));
        }
    }

    public void CacheCreds(String cusername, String cpassword)
    {
        DataEncrypt dataEncrypt=new DataEncrypt();
        String decrypt=null;
        boolean isCached=false;

        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c = db.query("mytable", null, null, null, null, null, null);
        if(!cpassword.isEmpty() && !cusername.isEmpty()) {
            if (c.moveToFirst()) {
                int nameColIndex = c.getColumnIndex("username");
                do {
                    try {
                        decrypt = dataEncrypt.Decrypt(c.getString(nameColIndex));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (cusername.equals(decrypt)) {
                        isCached = true;
                    }
                } while (c.moveToNext());
            }
            c.close();

            if(!isCached)
            {
                DataEncrypt encrypt = new DataEncrypt();
                byte[] data = null;
                data = encrypt.CryptingData(cusername);
                cv.put("username", Base64.encodeToString(data, Base64.DEFAULT));
                data = encrypt.CryptingData(cpassword);
                cv.put("password", Base64.encodeToString(data, Base64.DEFAULT));
                db.insert("mytable", null, cv);
                login.add(cusername);
                AutoArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, login);
                unAutoText.setAdapter(AutoArrayAdapter);
            }
        }
    }
    private byte[] cryptdata=null;
    private String serIpAddress;
    private String username;
    private String password;
    private String msg;

    private TextInputLayout pwEdit;
    private TextInputLayout ipTextEdit;
    private Button button;

    private AlertDialog alert;
    private AutoCompleteTextView unAutoText;
    private TextInputLayout unEdit;
    private ArrayList login;
    private DBHelper dbHelper;
    private ContentValues cv;
    private SQLiteDatabase db;
    private ArrayAdapter<String> AutoArrayAdapter;
}



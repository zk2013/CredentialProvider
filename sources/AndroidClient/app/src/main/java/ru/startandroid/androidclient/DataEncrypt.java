package ru.startandroid.androidclient;

import android.util.Base64;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Home on 23.03.2018.
 */

public class DataEncrypt {
    MainActivity mn;
    public byte[] CryptingData(String text)
    {
        // Original text
        String theTestText = text;
         mn=new MainActivity();
        SecretKeySpec sks = null;
        try {

            sks = new SecretKeySpec("passworddrowssap".getBytes(), "AES");
        } catch (Exception e) {
            mn.ShowMessage(e.getMessage());
        }

        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            ///CFB8/NoPadding
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(theTestText.getBytes("UTF-8"));
        } catch (Exception e) {
            mn.ShowMessage(e.getMessage());
        }
        return  encodedBytes;
    }

    public String Decrypt(String encoded) throws UnsupportedEncodingException {
        String tmp=null;
        char[]mas=new char[encoded.length()-1];
        encoded.getChars(0,encoded.length()-1,mas,0);
        tmp=String.copyValueOf(mas);

        byte [] encodedBytes= Base64.decode(tmp,Base64.DEFAULT);

        mn=new MainActivity();
        SecretKeySpec sks = null;
        try {

            sks = new SecretKeySpec("passworddrowssap".getBytes(), "AES");
        } catch (Exception e) {
            mn.ShowMessage(e.getMessage());
        }

        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, sks);
            decodedBytes = c.doFinal(encodedBytes);
        } catch (Exception e) {
            mn.ShowMessage(e.getMessage());
        }
        return new String(decodedBytes);
    }
}

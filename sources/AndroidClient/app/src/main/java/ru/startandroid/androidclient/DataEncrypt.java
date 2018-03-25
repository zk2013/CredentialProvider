package ru.startandroid.androidclient;

import android.widget.Toast;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Home on 23.03.2018.
 */

public class DataEncrypt {
    public byte[] CryptingData(String text)
    {
        // Original text
        String theTestText = text;
        MainActivity mn=new MainActivity();
        SecretKeySpec sks = null;
        try {

            sks = new SecretKeySpec("passworddrowssap".getBytes(), "AES");
        } catch (Exception e) {
            mn.ShowMessage(e.getMessage());
        }

        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {

            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(theTestText.getBytes("UTF8"));
        } catch (Exception e) {
            mn.ShowMessage(e.getMessage());
        }
        return  encodedBytes;
    }
}

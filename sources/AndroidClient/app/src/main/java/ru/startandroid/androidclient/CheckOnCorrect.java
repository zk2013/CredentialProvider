package ru.startandroid.androidclient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Home on 23.03.2018.
 */

public class CheckOnCorrect
{

    public String CorrectIP(String ip)
    {
        if(ip.isEmpty())
            return "Enter ip address";

        int i=0,num;
        boolean flag=false;
        pattern = Pattern.compile("([0-9]{1,3}\\.){3}[0-9]{1,3}");
        match = pattern.matcher(ip);
        if(!match.matches())
            return "Wrong ip format";

        String[] splits = ip.split("\\.");

        while (!flag && i < splits.length) {
            num = Integer.parseInt(splits[i]);
            if (num > 256)
                flag = true;
            ++i;
        }

        if(flag)
            return "Wrong ip format";

        return "Correct";
    }
    public String CorrectUsername(String username)
    {
        if(username.isEmpty())
            return "Enter username";
        if(username.length()>20)
            return "Username is too long";

        pattern=Pattern.compile("((.*[\\(\\;\\:\\[\\]\\'\\(\\)\\/\\,\\+\\*\\?\\<\\>\\ )]+))");
        Matcher m1=pattern.matcher(username);

        if(m1.lookingAt())
            return "Wrong username format";
        return "Correct";
    }
    public String CorrectPassword(String password)
    {
        if (password.isEmpty())
            return "Enter password";
        if(password.length()>128)
            return "Password is too long";
        pattern=Pattern.compile("((.*[\\(\\;\\:\\[\\]\\'\\(\\)\\/\\,\\+\\*\\?\\<\\>\\ )]+))");
        match=pattern.matcher(password);
        if(match.lookingAt())
           return "Wrong password format";
        return "Correct";
    }

    private Pattern pattern;
    private Matcher match;
}

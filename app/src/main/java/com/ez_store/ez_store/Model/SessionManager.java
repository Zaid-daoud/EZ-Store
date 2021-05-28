package com.ez_store.ez_store.Model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

import static com.ez_store.ez_store.Model.Crypto.decrypt;
import static com.ez_store.ez_store.Model.Crypto.encrypt;

public class SessionManager {


    //Session names
    public static final String SESSION_REMEMBER_ME = "rememberME";

    //User session variables
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Context context;

    //RememberMe variables
    private static final String IS_REMEMBER_ME = "IsRememberMe";
    public static final String KEY__SESSION_EMAIL = "email";
    public static final String KEY_SESSION_PASSWORD = "password";

    public SessionManager(Context _context , String sessionName) {
        this.context = _context;
        preferences = context.getSharedPreferences(sessionName ,Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void createRememberMeSession(String email, String password){
        editor.putBoolean(IS_REMEMBER_ME,true);
        editor.putString(KEY__SESSION_EMAIL,email);
        editor.putString(KEY_SESSION_PASSWORD,encrypt(password.getBytes()));
        editor.commit();
    }

    public HashMap<String,String> getRememberMeSession(){
        HashMap<String,String> userData = new HashMap<String, String>();

        userData.put(KEY__SESSION_EMAIL,preferences.getString(KEY__SESSION_EMAIL,null));
        userData.put(KEY_SESSION_PASSWORD,decrypt(preferences.getString(KEY_SESSION_PASSWORD,null).getBytes()));

        return userData;
    }

    public boolean checkRememberMe(){
        if(preferences.getBoolean(IS_REMEMBER_ME,false)){
            return true;
        }
        return false;
    }
}

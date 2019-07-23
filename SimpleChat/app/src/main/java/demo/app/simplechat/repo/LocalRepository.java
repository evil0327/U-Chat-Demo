package demo.app.simplechat.repo;

import android.content.SharedPreferences;

public class LocalRepository {
    private SharedPreferences sharedPreferences;
    private final static String KEY_UID = "uid";
    private final static String KEY_FIRST_TIME_USE = "first_time";
    private String UID;

    public LocalRepository(SharedPreferences sharedPreferences){
        this.sharedPreferences =  sharedPreferences;
    }

    public boolean isLogin(){
        return sharedPreferences.contains(KEY_UID);
    }

    public String getUID(){
        return sharedPreferences.getString(KEY_UID, "");
    }

    public void saveUID(String uid){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_UID, uid).commit();
    }


    public void saveFirstTimeUse(boolean firstTime){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_FIRST_TIME_USE, firstTime).commit();
    }

    public boolean isFirstTimeUse(){
        return sharedPreferences.getBoolean(KEY_FIRST_TIME_USE, true);
    }

}

package Util;

import android.app.Application;

//this extends application because we want to be able to access the data from anywhere in the application
//must also add to the manifest file "name"
public class JournalApi extends Application {

    private String username;
    private String userId;
    //we need this for making this class a singleton class
    private static JournalApi instance;

    public static JournalApi getInstance(){
        if(instance == null){
            instance = new JournalApi();
        }
        return instance;
    }

    public JournalApi() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

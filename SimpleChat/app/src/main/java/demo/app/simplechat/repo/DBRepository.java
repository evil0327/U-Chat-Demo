package demo.app.simplechat.repo;

import android.app.Application;

import java.util.List;

import demo.app.simplechat.db.ChatMessage;
import demo.app.simplechat.db.MyDatabase;
import demo.app.simplechat.db.User;
import io.reactivex.Single;

public class DBRepository {
    private Application mApplication;

    public DBRepository(Application application){
        mApplication = application;
    }

    public Single<List<User>> getAllUsers(){
        return MyDatabase.getDatabase(mApplication).getUserDao().getAllUsers();
    }

    public Single<User> getUserById(String uid){
        return  MyDatabase.getDatabase(mApplication).getUserDao().getUserById(uid);
    }

    public void upsertUser(User u){
        MyDatabase.getDatabase(mApplication).getUserDao().upsertUser(u);
    }

    public void insertChatMessage(ChatMessage message){
        MyDatabase.getDatabase(mApplication).getChatMessageDao().insertChatMessage(message);
    }

    public Single<List<ChatMessage>> getChatMessages(long ct){
        return MyDatabase.getDatabase(mApplication).getChatMessageDao().getChatMessages(ct);
    }
}

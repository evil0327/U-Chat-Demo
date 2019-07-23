package demo.app.simplechat.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import demo.app.simplechat.db.dao.ChatMessageDao;
import demo.app.simplechat.db.dao.UserDao;

@Database(entities = {ChatMessage.class, User.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {
    private static MyDatabase sInstance;

    public static MyDatabase getDatabase(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context.getApplicationContext(), MyDatabase.class,
                    "simplechat.db").build();
        }
        return sInstance;
    }

    public abstract ChatMessageDao getChatMessageDao();
    public abstract UserDao getUserDao();

}
package demo.app.simplechat;

import android.content.Context;

import androidx.room.Room;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import demo.app.simplechat.db.ChatMessage;
import demo.app.simplechat.db.MyDatabase;
import demo.app.simplechat.db.User;
import io.reactivex.functions.Predicate;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    private MyDatabase mDatabase;

    @Before
    public void initDb() throws Exception {
        mDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getContext(),
                MyDatabase.class)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build();
    }

    @Test
    public void testOneUserInsertAndQuery(){
        User insertUser = new User();
        insertUser.setUid("0");
        insertUser.setName("user0");

        mDatabase.getUserDao().upsertUser(insertUser);

        mDatabase.getUserDao()
                .getUserById(insertUser.getUid())
                .test()
                // assertValue asserts that there was only one emission
                .assertValue(user -> user.getUid().equals(insertUser.getUid()) &&
                        user.getName().equals(insertUser.getName()));

    }

    @Test
    public void testMutipleUserInsertAndQuery(){
        User user1 = new User();
        user1.setUid("1");
        user1.setName("user1");

        User user2 = new User();
        user2.setUid("2");
        user2.setName("user2");

        mDatabase.getUserDao().upsertUser(user1);
        mDatabase.getUserDao().upsertUser(user2);

        mDatabase.getUserDao()
                .getAllUsers()
                .test()
                 .assertValue(users -> users.size() == 2);
    }

    @Test
    public void testChatMessageInsertAndQuery(){
        for(int i=0;i<10;i++){
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setId(String.valueOf(i));
            chatMessage.setContent("message_"+i);
            chatMessage.setCreateTime(System.currentTimeMillis());
            mDatabase.getChatMessageDao().insertChatMessage(chatMessage);
        }

        mDatabase.getChatMessageDao().getChatMessages(Long.MAX_VALUE)
                .test().assertValue(chatMessages -> chatMessages.size()==10);

    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("demo.app.simplechat", appContext.getPackageName());
    }

    @After
    public void closeDb() throws Exception {
        mDatabase.close();
    }
}

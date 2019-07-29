package demo.app.simplechat.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import demo.app.simplechat.db.ChatMessage;
import io.reactivex.Single;

@Dao
public interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertChatMessage(ChatMessage chatMessage);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<List<Long>> insertChatMessages(List<ChatMessage> chatMessages);

    @Query("SELECT * FROM `chat_message` WHERE createTime<:createTime order by createTime desc limit 20")
    Single<List<ChatMessage>> getChatMessages(long createTime);

}

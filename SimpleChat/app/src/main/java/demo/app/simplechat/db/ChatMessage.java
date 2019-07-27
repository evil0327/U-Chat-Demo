package demo.app.simplechat.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@Entity(tableName = "chat_message")
@IgnoreExtraProperties
public class ChatMessage {
    public static final int STATE_SUCCESS = 0;
    public static final int STATE_PROCESSING = 1;
    public static final int STATE_FAIL = 0;

    @PrimaryKey
    @NonNull
    private String id;
    private String content;
    private String userId;
    private long createTime;
    private int state = STATE_SUCCESS;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Exclude
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", userId='" + userId + '\'' +
                ", createTime=" + createTime +
                ", state=" + state +
                '}';
    }
}

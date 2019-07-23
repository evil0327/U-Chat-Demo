package demo.app.simplechat.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.IgnoreExtraProperties;

@Entity(tableName = "user")
@IgnoreExtraProperties
public class User {
    @PrimaryKey
    @NonNull
    private String uid;
    private String name;
    private int avatar;

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }
}

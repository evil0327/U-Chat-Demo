package demo.app.simplechat.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import demo.app.simplechat.db.User;
import io.reactivex.Single;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> upsertUser(User user);

    @Query("SELECT * FROM `user`")
    Single<List<User>> getAllUsers();

    @Query("SELECT * FROM `user` where uid=:uid")
    Single<User> getUserById(String uid);
}

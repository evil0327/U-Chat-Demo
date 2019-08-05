package demo.app.simplechat.cache;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;

import java.util.HashMap;

import demo.app.simplechat.db.User;

public class UserLiveCache extends LiveData<HashMap<String, User>> {
    public UserLiveCache(){
        setValue(new HashMap<>());
    }

    public void putUser(User user){
        HashMap<String, User> map = getValue();
        map.put(user.getUid(), user);
        postValue(map);
    }


    public User getUserByUid(String uid){
        return getValue().get(uid);
    }

    public int getSize(){
        return getValue().size();
    }

    public boolean containsUser(User user){
        return getValue().containsKey(user.getUid());
    }
}

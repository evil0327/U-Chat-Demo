package demo.app.simplechat.cache;

import java.util.HashMap;

import demo.app.simplechat.db.User;

public class UserCache {
    private HashMap<String, User> mUserMap = new HashMap<>();

    public void putUser(User user){
        mUserMap.put(user.getUid(), user);
    }

    public HashMap<String, User> getUserMap(){
        return mUserMap;
    }

    public User getUserByUid(String uid){
        return mUserMap.get(uid);
    }

    public int getSize(){
        return mUserMap.size();
    }

    public boolean containsUser(User user){
        return mUserMap.containsKey(user.getUid());
    }

}

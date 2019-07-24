package demo.app.simplechat.vm;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.protobuf.Api;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import demo.app.simplechat.cache.UserCache;
import demo.app.simplechat.db.User;
import demo.app.simplechat.repo.DBRepository;
import demo.app.simplechat.repo.LocalRepository;
import io.reactivex.Completable;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MainViewModel extends BaseViewModel {
    private MutableLiveData<HashMap<String, User>> mUsersLiveData = new MutableLiveData<>();
    private DBRepository mDBRepository;
    private LocalRepository mLocalRepository;
    private ListenerRegistration mEventsListener;
    private UserCache mUserCache;

    @Inject
    public MainViewModel(DBRepository dbRepository, LocalRepository localRepository, UserCache userCache) {
        this.mDBRepository = dbRepository;
        this.mLocalRepository = localRepository;
        this.mUserCache = userCache;

        refreshUsers();
    }

    public LiveData<HashMap<String, User>> getLiveUserMap(){
        return mUsersLiveData;
    }

    public void refreshUsers(){
        mDBRepository.getAllUsers().subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<List<User>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposables.add(d);
                    }

                    @Override
                    public void onSuccess(List<User> users) {
                        for(User u : users){
                            mUserCache.putUser(u);
                        }
                        mUsersLiveData.postValue(mUserCache.getUserMap());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

    }


    public void startListen(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mEventsListener = db.collection("users").addSnapshotListener((snapshots, e) -> {
            Timber.d("onEvent snapshots=" + (snapshots == null));
            if (snapshots == null) return;

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                        Timber.d("add event: " + dc.getDocument().getData());
                        final User newUser = dc.getDocument().toObject(User.class);
                        upsertUserToDB(newUser);
                        break;
                    case MODIFIED:
                        Timber.d("Modified event: " + dc.getDocument().getData());
                        final User modifyUser = dc.getDocument().toObject(User.class);
                        upsertUserToDB(modifyUser);
                        break;
                }
            }
            mUsersLiveData.postValue(mUserCache.getUserMap());
        });
    }

    private void upsertUserToDB(final User user){
        HashMap<String, User> map = mUserCache.getUserMap();
        map.put(user.getUid(), user);

        Disposable d = Completable.fromAction(() -> mDBRepository.upsertUser(user)).subscribeOn(Schedulers.io()).subscribe();
        mDisposables.add(d);

    }

    public void stopListen(){
        if(mEventsListener==null){
            mEventsListener.remove();
        }
    }

    public void getFcmToken(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            final String deviceToken = instanceIdResult.getToken();
            Timber.d("getFcmToken token="+deviceToken);
            updateFcmToken(deviceToken, mLocalRepository.getUID());
        });
    }

    private void updateFcmToken(String token, String myId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(myId).update("token", token).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Timber.d("updateFcmToken onSuccess");
            }
        });
    }
}

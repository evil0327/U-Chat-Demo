package demo.app.simplechat.vm;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;
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

import demo.app.simplechat.cache.UserLiveCache;
import demo.app.simplechat.db.User;
import demo.app.simplechat.repo.ApiRepository;
import demo.app.simplechat.repo.DBRepository;
import demo.app.simplechat.repo.LocalRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;
import static com.google.firebase.firestore.DocumentChange.Type.MODIFIED;

public class MainViewModel extends BaseViewModel implements LifecycleObserver {
    private DBRepository mDBRepository;
    private ApiRepository mApiRepository;
    private LocalRepository mLocalRepository;
    private ListenerRegistration mEventsListener;
    private UserLiveCache mUserCache;

    @Inject
    public MainViewModel(DBRepository dbRepository, LocalRepository localRepository, ApiRepository apiRepository, UserLiveCache userCache) {
        this.mDBRepository = dbRepository;
        this.mLocalRepository = localRepository;
        this.mApiRepository = apiRepository;
        this.mUserCache = userCache;

    }

    public void refreshUsers() {
        mDBRepository.getAllUsers()
                .subscribeOn(Schedulers.io())
                .flatMapObservable((Function<List<User>, ObservableSource<User>>) users -> Observable.fromIterable(users))
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposables.add(d);
                    }

                    @Override
                    public void onNext(User user) {
                        mUserCache.putUser(user);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                    @Override
                    public void onComplete() {
                    }
        });


    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void startListen() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mEventsListener = db.collection("users").addSnapshotListener((snapshots, e) -> {
            Timber.d("onEvent snapshots=" + (snapshots == null));
            if (snapshots == null) return;

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                if(dc.getType()== ADDED || dc.getType()==MODIFIED){
                    Timber.d("add/modified event: " + dc.getDocument().getData());
                    final User newUser = dc.getDocument().toObject(User.class);
                    mUserCache.putUser(newUser);
                    upsertUserToDB(newUser);
                }
            }
        });
    }

    private void upsertUserToDB(final User user) {
        mDBRepository.upsertUser(user).subscribeOn(Schedulers.io())
                .subscribe();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void stopListen() {
        if (mEventsListener != null) {
            mEventsListener.remove();
        }
    }

    public void getFcmToken() {
        mApiRepository.getFcmToken().subscribeOn(Schedulers.io())
                .flatMap((Function<String, SingleSource<?>>)
                        token -> mApiRepository.updateFcmToken(token, mLocalRepository.getUID()))
                .subscribe();
    }

}

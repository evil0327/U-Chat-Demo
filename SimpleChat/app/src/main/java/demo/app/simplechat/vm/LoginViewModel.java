package demo.app.simplechat.vm;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

import javax.inject.Inject;
import demo.app.simplechat.db.User;
import demo.app.simplechat.repo.ApiRepository;
import demo.app.simplechat.repo.DBRepository;
import demo.app.simplechat.repo.LocalRepository;
import io.reactivex.Completable;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class LoginViewModel extends BaseViewModel {
    private MutableLiveData<Boolean> mLoadingShowLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> mLoginStateLiveData = new MutableLiveData<>();

    private LocalRepository mLocalRepository;
    private DBRepository mDBRepository;
    private ApiRepository mApiRepository;

    @Inject
    public LoginViewModel(LocalRepository localRepository, DBRepository dbRepository, ApiRepository apiRepository){
        this.mLocalRepository = localRepository;
        this.mDBRepository = dbRepository;
        this.mApiRepository = apiRepository;
    }

    public LiveData<Boolean> getLoadingShowLiveData(){
        return mLoadingShowLiveData;
    }

    public LiveData<String> getToastLiveData(){
        return mToastMsgLiveData;
    }

    public LiveData<Boolean> getLoginStateLiveData(){
        return mLoginStateLiveData;
    }

    public void doLogin(){
        mLoadingShowLiveData.postValue(true);

        final User user = new User();

        mApiRepository.doSignInAnonymously().subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap((Function<String, SingleSource<User>>) userId -> {
                    user.setUid(userId);
                    user.setName("user_"+ UUID.randomUUID().toString().substring(0,4));

                    Timber.d("doLogin success userId="+userId);

                    return mApiRepository.createUserOnFirebase(user);
                }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap((Function<User, SingleSource<Long>>) u -> mDBRepository.upsertUser(u))
                .subscribe(new SingleObserver<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposables.add(d);
                    }

                    @Override
                    public void onSuccess(Long aLong) {
                        mLocalRepository.saveUID(user.getUid());
                        mLoadingShowLiveData.postValue(false);
                        mLoginStateLiveData.postValue(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mLoadingShowLiveData.postValue(false);
                        mLoginStateLiveData.postValue(false);
                        mToastMsgLiveData.postValue("Auth Fail");

                    }
        });

    }



}

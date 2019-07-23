package demo.app.simplechat.vm;

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
import demo.app.simplechat.repo.DBRepository;
import demo.app.simplechat.repo.LocalRepository;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class LoginViewModel extends BaseViewModel {
    private MutableLiveData<Boolean> mLoadingShowLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> mLoginStateLiveData = new MutableLiveData<>();

    private LocalRepository mLocalRepository;
    private DBRepository mDBRepository;

    @Inject
    public LoginViewModel(LocalRepository localRepository, DBRepository dbRepository){
        this.mLocalRepository = localRepository;
        this.mDBRepository = dbRepository;
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

    public void login(){
        mLoadingShowLiveData.postValue(true);

        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser =  FirebaseAuth.getInstance().getCurrentUser();
                        mLocalRepository.saveUID(firebaseUser.getUid());

                        final User user = new User();
                        user.setUid(firebaseUser.getUid());
                        user.setName("user_"+ UUID.randomUUID().toString().substring(0,4));

                        Disposable d = Completable.fromAction(() -> {
                            mDBRepository.upsertUser(user);
                            createUserOnFirebase(user);
                        }).subscribeOn(Schedulers.io()).subscribe();
                        mDisposables.add(d);
                    } else {
                        mLoadingShowLiveData.postValue(false);
                        mLoginStateLiveData.postValue(false);
                        mToastMsgLiveData.postValue("Authentication failed.");
                    }
                });
    }

    private void createUserOnFirebase(User user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).set(user).addOnSuccessListener(aVoid -> {
            mLoginStateLiveData.postValue(true);
            mLoadingShowLiveData.postValue(false);
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mLoginStateLiveData.postValue(false);
                mToastMsgLiveData.postValue("Connecting to server error.");
                mLoadingShowLiveData.postValue(false);
            }
        });
    }
}

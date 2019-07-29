package demo.app.simplechat.repo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import demo.app.simplechat.db.ChatMessage;
import demo.app.simplechat.db.User;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ApiRepository {

    public Single<List<ChatMessage>> getMessagesFromFirebase(long createTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("chats").whereLessThan("createTime", createTime).orderBy("createTime", Query.Direction.DESCENDING).limit(20);

        return Single.create(emitter -> query.get().addOnSuccessListener(documentSnapshots -> {
            List<ChatMessage> rs = new ArrayList<>();
            for (DocumentSnapshot dc : documentSnapshots) {
                ChatMessage chatMessage = dc.toObject(ChatMessage.class);
                chatMessage.setId(dc.getId());
                rs.add(chatMessage);
            }
            emitter.onSuccess(rs);

        }).addOnFailureListener(e -> {
            if (!emitter.isDisposed()) {
                emitter.onError(e);
            }

        }));
    }


    public String getNewChatId() {
        DocumentReference document = FirebaseFirestore.getInstance().collection("chats").document();
        return document.getId();
    }

    public Single<String> sendMessageToFirebase(ChatMessage message) {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> e) throws Exception {
                FirebaseFirestore.getInstance().collection("chats").document(message.getId()).set(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        e.onSuccess(message.getId());

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exp) {
                        e.onError(exp);
                    }
                });

            }
        });
    }

    public Single<String> doSignInAnonymously() {
        return Single.create(e -> FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        e.onSuccess(firebaseUser.getUid());
                    } else {
                        e.onError(task.getException());
                    }
                }));
    }

    public Single<User> createUserOnFirebase(User user) {
        return Single.create(e -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).set(user)
                    .addOnSuccessListener(aVoid -> e.onSuccess(user))
                    .addOnFailureListener(exp -> e.onError(exp));
        });
    }

    public Single<String> getFcmToken() {
        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> e) throws Exception {
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
                    final String deviceToken = instanceIdResult.getToken();
                    Timber.d("getFcmToken token=" + deviceToken);
                    e.onSuccess(deviceToken);
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exp) {
                        e.onError(exp);
                    }
                });
            }
        });
    }


    public Single updateFcmToken(String token, String myId) {
        return Single.create((SingleOnSubscribe<String>) e -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(myId).update("token", token)
                    .addOnSuccessListener(aVoid -> {
                        Timber.d("updateFcmToken onSuccess");
                        e.onSuccess(token);
                    }).addOnFailureListener(exp -> e.onError(exp));
        });

    }

}

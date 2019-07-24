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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import demo.app.simplechat.db.ChatMessage;
import demo.app.simplechat.repo.DBRepository;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ChatViewModel extends BaseViewModel {
    private MutableLiveData<ArrayList<ChatMessage>> mMessagesLiveData = new MutableLiveData<>(new ArrayList<>());
    private Set<String> mMessageIdSet = new HashSet<>();
    private boolean isLoadingData = false;
    private boolean isNoMoreData = false;
    private long listenTimeStamp;
    private ListenerRegistration mEventsListener;

    public LiveData<ArrayList<ChatMessage>> getMessagesLiveData() {
        return mMessagesLiveData;
    }

    private DBRepository mDBRepository;

    @Inject
    public ChatViewModel(DBRepository dbRepository) {
        this.mDBRepository = dbRepository;
    }

    public void sendMessage(final ChatMessage chatMessage) {
        final ArrayList<ChatMessage> currentList = mMessagesLiveData.getValue();
        currentList.add(0, chatMessage);
        mMessagesLiveData.postValue(currentList);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference document = db.collection("chats").document();
        String id = document.getId();
        chatMessage.setId(id);

        mMessageIdSet.add(id);

        document.set(chatMessage).addOnSuccessListener(aVoid -> {
            Disposable d = Completable.fromAction(() -> {
                chatMessage.setState(ChatMessage.STATE_SUCCESS);
                mDBRepository.insertChatMessage(chatMessage);
                mMessagesLiveData.postValue(currentList);
            }).subscribeOn(Schedulers.io()).subscribe();

            mDisposables.add(d);
        });

    }

    public void loadMessages(final long createTime) {
        if (isLoadingData || isNoMoreData) {
            return;
        }
        isLoadingData = true;

        mDBRepository.getChatMessages(createTime).subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<List<ChatMessage>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposables.add(d);
                    }

                    @Override
                    public void onSuccess(List<ChatMessage> list) {
                        if (list != null && list.size() > 0) {
                            Timber.d("start loadData list=" + list.size());
                            ArrayList<ChatMessage> currentList = mMessagesLiveData.getValue();
                            for (ChatMessage chatMessage : list) {
                                if (chatMessage.getCreateTime() > listenTimeStamp) {
                                    listenTimeStamp = chatMessage.getCreateTime();
                                    startListen();
                                }
                                mMessageIdSet.add(chatMessage.getId());
                                currentList.add(chatMessage);
                            }
                            mMessagesLiveData.postValue(currentList);
                            isLoadingData = false;
                        } else {
                            loadFromFirebase(createTime);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void loadFromFirebase(final long createTime) {
        Timber.d("start loadData from firebase createTime=" + createTime);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chats").whereLessThan("createTime", createTime).orderBy("createTime", Query.Direction.DESCENDING).limit(20).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().size() == 0) {
                    isNoMoreData = true;
                    if(listenTimeStamp==0){
                        listenTimeStamp = System.currentTimeMillis();
                        startListen();
                    }
                    return;
                }

                ArrayList<ChatMessage> currentList = mMessagesLiveData.getValue();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    final ChatMessage chatMessage = document.toObject(ChatMessage.class);
                    chatMessage.setId(document.getId());

                    if (chatMessage.getCreateTime() > listenTimeStamp) {
                        listenTimeStamp = chatMessage.getCreateTime();
                        startListen();
                    }

                    mMessageIdSet.add(chatMessage.getId());
                    currentList.add(chatMessage);

                    Disposable d = Completable.fromAction(new Action() {
                        @Override
                        public void run() throws Exception {
                            mDBRepository.insertChatMessage(chatMessage);
                        }
                    }).subscribeOn(Schedulers.io()).subscribe();
                    mDisposables.add(d);

                }

                mMessagesLiveData.postValue(currentList);
            } else {
            }
            isLoadingData = false;
        });
    }


    public void startListen() {
        if (listenTimeStamp == 0) {
            return;
        }
        if (mEventsListener != null) {
            mEventsListener.remove();
        }
        Timber.d("startListen createTime=" + listenTimeStamp);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mEventsListener = db.collection("chats").whereGreaterThan("createTime", listenTimeStamp).addSnapshotListener((snapshots, e) -> {
            if (snapshots == null) return;
            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                switch (dc.getType()) {
                    case ADDED:
                        Timber.d("add event: " + dc.getDocument().getData());
                        String id = dc.getDocument().getId();
                        if (!mMessageIdSet.contains(id)) {
                            mMessageIdSet.add(id);
                            final ChatMessage chatMessage = dc.getDocument().toObject(ChatMessage.class);
                            chatMessage.setId(dc.getDocument().getId());

                            ArrayList<ChatMessage> currentList = mMessagesLiveData.getValue();
                            currentList.add(0, chatMessage);
                            mMessagesLiveData.postValue(currentList);

                            Disposable d = Completable.fromAction(new Action() {
                                @Override
                                public void run() throws Exception {
                                    mDBRepository.insertChatMessage(chatMessage);
                                }
                            })
                                    .subscribeOn(Schedulers.io())
                                    .subscribe();
                            mDisposables.add(d);
                        }
                        break;
                }
            }
        });
    }

    public void stopListen() {
        if (mEventsListener != null) {
            mEventsListener.remove();
        }
    }
}

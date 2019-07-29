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
import java.util.concurrent.Callable;

import javax.annotation.Nullable;
import javax.inject.Inject;

import demo.app.simplechat.db.ChatMessage;
import demo.app.simplechat.repo.ApiRepository;
import demo.app.simplechat.repo.DBRepository;
import io.reactivex.Completable;
import io.reactivex.MaybeObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
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
    private ApiRepository mApiRepository;

    @Inject
    public ChatViewModel(DBRepository dbRepository, ApiRepository apiRepository) {
        this.mDBRepository = dbRepository;
        this.mApiRepository = apiRepository;
    }

    public void sendMessage(final ChatMessage chatMessage) {
        final ArrayList<ChatMessage> currentList = mMessagesLiveData.getValue();
        currentList.add(0, chatMessage);
        mMessagesLiveData.postValue(currentList);

        String id = mApiRepository.getNewChatId();
        Timber.d("sendMessage get chat id: "+id);

        chatMessage.setId(id);

        Timber.d("sendMessage chatMessage: "+chatMessage.toString());

        mMessageIdSet.add(id);

        mApiRepository.sendMessageToFirebase(chatMessage).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap((Function<String, SingleSource<Long>>) chatId -> {
                    chatMessage.setState(ChatMessage.STATE_SUCCESS);
                    return  mDBRepository.insertChatMessage(chatMessage);
                }).subscribe(new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposables.add(d);
            }

            @Override
            public void onSuccess(Long aLong) {
                mMessagesLiveData.postValue(currentList);
            }

            @Override
            public void onError(Throwable e) {

            }
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
                                Timber.d("loadData chatMessage=" + chatMessage.toString());
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

    public void loadFromFirebase(final long createTime) {
        Timber.d("start loadData from firebase createTime=" + createTime);
        mApiRepository.getMessagesFromFirebase(createTime).subscribe(new SingleObserver<List<ChatMessage>>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposables.add(d);
            }

            @Override
            public void onSuccess(List<ChatMessage> messages) {
                isLoadingData = false;
                if (messages.size() == 0) {
                    isNoMoreData = true;
                    if (listenTimeStamp == 0) {
                        listenTimeStamp = System.currentTimeMillis();
                        startListen();
                    }
                    return;
                }

                ArrayList<ChatMessage> currentList = mMessagesLiveData.getValue();
                for (ChatMessage chatMessage : messages) {
                    if (chatMessage.getCreateTime() > listenTimeStamp) {
                        listenTimeStamp = chatMessage.getCreateTime();
                        startListen();
                    }

                    mMessageIdSet.add(chatMessage.getId());
                    currentList.add(chatMessage);
                }
                saveChatMessageToDB(messages);
                mMessagesLiveData.postValue(currentList);
            }

            @Override
            public void onError(Throwable e) {
                isLoadingData = false;
            }

        });
    }


    private void saveChatMessageToDB(List<ChatMessage> messages){
        mDBRepository.insertChatMessages(messages)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> mDisposables.add(disposable))
                .subscribe();
    }

    public void startListen() {
        if (listenTimeStamp == 0) {
            return;
        }
        if (mEventsListener != null) {
            mEventsListener.remove();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timber.d("startListen createTime=" + listenTimeStamp);
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

                            mDBRepository.insertChatMessage(chatMessage).subscribeOn(Schedulers.io())
                                    .doOnSubscribe(disposable -> mDisposables.add(disposable)).subscribe();
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

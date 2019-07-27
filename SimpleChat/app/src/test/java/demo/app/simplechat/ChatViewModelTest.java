package demo.app.simplechat;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import demo.app.simplechat.db.ChatMessage;
import demo.app.simplechat.db.MyDatabase;
import demo.app.simplechat.repo.ApiRepository;
import demo.app.simplechat.repo.DBRepository;
import demo.app.simplechat.vm.ChatViewModel;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ChatViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();

    @Mock
    DBRepository mDBRepository;
    @Mock
    ApiRepository mApiRepository;

    private ChatViewModel mViewModel;

    private static List<ChatMessage> FAKE_LIST = new ArrayList<>();

    @BeforeClass
    public static void setUpRxSchedulers() {
        Scheduler immediate = new Scheduler() {
            @Override
            public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
                // this prevents StackOverflowErrors when scheduling with a delay
                return super.scheduleDirect(run, 0, unit);
            }

            @Override
            public Worker createWorker() {
                return new ExecutorScheduler.ExecutorWorker(Runnable::run);
            }
        };

        RxJavaPlugins.setInitIoSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitComputationSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitNewThreadSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitSingleSchedulerHandler(scheduler -> immediate);
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> immediate);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mViewModel  = new ChatViewModel(mDBRepository, mApiRepository);
    }

    @Test
    public void test_sendMessage(){
        when(mApiRepository.getNewChatId()).thenReturn("chat_id");
        when(mApiRepository.sendMessageToFirebase(any())).thenReturn(Single.just("chat_id"));

        ChatMessage chatMessage = new ChatMessage();
        mViewModel.sendMessage(chatMessage);

        List<ChatMessage> list = mViewModel.getMessagesLiveData().getValue();
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0).getId(), "chat_id");
        Assert.assertEquals(list.get(0).getState(), ChatMessage.STATE_SUCCESS);
    }

    @Test
    public void test_loadFromFirebase(){
        FAKE_LIST.clear();
        ChatMessage chatMessage = new ChatMessage();
        FAKE_LIST.add(chatMessage);

        when(mApiRepository.getMessagesFromFirebase(anyLong())).thenReturn(Single.just(FAKE_LIST));

        mViewModel.loadFromFirebase(Long.MAX_VALUE);
        Assert.assertEquals(mViewModel.getMessagesLiveData().getValue().size(), 1);
    }


    @Test
    public void test_loadMessages(){
        FAKE_LIST.clear();
        ChatMessage chatMessage = new ChatMessage();
        FAKE_LIST.add(chatMessage);
        when(mDBRepository.getChatMessages(anyLong())).thenReturn(Single.just(FAKE_LIST));

        mViewModel.loadMessages(Long.MAX_VALUE);
        Assert.assertEquals(mViewModel.getMessagesLiveData().getValue().size(), 1);
    }

}
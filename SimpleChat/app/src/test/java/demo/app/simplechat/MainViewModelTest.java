package demo.app.simplechat;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import demo.app.simplechat.cache.UserLiveCache;
import demo.app.simplechat.db.ChatMessage;
import demo.app.simplechat.db.User;
import demo.app.simplechat.repo.ApiRepository;
import demo.app.simplechat.repo.DBRepository;
import demo.app.simplechat.repo.LocalRepository;
import demo.app.simplechat.vm.ChatViewModel;
import demo.app.simplechat.vm.MainViewModel;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.plugins.RxJavaPlugins;

import static org.mockito.Mockito.when;

public class MainViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();

    @Mock
    DBRepository mDBRepository;
    @Mock
    LocalRepository mLocalRepository;
    @Mock
    ApiRepository mApiRepository;

    private MainViewModel mViewModel;
    private UserLiveCache mUserCache;

    private static List<User> FAKE_LIST = new ArrayList<>();

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
        mUserCache = new UserLiveCache();
        mViewModel  = new MainViewModel(mDBRepository, mLocalRepository, mApiRepository, mUserCache);
    }

    @Test
    public void test_refreshUsers(){
        User u1 = new User();
        User u2 = new User();
        u1.setUid("u1");
        u2.setUid("u2");

        FAKE_LIST.add(u1);
        FAKE_LIST.add(u2);

        when(mDBRepository.getAllUsers()).thenReturn(Single.just(FAKE_LIST));
        mViewModel.refreshUsers();

        Assert.assertTrue(mUserCache.containsUser(u1));
        Assert.assertTrue(mUserCache.containsUser(u2));
        Assert.assertEquals(mUserCache.getSize(), 2);
    }
}

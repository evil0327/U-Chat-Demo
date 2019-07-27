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

import java.util.concurrent.TimeUnit;

import demo.app.simplechat.db.User;
import demo.app.simplechat.repo.ApiRepository;
import demo.app.simplechat.repo.DBRepository;
import demo.app.simplechat.repo.LocalRepository;
import demo.app.simplechat.vm.ChatViewModel;
import demo.app.simplechat.vm.LoginViewModel;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.plugins.RxJavaPlugins;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LoginViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();

    @Mock
    DBRepository mDBRepository;
    @Mock
    ApiRepository mApiRepository;
    @Mock
    LocalRepository mLocalRepository;

    private LoginViewModel mViewModel;

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
        mViewModel  = new LoginViewModel(mLocalRepository, mDBRepository, mApiRepository);
    }

    @Test
    public void test_login(){
        String userId = "user_id";
        User user = new User();
        user.setUid(userId);

        when(mApiRepository.doSignInAnonymously()).thenReturn(Single.just(userId));
        when(mApiRepository.createUserOnFirebase(any())).thenReturn(Single.just(user));

        mViewModel.doLogin();

        Assert.assertEquals(mViewModel.getLoadingShowLiveData().getValue(), false);
        Assert.assertEquals(mViewModel.getLoginStateLiveData().getValue(), true);
    }


    @Test
    public void test_login_fail(){
        String userId = "user_id";
        User user = new User();
        user.setUid(userId);
        when(mApiRepository.doSignInAnonymously()).thenReturn(Single.error(new Exception()));

        mViewModel.doLogin();

        Assert.assertEquals(mViewModel.getLoadingShowLiveData().getValue(), false);
        Assert.assertEquals(mViewModel.getLoginStateLiveData().getValue(), false);
    }

    @Test
    public void test_create_user_fail(){
        String userId = "user_id";
        User user = new User();
        user.setUid(userId);

        when(mApiRepository.doSignInAnonymously()).thenReturn(Single.just(userId));
        when(mApiRepository.createUserOnFirebase(any())).thenReturn(Single.error(new Exception()));

        mViewModel.doLogin();

        Assert.assertEquals(mViewModel.getLoadingShowLiveData().getValue(), false);
        Assert.assertEquals(mViewModel.getLoginStateLiveData().getValue(), false);
    }
}


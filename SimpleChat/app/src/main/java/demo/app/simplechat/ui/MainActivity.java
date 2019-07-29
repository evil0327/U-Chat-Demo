package demo.app.simplechat.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import demo.app.simplechat.R;
import demo.app.simplechat.db.User;
import demo.app.simplechat.di.DaggerComponentHolder;
import demo.app.simplechat.ui.dialog.ProfileDialog;
import demo.app.simplechat.repo.LocalRepository;
import demo.app.simplechat.ui.login.LoginActivity;
import demo.app.simplechat.util.MyEvent;
import demo.app.simplechat.vm.MainViewModel;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    @Inject
    LocalRepository mLocalRepository;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private MainViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DaggerComponentHolder.getAppComponent().inject(this);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(MainViewModel.class);
        mViewModel.getLiveUserMap().observe(this, map -> EventBus.getDefault().post(new MyEvent(MyEvent.NOTIFY_USER_CHANGE)));

        Observable.fromCallable(() -> mLocalRepository.isLogin()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isLogin -> {
                    if(!isLogin){
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        setContentView(R.layout.activity_main);
                        mViewModel.getFcmToken();
                        mViewModel.refreshUsers();
                    }
                });


    }

    @Override
    public boolean onSupportNavigateUp() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.host_fragment);
        return NavHostFragment.findNavController(fragment).navigateUp();
    }


    @Override
    public void onStart() {
        super.onStart();
        if(mViewModel!=null){
            mViewModel.startListen();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mViewModel!=null){
            mViewModel.stopListen();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}



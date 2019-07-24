package demo.app.simplechat.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import demo.app.simplechat.R;
import demo.app.simplechat.di.DaggerComponentHolder;
import demo.app.simplechat.ui.MainActivity;
import demo.app.simplechat.ui.dialog.LoadingDialog;
import demo.app.simplechat.repo.LocalRepository;
import demo.app.simplechat.vm.LoginViewModel;

public class LoginActivity  extends AppCompatActivity{
    private LoadingDialog mLoadingDialog;
    @Inject
    LocalRepository mLocalRepository;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private LoginViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DaggerComponentHolder.getAppComponent().inject(this);
        ButterKnife.bind(this);

        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(LoginViewModel.class);
        mLoadingDialog = new LoadingDialog(this);

        mViewModel.getLoadingShowLiveData().observe(this, isShow -> {
            if(isShow){
                if(!mLoadingDialog.isShowing()){
                    mLoadingDialog.show();
                }
            }else{
                mLoadingDialog.dismiss();
            }
        });

        mViewModel.getToastLiveData().observe(this, msg -> Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show());

        mViewModel.getLoginStateLiveData().observe(this, isLogin -> {
            if(isLogin){
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    @OnClick(R.id.login)
    public void doLogin(){
        mViewModel.login();
    }
}
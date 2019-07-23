package demo.app.simplechat.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.request.RequestOptions;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import demo.app.simplechat.R;
import demo.app.simplechat.db.User;
import demo.app.simplechat.di.DaggerComponentHolder;
import demo.app.simplechat.repo.DBRepository;
import demo.app.simplechat.repo.LocalRepository;
import demo.app.simplechat.ui.dialog.ProfileDialog;
import demo.app.simplechat.ui.login.LoginActivity;
import demo.app.simplechat.util.GlideApp;
import demo.app.simplechat.util.ImageHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainFragment extends Fragment {
    private Unbinder mUnbinder;

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private ProfileDialog mProfileDialog;
    @Inject
    LocalRepository mLocalRepository;
    @Inject
    DBRepository mDBRepository;

    @BindView(R.id.name)
    TextView mNameView;
    @BindView(R.id.avatar)
    ImageView mAvatarView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectCustomSlowCalls() //API等級11，使用StrictMode.noteSlowCode
                .detectAll()
                .penaltyLog() //在Logcat 中列印違規異常資訊
                .build());
        DaggerComponentHolder.getAppComponent().inject(this);

        mProfileDialog = new ProfileDialog(getContext());
        if(mLocalRepository.isFirstTimeUse()){
            mLocalRepository.saveFirstTimeUse(false);
            mProfileDialog.show();
        }
        mProfileDialog.setOnDismissListener(dialog -> refreshData());

        refreshData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick(R.id.start_chat)
    public void startChat(){
        Navigation.findNavController(getView()).navigate(R.id.action_mainFragment_to_detailFragment);
    }

    @OnClick(R.id.profile_area)
    public void showProfileDialog(){
        mProfileDialog.show();
    }

    private void refreshData(){
        mDBRepository.getUserById(mLocalRepository.getUID()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    mNameView.setText(user.getName());

                    GlideApp.with(getContext())
                            .load(ImageHelper.getResource(user.getAvatar()))
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .into(mAvatarView);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}

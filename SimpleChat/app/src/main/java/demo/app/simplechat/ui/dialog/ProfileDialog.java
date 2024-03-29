package demo.app.simplechat.ui.dialog;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import demo.app.simplechat.R;
import demo.app.simplechat.db.User;
import demo.app.simplechat.di.DaggerComponentHolder;
import demo.app.simplechat.repo.DBRepository;
import demo.app.simplechat.repo.LocalRepository;
import demo.app.simplechat.util.GlideApp;
import demo.app.simplechat.util.ImageHelper;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileDialog extends Dialog {
    @Inject
    LocalRepository mLocalRepository;
    @Inject
    DBRepository mDBRepository;
    private LoadingDialog mLoadingDialog;
    private User mUser;
    private int mSelectAvatar;

    @BindView(R.id.name_edit)
    EditText mNameEdit;
    @BindView(R.id.avatar)
    ImageView mAvatarView;

    private boolean mIsFliping =false;

    public ProfileDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.view_profile_dialog);
        DaggerComponentHolder.getAppComponent().inject(this);
        ButterKnife.bind(this, getWindow().getDecorView());

        setCancelable(false);

        mLoadingDialog = new LoadingDialog(context);

        initData();
    }

    @OnClick(R.id.save)
    void save(){
        mUser.setAvatar(mSelectAvatar);
        mUser.setName(mNameEdit.getText().toString());

        mLoadingDialog.show();

        mDBRepository.upsertUser(mUser).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
               .subscribe(new SingleObserver<Long>() {
                   @Override
                   public void onSubscribe(Disposable d) {

                   }

                   @Override
                   public void onSuccess(Long aLong) {
                       updateUserOnFirebase(mUser);
                       mLoadingDialog.dismiss();
                       dismiss();

                   }

                   @Override
                   public void onError(Throwable e) {

                   }
               });
    }

    @OnClick(R.id.avatar)
    void changeAvatar(){
        ObjectAnimator animation = ObjectAnimator.ofFloat(mAvatarView, "rotationY", 0f, 180f);
        animation.setDuration(700);
        animation.addUpdateListener(valueAnimator -> {
            if(((Float)valueAnimator.getAnimatedValue()>=90f)){
                setNextAvatar();
            }
        });
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAvatarView.setRotationY(0);

                GlideApp.with(getContext())
                        .load(ImageHelper.getResource(mSelectAvatar))
                        .centerCrop()
                        .apply(RequestOptions.circleCropTransform())
                        .into(mAvatarView);

                mIsFliping = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animation.start();
    }


    private void setNextAvatar(){
        if(mIsFliping){
            return;
        }
        mIsFliping = true;

        int[] rs = ImageHelper.getNextResoure(mSelectAvatar);
        mSelectAvatar = rs[0];


        GlideApp.with(getContext())
                .load(ImageHelper.getResource(mSelectAvatar))
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
               .into(new SimpleTarget<Drawable>() {
                   @Override
                   public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                       BitmapDrawable bd = (BitmapDrawable) resource;
                       Bitmap bm = bd.getBitmap();

                       Matrix matrix = new Matrix();
                       matrix.preScale(-1.0f, 1.0f);
                       Bitmap rs = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

                       mAvatarView.setImageBitmap(rs);
                   }
               });

    }

    private void initData(){
        mDBRepository.getUserById(mLocalRepository.getUID()).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<User>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }
                    @Override
                    public void onSuccess(User me) {
                        mUser = me;
                        mSelectAvatar = me.getAvatar();
                        mNameEdit.setText(me.getName());
                        GlideApp.with(getContext())
                                .load(ImageHelper.getResource(me.getAvatar()))
                                .centerCrop()
                                .apply(RequestOptions.circleCropTransform())
                                .into(mAvatarView);

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void updateUserOnFirebase(User user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap map = new HashMap();
        map.put("avatar", user.getAvatar());
        map.put("name", user.getName());
        db.collection("users").document(user.getUid()).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

}


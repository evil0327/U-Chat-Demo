package demo.app.simplechat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import demo.app.simplechat.R;

public  class LoadingDialog extends Dialog {
    private ImageView mLoadingView;
    private Animation mAnim;
    private Context context;

    public LoadingDialog(Context context) {
        super(context, R.style.loading_dialog);
        this.context = context;
        init();
    }

    /**
     * Init dialog.
     */
    private void init() {
        setContentView(R.layout.view_loading_dialog);
        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.loading_rotate);
        LinearInterpolator lin = new LinearInterpolator();
        mAnim.setInterpolator(lin);

        mLoadingView = this.findViewById(R.id.loading);
    }


    @Override
    public void show() {
        super.show();
        mLoadingView.startAnimation(mAnim);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mAnim.cancel();
    }
}
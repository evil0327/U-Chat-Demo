package demo.app.simplechat.vm;

import androidx.lifecycle.ViewModel;

import demo.app.simplechat.util.SingleLiveEvent;
import io.reactivex.disposables.CompositeDisposable;

public class BaseViewModel extends ViewModel {
    protected CompositeDisposable mDisposables = new CompositeDisposable();
    protected SingleLiveEvent<String> mToastMsgLiveData = new SingleLiveEvent<>();

    @Override
    protected void onCleared() {
        super.onCleared();
        mDisposables.dispose();
    }
}

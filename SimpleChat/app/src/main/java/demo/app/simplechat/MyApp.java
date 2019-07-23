package demo.app.simplechat;

import android.app.Application;

import demo.app.simplechat.di.AppModule;
import demo.app.simplechat.di.DaggerComponentHolder;
import demo.app.simplechat.di.DaggerMyDaggerComponent;
import demo.app.simplechat.di.MyDaggerComponent;
import demo.app.simplechat.di.RepoModule;
import timber.log.Timber;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        MyDaggerComponent daggerComponent = DaggerMyDaggerComponent.builder()
                .appModule(new AppModule(this)) // This also corresponds to the name of your module: %component_name%Module
                .repoModule(new RepoModule())
                .build();
        DaggerComponentHolder.setAppComponent(daggerComponent);
    }
}

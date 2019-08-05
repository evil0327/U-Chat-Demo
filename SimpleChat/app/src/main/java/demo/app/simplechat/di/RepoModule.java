package demo.app.simplechat.di;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.firestore.FirebaseFirestore;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import demo.app.simplechat.cache.UserLiveCache;
import demo.app.simplechat.repo.ApiRepository;
import demo.app.simplechat.repo.DBRepository;
import demo.app.simplechat.repo.LocalRepository;

@Module
public class RepoModule {
    @Provides
    @Singleton
    public LocalRepository provideLocalRepo(SharedPreferences sp) {
        return new LocalRepository(sp);
    }

    @Provides
    @Singleton
    public DBRepository provideDatabaseRepo(Application application) {
        return new DBRepository(application);
    }

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    UserLiveCache provideUserCache() {
        UserLiveCache cache = new UserLiveCache();
        return cache;
    }

    @Singleton
    @Provides
    ApiRepository providesApiRepo() {
        return new ApiRepository();
    }
}

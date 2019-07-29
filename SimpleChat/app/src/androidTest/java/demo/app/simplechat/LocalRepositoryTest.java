package demo.app.simplechat;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import demo.app.simplechat.repo.LocalRepository;

@RunWith(AndroidJUnit4.class)
public class LocalRepositoryTest {
    private LocalRepository mLocalRepository;
    private SharedPreferences mSharedPreferences;

    @Before
    public void before() {
        Context context = InstrumentationRegistry.getTargetContext();
        mSharedPreferences = context.getSharedPreferences("test_pref", Context.MODE_PRIVATE);
        mLocalRepository = new LocalRepository(mSharedPreferences);
    }

    @Test
    public void test(){
        String uid = "uid";
        mLocalRepository.saveUID(uid);

        Assert.assertEquals(mLocalRepository.getUID(), uid);
        Assert.assertEquals(mLocalRepository.isLogin(), true);

        mLocalRepository.saveFirstTimeUse(false);
        Assert.assertEquals(mLocalRepository.isFirstTimeUse(), false);
    }

    @After
    public void after() {
        mSharedPreferences.edit().clear();
    }

}

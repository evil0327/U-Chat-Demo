package demo.app.simplechat.di;


import javax.inject.Singleton;

import dagger.Component;
import demo.app.simplechat.ui.chat.ChatMessageAdapter;
import demo.app.simplechat.ui.login.LoginActivity;
import demo.app.simplechat.ui.MainActivity;
import demo.app.simplechat.ui.chat.ChatFragment;
import demo.app.simplechat.ui.MainFragment;
import demo.app.simplechat.ui.dialog.ProfileDialog;

@Singleton
@Component(modules={AppModule.class, RepoModule.class})
public interface MyDaggerComponent {
    void inject(MainFragment fragment);
    void inject(ChatFragment fragment);
    void inject(MainActivity activity);
    void inject(LoginActivity activity);
    void inject(ProfileDialog profileDialog);
    void inject(ChatMessageAdapter adapter);
}
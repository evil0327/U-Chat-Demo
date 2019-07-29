package demo.app.simplechat.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import demo.app.simplechat.ui.MainActivity;
import demo.app.simplechat.R;

public class MyFirebaseService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {
            String content = remoteMessage.getData().get("body");

            PendingIntent pendingIndent = new NavDeepLinkBuilder(getBaseContext())
                    .setComponentName(MainActivity.class)
                    .setGraph(R.navigation.nav)
                    .setDestination(R.id.chatFragment)
                    .createPendingIntent();

            Notification notification = new NotificationCompat.Builder(getBaseContext(), "my_channel_0")
                    .setSmallIcon(R.mipmap.app)
                    .setContentTitle(getString(R.string.message_coming))
                    .setContentText( content)
                    .setContentIntent(pendingIndent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = notificationManager.getNotificationChannel("my_channel_0");
                if (channel == null ) {
                    channel = new NotificationChannel("my_channel_0", getString(R.string.message_coming), importance);
                    notificationManager.createNotificationChannel(channel);
                }
            }
            notificationManager.notify(0, notification);
        }

    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}
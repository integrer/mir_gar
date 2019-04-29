package org.mirgar;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.DateFormat;

import java.sql.Date;

public class AppealSender extends Service {

    private int maxValue;
    private int currentProgress;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void sendAppeal(long localId) {
//        try(){
        //fetch appeal
        //send to server

//        }
    }

    private void createNotofication() {
//        final Notification.Builder builder = getNotification();
//        NotificationManagerCompat.from(getApplicationContext()).notify(0, builder.build());
    }

    private void publishProgress() {
    }

    private Notification.Builder getNotification(String appealName, Date appealDate) {
        return new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.logo_mg_label_acenter)
                .setContentTitle("Отправка вашего обращения...")
                .setContentText(appealName + " / " + DateFormat.format("dd.MM.yyyy", appealDate));
    }
}

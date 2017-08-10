package com.mejestic.bump.application;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import com.mejestic.R;

public class CustomNotificationManager {

  private final Context mContext;

  public CustomNotificationManager(Context context) {
    mContext = context;
  }

  //Creating notification that is created when pot hole is found near
  public void createNotifcation() {
    Notification notification = getNotification();
    NotificationManager mNotificationManager =
        (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    // mId allows you to update the notification later on.
    mNotificationManager.notify(1, notification);
  }

  // Get Data for the notification
  private Notification getNotification() {
    Intent intent = new Intent();
    NotificationCompat.Builder builder = getBuilder(intent);
    Notification notification = builder.build();
    setBehavioralFlags(notification);
    return notification;
  }

  // Setting default information for the notification
  private Notification setBehavioralFlags(Notification notification) {
    notification.sound =
        Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.pot_h_noti);
    notification.flags |= Notification.FLAG_AUTO_CANCEL;
    notification.flags |= Notification.FLAG_SHOW_LIGHTS;
    notification.ledARGB = Color.YELLOW;
    notification.ledOnMS = 200;
    notification.ledOffMS = 1000;
    return notification;
  }

  // Initialise Notification object
  @NonNull private NotificationCompat.Builder getBuilder(Intent intent) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext).setSmallIcon(
        android.R.drawable.ic_notification_overlay)
        .setContentTitle("Pot Hole Ahead")
        .setTicker("")
        .setSmallIcon(android.R.drawable.ic_notification_overlay)
        .setStyle(new NotificationCompat.BigTextStyle().bigText("message"))
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentText("Please go slow for your safety")
        .setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));

    return builder;
  }
}

package com.example.messengerapplication.notifications.entity

data class PushNotification(
    val data: NotificationData,
    val to: String
)
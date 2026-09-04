package com.orielair.api.user.valueObject

data class UserSettings (
    var notifications: NotificationSettings = NotificationSettings(),
    var app: AppSettings = AppSettings()
)

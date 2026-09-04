package com.orielair.api.user.valueObject

import com.orielair.api.user.enum.Theme

data class AppSettings(
    var theme: Theme = Theme.SYSTEM,
)
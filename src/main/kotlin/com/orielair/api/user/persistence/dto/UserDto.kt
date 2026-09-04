package com.orielair.api.user.persistence.dto

import com.orielair.api.user.valueObject.UserSettings
import java.util.UUID

class UserDto (
    var id: UUID? = null,
    var username: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var settings: UserSettings? = null,
)
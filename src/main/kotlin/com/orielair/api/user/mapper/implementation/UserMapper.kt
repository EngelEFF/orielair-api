package com.orielair.api.user.mapper.implementation

import com.orielair.api.user.persistence.dto.UserDto
import com.orielair.api.user.persistence.entity.User
import com.orielair.api.vital.mapper.Mapper
import org.springframework.stereotype.Component

@Component
class UserMapper: Mapper<User, UserDto> {

    override fun mapTo(e: User): UserDto {
        return UserDto(
            id = e.id,
            username = e.username,
            firstName = e.firstName,
            lastName = e.lastName,
            email = e.email,
            settings =e.settings,
        )
    }

    override fun mapFrom(d: UserDto): User {
        return User(
            id = d.id,
            username = d.username,
            firstName = d.firstName,
            lastName = d.lastName,
            email = d.email,
            settings = d.settings
        )
    }
}
package com.orielair.api.user.service

import com.orielair.api.user.persistence.entity.User
import com.orielair.api.user.persistence.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID


@Service
class UserService(
    val userRepository: UserRepository
   ) {

    private val logger = LoggerFactory.getLogger(User::class.java)

    fun create(user: User): User {
        return userRepository.save(user)
    }


    fun read(id: UUID): User? {
        return userRepository.findByIdOrNull(id)
    }

    fun readAll(pageable: Pageable): Page<User> {
        return userRepository.findAll(pageable)
    }

    fun partialUpdate(id: UUID, user: User): User?{

        // verifies if the id in the path patches the one of the entity to be updated to ensure consistency
        if(user.id != null && user.id != id){
            throw IllegalArgumentException("ID mismatch between path and payload $id and ${user.id}")
        }


        // checks the existence of the userService
        return userRepository.findByIdOrNull(id)?.let { retrievedLocalUser ->

            // if the userService exists, retrieve from the data and from keycloak as well, updates both sources
            user.username?.let { retrievedLocalUser.username = it;}
            user.firstName?.let { retrievedLocalUser.firstName = it;  }
            user.lastName?.let { retrievedLocalUser.lastName = it;  }
            user.email?.let { retrievedLocalUser.email = it; }

            // Retrieves the user's settings and updates only the provided setting(s)
            user.settings?.app?.theme.let { retrievedLocalUser.settings?.app?.theme = it!!}
            user.settings?.notifications?.elevatedRiskAlert.let { retrievedLocalUser.settings?.notifications?.elevatedRiskAlert = it!! }

            // logs success info
            logger.info("✅✅Update successful for both keycloak and db✅✅");
            return userRepository.save(retrievedLocalUser)
        }


    }

    fun delete(id: UUID) {
        userRepository.deleteById(id)
    }

    fun deleteAll(){
        userRepository.deleteAll()
    }


}
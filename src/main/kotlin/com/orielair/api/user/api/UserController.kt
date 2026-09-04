package com.orielair.api.user.api

import com.orielair.api.user.mapper.implementation.UserMapper
import com.orielair.api.user.persistence.dto.UserDto
import com.orielair.api.user.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID


@RestController
@RequestMapping("/users")
class UserController(
    val userService: UserService,
    val userMapper: UserMapper,
) {


    // retrieves a user by id
    @GetMapping(path = ["/{id}"])
    fun getUser(@PathVariable id:UUID) : ResponseEntity<UserDto> {

        return userService.read(id)?.let{ user ->

            val userDto = userMapper.mapTo(user)

            ResponseEntity(userDto, HttpStatus.OK)

        }?: ResponseEntity(HttpStatus.NOT_FOUND)

    }


    @GetMapping
    fun getUsers(
        @RequestParam(value = "page", defaultValue = "0") page: Int,
        @RequestParam(value = "pageSize", defaultValue = "10") pageSize: Int,
    ): ResponseEntity<Page<UserDto>>{

        val pageable = PageRequest.of(page, pageSize)

        val userPage = userService.readAll(pageable)

        val userPageDto = userPage.map { userMapper.mapTo(it) }

        return ResponseEntity(userPageDto, HttpStatus.OK)

    }

    /* Creating of userService profile will be handled internally */

    @PatchMapping(path = ["/{id}"])
    fun partialUpdateProfile(@PathVariable id: UUID, @RequestBody userDto: UserDto): ResponseEntity<UserDto> {

        val user = userMapper.mapFrom(userDto)

        return userService.partialUpdate(id, user)?.let{

            val userDto = userMapper.mapTo(it)
            ResponseEntity(userDto, HttpStatus.OK)

        }?: ResponseEntity(userDto, HttpStatus.NOT_FOUND) // returns the userService we had to update that we couldn't find it from the database

    }

    @PostMapping
    fun createUser(/*@Valid*/ @RequestBody userDto: UserDto): ResponseEntity<UserDto> {
        val user = userMapper.mapFrom(userDto)
        val savedUserDto = userMapper.mapTo(userService.create(user))
        return ResponseEntity(savedUserDto, HttpStatus.CREATED)
    }


    @DeleteMapping(path = ["/{id}"])
    fun deleteUser(@PathVariable("id") id: UUID): ResponseEntity<Void> {
        userService.delete(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @DeleteMapping
    fun deleteUser(): ResponseEntity<Void> {
        userService.deleteAll()
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}
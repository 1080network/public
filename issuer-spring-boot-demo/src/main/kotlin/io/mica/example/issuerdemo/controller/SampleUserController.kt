package io.mica.example.issuerdemo.controller

import io.mica.example.issuerdemo.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/users")
class SampleUserController(val userService: UserService) {

    @PostMapping(consumes = ["application/json"])
    suspend fun registerUser(user: User): User =
        userService.createUser(user).let {
            it.toUser()
        }

    @GetMapping("/{id}", produces = ["application/json"])
    suspend fun readUser(@PathVariable id: String): User =
        userService.readUser(id)?.let {
            it.toUser()
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User with id $id was not found.")

}
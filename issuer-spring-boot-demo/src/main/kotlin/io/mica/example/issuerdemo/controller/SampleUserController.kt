package io.mica.example.issuerdemo.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleUserController {

    @PostMapping("/users")
    suspend fun RegisterUser(user: User): User {
        return user
    }
}
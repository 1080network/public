package io.mica.example.issuerdemo.service

import io.mica.example.issuerdemo.controller.User
import io.mica.example.issuerdemo.controller.toUserData
import io.mica.example.issuerdemo.datalayer.InMemoryRepository
import io.mica.example.issuerdemo.datalayer.UserData
import io.mica.example.issuerdemo.micaclient.MicaIssuanceClient
import org.springframework.stereotype.Component

import java.util.UUID

@Component
class UserService(private val repository: InMemoryRepository, private val micaClient: MicaIssuanceClient) {

    suspend fun createUser(user: User): UserData {
        val userData = user.toUserData()
        val newUser = repository.createUser(userData)
        //register this user with Mica this should really be done
        //with a more robust approach like a reconciliation loop that
        //keeps retrying if something fails, this is a naive approach
        val micaKey = micaClient.registerUser(newUser)
        newUser.micaIdKey = micaKey
        repository.updateUser(newUser)
        return newUser
    }

    suspend fun readUser(id: String) : UserData? = repository.searchUserById(id)

}
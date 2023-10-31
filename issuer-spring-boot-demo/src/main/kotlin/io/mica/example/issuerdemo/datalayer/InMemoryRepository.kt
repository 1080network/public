package io.mica.example.issuerdemo.datalayer

import org.springframework.stereotype.Repository

@Repository
class InMemoryRepository {
    private val idToUserMap: HashMap<String, UserData> = HashMap()

    fun createUser(userData: UserData): UserData{
        if (idToUserMap.containsKey(userData.id)){
            throw IllegalStateException("id already in use")
        }
        idToUserMap[userData.id] = userData
        return userData
    }

    fun updateUser(userData: UserData): UserData{
        if (idToUserMap.containsKey(userData.id)){
            throw IllegalStateException("id not found")
        }
        idToUserMap[userData.id] = userData
        return userData
    }

    fun searchUserById(id: String): UserData? {
        return idToUserMap[id]
    }

}
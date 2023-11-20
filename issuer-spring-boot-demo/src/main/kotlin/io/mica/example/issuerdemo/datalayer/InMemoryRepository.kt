package io.mica.example.issuerdemo.datalayer

import org.springframework.stereotype.Repository

@Repository
class InMemoryRepository {
    private val idToUserMap: HashMap<String, UserData> = HashMap()
    private val userIdToAccount: HashMap<String, UserAccount> = HashMap()
    private val idToUserAccount: HashMap<String, UserAccount> = HashMap()

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

    fun searchUserById(id: String): UserData? = idToUserMap[id]


    fun createAccount(account: UserAccount){
        if (userIdToAccount.containsKey(account.userId)){
            throw IllegalStateException("user already has an account")
        }
        userIdToAccount[account.userId] = account
        idToUserAccount[account.id] = account
    }
    fun getAccountByUserId(userId: String): UserAccount? = userIdToAccount[userId]

    fun getAccountById(id: String) : UserAccount? = idToUserAccount[id]

    fun updateAccount(account: UserAccount){
        userIdToAccount[account.userId] = account
        idToUserAccount[account.id] = account
    }


}
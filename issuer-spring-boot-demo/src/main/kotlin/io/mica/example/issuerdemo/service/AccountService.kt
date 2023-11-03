package io.mica.example.issuerdemo.service

import io.mica.example.issuerdemo.datalayer.InMemoryRepository
import io.mica.example.issuerdemo.datalayer.UserAccount
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountService (val repository: InMemoryRepository) {

    fun debitAccount(accountId: String, requestedAmount: Long, isPartial: Boolean): TransactionResult{
        //TODO big overlook by not creating a transaction and operating off that with a ledger, this is just an example
        val account = repository.getAccountById(accountId) ?: return TransactionResult(ApprovalType.ACCOUNT_NOT_FOUND, 0)
        if (account.balance <= 0 ){
            return TransactionResult(ApprovalType.INSUFFICIENT_BALANCE, 0)
        }
        val result : Pair<UserAccount, TransactionResult> = when{
            isPartial && account.balance < requestedAmount -> UserAccount(id = account.id, userId = account.userId, shortName = account.shortName, 0, micaIdKey = account.micaIdKey) to TransactionResult(ApprovalType.PARTIAL_APPROVAL, account.balance)
            !isPartial && account.balance < requestedAmount -> account to TransactionResult(ApprovalType.INSUFFICIENT_BALANCE, 0)
            else -> {
                val newBalance = account.balance - requestedAmount
                UserAccount(id = account.id, userId = account.userId, shortName = account.shortName, newBalance, micaIdKey = account.micaIdKey) to TransactionResult(ApprovalType.FULL_APPROVAL, newBalance)
            }
        }
        repository.updateAccount(result.first)
        return result.second
    }
}

enum class ApprovalType{
    ACCOUNT_NOT_FOUND, ACCOUNT_CLOSED, FULL_APPROVAL, PARTIAL_APPROVAL, INSUFFICIENT_BALANCE
}

data class TransactionResult(val approvalCode: ApprovalType, val approvedAmount: Long, val transactionReference: String = UUID.randomUUID().toString())
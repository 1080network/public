package io.mica.example.issuerdemo.grpc.server

import com.google.protobuf.Timestamp
import io.mica.example.issuerdemo.service.AccountService
import io.mica.example.issuerdemo.service.ApprovalType
import io.mica.serviceprovider.service.v1.ServiceProviderFromMicaServiceGrpcKt
import io.mica.serviceprovider.value.v1.ValueProto
import io.mica.serviceprovider.value.v1.ValueProto.ObtainValueResponse
import io.mica.serviceprovider.value.v1.obtainValueResponse
import io.micashared.common.enums.v1.ApprovalTypeEnumProto
import io.micashared.common.ping.v1.PingProto
import io.micashared.common.ping.v1.PingProto.PingResponse
import io.micashared.common.ping.v1.pingResponse
import io.micashared.common.v1.ErrorProto
import net.devh.boot.grpc.server.service.GrpcService
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Instant
import java.util.UUID

@GrpcService
class SampleIssuerServer(val accountService: AccountService) : ServiceProviderFromMicaServiceGrpcKt.ServiceProviderFromMicaServiceCoroutineImplBase() {

    override suspend fun ping(request: PingProto.PingRequest): PingProto.PingResponse =
         pingResponse {
            status = PingResponse.Status.STATUS_SUCCESS
            buildSha = "SAMPLESHA"
            buildTime = "unknown"
            serverStartTime = startTime
            serverTime = Timestamp.newBuilder().setSeconds(Instant.now().epochSecond).build()
        }


    override suspend fun obtainValue(request: ValueProto.ObtainValueRequest): ValueProto.ObtainValueResponse {
        val accountId = request.value.serviceProviderInstrumentRef
        val isPartial = when(request.approvalType) {
            ApprovalTypeEnumProto.ApprovalType.APPROVAL_TYPE_PARTIAL -> true
            else -> false
        }
        val amount = floatingStringToLong(request.value.requestedAmount) ?: return obtainValueResponse {
            status = ObtainValueResponse.Status.STATUS_ERROR
            transactionRef = UUID.randomUUID().toString()
            approvedAmount = "0"
            error = ErrorProto.Error.newBuilder().setMessage("incorrect amount").build()
        }
        val result = accountService.debitAccount(accountId, amount, isPartial)
        return obtainValueResponse {
            status = result.approvalCode.toMicaStatus()
            approvedAmount = toStringAmount(result.approvedAmount.toDouble())
            transactionRef = result.transactionReference
        }
    }

    override suspend fun receiveValue(request: ValueProto.ReceiveValueRequest): ValueProto.ReceiveValueResponse {
        return super.receiveValue(request)
    }


    companion object{
        private val startTime: Timestamp = Timestamp.newBuilder().setSeconds(Instant.now().epochSecond).build()
    }
}

private val decimalFormat: DecimalFormat = DecimalFormat("0.000").apply { this.roundingMode = RoundingMode.HALF_UP}
private fun toStringAmount(amount: Double): String {
    val decimalAmount: Double = amount/100
    return decimalFormat.format(decimalAmount)
}

private val rounder = DecimalFormat("#.##").apply {
    roundingMode = RoundingMode.HALF_UP
}
private fun floatingStringToLong(input: String): Long? = input.toFloatOrNull()?.let { it1 ->
    val roundedNumber = rounder.format(it1)
    roundedNumber.toFloatOrNull()?.let { it2 -> (it2 * 100).toLong() }
}

fun ApprovalType.toMicaStatus() : ObtainValueResponse.Status = when(this){
    ApprovalType.FULL_APPROVAL -> ObtainValueResponse.Status.STATUS_FULL_APPROVAL
    ApprovalType.PARTIAL_APPROVAL -> ObtainValueResponse.Status.STATUS_PARTIAL_APPROVAL
    ApprovalType.INSUFFICIENT_BALANCE -> ObtainValueResponse.Status.STATUS_INSUFFICIENT_VALUE
    ApprovalType.ACCOUNT_NOT_FOUND -> ObtainValueResponse.Status.STATUS_NOT_FOUND
    ApprovalType.ACCOUNT_CLOSED -> ObtainValueResponse.Status.STATUS_INSTRUMENT_CLOSED
}
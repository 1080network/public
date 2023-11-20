package io.mica.example.issuerdemo.grpc.server

import io.mica.example.issuerdemo.datalayer.InMemoryRepository
import io.mica.example.issuerdemo.datalayer.UserAccount
import io.mica.example.issuerdemo.datalayer.UserData
import io.mica.example.issuerdemo.service.AccountService
import io.mica.serviceprovider.value.v1.ValueProto.ObtainValueResponse
import io.mica.serviceprovider.value.v1.obtainValueRequest
import io.mica.serviceprovider.value.v1.valueRequest
import io.micashared.common.enums.v1.*
import io.micashared.common.ping.v1.PingProto.PingResponse
import io.micashared.common.ping.v1.pingRequest
import io.micashared.common.v1.AddressProto.Address
import io.micashared.common.v1.address
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import java.time.LocalDate
import java.util.*

class SampleIssuerServerTest {

    private val inMemoryRepository  = InMemoryRepository()
    private val accountService = AccountService(inMemoryRepository)
    private val gRPCService = SampleIssuerServer(accountService)
    @Test
    fun `test ping in process`() = runTest{
        val response = gRPCService.ping(pingRequest {  })
        Assertions.assertEquals(PingResponse.Status.STATUS_SUCCESS, response.status)
    }

    @Test
    fun `test ObtainValue in Process`() = runTest {
        val user = UserData(id = UUID.randomUUID().toString(),
            name = "testUser",
            lastName = "testUser2",
            phoneNumber = "+15125434534",
            addressLine1 = "606 Milky Way",
            city = "Ann Arbor",
            state =  "MI",
            country = "US",
            dateOfBirth = LocalDate.parse("1980-11-20"),
            micaIdKey = "EKm6m4rf4n0cIoTixQqKDIqdtcHoJ7w"
        )
        val account = UserAccount(
            id = UUID.randomUUID().toString(),
            userId = user.id,
            shortName = "checking",
            balance = 10000, // USD $100
            micaIdKey = "EKm6m4rf4PVqV55tsTwSgTEXcrER1tA"
        )
        inMemoryRepository.createUser(user)
        inMemoryRepository.createAccount(account)

        //this is what Mica would send over
        val request = obtainValueRequest {
            approvalType = ApprovalTypeEnumProto.ApprovalType.APPROVAL_TYPE_PARTIAL
            value = valueRequest {
                transactionKey = "EKm6m4rf4PVqV55tsTwSgTSGtrER1tA" //opaque string that is the transaction id inside Mica
                serviceProviderInstrumentKey = account.micaIdKey //notice this will match the account mica key
                serviceProviderInstrumentRef = account.id //and this will match the account id
                currency = CurrencyEnumProto.Currency.CURRENCY_USD
                //all of these are data fields for the merchant that read the UUEK
                organizationKey = "EKm3am2y0n0cIoTixQqKDIqdtcHoJ7w"
                organizationName = "test organization"
                category = OrganizationCategoryEnumProto.OrganizationCategory.ORGANIZATION_CATEGORY_ADULT_SERVICES //this is the category for liquor and others
                storeAddress = Address.newBuilder().apply {
                    addStreetLines("750 Broadway")
                    locality = "New York"
                    region = RegionEnumProto.Region.REGION_US_NY
                    postalCode = "10003"
                    country = CountryEnumProto.Country.COUNTRY_US
                }.build()
                orderNumber = "455543433"
                storeKey = "EKm3am2y0n0cIoTixQqKDIqdtcHoJ7w"
                clerkIdentifier = "12345343223"
                //amounts
                totalAmount = "65.00" //total that was entered in the cart
                discountAmount = "0.0" //amount discounted, could be empty string
                ineligibleAmount = "0.0" //amount that was removed due to adjudication could be empty string
                requestedAmount = "65.00" //this is the actual total that needs to be authorized
            }
        }
        val response = gRPCService.obtainValue(request)
        Assertions.assertEquals(ObtainValueResponse.Status.STATUS_FULL_APPROVAL, response.status)
        Assertions.assertEquals("65.000", response.approvedAmount)
    }
}
package com.marketpos.data.network

import com.marketpos.BuildConfig
import com.marketpos.domain.model.AppUpdateInfo
import com.marketpos.domain.model.MobilePosSaleSyncPayload
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class BarkodSpaceApiClient @Inject constructor(
    private val client: OkHttpClient
) {
    suspend fun fetchLatestAppUpdate(): AppUpdateInfo = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "app-updates/android/latest.json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(parseErrorMessage(body, response.code))
            }
            val payload = JSONObject(body)
            AppUpdateInfo(
                versionCode = payload.getInt("versionCode"),
                versionName = payload.getString("versionName"),
                apkUrl = payload.getString("apkUrl"),
                notes = payload.optString("notes").takeIf { it.isNotBlank() },
                forceUpdate = payload.optBoolean("forceUpdate", false)
            )
        }
    }

    suspend fun fetchGlobalCatalogSuggestion(barcode: String): GlobalCatalogSuggestionResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/catalog/products/$barcode/suggestion")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val payload = JSONObject(body)
            GlobalCatalogSuggestionResponse(
                barcode = payload.getString("barcode"),
                name = payload.getString("name"),
                groupName = payload.optString("groupName").takeIf { it.isNotBlank() },
                updatedAt = payload.optLong("updatedAt").takeIf { !payload.isNull("updatedAt") }
            )
        }
    }

    suspend fun activateDevice(
        companyName: String? = null,
        companyCode: String? = null,
        forceNewCompany: Boolean = false,
        deviceUid: String,
        deviceName: String,
        appVersion: String,
        accountAccessToken: String? = null
    ): DeviceActivationResponse = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)
            .put("appVersion", appVersion)
            .put("forceNewCompany", forceNewCompany)
        companyName?.takeIf { it.isNotBlank() }?.let { json.put("companyName", it) }
        companyCode?.takeIf { it.isNotBlank() }?.let { json.put("companyCode", it) }

        val requestBuilder = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/device/activate")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
        accountAccessToken?.takeIf { it.isNotBlank() }?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }
        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val payload = JSONObject(body)
            DeviceActivationResponse(
                companyId = payload.getLong("companyId"),
                companyName = payload.getString("companyName"),
                companyCode = payload.getString("companyCode"),
                deviceId = payload.getLong("deviceId"),
                activationToken = payload.getString("activationToken"),
                activatedAt = payload.getLong("activatedAt")
            )
        }
    }

    suspend fun syncCatalogBatch(
        activationToken: String,
        deviceUid: String,
        batchUuid: String,
        eventsJsonArray: JSONArray
    ): CatalogSyncResponse = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("batchUuid", batchUuid)
            .put("deviceUid", deviceUid)
            .put("events", eventsJsonArray)

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/sync/catalog-batch")
            .header("Authorization", "Bearer $activationToken")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(parseErrorMessage(body, response.code))
            }
            val payload = JSONObject(body)
            val resultsArray = payload.optJSONArray("results") ?: JSONArray()
            val results = buildList {
                for (index in 0 until resultsArray.length()) {
                    val item = resultsArray.getJSONObject(index)
                    add(
                        SyncEventResult(
                            eventUuid = item.optString("eventUuid"),
                            status = item.optString("status"),
                            message = item.optString("message").takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
            CatalogSyncResponse(
                accepted = payload.optInt("accepted"),
                rejected = payload.optInt("rejected"),
                results = results,
                serverTime = payload.optLong("serverTime")
            )
        }
    }

    suspend fun registerMobileUser(
        name: String,
        email: String,
        password: String,
        deviceUid: String,
        deviceName: String
    ): AuthSessionResponse = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("name", name)
            .put("email", email)
            .put("password", password)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/register")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            parseAuthSessionResponse(body)
        }
    }

    suspend fun loginMobileUser(
        email: String,
        password: String,
        deviceUid: String,
        deviceName: String
    ): AuthSessionResponse = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/login")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            parseAuthSessionResponse(body)
        }
    }

    suspend fun logoutMobileUser(accessToken: String): Unit = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/logout")
            .header("Authorization", "Bearer $accessToken")
            .post(ByteArray(0).toRequestBody(null))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
        }
    }

    suspend fun updateMobileUserPassword(
        accessToken: String,
        currentPassword: String,
        newPassword: String
    ): Unit = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("currentPassword", currentPassword)
            .put("newPassword", newPassword)

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/password")
            .header("Authorization", "Bearer $accessToken")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
        }
    }

    suspend fun requestMobileUserPasswordReset(email: String): PasswordResetRequestResponse = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("email", email)

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/password/forgot")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val payload = JSONObject(body)
            PasswordResetRequestResponse(
                message = payload.optString("message").ifBlank { "Şifre sıfırlama kodu gönderildi" },
                expiresAt = payload.optLong("expiresAt").takeIf { !payload.isNull("expiresAt") }
            )
        }
    }

    suspend fun resetMobileUserPassword(
        email: String,
        code: String,
        newPassword: String
    ): Unit = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("email", email)
            .put("code", code)
            .put("newPassword", newPassword)

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/password/reset")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
        }
    }

    suspend fun fetchMobileUserPremium(accessToken: String): AccountPremiumResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/premium")
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val premium = JSONObject(body).getJSONObject("premium")
            AccountPremiumResponse(
                tier = premium.getString("tier"),
                source = premium.getString("source"),
                activatedAt = premium.optLong("activatedAt").takeIf { !premium.isNull("activatedAt") },
                expiresAt = premium.optLong("expiresAt").takeIf { !premium.isNull("expiresAt") },
                licenseCodeMasked = premium.optString("licenseCodeMasked").takeIf { it.isNotBlank() }
            )
        }
    }

    suspend fun syncMobileUserPremium(
        accessToken: String,
        tier: String,
        source: String,
        activatedAt: Long?,
        expiresAt: Long?,
        licenseCodeMasked: String?
    ): Unit = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("tier", tier)
            .put("source", source)
        activatedAt?.let { json.put("activatedAt", it) }
        expiresAt?.let { json.put("expiresAt", it) }
        licenseCodeMasked?.let { json.put("licenseCodeMasked", it) }

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/premium/sync")
            .header("Authorization", "Bearer $accessToken")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
        }
    }

    suspend fun syncMobileUserConsent(
        accessToken: String,
        version: String,
        acceptedAt: Long
    ): Unit = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("version", version)
            .put("acceptedAt", acceptedAt)

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/consent")
            .header("Authorization", "Bearer $accessToken")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
        }
    }

    suspend fun fetchSupportInbox(
        accessToken: String,
        status: String? = null
    ): List<SupportTicketSummaryResponse> = withContext(Dispatchers.IO) {
        val query = status?.takeIf { it.isNotBlank() }?.let { "?status=${urlEncode(it)}" }.orEmpty()
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/support/inbox$query")
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val payload = JSONObject(body)
            if (!payload.optBoolean("ok", false)) {
                throw IllegalStateException(payload.optString("message").ifBlank { "Ticket kutusu okunamadi." })
            }

            val tickets = payload.optJSONArray("tickets") ?: JSONArray()
            buildList {
                for (index in 0 until tickets.length()) {
                    val item = tickets.optJSONObject(index) ?: continue
                    add(
                        SupportTicketSummaryResponse(
                            ticketId = item.optLong("ticketId"),
                            type = item.optString("type"),
                            source = item.optString("source"),
                            status = item.optString("status"),
                            title = item.optString("title"),
                            lastMessage = item.optString("lastMessage").takeIf { it.isNotBlank() },
                            lastMessageAt = item.optLong("lastMessageAt").takeIf { value -> value > 0L },
                            createdAt = item.optLong("createdAt").takeIf { value -> value > 0L },
                            updatedAt = item.optLong("updatedAt").takeIf { value -> value > 0L }
                        )
                    )
                }
            }
        }
    }

    suspend fun createSupportTicket(
        accessToken: String,
        type: String,
        title: String,
        description: String,
        source: String = "mobile",
        companyCode: String? = null,
        deviceUid: String? = null,
        appVersion: String? = null
    ): SupportTicketDetailResponse = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("type", type)
            .put("source", source)
            .put("title", title)
            .put("description", description)

        companyCode?.takeIf { it.isNotBlank() }?.let { payload.put("companyCode", it) }
        deviceUid?.takeIf { it.isNotBlank() }?.let { payload.put("deviceUid", it) }
        appVersion?.takeIf { it.isNotBlank() }?.let { payload.put("appVersion", it) }

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/support/tickets")
            .header("Authorization", "Bearer $accessToken")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val createdTicketId = client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val json = JSONObject(body)
            if (!json.optBoolean("ok", false)) {
                throw IllegalStateException(json.optString("message").ifBlank { "Ticket olusturulamadi." })
            }

            json.optJSONObject("data")?.optLong("ticketId")?.takeIf { it > 0L }
        }

        if (createdTicketId == null) {
            throw IllegalStateException("Ticket olusturuldu ancak detay alinamadi.")
        }

        fetchSupportTicket(accessToken = accessToken, ticketId = createdTicketId)
    }

    suspend fun fetchSupportTicket(
        accessToken: String,
        ticketId: Long
    ): SupportTicketDetailResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/support/tickets/$ticketId")
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val payload = JSONObject(body)
            if (!payload.optBoolean("ok", false)) {
                throw IllegalStateException(payload.optString("message").ifBlank { "Ticket detayi alinamadi." })
            }
            val data = payload.optJSONObject("data")
                ?: throw IllegalStateException("Ticket detay verisi bulunamadi.")
            parseSupportTicketDetail(data)
        }
    }

    suspend fun replySupportTicket(
        accessToken: String,
        ticketId: Long,
        message: String
    ): SupportTicketDetailResponse = withContext(Dispatchers.IO) {
        val payload = JSONObject().put("message", message)
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/support/tickets/$ticketId/reply")
            .header("Authorization", "Bearer $accessToken")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
        }

        fetchSupportTicket(accessToken = accessToken, ticketId = ticketId)
    }

    suspend fun reopenSupportTicket(
        accessToken: String,
        ticketId: Long
    ): SupportTicketDetailResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/support/tickets/$ticketId/reopen")
            .header("Authorization", "Bearer $accessToken")
            .post(ByteArray(0).toRequestBody(null))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
        }

        fetchSupportTicket(accessToken = accessToken, ticketId = ticketId)
    }

    suspend fun fetchOwnedCompanies(accessToken: String, deviceUid: String): List<RecoverableCompanyResponse> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/companies?deviceUid=$deviceUid")
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val payload = JSONObject(body)
            val companies = payload.optJSONArray("companies") ?: JSONArray()
            buildList {
                for (index in 0 until companies.length()) {
                    val item = companies.getJSONObject(index)
                    add(
                        RecoverableCompanyResponse(
                            companyId = item.getLong("companyId"),
                            companyName = item.getString("companyName"),
                            companyCode = item.getString("companyCode"),
                            createdVia = item.optString("createdVia").takeIf { it.isNotBlank() },
                            productCount = item.optInt("productCount"),
                            lastSyncedAt = item.optLong("lastSyncedAt").takeIf { !item.isNull("lastSyncedAt") }
                        )
                    )
                }
            }
        }
    }

    suspend fun fetchDeviceHistoryCompanies(deviceUid: String): List<RecoverableCompanyResponse> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/device/history?deviceUid=$deviceUid")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val payload = JSONObject(body)
            val companies = payload.optJSONArray("companies") ?: JSONArray()
            buildList {
                for (index in 0 until companies.length()) {
                    val item = companies.getJSONObject(index)
                    add(
                        RecoverableCompanyResponse(
                            companyId = item.getLong("companyId"),
                            companyName = item.getString("companyName"),
                            companyCode = item.getString("companyCode"),
                            createdVia = item.optString("createdVia").takeIf { it.isNotBlank() },
                            productCount = item.optInt("productCount"),
                            lastSyncedAt = item.optLong("lastSyncedAt").takeIf { !item.isNull("lastSyncedAt") }
                        )
                    )
                }
            }
        }
    }

    suspend fun fetchCompanyCatalog(
        accessToken: String,
        companyCode: String
    ): List<CloudCatalogProductResponse> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/auth/companies/$companyCode/catalog")
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val payload = JSONObject(body)
            val products = payload.optJSONArray("products") ?: JSONArray()
            buildList {
                for (index in 0 until products.length()) {
                    val item = products.getJSONObject(index)
                    add(
                        CloudCatalogProductResponse(
                            barcode = item.getString("barcode"),
                            name = item.getString("name"),
                            groupName = item.optString("groupName").takeIf { it.isNotBlank() },
                            salePriceKurus = item.getLong("salePriceKurus"),
                            costPriceKurus = item.getLong("costPriceKurus"),
                            note = item.optString("note").takeIf { it.isNotBlank() },
                            updatedAt = item.getLong("updatedAt")
                        )
                    )
                }
            }
        }
    }

    suspend fun fetchCompanyCatalogChanges(
        accessToken: String,
        companyCode: String,
        sinceUpdatedAt: Long,
        limit: Int = 200
    ): CloudCatalogChangePageResponse = withContext(Dispatchers.IO) {
        val encodedCompanyCode = urlEncode(companyCode)
        val request = Request.Builder()
            .url(
                BuildConfig.WEB_API_BASE_URL +
                    "api/v1/auth/companies/$encodedCompanyCode/catalog/changes" +
                    "?sinceUpdatedAt=$sinceUpdatedAt&limit=$limit"
            )
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }

            val payload = JSONObject(body)
            val changesArray = payload.optJSONArray("changes") ?: JSONArray()
            val changes = buildList {
                for (index in 0 until changesArray.length()) {
                    val item = changesArray.optJSONObject(index) ?: continue
                    add(
                        CloudCatalogChangeResponse(
                            barcode = item.optString("barcode"),
                            name = item.optString("name"),
                            groupName = item.optString("groupName").takeIf { it.isNotBlank() },
                            salePriceKurus = item.optLong("salePriceKurus"),
                            costPriceKurus = item.optLong("costPriceKurus"),
                            note = item.optString("note").takeIf { it.isNotBlank() },
                            isActive = item.optBoolean("isActive", true),
                            updatedAt = item.optLong("updatedAt")
                        )
                    )
                }
            }

            CloudCatalogChangePageResponse(
                nextCursor = payload.optLong("nextCursor", sinceUpdatedAt),
                hasMore = payload.optBoolean("hasMore", false),
                changes = changes
            )
        }
    }

    suspend fun fetchActiveWebSaleSession(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String
    ): ActiveWebSaleSessionResponse = withContext(Dispatchers.IO) {
        val encodedCompanyCode = urlEncode(companyCode)
        val encodedDeviceUid = urlEncode(deviceUid)
        val encodedDeviceName = urlEncode(deviceName)

        val request = Request.Builder()
            .url(
                BuildConfig.WEB_API_BASE_URL +
                    "api/v1/mobile/web-sale/active" +
                    "?companyCode=$encodedCompanyCode" +
                    "&deviceUid=$encodedDeviceUid" +
                    "&deviceName=$encodedDeviceName"
            )
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            parseWebSaleSessionResponse(body)
        }
    }

    suspend fun scanWebSale(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): ActiveWebSaleSessionResponse = mutateWebSale(
        accessToken = accessToken,
        path = "api/v1/mobile/web-sale/scan",
        payload = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)
            .put("barcode", barcode),
    )

    suspend fun incrementWebSaleItem(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): ActiveWebSaleSessionResponse = mutateWebSale(
        accessToken = accessToken,
        path = "api/v1/mobile/web-sale/item/increment",
        payload = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)
            .put("barcode", barcode),
    )

    suspend fun decrementWebSaleItem(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): ActiveWebSaleSessionResponse = mutateWebSale(
        accessToken = accessToken,
        path = "api/v1/mobile/web-sale/item/decrement",
        payload = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)
            .put("barcode", barcode),
    )

    suspend fun removeWebSaleItem(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): ActiveWebSaleSessionResponse = mutateWebSale(
        accessToken = accessToken,
        path = "api/v1/mobile/web-sale/item/remove",
        payload = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)
            .put("barcode", barcode),
    )

    suspend fun setWebSaleCustomPrice(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String,
        salePriceKurus: Long
    ): ActiveWebSaleSessionResponse = mutateWebSale(
        accessToken = accessToken,
        path = "api/v1/mobile/web-sale/item/custom-price",
        payload = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)
            .put("barcode", barcode)
            .put("salePriceKurus", salePriceKurus),
    )

    suspend fun applyWebSalePercentDiscount(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String,
        percent: Double
    ): ActiveWebSaleSessionResponse = mutateWebSale(
        accessToken = accessToken,
        path = "api/v1/mobile/web-sale/item/percent-discount",
        payload = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)
            .put("barcode", barcode)
            .put("percent", percent),
    )

    suspend fun applyWebSaleFixedDiscount(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String,
        discountKurus: Long
    ): ActiveWebSaleSessionResponse = mutateWebSale(
        accessToken = accessToken,
        path = "api/v1/mobile/web-sale/item/fixed-discount",
        payload = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)
            .put("barcode", barcode)
            .put("discountKurus", discountKurus),
    )

    suspend fun resetWebSaleItemPrice(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        barcode: String
    ): ActiveWebSaleSessionResponse = mutateWebSale(
        accessToken = accessToken,
        path = "api/v1/mobile/web-sale/item/reset-price",
        payload = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)
            .put("barcode", barcode),
    )

    suspend fun completeWebSale(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String
    ): ActiveWebSaleSessionResponse = mutateWebSale(
        accessToken = accessToken,
        path = "api/v1/mobile/web-sale/complete",
        payload = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName),
    )

    suspend fun triggerWebSalePrint(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String
    ): WebSalePrintResponse = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/mobile/web-sale/print")
            .header("Authorization", "Bearer $accessToken")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val json = JSONObject(body)
            val data = json.optJSONObject("data") ?: JSONObject()

            WebSalePrintResponse(
                printReady = data.optBoolean("printReady", false),
                message = json.optString("message").ifBlank { "Yazdirma baglantisi hazirlandi." },
                printUrl = data.optString("printUrl").takeIf { it.isNotBlank() },
                previewUrl = data.optString("previewUrl").takeIf { it.isNotBlank() },
                pdfUrl = data.optString("pdfUrl").takeIf { it.isNotBlank() },
                saleId = data.optLong("saleId").takeIf { value -> value > 0L }
            )
        }
    }

    suspend fun publishMobilePosSale(
        accessToken: String,
        companyCode: String,
        deviceUid: String,
        deviceName: String,
        payload: MobilePosSaleSyncPayload
    ): Unit = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("companyCode", companyCode)
            .put("deviceUid", deviceUid)
            .put("deviceName", deviceName)
            .put("localSaleId", payload.localSaleId)
            .put("createdAt", payload.createdAt)
            .put("totalItems", payload.totalItems)
            .put("totalAmountKurus", payload.totalAmountKurus)
            .put("totalCostKurus", payload.totalCostKurus)
            .put("profitKurus", payload.profitKurus)
            .put("paymentMethod", payload.paymentMethod)

        val itemsArray = JSONArray()
        payload.items.forEach { item ->
            itemsArray.put(
                JSONObject()
                    .put("barcode", item.barcode)
                    .put("productName", item.productName)
                    .put("quantity", item.quantity)
                    .put("unitSalePriceKurus", item.unitSalePriceKurus)
                    .put("unitCostPriceKurus", item.unitCostPriceKurus)
                    .put("lineTotalKurus", item.lineTotalKurus)
                    .put("lineProfitKurus", item.lineProfitKurus)
            )
        }
        json.put("items", itemsArray)

        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + "api/v1/mobile/sales/publish")
            .header("Authorization", "Bearer $accessToken")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            val result = JSONObject(body)
            if (!result.optBoolean("ok", false)) {
                throw IllegalStateException(result.optString("message").ifBlank { "Satis buluta aktarilamadi." })
            }
        }
    }

    private suspend fun mutateWebSale(
        accessToken: String,
        path: String,
        payload: JSONObject
    ): ActiveWebSaleSessionResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.WEB_API_BASE_URL + path)
            .header("Authorization", "Bearer $accessToken")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw parseApiException(body, response.code)
            }
            parseWebSaleSessionResponse(body)
        }
    }

    private fun parseWebSaleSessionResponse(body: String): ActiveWebSaleSessionResponse {
        val payload = JSONObject(body)
        if (!payload.optBoolean("ok", false)) {
            throw IllegalStateException(payload.optString("message").ifBlank { "Web satis istegi basarisiz" })
        }

        val data = payload.optJSONObject("data") ?: JSONObject()
        val summary = data.optJSONObject("summary") ?: JSONObject()
        val sale = data.optJSONObject("sale")
        val lastSaleNode = data.optJSONObject("lastSale") ?: sale
        val cartItemsArray = data.optJSONArray("cartItems") ?: JSONArray()
        val recentSalesArray = data.optJSONArray("recentSales") ?: JSONArray()

        val cartItems = buildList {
            for (index in 0 until cartItemsArray.length()) {
                val item = cartItemsArray.optJSONObject(index) ?: continue
                add(
                    CompanionCartItemResponse(
                        barcode = item.optString("barcode"),
                        productName = item.optString("productName"),
                        quantity = item.optInt("quantity"),
                        baseSalePriceKurus = item.optLong("baseSalePriceKurus"),
                        salePriceKurus = item.optLong("salePriceKurus"),
                        lineTotalKurus = item.optLong("lineTotalKurus"),
                        hasCustomPrice = item.optBoolean("hasCustomPrice", false),
                    )
                )
            }
        }

        val recentSales = buildList {
            for (index in 0 until recentSalesArray.length()) {
                val item = recentSalesArray.optJSONObject(index) ?: continue
                val saleId = item.optLong("saleId").takeIf { value -> value > 0L } ?: continue
                add(
                    CompanionRecentSaleResponse(
                        saleId = saleId,
                        totalAmountKurus = item.optLong("totalAmountKurus"),
                        totalItems = item.optInt("totalItems"),
                        completedAtEpochMs = item.optLong("completedAtEpochMs").takeIf { value -> value > 0L },
                        completedAtLabel = item.optString("completedAtLabel").takeIf { it.isNotBlank() },
                        registerName = item.optString("registerName").takeIf { it.isNotBlank() },
                        paymentMethod = item.optString("paymentMethod").takeIf { it.isNotBlank() }
                    )
                )
            }
        }

        return ActiveWebSaleSessionResponse(
            hasActiveSession = data.optBoolean("hasActiveSession", false),
            companyCode = data.optJSONObject("company")?.optString("companyCode"),
            companyName = data.optJSONObject("company")?.optString("companyName"),
            branchName = data.optJSONObject("posSession")?.optString("branchName"),
            registerName = data.optJSONObject("posSession")?.optString("registerName"),
            posSessionId = data.optJSONObject("posSession")?.optLong("id"),
            saleSessionId = data.optJSONObject("saleSession")?.optLong("id"),
            saleSessionLabel = data.optJSONObject("saleSession")?.optString("label"),
            summary = CompanionSaleSummaryResponse(
                itemCount = summary.optInt("itemCount"),
                totalAmountKurus = summary.optLong("totalAmountKurus"),
                canCheckout = summary.optBoolean("canCheckout", false)
            ),
            cartItems = cartItems,
            lastSale = lastSaleNode?.let {
                CompanionSaleReceiptResponse(
                    saleId = it.optLong("saleId").takeIf { value -> value > 0L } ?: it.optLong("id"),
                    totalAmountKurus = it.optLong("totalAmountKurus"),
                    totalItems = it.optInt("totalItems"),
                    completedAtEpochMs = it.optLong("completedAtEpochMs").takeIf { value -> value > 0L }
                        ?: it.optLong("completedAt").takeIf { value -> value > 0L },
                )
            },
            recentSales = recentSales,
            message = payload.optString("message").takeIf { it.isNotBlank() }
                ?: data.optString("message").takeIf { it.isNotBlank() }
        )
    }

    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
    }

    private fun parseErrorMessage(body: String, code: Int): String {
        return runCatching { JSONObject(body).optString("message") }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "Sunucu hatasi ($code)"
    }

    private fun parseApiException(body: String, code: Int): IllegalStateException {
        val payload = runCatching { JSONObject(body) }.getOrNull()
        val message = payload?.optString("message").takeIf { !it.isNullOrBlank() } ?: "Sunucu hatasi ($code)"

        if (code == 409 && payload?.optString("errorCode") == "DEVICE_ALREADY_BOUND") {
            val existingCompanyName = payload.optString("existingCompanyName")
            val existingCompanyCode = payload.optString("existingCompanyCode")
            if (existingCompanyName.isNotBlank() && existingCompanyCode.isNotBlank()) {
                return ActivationConflictException(
                    existingCompanyName = existingCompanyName,
                    existingCompanyCode = existingCompanyCode,
                    message = message
                )
            }
        }

        return IllegalStateException(message)
    }

    private fun parseAuthSessionResponse(body: String): AuthSessionResponse {
        val payload = JSONObject(body)
        val user = payload.getJSONObject("user")
        return AuthSessionResponse(
            user = MobileUserResponse(
                id = user.getLong("id"),
                name = user.getString("name"),
                email = user.getString("email")
            ),
            accessToken = payload.getString("accessToken")
        )
    }

    private fun parseSupportTicketDetail(data: JSONObject): SupportTicketDetailResponse {
        val messagesArray = data.optJSONArray("messages") ?: JSONArray()
        val messages = buildList {
            for (index in 0 until messagesArray.length()) {
                val item = messagesArray.optJSONObject(index) ?: continue
                add(
                    SupportTicketMessageResponse(
                        messageId = item.optLong("messageId"),
                        authorType = item.optString("authorType"),
                        authorId = item.optLong("authorId").takeIf { value -> value > 0L },
                        message = item.optString("message"),
                        createdAt = item.optLong("createdAt").takeIf { value -> value > 0L }
                    )
                )
            }
        }

        return SupportTicketDetailResponse(
            ticketId = data.optLong("ticketId"),
            type = data.optString("type"),
            source = data.optString("source"),
            status = data.optString("status"),
            title = data.optString("title"),
            description = data.optString("description"),
            createdAt = data.optLong("createdAt").takeIf { value -> value > 0L },
            updatedAt = data.optLong("updatedAt").takeIf { value -> value > 0L },
            messages = messages
        )
    }
}

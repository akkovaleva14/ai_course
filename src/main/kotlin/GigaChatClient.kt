import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class GigaChatClient(
    private val authorizationKey: String,
    private val scope: String = "GIGACHAT_API_PERS",
    private val unsafeSsl: Boolean = false
) {
    private val httpClient: OkHttpClient = if (unsafeSsl) buildUnsafeClient() else buildSafeClient()

    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private var accessToken: String? = null
    private var accessTokenExpiresAt: Long? = null

    fun getModels(): List<String> {
        val token = getAccessToken()

        val request = Request.Builder()
            .url("https://gigachat.devices.sberbank.ru/api/v1/models")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                error("Models API error ${response.code}: ${body.take(1000)}")
            }

            val parsed: ModelsResponse = mapper.readValue(body)
            return parsed.data.map { it.id }
        }
    }

    fun ask(
        model: String,
        temperature: Double,
        messages: List<RequestMessage>
    ): String {
        val token = getAccessToken()

        val payload = GigaChatRequest(
            model = model,
            temperature = temperature,
            messages = messages
        )

        val json = mapper.writeValueAsString(payload)

        val request = Request.Builder()
            .url("https://gigachat.devices.sberbank.ru/api/v1/chat/completions")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                error("Chat API error ${response.code}: ${body.take(1000)}")
            }

            val parsed: GigaChatResponse = mapper.readValue(body)
            return parsed.choices.firstOrNull()?.message?.content
                ?: error("Empty response from GigaChat")
        }
    }

    private fun getAccessToken(): String {
        val nowSeconds = System.currentTimeMillis() / 1000

        if (
            accessToken != null &&
            accessTokenExpiresAt != null &&
            nowSeconds < accessTokenExpiresAt!! - 60
        ) {
            return accessToken!!
        }

        val formBody = FormBody.Builder()
            .add("scope", scope)
            .build()

        val request = Request.Builder()
            .url("https://ngw.devices.sberbank.ru:9443/api/v2/oauth")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Accept", "application/json")
            .addHeader("RqUID", UUID.randomUUID().toString())
            .addHeader("Authorization", "Basic $authorizationKey")
            .post(formBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                error("OAuth error ${response.code}: ${body.take(1000)}")
            }

            val parsed: OAuthTokenResponse = mapper.readValue(body)
            accessToken = parsed.access_token
            accessTokenExpiresAt = parsed.expires_at
            return parsed.access_token
        }
    }

    private fun buildSafeClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private fun buildUnsafeClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
        )

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val trustManager = trustAllCerts[0] as X509TrustManager

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
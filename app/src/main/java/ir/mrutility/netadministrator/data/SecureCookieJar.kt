package ir.mrutility.netadministrator.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Persists only the server session cookie, encrypted with an Android Keystore AES/GCM key. */
class SecureCookieJar(context: Context) : CookieJar {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("encrypted_sync_session", Context.MODE_PRIVATE)
    private val alias = "netadmin.sync.cookie.aes"

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val session = cookies.firstOrNull { it.name == "nl_session" && it.matches(url) } ?: return
        val json = JSONObject()
            .put("name", session.name)
            .put("value", session.value)
            .put("domain", session.domain)
            .put("path", session.path)
            .put("expiresAt", session.expiresAt)
            .put("secure", session.secure)
            .put("httpOnly", session.httpOnly)
            .put("hostOnly", session.hostOnly)
        prefs.edit().putString("cookie", encrypt(json.toString())).apply()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val raw = prefs.getString("cookie", null) ?: return emptyList()
        return try {
            val j = JSONObject(decrypt(raw))
            val builder = Cookie.Builder()
                .name(j.getString("name"))
                .value(j.getString("value"))
                .path(j.optString("path", "/"))
            if (j.optBoolean("hostOnly", false)) builder.hostOnlyDomain(j.getString("domain"))
            else builder.domain(j.getString("domain"))
            if (j.optBoolean("secure", false)) builder.secure()
            if (j.optBoolean("httpOnly", true)) builder.httpOnly()
            val expires = j.optLong("expiresAt", Long.MAX_VALUE)
            if (expires != Long.MAX_VALUE) builder.expiresAt(expires)
            val cookie = builder.build()
            if (cookie.expiresAt > System.currentTimeMillis() && cookie.matches(url)) listOf(cookie)
            else { clear(); emptyList() }
        } catch (_: Exception) {
            clear(); emptyList()
        }
    }

    @Synchronized
    fun clear() { prefs.edit().remove("cookie").apply() }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getKey(alias, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val combined = cipher.iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)
        require(combined.size > 12) { "Encrypted cookie payload is invalid" }
        val iv = combined.copyOfRange(0, 12)
        val cipherText = combined.copyOfRange(12, combined.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }
}

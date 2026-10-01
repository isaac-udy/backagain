package feature.live.server.data

import feature.live.server.data.storage.PresenterSessionStorage
import feature.live.server.domain.IsPresenter
import feature.live.server.domain.SignInPresenter
import feature.live.server.domain.SignOutPresenter
import platform.server.postgres.tables.PresenterSessionRow
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Presenter sessions are random tokens. Only their SHA-256 is stored, so a leaked database does not
 * hand out a working token, and a redeploy does not sign the presenter out.
 */
internal class PresenterSessionRepository(
    private val storage: PresenterSessionStorage,
    private val config: PresenterConfig,
    private val clock: Clock,
) {
    private val sessionLength = 14.days
    private val validationCacheLength = 60.seconds
    private val random = SecureRandom()
    private val validUntilByTokenHash = ConcurrentHashMap<String, Instant>()

    val signInPresenter = SignInPresenter { password ->
        if (!MessageDigest.isEqual(password.toByteArray(), config.password.toByteArray())) {
            return@SignInPresenter null
        }
        val token = ByteArray(32)
            .also(random::nextBytes)
            .let { Base64.getUrlEncoder().withoutPadding().encodeToString(it) }
        val now = clock.now()
        storage.insert(
            PresenterSessionRow(
                tokenHash = hash(token),
                createdAt = now,
                expiresAt = now + sessionLength,
            ),
        )
        token
    }

    val isPresenter = IsPresenter { token ->
        if (token == null) return@IsPresenter false
        val tokenHash = hash(token)
        val now = clock.now()
        val cachedUntil = validUntilByTokenHash[tokenHash]
        if (cachedUntil != null && cachedUntil > now) return@IsPresenter true
        val session = storage.get(tokenHash)
        val isValid = session != null && session.expiresAt > now
        if (isValid) validUntilByTokenHash[tokenHash] = now + validationCacheLength
        isValid
    }

    val signOutPresenter = SignOutPresenter { token ->
        val tokenHash = hash(token)
        validUntilByTokenHash.remove(tokenHash)
        storage.delete(tokenHash)
    }

    private fun hash(token: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
}

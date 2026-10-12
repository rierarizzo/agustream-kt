package com.misgus.agustream.data.backend

import com.misgus.agustream.core.repository.SignInMethod
import com.misgus.agustream.core.repository.SignInRequest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NuvioAccountRepositoryTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun loadsProfilesWhenTheSignInCallerIsCancelled() =
        runTest {
            val engine =
                MockEngine { request ->
                    val path = request.url.encodedPath
                    when {
                        path.endsWith("/.well-known/nuvio") ->
                            respond(
                                """{"version":1,"service":"nuvio","backend_url":"https://x","publishable_key":"anon"}""",
                                HttpStatusCode.OK,
                                jsonHeaders,
                            )
                        path.contains("/auth/v1/token") ->
                            respond(
                                """{"access_token":"a","refresh_token":"r","user":{"id":"u1","email":"me@example.com"},"expires_at":9999999999}""",
                                HttpStatusCode.OK,
                                jsonHeaders,
                            )
                        path.contains("/rest/v1/profiles") ->
                            respond("""[{"profile_id":1,"name":"Main"}]""", HttpStatusCode.OK, jsonHeaders)
                        else -> respond("{}", HttpStatusCode.NotFound, jsonHeaders)
                    }
                }
            val stack = nuvioTestStack(engine)
            val repository = NuvioAccountRepository(stack.auth, stack.rest, scope = backgroundScope)

            // Mirrors the Settings card: the sign-in runs on a scope that the
            // gate disposes as soon as `isSignedIn` flips.
            launch {
                repository.signIn(SignInRequest.EmailPassword("me@example.com", "secret"))
                throw CancellationException("screen disposed")
            }.join()

            val state =
                withTimeout(5_000) {
                    repository.state.first { !it.isLoadingProfiles }
                }

            assertTrue(state.isSignedIn)
            assertFalse(state.isLoadingProfiles)
            assertEquals(listOf("Main"), state.profiles.map { it.name })
            assertEquals(listOf<SignInMethod>(SignInMethod.EmailPassword), repository.signInMethods)
        }
}

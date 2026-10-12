package com.misgus.agustream.data.backend

import com.misgus.agustream.core.model.BackendProfile
import com.misgus.agustream.core.repository.AccountRepository
import com.misgus.agustream.core.repository.AccountState
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NuvioAddonRepositoryTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun cachesAddonsAndRefetchesWhenTheProfileChanges() =
        runTest {
            var addonCalls = 0
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
                        path.contains("/rest/v1/addons") -> {
                            addonCalls++
                            respond(
                                """[{"id":"a1","url":"https://addon/manifest.json","name":"Addon","enabled":true,"sort_order":0,"profile_id":1}]""",
                                HttpStatusCode.OK,
                                jsonHeaders,
                            )
                        }
                        else -> respond("{}", HttpStatusCode.NotFound, jsonHeaders)
                    }
                }
            val client =
                NuvioClient(
                    baseUrl = "https://x",
                    http = HttpClient(engine) { expectSuccess = false },
                )
            val account = FakeAccount(profileKey = "p1", profileId = 1)
            val repository = NuvioAddonRepository(client, account)
            client.signIn("me@example.com", "secret")

            val first = repository.all()
            val second = repository.all()

            assertEquals(1, addonCalls)
            assertEquals(1, first.size)
            assertEquals(first, second)

            // A different profile is a different addon list, so the cache misses.
            account.setProfile(profileKey = "p2", profileId = 2)
            repository.all()

            assertEquals(2, addonCalls)
        }

    private class FakeAccount(
        profileKey: String,
        profileId: Int,
    ) : AccountRepository {
        private val _state =
            MutableStateFlow(
                AccountState(
                    isSignedIn = true,
                    requiresProfile = true,
                    activeProfile = BackendProfile(id = profileKey, name = "Profile", profileId = profileId),
                ),
            )
        override val state: StateFlow<AccountState> = _state.asStateFlow()

        override suspend fun signIn(
            email: String,
            password: String,
        ) = Unit

        override suspend fun restoreSession(): Boolean = true

        override suspend fun signOut() = Unit

        override suspend fun loadProfiles() = Unit

        override suspend fun selectProfile(profile: BackendProfile) = Unit

        fun setProfile(
            profileKey: String,
            profileId: Int,
        ) {
            _state.value =
                _state.value.copy(
                    activeProfile = BackendProfile(id = profileKey, name = "Profile", profileId = profileId),
                )
        }
    }
}

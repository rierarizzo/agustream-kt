package com.misgus.pengu.data.backend

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NuvioBackendTest {
    @Test
    fun renewsSessionAfter401AndRetriesOnce() =
        runTest {
            var refreshCalls = 0
            var dataCalls = 0
            val engine =
                MockEngine { request ->
                    val path = request.url.encodedPath
                    val authorization = request.headers[HttpHeaders.Authorization]
                    when {
                        path.endsWith("/.well-known/nuvio") ->
                            respond(NUVIO_DISCOVERY_JSON, HttpStatusCode.OK, nuvioJsonHeaders)
                        path.endsWith("/auth/v1/token") &&
                            request.url.parameters["grant_type"] == "refresh_token" -> {
                            refreshCalls++
                            respond(nuvioRefreshBody(), HttpStatusCode.OK, nuvioJsonHeaders)
                        }
                        path.contains("/rest/v1/library_items") -> {
                            dataCalls++
                            if (authorization == "Bearer old") {
                                respond("", HttpStatusCode.Unauthorized)
                            } else {
                                respond("[]", HttpStatusCode.OK, nuvioJsonHeaders)
                            }
                        }
                        else -> respond("{}", HttpStatusCode.NotFound, nuvioJsonHeaders)
                    }
                }
            val stack =
                nuvioTestStack(
                    engine,
                    store = MemorySessionStore(nuvioSessionJson("old", NUVIO_FAR_FUTURE)),
                )
            assertTrue(stack.auth.restoreSession())

            val rows = stack.rest.select("library_items")

            assertEquals(0, rows.size)
            assertEquals(1, refreshCalls)
            assertEquals(2, dataCalls)
            assertEquals("new", stack.auth.session?.accessToken)
        }

    @Test
    fun renewsAnExpiredSessionWhenRestoring() =
        runTest {
            var refreshCalls = 0
            var dataCalls = 0
            val engine =
                MockEngine { request ->
                    val path = request.url.encodedPath
                    when {
                        path.endsWith("/.well-known/nuvio") ->
                            respond(NUVIO_DISCOVERY_JSON, HttpStatusCode.OK, nuvioJsonHeaders)
                        path.endsWith("/auth/v1/token") -> {
                            refreshCalls++
                            respond(nuvioRefreshBody(), HttpStatusCode.OK, nuvioJsonHeaders)
                        }
                        path.contains("/rest/v1/library_items") -> {
                            dataCalls++
                            respond("[]", HttpStatusCode.OK, nuvioJsonHeaders)
                        }
                        else -> respond("{}", HttpStatusCode.NotFound, nuvioJsonHeaders)
                    }
                }
            val stack =
                nuvioTestStack(
                    engine,
                    store = MemorySessionStore(nuvioSessionJson("old", expiresAt = 0)),
                )

            assertTrue(stack.auth.restoreSession())
            val rows = stack.rest.select("library_items")

            assertEquals(0, rows.size)
            assertEquals(1, refreshCalls)
            assertEquals(1, dataCalls)
            assertEquals("new", stack.auth.session?.accessToken)
        }
}

package org.littlegit.client.interceptor

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Before
import org.junit.Test
import org.littlegit.client.engine.api.ApiResponse
import org.littlegit.client.engine.controller.AuthInterceptor
import org.littlegit.client.engine.controller.AuthTokensProvider
import org.littlegit.client.engine.model.AuthTokens
import org.littlegit.client.engine.model.RefreshResponse
import org.littlegit.client.testUtils.upon
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthInterceptorTests {

    private lateinit var mockChain: Interceptor.Chain
    private lateinit var request: Request

    @Before
    fun setup() {
        mockChain = mock(Interceptor.Chain::class.java)

        request = Request.Builder().url("http://www.example.com").build()
        upon(mockChain.request()).thenReturn(request)
    }

    @Test
    fun testNoRefreshNeeded_IsSuccessful() {
        val tokens = AuthTokens("accessToken", "refreshToken")
        val tokensProvider = mock(AuthTokensProvider::class.java)

        val response = buildResponse(200)
        val tokensProviderSpy = spy(tokensProvider)
        val captor = ArgumentCaptor.forClass(Request::class.java)

        upon(tokensProvider.authTokens).thenReturn(tokens)
        upon(mockChain.proceed(captor.capture())).thenReturn(response)

        val interceptor = AuthInterceptor(tokensProvider)
        interceptor.intercept(mockChain)

        assertNotNull(captor.value.header("Authorization"))
        assertEquals(captor.value.header("Authorization"), "Bearer ${tokens.accessToken}")
        verify(tokensProviderSpy, times(0)).refreshToken()
    }

    @Test
    fun testBlankToken_NoRefreshAttemptedAndSuccess() {
        val tokens = AuthTokens("", "refreshToken")
        val tokensProvider = mock(AuthTokensProvider::class.java)

        val response = buildResponse(200)
        val tokensProviderSpy = spy(tokensProvider)
        val captor = ArgumentCaptor.forClass(Request::class.java)

        upon(tokensProvider.authTokens).thenReturn(tokens)
        upon(mockChain.proceed(captor.capture())).thenReturn(response)

        val interceptor = AuthInterceptor(tokensProvider)
        interceptor.intercept(mockChain)

        assertNull(captor.value.header("Authorization"))
        verify(tokensProviderSpy, times(0)).refreshToken()
    }

    @Test
    fun testRefreshRequired_IsSuccessful() {
        val tokens = AuthTokens("accessToken", "refreshToken")
        val refreshResponse = RefreshResponse("new-access-token", "Bearer", 10)
        val tokensProvider = mock(AuthTokensProvider::class.java)

        val firstResponse = buildResponse(401)
        val captor = ArgumentCaptor.forClass(Request::class.java)

        upon(tokensProvider.authTokens).thenReturn(tokens)
        upon(tokensProvider.refreshToken()).thenReturn(ApiResponse.success(refreshResponse))
        upon(mockChain.proceed(captor.capture())).thenReturn(firstResponse)

        val interceptor = AuthInterceptor(tokensProvider)
        interceptor.intercept(mockChain)

        // The original auth tokens should have been added to first request
        assertNotNull(captor.allValues.first().header("Authorization"))
        assertEquals(captor.allValues.first().header("Authorization"), "Bearer ${tokens.accessToken}")

        // New authtokens should have been added to the second
        assertNotNull(captor.allValues.last().header("Authorization"))
        assertEquals(captor.allValues.last().header("Authorization"), "Bearer ${refreshResponse.accessToken}")

        verify(tokensProvider, times(1)).refreshToken()
    }


    private fun buildResponse(code: Int): Response? {
        return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_0)
                .code(code)
                .message("Response")
                .build()
    }
}
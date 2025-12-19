package io.github.shigaichi.jquants.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.shigaichi.jquants.client.listedinfo.ListedInfo;
import io.github.shigaichi.jquants.client.listedinfo.ListedInfoQuery;
import io.github.shigaichi.jquants.client.listedinfo.ListedInfoResponse;
import io.github.shigaichi.jquants.client.token.IdTokenResponse;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JQuantsClientTest {
    @Test
    @DisplayName("銘柄一覧を取得し、リクエストが正しく組み立てられる")
    void fetchListedInfoWithQuery() throws Exception {
        String responseBody =
                "{\n"
                        + "  \"info\": [\n"
                        + "    {\n"
                        + "      \"Date\": \"2024-02-09\",\n"
                        + "      \"Code\": \"86970\",\n"
                        + "      \"CompanyName\": \"日本取引所グループ\",\n"
                        + "      \"CompanyNameEnglish\": \"Japan Exchange Group,Inc.\",\n"
                        + "      \"Sector17Code\": \"16\",\n"
                        + "      \"Sector17CodeName\": \"金融（除く銀行）\",\n"
                        + "      \"Sector33Code\": \"7200\",\n"
                        + "      \"Sector33CodeName\": \"その他金融業\",\n"
                        + "      \"ScaleCategory\": \"TOPIX Large70\",\n"
                        + "      \"MarketCode\": \"0111\",\n"
                        + "      \"MarketCodeName\": \"プライム\",\n"
                        + "      \"MarginCode\": \"2\",\n"
                        + "      \"MarginCodeName\": \"貸借\",\n"
                        + "      \"BasePrice\": 1234.0\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"pagination_key\": \"next-1\"\n"
                        + "}";

        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);

        HttpRequestExecutor executor = mock(HttpRequestExecutor.class);
        when(executor.send(any())).thenReturn(httpResponse);

        JQuantsClient client =
                new JQuantsClient("dummy-token", JQuantsClient.DEFAULT_BASE_URI, executor);

        ListedInfoQuery query =
                ListedInfoQuery.builder()
                        .code("86970")
                        .date("2024-02-09")
                        .paginationKey("next-0")
                        .build();
        ListedInfoResponse response = client.getListedInfo(query);

        assertEquals(1, response.getInfo().size());
        ListedInfo item = response.getInfo().get(0);
        assertEquals("2024-02-09", item.getDate());
        assertEquals("86970", item.getCode());
        assertEquals("日本取引所グループ", item.getCompanyName());
        assertEquals("Japan Exchange Group,Inc.", item.getCompanyNameEnglish());
        assertEquals("16", item.getSector17Code());
        assertEquals("金融（除く銀行）", item.getSector17CodeName());
        assertEquals("7200", item.getSector33Code());
        assertEquals("その他金融業", item.getSector33CodeName());
        assertEquals("TOPIX Large70", item.getScaleCategory());
        assertEquals("0111", item.getMarketCode());
        assertEquals("プライム", item.getMarketCodeName());
        assertEquals("2", item.getMarginCode());
        assertEquals("貸借", item.getMarginCodeName());
        assertEquals("1234.0", item.getBasePrice().toPlainString());
        assertEquals(Optional.of("next-1"), response.getPaginationKey());

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(executor).send(requestCaptor.capture());
        HttpRequest lastRequest = requestCaptor.getValue();
        assertEquals(
                JQuantsClient.DEFAULT_BASE_URL
                        + "/listed/info?code=86970&date=2024-02-09&pagination_key=next-0",
                lastRequest.uri().toString());
        assertEquals(
                "Bearer dummy-token", lastRequest.headers().firstValue("Authorization").orElse(""));
    }

    @Test
    @DisplayName("引数がnullの場合は全件取得となり、リクエストが正しく組み立てられる")
    void fetchListedInfoWithNullQuery() throws Exception {
        String responseBody = "{\"info\": [], \"pagination_key\": null}";

        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);

        HttpRequestExecutor executor = mock(HttpRequestExecutor.class);
        when(executor.send(any())).thenReturn(httpResponse);

        JQuantsClient client =
                new JQuantsClient("dummy-token", JQuantsClient.DEFAULT_BASE_URI, executor);

        // 他のテストで実施しているためレスポンスの検証はしない
        client.getListedInfo(null);

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(executor).send(requestCaptor.capture());
        HttpRequest lastRequest = requestCaptor.getValue();
        assertEquals(JQuantsClient.DEFAULT_BASE_URL + "/listed/info", lastRequest.uri().toString());
        assertEquals(
                "Bearer dummy-token", lastRequest.headers().firstValue("Authorization").orElse(""));
    }

    @Test
    @DisplayName("エラー応答の場合に例外をスローする")
    void throwExceptionOnErrorResponse() throws IOException, InterruptedException {
        String responseBody = "{\"message\":\"無効なリクエスト\"}";
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn(responseBody);

        HttpRequestExecutor executor = mock(HttpRequestExecutor.class);
        when(executor.send(any())).thenReturn(httpResponse);

        JQuantsClient client =
                new JQuantsClient("dummy-token", JQuantsClient.DEFAULT_BASE_URI, executor);

        ListedInfoQuery query = ListedInfoQuery.builder().build();
        JQuantsApiException exception =
                assertThrows(JQuantsApiException.class, () -> client.getListedInfo(query));
        assertThat(exception.getMessage()).contains("status=400");
        assertThat(exception.getMessage()).contains("無効なリクエスト");
    }

    @Test
    @DisplayName("レスポンスがJSONでなかった場合はそのままbodyを返す")
    void throwExceptionOnNonJsonResponse() throws IOException, InterruptedException {
        String responseBody = "NON_JSON_RESPONSE";
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn(responseBody);

        HttpRequestExecutor executor = mock(HttpRequestExecutor.class);
        when(executor.send(any())).thenReturn(httpResponse);

        JQuantsClient client =
                new JQuantsClient("dummy-token", JQuantsClient.DEFAULT_BASE_URI, executor);

        ListedInfoQuery query = ListedInfoQuery.builder().build();
        JQuantsApiException exception =
                assertThrows(JQuantsApiException.class, () -> client.getListedInfo(query));
        assertThat(exception.getMessage()).contains("status=400");
        assertThat(exception.getMessage()).contains("NON_JSON_RESPONSE");
    }

    @Test
    @DisplayName("/token/auth_userでトークンを取得する")
    void authenticateUser() throws Exception {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body())
                .thenReturn(
                        "{"
                                + "\"refreshToken\":\"refresh-token\","
                                + "\"idToken\":\"issued-id-token\""
                                + "}");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpRequestExecutor executor = mock(HttpRequestExecutor.class);
        when(executor.send(requestCaptor.capture())).thenReturn(httpResponse);

        IdTokenResponse response =
                JQuantsClient.authenticateUser(
                        "user@example.com", "password", JQuantsClient.DEFAULT_BASE_URI, executor);

        assertEquals("issued-id-token", response.getIdToken());
        assertEquals("refresh-token", response.getRefreshToken());

        HttpRequest request = requestCaptor.getValue();
        assertEquals(JQuantsClient.DEFAULT_BASE_URL + "/token/auth_user", request.uri().toString());
        assertEquals("application/json", request.headers().firstValue("Content-Type").orElse(""));
        assertEquals("POST", request.method());
        assertEquals(
                "{\"mailaddress\":\"user@example.com\",\"password\":\"password\"}",
                extractBody(request));
    }

    @Test
    @DisplayName("/token/auth_userがrefreshTokenのみ返す場合はauth_refreshを呼び出す")
    void authenticateUserWithRefreshTokenOnly() throws Exception {
        HttpResponse<String> authResponse = mock(HttpResponse.class);
        when(authResponse.statusCode()).thenReturn(200);
        when(authResponse.body()).thenReturn("{\"refreshToken\":\"refresh-token\"}");

        HttpResponse<String> refreshResponse = mock(HttpResponse.class);
        when(refreshResponse.statusCode()).thenReturn(200);
        when(refreshResponse.body()).thenReturn("{\"idToken\":\"renewed-id-token\"}");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpRequestExecutor executor = mock(HttpRequestExecutor.class);
        when(executor.send(requestCaptor.capture())).thenReturn(authResponse, refreshResponse);

        IdTokenResponse response =
                JQuantsClient.authenticateUser(
                        "user@example.com", "password", JQuantsClient.DEFAULT_BASE_URI, executor);

        assertEquals("renewed-id-token", response.getIdToken());
        assertEquals("refresh-token", response.getRefreshToken());

        List<HttpRequest> requests = requestCaptor.getAllValues();
        assertEquals(2, requests.size());
        assertEquals(
                JQuantsClient.DEFAULT_BASE_URL + "/token/auth_user",
                requests.get(0).uri().toString());
        assertEquals(
                JQuantsClient.DEFAULT_BASE_URL + "/token/auth_refresh?refreshtoken=refresh-token",
                requests.get(1).uri().toString());
    }

    @Test
    @DisplayName("/token/auth_userがエラーの場合に例外をスローする")
    void throwExceptionOnAuthUserError() throws IOException, InterruptedException {
        String responseBody = "{\"message\":\"メールアドレスまたはパスワードが不正です\"}";
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(401);
        when(httpResponse.body()).thenReturn(responseBody);

        HttpRequestExecutor executor = mock(HttpRequestExecutor.class);
        when(executor.send(any())).thenReturn(httpResponse);

        JQuantsApiException exception =
                assertThrows(
                        JQuantsApiException.class,
                        () ->
                                JQuantsClient.authenticateUser(
                                        "user@example.com",
                                        "password",
                                        JQuantsClient.DEFAULT_BASE_URI,
                                        executor));
        assertTrue(exception.getMessage().contains("status=401"));
        assertTrue(exception.getMessage().contains("メールアドレスまたはパスワードが不正です"));
    }

    @Test
    @DisplayName("リフレッシュトークンからIDトークンを取得する")
    void refreshIdToken() throws Exception {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("{\"idToken\":\"renewed-id-token\"}");

        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        HttpRequestExecutor executor = mock(HttpRequestExecutor.class);
        when(executor.send(requestCaptor.capture())).thenReturn(httpResponse);

        String idToken =
                JQuantsClient.refreshIdToken(
                        "refresh-token", JQuantsClient.DEFAULT_BASE_URI, executor);

        assertEquals("renewed-id-token", idToken);
        HttpRequest request = requestCaptor.getValue();

        assertEquals(
                JQuantsClient.DEFAULT_BASE_URL + "/token/auth_refresh?refreshtoken=refresh-token",
                request.uri().toString());
        assertTrue(request.headers().firstValue("Content-Type").isEmpty());
        assertEquals("POST", request.method());

        assertEquals("", extractBody(request));
    }

    @Test
    @DisplayName("リフレッシュトークン取得に失敗した場合は例外をスローする")
    void throwExceptionOnRefreshError() throws IOException, InterruptedException {
        String responseBody = "{\"message\":\"refreshtokenが不正です\"}";
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(403);
        when(httpResponse.body()).thenReturn(responseBody);

        HttpRequestExecutor executor = mock(HttpRequestExecutor.class);
        when(executor.send(any())).thenReturn(httpResponse);

        JQuantsApiException exception =
                assertThrows(
                        JQuantsApiException.class,
                        () ->
                                JQuantsClient.refreshIdToken(
                                        "refresh-token", JQuantsClient.DEFAULT_BASE_URI, executor));
        assertTrue(exception.getMessage().contains("status=403"));
        assertTrue(exception.getMessage().contains("refreshtokenが不正です"));
    }

    private static String extractBody(HttpRequest request) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder builder = new StringBuilder();
        request.bodyPublisher()
                .orElseThrow()
                .subscribe(
                        new Flow.Subscriber<ByteBuffer>() {
                            @Override
                            public void onSubscribe(Flow.Subscription subscription) {
                                subscription.request(Long.MAX_VALUE);
                            }

                            @Override
                            public void onNext(ByteBuffer item) {
                                byte[] bytes = new byte[item.remaining()];
                                item.get(bytes);
                                builder.append(new String(bytes, StandardCharsets.UTF_8));
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                latch.countDown();
                            }

                            @Override
                            public void onComplete() {
                                latch.countDown();
                            }
                        });
        latch.await();
        return builder.toString();
    }

    @Test
    @DisplayName("authenticateUser: メールアドレス/パスワードが null/空文字の場合に IllegalArgumentException を送出する")
    void authenticateUserThrowIllegalArgumentExceptionWithInvalidInputs() {
        // mailAddress が null/空文字
        assertThrows(
                IllegalArgumentException.class,
                () -> JQuantsClient.authenticateUser(null, "password"));
        assertThrows(
                IllegalArgumentException.class,
                () -> JQuantsClient.authenticateUser("", "password"));
        assertThrows(
                IllegalArgumentException.class,
                () -> JQuantsClient.authenticateUser("   ", "password"));

        // password が null/空文字
        assertThrows(
                IllegalArgumentException.class,
                () -> JQuantsClient.authenticateUser("user@example.com", null));
        assertThrows(
                IllegalArgumentException.class,
                () -> JQuantsClient.authenticateUser("user@example.com", ""));
        assertThrows(
                IllegalArgumentException.class,
                () -> JQuantsClient.authenticateUser("user@example.com", "   "));
    }

    @Test
    @DisplayName("refreshIdToken: refreshToken が null/空文字の場合に IllegalArgumentException を送出する")
    void refreshIdTokenThrowIllegalArgumentExceptionWithInvalidInputs() {
        // refreshToken が null/空文字
        assertThrows(IllegalArgumentException.class, () -> JQuantsClient.refreshIdToken(null));
        assertThrows(IllegalArgumentException.class, () -> JQuantsClient.refreshIdToken(""));
        assertThrows(IllegalArgumentException.class, () -> JQuantsClient.refreshIdToken("   "));
    }
}

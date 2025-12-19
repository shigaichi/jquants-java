package io.github.shigaichi.jquants.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.shigaichi.jquants.client.listedinfo.ListedInfoQuery;
import io.github.shigaichi.jquants.client.listedinfo.ListedInfoResponse;
import io.github.shigaichi.jquants.client.token.IdTokenResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;

/**
 * J-Quants API クライアント。
 */
public class JQuantsClient {
    public static final String DEFAULT_BASE_URL = "https://api.jquants.com/v1";
    public static final URI DEFAULT_BASE_URI = URI.create(DEFAULT_BASE_URL);

    private final String idToken;
    private final URI baseUri;
    private final HttpRequestExecutor requestExecutor;
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    public JQuantsClient(String idToken) {
        this(idToken, HttpClient.newHttpClient(), DEFAULT_BASE_URI);
    }

    public JQuantsClient(String idToken, HttpClient httpClient, URI baseUri) {
        this(idToken, baseUri, new HttpClientRequestExecutor(httpClient));
    }

    JQuantsClient(String idToken, URI baseUri, HttpRequestExecutor requestExecutor) {
        this.idToken = requireNonEmpty(idToken, "idToken");
        this.baseUri = normalizeBaseUri(baseUri);
        this.requestExecutor = Objects.requireNonNull(requestExecutor, "requestExecutor");
    }

    /**
     * メールアドレスとパスワードを用いて ID トークンとリフレッシュトークンを取得します。
     *
     * @param mailAddress 登録メールアドレス
     * @param password パスワード
     * @return 取得したトークン
     * @throws JQuantsApiException APIエラー時
     * @throws IOException 通信失敗時
     * @throws InterruptedException 通信割り込み時
     */
    public static IdTokenResponse authenticateUser(String mailAddress, String password)
            throws IOException, InterruptedException {
        return authenticateUser(mailAddress, password, DEFAULT_BASE_URI);
    }

    /**
     * メールアドレスとパスワードを用いて ID トークンとリフレッシュトークンを取得します（ベースURLを変更したい場合）。
     *
     * @param mailAddress 登録メールアドレス
     * @param password パスワード
     * @param baseUri API ベース URL
     * @return 取得したトークン
     * @throws JQuantsApiException APIエラー時
     * @throws IOException 通信失敗時
     * @throws InterruptedException 通信割り込み時
     */
    public static IdTokenResponse authenticateUser(String mailAddress, String password, URI baseUri)
            throws IOException, InterruptedException {
        HttpRequestExecutor requestExecutor =
                new HttpClientRequestExecutor(HttpClient.newHttpClient());
        return authenticateUser(mailAddress, password, baseUri, requestExecutor);
    }

    static IdTokenResponse authenticateUser(
            String mailAddress, String password, URI baseUri, HttpRequestExecutor requestExecutor)
            throws IOException, InterruptedException {
        String validatedMailAddress = requireNonEmpty(mailAddress, "mailaddress");
        String validatedPassword = requireNonEmpty(password, "password");
        Objects.requireNonNull(requestExecutor, "requestExecutor");
        URI normalizedBaseUri = normalizeBaseUri(baseUri);

        URI uri = buildUri(normalizedBaseUri, "token/auth_user");
        Map<String, String> requestBody = new LinkedHashMap<>();
        requestBody.put("mailaddress", validatedMailAddress);
        requestBody.put("password", validatedPassword);
        String body = OBJECT_MAPPER.writeValueAsString(requestBody);

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(uri)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")
                        .build();

        HttpResponse<String> response = requestExecutor.send(request);
        if (response.statusCode() != 200) {
            throw buildException(response);
        }

        IdTokenResponse tokenResponse =
                OBJECT_MAPPER.readValue(response.body(), IdTokenResponse.class);
        String issuedIdToken = tokenResponse.getIdToken();
        String refreshToken = tokenResponse.getRefreshToken();

        if (StringUtils.isBlank(issuedIdToken)) {
            if (StringUtils.isBlank(refreshToken)) {
                throw new JQuantsApiException("IDトークンの取得に失敗しました。", response.statusCode());
            }
            String renewedToken = refreshIdToken(refreshToken, normalizedBaseUri, requestExecutor);
            tokenResponse.setIdToken(renewedToken);
        }

        if (StringUtils.isBlank(tokenResponse.getRefreshToken())) {
            throw new JQuantsApiException("リフレッシュトークンの取得に失敗しました。", response.statusCode());
        }
        return tokenResponse;
    }

    /**
     * リフレッシュトークンを用いて ID トークンを取得します。
     *
     * @param refreshToken リフレッシュトークン
     * @return 新規に取得した ID トークン
     * @throws JQuantsApiException APIエラー時
     * @throws IOException 通信失敗時
     * @throws InterruptedException 通信割り込み時
     */
    public static String refreshIdToken(String refreshToken)
            throws IOException, InterruptedException {
        return refreshIdToken(refreshToken, DEFAULT_BASE_URI);
    }

    /**
     * リフレッシュトークンを用いて ID トークンを取得します。
     *
     * @param refreshToken リフレッシュトークン
     * @param baseUri API ベース URL
     * @return 新規に取得した ID トークン
     * @throws JQuantsApiException APIエラー時
     * @throws IOException 通信失敗時
     * @throws InterruptedException 通信割り込み時
     */
    public static String refreshIdToken(String refreshToken, URI baseUri)
            throws IOException, InterruptedException {
        HttpRequestExecutor requestExecutor =
                new HttpClientRequestExecutor(HttpClient.newHttpClient());
        return refreshIdToken(refreshToken, baseUri, requestExecutor);
    }

    static String refreshIdToken(
            String refreshToken, URI baseUri, HttpRequestExecutor requestExecutor)
            throws IOException, InterruptedException {
        String validatedToken = requireNonEmpty(refreshToken, "refreshtoken");
        Objects.requireNonNull(requestExecutor, "requestExecutor");
        URI normalizedBaseUri = normalizeBaseUri(baseUri);
        Map<String, String> params = Map.of("refreshtoken", validatedToken);
        URI uri = buildUri(normalizedBaseUri, "token/auth_refresh", params);

        HttpRequest request =
                HttpRequest.newBuilder().uri(uri).POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = requestExecutor.send(request);
        if (response.statusCode() != 200) {
            throw buildException(response);
        }

        IdTokenResponse tokenResponse =
                OBJECT_MAPPER.readValue(response.body(), IdTokenResponse.class);
        if (StringUtils.isBlank(tokenResponse.getIdToken())) {
            throw new JQuantsApiException("IDトークンの取得に失敗しました。", response.statusCode());
        }
        return tokenResponse.getIdToken();
    }

    /**
     * /listed/info API を呼び出します。
     *
     * @param query 検索条件。null の場合は全件取得。
     * @return APIレスポンス
     * @throws JQuantsApiException APIエラー時
     * @throws IOException 通信失敗時
     * @throws InterruptedException 通信割り込み時
     */
    public ListedInfoResponse getListedInfo(ListedInfoQuery query)
            throws IOException, InterruptedException {
        URI uri = buildUri("/listed/info", query);
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(uri)
                        .GET()
                        .header("Authorization", "Bearer " + idToken)
                        .build();
        HttpResponse<String> response = requestExecutor.send(request);
        if (response.statusCode() != 200) {
            throw buildException(response);
        }
        return OBJECT_MAPPER.readValue(response.body(), ListedInfoResponse.class);
    }

    /**
     * baseUri を基準にパスとクエリを組み立てます。
     *
     * @param path 結合するパス
     * @param query クエリパラメータ
     * @return 組み立てた URI
     */
    private URI buildUri(String path, ListedInfoQuery query) {
        Map<String, String> params = new LinkedHashMap<>();
        ListedInfoQuery effectiveQuery = query == null ? ListedInfoQuery.builder().build() : query;
        effectiveQuery.getCode().ifPresent(v -> params.put("code", v));
        effectiveQuery.getDate().ifPresent(v -> params.put("date", v));
        effectiveQuery.getPaginationKey().ifPresent(v -> params.put("pagination_key", v));

        return buildUri(path, params);
    }

    /**
     * baseUri を基準にパスと任意のクエリパラメータを結合します。
     *
     * @param path 結合するパス
     * @param params 付与するクエリパラメータ
     * @return 組み立てた URI
     */
    private URI buildUri(String path, Map<String, String> params) {
        return buildUri(baseUri, path, params);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * null/空/空白のみを拒否し、フィールド名付きで例外を送出します。
     *
     * @param value 検証する文字列
     * @param fieldName フィールド名
     * @return 元の値
     * @throws IllegalArgumentException value が空の場合
     */
    private static String requireNonEmpty(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(fieldName + "が指定されていません。");
        }
        return value;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private static JQuantsApiException buildException(HttpResponse<String> response) {
        String message = parseErrorMessage(response.body()).orElse("API呼び出しに失敗しました。");
        String detailedMessage =
                String.format("status=%d, message=%s", response.statusCode(), message);
        return new JQuantsApiException(detailedMessage, response.statusCode());
    }

    private static Optional<String> parseErrorMessage(String body) {
        try {
            Map<?, ?> parsed = OBJECT_MAPPER.readValue(body, Map.class);
            Object message = parsed.get("message");
            if (message != null) {
                return Optional.of(String.valueOf(message));
            }
        } catch (JsonProcessingException ignored) {
            // エラー時はボディをそのまま返す
            return Optional.ofNullable(body).filter(b -> !b.isBlank());
        }
        return Optional.empty();
    }

    /**
     * 正規化済み baseUri に対してパスを解決します。
     *
     * @param normalizedBaseUri 末尾スラッシュ付きのベース URI
     * @param path 結合するパス
     * @return 組み立てた URI
     */
    private static URI buildUri(URI normalizedBaseUri, String path) {
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        return normalizedBaseUri.resolve(normalizedPath);
    }

    /**
     * 正規化済み baseUri に対してパスとクエリパラメータを結合します。
     *
     * @param normalizedBaseUri 末尾スラッシュ付きのベース URI
     * @param path 結合するパス
     * @param params 付与するクエリパラメータ
     * @return 組み立てた URI
     */
    private static URI buildUri(URI normalizedBaseUri, String path, Map<String, String> params) {
        URI uri = buildUri(normalizedBaseUri, path);
        if (params.isEmpty()) {
            return uri;
        }
        StringBuilder builder = new StringBuilder(uri.toString());
        StringJoiner joiner = new StringJoiner("&", "?", "");
        params.forEach((key, value) -> joiner.add(encode(key) + "=" + encode(value)));
        builder.append(joiner);
        return URI.create(builder.toString());
    }

    /**
     * ベース URI を正規化し、末尾スラッシュを保証します。
     *
     * @param uri 元の URI
     * @return 末尾スラッシュ付きの URI
     */
    private static URI normalizeBaseUri(URI uri) {
        Objects.requireNonNull(uri, "baseUri");
        String value = uri.toString();
        if (!value.endsWith("/")) {
            value = value + "/";
        }
        return URI.create(value);
    }
}

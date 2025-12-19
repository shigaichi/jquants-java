package io.github.shigaichi.jquants;

import io.github.shigaichi.jquants.client.JQuantsClient;
import io.github.shigaichi.jquants.client.listedinfo.ListedInfoQuery;
import io.github.shigaichi.jquants.client.listedinfo.ListedInfoResponse;
import io.github.shigaichi.jquants.client.token.IdTokenResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Optional;

public class App {
    public static void main(String[] args) throws Exception {
        String mailAddress = System.getenv("JQUANTS_MAIL_ADDRESS");
        if (mailAddress == null || mailAddress.isBlank()) {
            throw new IllegalStateException("環境変数JQUANTS_MAIL_ADDRESSが設定されていません。");
        }

        String password = System.getenv("JQUANTS_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("環境変数JQUANTS_PASSWORDが設定されていません。");
        }

        URI baseUri =
                Optional.ofNullable(System.getenv("JQUANTS_BASE_URL"))
                        .filter(url -> !url.isBlank())
                        .map(URI::create)
                        .orElse(JQuantsClient.DEFAULT_BASE_URI);

        IdTokenResponse tokenResponse =
                JQuantsClient.authenticateUser(mailAddress, password, baseUri);
        JQuantsClient client =
                new JQuantsClient(tokenResponse.getIdToken(), HttpClient.newHttpClient(), baseUri);
        ListedInfoResponse response = client.getListedInfo(ListedInfoQuery.builder().build());

        response.getInfo().stream()
                .limit(10)
                .forEach(info -> System.out.println(info.getCode() + " " + info.getCompanyName()));
        response.getPaginationKey().ifPresent(key -> System.out.println("次ページキー: " + key));
    }
}

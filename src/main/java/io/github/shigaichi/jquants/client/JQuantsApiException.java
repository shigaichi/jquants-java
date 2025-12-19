package io.github.shigaichi.jquants.client;

import lombok.Getter;

/**
 * API呼び出しでエラーが発生した際の例外。
 */
@Getter
public class JQuantsApiException extends RuntimeException {
    private final int statusCode;

    public JQuantsApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}

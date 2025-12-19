package io.github.shigaichi.jquants.client.listedinfo;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * /listed/info の検索条件。
 */
@ToString
@EqualsAndHashCode
public final class ListedInfoQuery {
    /** 銘柄コード（4桁または5桁）。 */
    private final String code;

    /** 基準日付（YYYY-MM-DD または YYYYMMDD）。 */
    private final String date;

    /** ページング用キー。 */
    private final String paginationKey;

    private ListedInfoQuery(Builder builder) {
        this.code = builder.code;
        this.date = builder.date;
        this.paginationKey = builder.paginationKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> getCode() {
        return Optional.ofNullable(code);
    }

    public Optional<String> getDate() {
        return Optional.ofNullable(date);
    }

    public Optional<String> getPaginationKey() {
        return Optional.ofNullable(paginationKey);
    }

    public static final class Builder {
        private String code;
        private String date;
        private String paginationKey;

        public Builder code(String code) {
            this.code = normalize(code);
            return this;
        }

        public Builder date(String date) {
            this.date = normalize(date);
            return this;
        }

        public Builder paginationKey(String paginationKey) {
            this.paginationKey = normalize(paginationKey);
            return this;
        }

        public ListedInfoQuery build() {
            return new ListedInfoQuery(this);
        }

        private String normalize(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.strip();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}

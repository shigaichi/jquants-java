package io.github.shigaichi.jquants.client.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdTokenResponse {
    /** IDトークン。 */
    @JsonProperty("idToken")
    private String idToken;

    /** リフレッシュトークン（/token/auth_user の場合に返却）。 */
    @JsonProperty("refreshToken")
    private String refreshToken;
}

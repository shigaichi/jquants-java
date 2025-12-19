package io.github.shigaichi.jquants.client.token;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(exclude = {"idToken", "refreshToken"})
public class IdTokenResponse {
    /** IDトークン。 */
    @JsonProperty("idToken")
    private String idToken;

    /** リフレッシュトークン（/token/auth_user の場合に返却）。 */
    @JsonProperty("refreshToken")
    private String refreshToken;
}

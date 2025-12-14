package io.github.shigaichi.jquants.client.listedinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListedInfoResponse {
    /** 銘柄情報リスト。 */
    @JsonProperty("info")
    private List<ListedInfo> info = Collections.emptyList();

    /** ページング用キー。 */
    @JsonProperty("pagination_key")
    private String paginationKey;

    public List<ListedInfo> getInfo() {
        return info == null ? Collections.emptyList() : Collections.unmodifiableList(info);
    }

    public Optional<String> getPaginationKey() {
        return Optional.ofNullable(paginationKey);
    }
}

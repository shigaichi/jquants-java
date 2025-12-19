package io.github.shigaichi.jquants.client.listedinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListedInfo {
    /** 情報適用年月日（YYYY-MM-DD）。 */
    @JsonProperty("Date")
    private String date;

    /** 銘柄コード。 */
    @JsonProperty("Code")
    private String code;

    /** 会社名（日本語）。 */
    @JsonProperty("CompanyName")
    private String companyName;

    /** 会社名（英語）。 */
    @JsonProperty("CompanyNameEnglish")
    private String companyNameEnglish;

    /** 17業種コード。 */
    @JsonProperty("Sector17Code")
    private String sector17Code;

    /** 17業種コード名。 */
    @JsonProperty("Sector17CodeName")
    private String sector17CodeName;

    /** 33業種コード。 */
    @JsonProperty("Sector33Code")
    private String sector33Code;

    /** 33業種コード名。 */
    @JsonProperty("Sector33CodeName")
    private String sector33CodeName;

    /** 規模コード。 */
    @JsonProperty("ScaleCategory")
    private String scaleCategory;

    /** 市場区分コード。 */
    @JsonProperty("MarketCode")
    private String marketCode;

    /** 市場区分名。 */
    @JsonProperty("MarketCodeName")
    private String marketCodeName;

    /** 貸借信用区分コード。 */
    @JsonProperty("MarginCode")
    private String marginCode;

    /** 貸借信用区分名。 */
    @JsonProperty("MarginCodeName")
    private String marginCodeName;

    /** 制限値幅算出の基準となる値段。 */
    @JsonProperty("BasePrice")
    private BigDecimal basePrice;
}

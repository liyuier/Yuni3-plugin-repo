import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: DailyNewsResponse
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/7 7:11
 * @description:
 */

@Data
@NoArgsConstructor
public class DailyNewsResponse {
    private Integer code;
    private String msg;
    @JsonProperty("imageUrl")
    private String imageUrl;
    private String datatime;
}

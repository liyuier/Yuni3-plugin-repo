package steamstatus.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: SteamApiKey
 * @Author yuier
 * @Package steamstatus.config
 * @Date 2026/6/17 20:04
 * @description: API key 模型类
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SteamApiKey {

    private String apikey;
}

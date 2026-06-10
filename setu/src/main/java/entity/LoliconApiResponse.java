package entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Title: LoliconApiResponse
 * @Author yuier
 * @Package entity
 * @Date 2026/2/5 14:00
 * @description: 接口响应
 */

@Data
@NoArgsConstructor
public class LoliconApiResponse {

    // 报错信息，如果没有报错就是空字符串
    private String error;
    // 色图列表
    private List<ApiResponseSetu> data;
}

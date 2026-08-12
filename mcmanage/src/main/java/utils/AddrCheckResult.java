package utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 地址校验结果。
 */

@Getter
@AllArgsConstructor
public class AddrCheckResult {

    /** 是否校验通过 */
    private boolean success;
    /** 通过时为解析出的主机名 */
    private String host;
    /** 通过时为解析出的端口（-1 表示未指定） */
    private int port;
    /** 失败时的错误描述 */
    private String message;

    static AddrCheckResult ok(String host, int port) {
        return new AddrCheckResult(true, host, port, null);
    }

    static AddrCheckResult fail(String message) {
        return new AddrCheckResult(false, null, -1, message);
    }
}

package utils;

/**
 * @Title: MCManageUtils
 * @Author yuier
 * @Package utils
 * @Date 2026/8/12 20:22
 * @description: 通用工具类
 */
public class MCManageUtils {

    /**
     * 将用户输入的地址拆分为 host + port。不做合法性校验。
     *
     * @param addr 用户输入的服务器地址（如 "mc.example.com:25565"）
     * @return 解析结果，携带 host 和 port（未指定端口时为 -1）
     */
    public static AddrCheckResult parseAddr(String addr) {
        if (addr == null || addr.isBlank()) {
            return AddrCheckResult.fail("服务器地址不能为空");
        }
        String trimmed = addr.strip();
        int lastColon = trimmed.lastIndexOf(':');
        if (lastColon > 0 && lastColon < trimmed.length() - 1) {
            String portPart = trimmed.substring(lastColon + 1);
            if (portPart.matches("\\d{1,5}")) {
                return AddrCheckResult.ok(
                        trimmed.substring(0, lastColon),
                        Integer.parseInt(portPart));
            }
        }
        return AddrCheckResult.ok(trimmed, -1);
    }

    /**
     * 校验 MC 服务器地址是否合法。
     *
     * <p>在 {@link #parseAddr} 基础上增加：禁止 localhost、端口范围校验、主机格式校验。</p>
     *
     * @param addr 用户输入的服务器地址
     * @return 校验结果
     */
    public static AddrCheckResult mcServerAddrValid(String addr) {
        AddrCheckResult parsed = parseAddr(addr);
        if (!parsed.isSuccess()) {
            return parsed;
        }
        String host = parsed.getHost();
        int port = parsed.getPort();

        if ("localhost".equalsIgnoreCase(host)) {
            return AddrCheckResult.fail("不允许使用 localhost，请输入可公开访问的服务器地址");
        }
        if (port != -1 && (port < 1 || port > 65535)) {
            return AddrCheckResult.fail("端口号不合法（1~65535）：" + port);
        }
        return isValidHost(host)
                ? AddrCheckResult.ok(host, port)
                : AddrCheckResult.fail("服务器地址格式不合法：" + host);
    }

    /**
     * 校验主机名（域名或 IPv4）。
     */
    private static boolean isValidHost(String host) {
        if (host.isBlank()) {
            return false;
        }
        // 去掉可能存在的方括号（IPv6 风格，暂不完全支持但容错）
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        // IPv4: x.x.x.x，每段 0-255
        if (host.matches("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$")) {
            String[] parts = host.split("\\.");
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        }
        // 域名: 字母/数字/连字符/点号，至少包含一个点号
        return host.matches("^[a-zA-Z0-9]([a-zA-Z0-9\\-]*[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9\\-]*[a-zA-Z0-9])?)*\\.[a-zA-Z]{2,}$");
    }

}

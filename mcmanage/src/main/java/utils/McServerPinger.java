package utils;

import lombok.extern.slf4j.Slf4j;

/**
 * MC 服务器状态查询工具。
 */
@Slf4j
public class McServerPinger {

    /**
     * 拼接 MC 服务器状态字符串。
     *
     * @param addr 用户输入的原始服务器地址
     * @return 服务器状态字符串
     */
    public static String buildMcServerStatusStr(String addr) {
        AddrCheckResult parsed = MCManageUtils.parseAddr(addr);
        if (!parsed.isSuccess()) {
            return parsed.getMessage();
        }
        String host = parsed.getHost();
        int port = parsed.getPort() == -1 ? 25565 : parsed.getPort();

        MCPinger.McServerStatus status = MCPinger.ping(host, port);
        if (status == null) {
            log.warn("MC 服务器 {}:{} 查询失败，返回错误提示给用户", host, port);
            return "无法连接到 " + host + ":" + port + "，请确认服务器地址和端口是否正确";
        }

        String serverName = status.getMotd() != null && !status.getMotd().isBlank()
                ? status.getMotd().replaceAll("§.", "").strip()
                : addr;
        return String.format(
                "========== MC 服务器状态 ==========\n" +
                "服务器: %s (%s:%d)\n" +
                "版本: %s (协议 %d)\n" +
                "在线人数: %d / %d",
                serverName, host, port,
                status.getVersionName(), status.getProtocol(),
                status.getOnline(), status.getMax());
    }
}

package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * MC 服务器状态查询（Server List Ping 协议自实现）。
 *
 * <p>协议流程：TCP 连接 → Handshake 包 → Status Request 包 → 读取 JSON 响应。</p>
 * <p>所有超时由本类控制，不依赖第三方库。</p>
 */
@Slf4j
public class MCPinger {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int CONNECT_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 5000;
    private static final int PROTOCOL_VERSION = 47;   // 1.8.x 协议号，兼容性最好

    /**
     * 查询 MC 服务器状态。
     *
     * @param host 服务器地址（域名或 IP）
     * @param port 服务器端口
     * @return 服务器状态，失败返回 null
     */
    public static McServerStatus ping(String host, int port) {
        // 解析全部地址逐个尝试（对齐 Python mcstatus 行为）
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                try {
                    McServerStatus status = pingAddress(address, port);
                    if (status != null) {
                        return status;
                    }
                } catch (Exception e) {
                    log.warn("地址 {}:{} 连接失败: {}", address.getHostAddress(), port, e.getMessage());
                }
            }
            return null;
        } catch (IOException e) {
            log.error("DNS 解析失败: {}", host, e);
            return null;
        }
    }

    private static McServerStatus pingAddress(InetAddress address, int port) throws IOException {
        try (Socket socket = new Socket()) {
            // 连接 + 读写超时完全可控
            socket.connect(new InetSocketAddress(address, port), CONNECT_TIMEOUT);
            socket.setSoTimeout(READ_TIMEOUT);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // 1. Handshake 包: [packetId=0, protocolVersion, host, port, nextState=1]
            ByteArrayOutputStream handshakeBuf = new ByteArrayOutputStream();
            DataOutputStream handshake = new DataOutputStream(handshakeBuf);
            writeVarInt(handshake, 0);
            writeVarInt(handshake, PROTOCOL_VERSION);
            writeString(handshake, address.getHostAddress());
            handshake.writeShort(port);
            writeVarInt(handshake, 1);
            writePacket(out, handshakeBuf.toByteArray());

            // 2. Status Request 包: [packetId=0, 空]
            out.writeByte(1);   // 包长度 1
            out.writeByte(0);   // packet ID 0
            out.flush();

            // 3. 读取响应: [packetLength, packetId=0, JSON字符串]
            int packetLength = readVarInt(in);
            if (packetLength <= 0 || packetLength > 1 << 20) {  // 1MB 上限防御
                throw new IOException("非法包长度: " + packetLength);
            }
            byte[] packetData = new byte[packetLength];
            in.readFully(packetData);
            DataInputStream packetIn = new DataInputStream(new java.io.ByteArrayInputStream(packetData));
            int packetId = readVarInt(packetIn);
            if (packetId != 0) {
                throw new IOException("预期状态响应包 (id=0)，实际收到 id=" + packetId);
            }
            String json = readString(packetIn);

            return parseStatus(json);
        }
    }

    private static McServerStatus parseStatus(String json) throws IOException {
        JsonNode root = MAPPER.readTree(json);
        JsonNode version = root.get("version");
        JsonNode players = root.get("players");
        JsonNode description = root.get("description");

        McServerStatus status = new McServerStatus();
        status.setVersionName(version != null && version.has("name") ? version.get("name").asText() : "未知");
        status.setProtocol(version != null && version.has("protocol") ? version.get("protocol").asInt() : -1);
        status.setOnline(players != null && players.has("online") ? players.get("online").asInt() : -1);
        status.setMax(players != null && players.has("max") ? players.get("max").asInt() : -1);
        status.setMotd(extractMotdText(description));
        // 在线玩家名列表（服务器通常最多返回 10~12 个）
        if (players != null && players.has("sample") && players.get("sample").isArray()) {
            java.util.List<String> playerNames = new java.util.ArrayList<>();
            for (JsonNode player : players.get("sample")) {
                if (player.has("name")) {
                    playerNames.add(player.get("name").asText());
                }
            }
            status.setPlayerNames(playerNames);
        } else {
            status.setPlayerNames(java.util.Collections.emptyList());
        }
        return status;
    }

    /** 提取 MOTD 纯文本（description 可能是字符串或 JSON 对象） */
    private static String extractMotdText(JsonNode description) {
        if (description == null) return "";
        if (description.isTextual()) {
            return description.asText();
        }
        // {"text": "xxx", "extra": [...]} 形式的富文本，递归拼接 text 字段
        StringBuilder sb = new StringBuilder();
        appendMotdText(description, sb);
        return sb.toString();
    }

    private static void appendMotdText(JsonNode node, StringBuilder sb) {
        if (node.isTextual()) {
            sb.append(node.asText());
            return;
        }
        if (node.has("text")) {
            sb.append(node.get("text").asText());
        }
        if (node.has("extra") && node.get("extra").isArray()) {
            for (JsonNode child : node.get("extra")) {
                appendMotdText(child, sb);
            }
        }
    }

    // ---- 协议工具 ----

    private static void writePacket(DataOutputStream out, byte[] packetData) throws IOException {
        writeVarInt(out, packetData.length);
        out.write(packetData);
        out.flush();
    }

    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        do {
            byte temp = (byte) (value & 0x7F);
            value >>>= 7;
            if (value != 0) {
                temp |= 0x80;
            }
            out.writeByte(temp);
        } while (value != 0);
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int result = 0;
        int shift = 0;
        byte b;
        do {
            b = in.readByte();
            result |= (b & 0x7F) << shift;
            shift += 7;
            if (shift > 35) {
                throw new IOException("VarInt 过大");
            }
        } while ((b & 0x80) != 0);
        return result;
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = readVarInt(in);
        if (length < 0 || length > 1 << 20) {
            throw new IOException("非法字符串长度: " + length);
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * MC 服务器状态。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McServerStatus {
        private String motd;
        private String versionName;
        private int protocol;
        private int online;
        private int max;
        /** 在线玩家名列表（服务器最多返回约 10~12 个，其余以 ... 省略） */
        private java.util.List<String> playerNames = java.util.Collections.emptyList();
    }
}

package db;

import com.yuier.yuni.plugin.data.annotation.Id;
import com.yuier.yuni.plugin.data.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Title: GroupMcServer
 * @Author yuier
 * @Package db
 * @Date 2026/8/12 19:59
 * @description: 群组 - MC服务器 记录表
 */

@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMcServer {
    @Id
    private Long id;
    // 群组 ID
    private Long groupId;
    // 服务器 名称
    private String serverName;
    // 服务器地址
    private String serverAddr;
}

package util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Title: util.TransferUtil
 * @Author yuier
 * @Package PACKAGE_NAME
 * @Date 2026/1/12 16:28
 * @description: 通用转换工具
 */

@Slf4j
public class TransferUtil {

    /**
     * 编码
     * @param rawWords 原始语句
     * @param transMap 映射表
     * @return  编码后的密文
     */
    public static String encode(String rawWords, List<String> transMap) {
        // 获取原始语句的字节数组
        byte[] rawWordsBytes = rawWords.getBytes(StandardCharsets.UTF_8);

        // 将字节数组转换为密文
        StringBuilder encodedBuilder  = new StringBuilder();
        for (byte b : rawWordsBytes) {
            /* 假设该字节十六进制为 0xAB, 那么提取高 4 位与低 4 位，分别得到 10, 11. 以之到映射表中进行映射*/
            // 提取高 4 位（右移 4 位）
            int highNibble = (b >> 4) & 0x0F;
            // 提取低 4位 （与0x0F掩码）
            int lowNibble = b & 0x0F;

            // 映射为密文
            encodedBuilder.append(transMap.get(highNibble));
            encodedBuilder.append(transMap.get(lowNibble));
        }

        return encodedBuilder.toString();
    }

    /**
     * 解码
     * @param secretWords 密文
     * @param transMap 映射表
     * @return  解码后的明文
     */
    public static String decode(String secretWords, List<String> transMap) {
        // 读取密文为映射表索引数组
        int[] digits = new int[secretWords.length()];
        for (int i = 0; i < secretWords.length(); i++) {
            // 取出每一位密文
            String charStr = String.valueOf(secretWords.charAt(i));
            // 获取密文在映射表中的索引
            int index = transMap.indexOf(charStr);
            if (index == -1) {
                throw new IllegalArgumentException("非法字符: " + charStr);
            }
            // 将索引存入数组
            digits[i] = index;
        }

        /* 将索引数组转换为字节数组，原理同编码。比如 10, 11 两个数字，转换后就是 0xAB 字节 */
        // 两个十六进制数字转换成一个字节，因此字节数组长度为 digits.length / 2
        byte[] bytes = new byte[digits.length / 2];
        for (int i = 0; i < bytes.length; i++) {
            // 第一个数字作为高位，第二个数字作为低位
            int highNibble = digits[i * 2];      // 高4位
            int lowNibble = digits[i * 2 + 1];   // 低4位
            bytes[i] = (byte) ((highNibble << 4) | lowNibble);  // 合并为一个字节
        }

        // UTF-8解码
        return new String(bytes, StandardCharsets.UTF_8);
    }
}

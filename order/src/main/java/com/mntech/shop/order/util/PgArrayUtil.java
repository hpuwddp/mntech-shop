package com.mntech.shop.order.util;

import cn.hutool.core.util.StrUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PostgreSQL 数组工具类
 */
public class PgArrayUtil {

    /**
     * PostgreSQL 数组转 List<String>
     */
    public static List<String> parseToList(String pgArray) {
        if (StrUtil.isBlank(pgArray)) {
            return new ArrayList<>();
        }

        String trimmed = pgArray.trim();

        // PostgreSQL 数组格式: {a,b,c}
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            String content = trimmed.substring(1, trimmed.length() - 1);
            if (StrUtil.isBlank(content)) {
                return new ArrayList<>();
            }
            return Arrays.stream(content.split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    /**
     * List<String> 转 PostgreSQL 数组字符串
     */
    public static String listToPgArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "{}";
        }
        return "{" + String.join(",", list) + "}";
    }

    /**
     * JSON 字符串 转 Map<String, List<String>>
     * 手写解析，不依赖 JSONUtil
     * 输入: {"color":["白色","黑色"],"storage":["256GB","512GB"]}
     */
    public static Map<String, List<String>> parseToMap(String jsonStr) {
        if (StrUtil.isBlank(jsonStr)) {
            return new HashMap<>();
        }

        Map<String, List<String>> result = new HashMap<>();
        String trimmed = jsonStr.trim();

        // 去掉最外层的大括号
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return result;
        }
        String content = trimmed.substring(1, trimmed.length() - 1);

        if (StrUtil.isBlank(content)) {
            return result;
        }

        // 按 , 分割，但要注意数组内部也有逗号
        // 简单处理：按 },{ 分割
        String[] pairs = content.split(",(?=\\s*\\\"[^\\\"]+\\\"\\s*:)");

        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) {
                continue;
            }

            String key = kv[0].trim().replaceAll("^\"|\"$", "");
            String valueStr = kv[1].trim();

            // 解析数组值
            List<String> values = parseArrayValue(valueStr);
            if (!values.isEmpty()) {
                result.put(key, values);
            }
        }

        return result;
    }

    /**
     * 解析 JSON 数组值
     * 输入: ["白色","黑色"]
     * 输出: ["白色","黑色"]
     */
    private static List<String> parseArrayValue(String valueStr) {
        List<String> result = new ArrayList<>();
        String trimmed = valueStr.trim();

        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return result;
        }

        String content = trimmed.substring(1, trimmed.length() - 1);
        if (StrUtil.isBlank(content)) {
            return result;
        }

        // 按逗号分割，每个元素用引号包围
        String[] items = content.split(",");
        for (String item : items) {
            String val = item.trim().replaceAll("^\"|\"$", "");
            if (StrUtil.isNotBlank(val)) {
                result.add(val);
            }
        }

        return result;
    }

    /**
     * Map<String, List<String>> 转 JSON 字符串
     * 手写生成，不依赖 JSONUtil
     */
    public static String mapToJson(Map<String, List<String>> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (count > 0) {
                sb.append(",");
            }
            String key = entry.getKey();
            List<String> values = entry.getValue();

            sb.append("\"").append(key).append("\":");
            sb.append("[");
            if (values != null && !values.isEmpty()) {
                for (int i = 0; i < values.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append("\"").append(values.get(i)).append("\"");
                }
            }
            sb.append("]");
            count++;
        }
        sb.append("}");
        return sb.toString();
    }
}
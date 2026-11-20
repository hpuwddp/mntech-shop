package com.mntech.shop.order.util;

import java.util.Random;

public class RandomNicknameGenerator {
    private static final String[] NICKNAMES = {
            "清风明月", "细雨流光", "山河远阔", "人间烟火", "浮生若梦",
            "且听风吟", "云淡风轻", "星河万里", "不负韶华", "岁月如歌",
            "陌上花开", "浅笑安然", "一蓑烟雨", "半盏流年", "清风徐来",
            "水波不兴", "月上柳梢", "人约黄昏", "把酒临风", "宠辱不惊",
            "小确幸", "暖暖", "甜甜圈", "小饼干", "棉花糖",
            "星星糖", "奶茶续命", "熬夜冠军", "网瘾少年", "躺平选手"
    };

    private static final Random random = new Random();

    public static String generate() {
        return NICKNAMES[random.nextInt(NICKNAMES.length)];
    }
}

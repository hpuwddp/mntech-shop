package com.mntech.shop.order.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mntech.shop.common.exceptions.BusinessException;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 分销海报生成器（服务端渲染，返回 base64 PNG）。
 * 画布：750x1334，顶部店铺名称，中部用户头像(圆形)+昵称，右上角二维码，底部引导文案。
 */
@Slf4j
public final class PosterGenerator {

    private static final int WIDTH = 750;
    private static final int HEIGHT = 1334;
    private static final int QR_SIZE = 260;

    private PosterGenerator() {
    }

    /**
     * 生成海报并返回 data URI 形式的 base64 PNG（内部用 ZXing 生成二维码）。
     */
    public static String generatePosterBase64(String nickname, String avatarUrl,
                                              String shopName, String qrContent, String userId) throws Exception {
        return generatePosterBase64(nickname, avatarUrl, shopName, createQrImage(qrContent, QR_SIZE, QR_SIZE), userId);
    }

    /**
     * 生成海报并返回 data URI 形式的 base64 PNG（使用外部传入的二维码图片，如微信小程序码）。
     * qrImage 为空时回退为 ZXing 生成占位内容。
     */
    public static String generatePosterBase64(String nickname, String avatarUrl,
                                              String shopName, BufferedImage qrImage, String userId) throws Exception {
        BufferedImage poster = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = poster.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 背景：白底 + 顶部品牌色渐变
            drawBackground(g);

            // 店铺名
            Font shopFont = loadChineseFont(Font.BOLD, 46);
            g.setFont(shopFont);
            g.setColor(new Color(51, 51, 51));
            drawCenteredText(g, shopName, WIDTH / 2, 150);

            // 二维码（右上区域） 650, 170
            BufferedImage qr = qrImage != null ? qrImage : createQrImage("pages/index/index", QR_SIZE, QR_SIZE);
            int qrX = 110;
            int qrY = 300;
            drawQrBackground(g, qrX, qrY);
            g.drawImage(qr, qrX, qrY, QR_SIZE, QR_SIZE, null);
            drawQrFrame(g, qrX, qrY);

            // 头像（圆形）
            BufferedImage avatar = loadAvatar(avatarUrl);
            if (avatar == null) {
                avatar = createPlaceholderAvatar(nickname);
            }
            int avatarSize = 200;
            int avatarX = WIDTH / 2 - avatarSize / 2;
            int avatarY = 700;
            g.drawImage(roundImage(avatar, avatarSize), avatarX, avatarY, null);

            // 昵称
            Font nameFont = loadChineseFont(Font.BOLD, 40);
            g.setFont(nameFont);
            g.setColor(new Color(51, 51, 51));
            drawCenteredText(g, nickname, WIDTH / 2, avatarY + avatarSize + 110);

            // 引导文案
            Font tipFont = loadChineseFont(Font.PLAIN, 32);
            g.setFont(tipFont);
            g.setColor(new Color(153, 153, 153));
            drawCenteredText(g, "长按识别二维码，进入" + shopName, WIDTH / 2, avatarY + avatarSize + 200);
            drawCenteredText(g, "分享赚佣金，好友下单享返利", WIDTH / 2, avatarY + avatarSize + 260);

            // 底部版权
            Font footerFont = loadChineseFont(Font.PLAIN, 24);
            g.setFont(footerFont);
            g.setColor(new Color(200, 200, 200));
            drawCenteredText(g, "琼浆玉液 · 官方分销", WIDTH / 2, HEIGHT - 60);
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(poster, "png", out);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
    }

    private static void drawBackground(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        // 顶部品牌色条
        GradientPaint gp = new GradientPaint(0, 0, new Color(180, 21, 21), WIDTH, 0, new Color(231, 76, 60));
        g.setPaint(gp);
        g.fillRect(0, 0, WIDTH, 240);
        // 品牌名白字放在色条上
        Font brandFont = loadChineseFont(Font.BOLD, 40);
        g.setFont(brandFont);
        g.setColor(Color.WHITE);
        drawCenteredText(g, "琼浆玉液", WIDTH / 2, 90);
        g.setColor(Color.WHITE);
        g.fillRect(0, 238, WIDTH, 2);
    }

    private static void drawQrFrame(Graphics2D g, int x, int y) {
        g.setColor(new Color(220, 220, 220));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(x - 12, y - 12, QR_SIZE + 24, QR_SIZE + 24, 20, 20);
    }

    private static void drawQrBackground(Graphics2D g, int x, int y) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(x - 12, y - 12, QR_SIZE + 24, QR_SIZE + 24, 20, 20);
    }

    private static BufferedImage createQrImage(String content, int width, int height) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    private static BufferedImage loadAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return null;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(avatarUrl).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            int code = conn.getResponseCode();
            if (code != 200) {
                return null;
            }
            try (InputStream in = conn.getInputStream()) {
                return ImageIO.read(in);
            }
        } catch (Exception e) {
            log.warn("头像下载失败: {}", avatarUrl, e);
            return null;
        }
    }

    private static BufferedImage createPlaceholderAvatar(String nickname) {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(200, 200, 200));
        g.fillOval(0, 0, 200, 200);
        g.setColor(Color.WHITE);
        g.setFont(loadChineseFont(Font.BOLD, 72));
        String ch = nickname != null && !nickname.isEmpty() ? nickname.substring(0, 1) : "友";
        FontMetrics fm = g.getFontMetrics();
        int x = (200 - fm.stringWidth(ch)) / 2;
        int y = 200 / 2 + (fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(ch, x, y);
        g.dispose();
        return img;
    }

    private static BufferedImage roundImage(BufferedImage img, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
        g.drawImage(img, 0, 0, size, size, null);
        g.dispose();
        return out;
    }

    private static void drawCenteredText(Graphics2D g, String text, int centerX, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;
        g.drawString(text, x, baselineY);
    }

    private static Font loadChineseFont(int style, int size) {
        String[] candidates = {
                "C:\\Windows\\Fonts\\msyh.ttc",
                "C:\\Windows\\Fonts\\simhei.ttf",
                "C:\\Windows\\Fonts\\simsun.ttc",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
        };
        for (String path : candidates) {
            try {
                File f = new File(path);
                if (f.exists()) {
                    Font base = Font.createFont(Font.TRUETYPE_FONT, f);
                    return base.deriveFont(style, (float) size);
                }
            } catch (Exception ignored) {
                // 继续尝试下一个字体
            }
        }
        return new Font(Font.SANS_SERIF, style, size);
    }
}
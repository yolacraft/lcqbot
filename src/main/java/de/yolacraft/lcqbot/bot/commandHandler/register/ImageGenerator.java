package de.yolacraft.lcqbot.bot.commandHandler.register;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageGenerator {

    public static BufferedImage createImage(String player, String twitch) throws Exception {
        BufferedImage background;
        try (InputStream bgStream = ImageGenerator.class.getResourceAsStream("/background.png")) {
            background = ImageIO.read(bgStream);
        }

        BufferedImage image = new BufferedImage(
                background.getWidth(),
                background.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = image.createGraphics();
        g.drawImage(background, 0, 0, null);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        URL url = new URL("https://mc-heads.net/avatar/" + player + "/256");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
        );

        connection.connect();

        BufferedImage avatar = ImageIO.read(connection.getInputStream());

        g.drawImage(avatar, 22, 22, 256, 256, null);

        Font minecraftFont;
        try (InputStream fontStream = ImageGenerator.class.getResourceAsStream("/Minecraft.ttf")) {
            minecraftFont = Font.createFont(Font.TRUETYPE_FONT, fontStream)
                    .deriveFont(96f); // Größe anpassen
        }

        g.setFont(minecraftFont);
        g.setColor(Color.WHITE);

        g.drawString(player, 320, 22+96);

        try (InputStream fontStream = ImageGenerator.class.getResourceAsStream("/Minecraft.ttf")) {
            minecraftFont = Font.createFont(Font.TRUETYPE_FONT, fontStream)
                    .deriveFont(72f);
        }


        g.setFont(minecraftFont);
        g.setColor(Color.GRAY);
        g.drawString(twitch, 440, 320-74);
        g.dispose();


        return image;
    }
}
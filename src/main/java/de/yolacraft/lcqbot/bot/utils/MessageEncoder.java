package de.yolacraft.lcqbot.bot.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MessageEncoder {

    private static final String DEFAULT_LANGUAGE_FILE = "lang/de_de.json";
    private static final Map<String, String> MESSAGES = loadMessages();

    private MessageEncoder() {
        // Utility class
    }

    private static Map<String, String> loadMessages() {
        try (InputStream stream = MessageEncoder.class.getClassLoader().getResourceAsStream(DEFAULT_LANGUAGE_FILE)) {
            if (stream == null) {
                return Collections.emptyMap();
            }

            Type type = new TypeToken<HashMap<String, String>>() {}.getType();
            return Collections.unmodifiableMap(new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), type));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load language file: " + DEFAULT_LANGUAGE_FILE, e);
        }
    }

    public static String getMessage(String key) {
        return MESSAGES.getOrDefault(key, "[missing:" + key + "]");
    }

    public static String format(String key, Object... args) {
        return String.format(getMessage(key), args);
    }

    public static String encode(String key, Object... args) {
        return format(key, args);
    }
}

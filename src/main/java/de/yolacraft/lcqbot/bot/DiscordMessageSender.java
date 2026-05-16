package de.yolacraft.lcqbot.bot;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class DiscordMessageSender {

    private static final int DISCORD_MESSAGE_LIMIT = 2000;

    private DiscordMessageSender() {
        // Utility class
    }

    public static List<String> splitMessage(String content) {
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> parts = new ArrayList<>();
        int offset = 0;

        while (offset < content.length()) {
            int end = Math.min(offset + DISCORD_MESSAGE_LIMIT, content.length());
            if (end == content.length()) {
                parts.add(content.substring(offset));
                break;
            }

            int splitAt = content.lastIndexOf('\n', end);
            if (splitAt <= offset) {
                splitAt = end;
            }

            parts.add(content.substring(offset, splitAt));
            offset = splitAt;
            if (offset < content.length() && content.charAt(offset) == '\n') {
                offset++;
            }
        }

        return parts;
    }

    public static void sendSplitTextMessages(TextChannel channel, String content, Consumer<List<String>> onComplete) {
        if (channel == null) {
            onComplete.accept(Collections.emptyList());
            return;
        }

        List<String> chunks = splitMessage(content);
        if (chunks.isEmpty()) {
            onComplete.accept(Collections.emptyList());
            return;
        }

        sendChunks(channel, chunks, 0, new ArrayList<>(), onComplete);
    }

    private static void sendChunks(TextChannel channel, List<String> chunks, int index, List<String> ids, Consumer<List<String>> onComplete) {
        if (index >= chunks.size()) {
            onComplete.accept(ids);
            return;
        }

        channel.sendMessage(chunks.get(index)).queue(message -> {
            ids.add(message.getId());
            sendChunks(channel, chunks, index + 1, ids, onComplete);
        }, error -> onComplete.accept(ids));
    }

    public static void deleteMessages(TextChannel channel, List<String> messageIds) {
        if (channel == null || messageIds == null) {
            return;
        }
        for (String id : messageIds) {
            if (id != null && !id.isBlank()) {
                channel.deleteMessageById(id).queue(null, ignored -> {
                });
            }
        }
    }
}

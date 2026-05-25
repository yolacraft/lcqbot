package de.yolacraft.lcqbot.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.yolacraft.lcqbot.model.api.ReactionRole;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
public class ReactionRoleFileService {

    private static final boolean DEBUG = false;

    private static final String DATA_DIR = "data/";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ReactionRoleFileService() {
        new File(DATA_DIR).mkdirs();
        log("Storage initialized at {}", DATA_DIR);
    }

    // ── DEBUG HELPER ─────────────────────────────────────────────

    private void log(String msg, Object... args) {
        if (!DEBUG) return;
        String formatted = (args.length > 0)
                ? String.format(msg.replace("{}", "%s"), args)
                : msg;
        System.out.println("[FileStorageService] " + formatted);
    }

    private File reactionRolesFile() {
        File f = new File(DATA_DIR + "reaction_roles.json");
        log("reactionRolesFile() -> {}", f.getPath());
        return f;
    }

    public List<ReactionRole> loadReactionRoles() {
        log("Loading reaction roles");
        return loadList(reactionRolesFile(), ReactionRole.class);
    }

    public Optional<ReactionRole> findReactionRoleByMessageId(String messageId) {
        log("Finding reaction role by messageId={}", messageId);

        return loadReactionRoles().stream()
                .filter(entry -> entry.getMessageId() != null && entry.getMessageId().equals(messageId))
                .findFirst();
    }

    public void saveReactionRole(ReactionRole reactionRole) {
        log("Saving reaction role messageId={}", reactionRole.getMessageId());

        List<ReactionRole> roles = loadReactionRoles();
        int before = roles.size();

        roles.removeIf(entry ->
                entry.getMessageId() != null &&
                        entry.getMessageId().equals(reactionRole.getMessageId()));

        roles.add(reactionRole);

        log("ReactionRoles updated: {} -> {}", before, roles.size());

        writeAtomically(reactionRolesFile(), roles);
    }

    public void deleteReactionRole(String messageId) {
        log("Deleting reaction role messageId={}", messageId);

        List<ReactionRole> roles = loadReactionRoles();

        boolean removed = roles.removeIf(entry ->
                entry.getMessageId() != null &&
                        entry.getMessageId().equals(messageId));

        log("ReactionRole removed? {}", removed);

        if (removed) {
            writeAtomically(reactionRolesFile(), roles);
        }
    }

    private <T> List<T> loadList(File file, Class<T> clazz) {
        log("loadList file={}", file.getName());

        if (!file.exists()) {
            log("File does not exist -> returning empty list");
            return new ArrayList<>();
        }

        try (Reader r = new FileReader(file)) {
            Type listType = TypeToken.getParameterized(List.class, clazz).getType();
            List<T> result = gson.fromJson(r, listType);

            log("Loaded {} entries from {}", result != null ? result.size() : 0, file.getName());

            return result != null ? result : new ArrayList<>();
        } catch (IOException e) {
            log("ERROR reading file {}", file.getName());
            throw new RuntimeException("Fehler beim Lesen: " + file.getName(), e);
        }
    }

    private void writeAtomically(File target, Object data) {
        log("writeAtomically -> {}", target.getName());

        File tmp = new File(target.getPath() + ".tmp");

        try (Writer w = new FileWriter(tmp)) {
            gson.toJson(data, w);
        } catch (IOException e) {
            log("ERROR writing temp file {}", tmp.getName());
            throw new RuntimeException("Fehler beim Schreiben: " + target.getName(), e);
        }

        if (!tmp.renameTo(target)) {
            log("rename failed -> fallback delete+rename");
            target.delete();
            tmp.renameTo(target);
        }

        log("Write successful -> {}", target.getName());
    }
}
package de.yolacraft.lcqbot.model.api;

import java.util.List;

public class McsrResponse {
    public String status;
    public McsrData data;

    public static class McsrData {
        public String status; // idle, counting, generate, ready, running, done
        public int type;
        public long time;
        public List<McsrPlayer> players;
        public List<McsrCompletion> completions;
    }

    public static class McsrPlayer {
        public String uuid;
        public String nickname;
    }

    public static class McsrCompletion {
        public String uuid;
        public long time;
    }
}
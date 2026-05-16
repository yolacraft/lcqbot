package de.yolacraft.lcqbot.bot.commandHandler;

import de.yolacraft.lcqbot.bot.DiscordMessageSender;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class TempSendHandler {
    public TempSendHandler(){

    }
    public void handle(SlashCommandInteractionEvent interaction) {
        StringBuilder sb = new StringBuilder("**").append("Leaderboard 7/7 Seeds - Top 12").append("**\n\n");

        sb.append("**").append(1).append(".** ").append("ulqtfan2").append(" - ").append("60").append(" Punkte\n");
        sb.append("**").append(2).append(".** ").append("rqichu").append(" - ").append("58").append(" Punkte\n");
        sb.append("**").append(3).append(".** ").append("cornflakesmcsr").append(" - ").append("53").append(" Punkte\n");
        sb.append("**").append(4).append(".** ").append("TheGamerGuyy").append(" - ").append("52").append(" Punkte\n");
        sb.append("**").append(5).append(".** ").append("Blad_d").append(" - ").append("37").append(" Punkte\n");
        sb.append("**").append(6).append(".** ").append("Finnitzko").append(" - ").append("36").append(" Punkte\n");
        sb.append("**").append(7).append(".** ").append("CallMeBlumi").append(" - ").append("33").append(" Punkte\n");
        sb.append("**").append(8).append(".** ").append("ulqt").append(" - ").append("26").append(" Punkte\n");
        sb.append("**").append(9).append(".** ").append("Anderdrache").append(" - ").append("17").append(" Punkte\n");
        sb.append("**").append(10).append(".** ").append("Farbwechsler").append(" - ").append("17").append(" Punkte\n");
        sb.append("**").append(11).append(".** ").append("Th1tme0").append(" - ").append("17").append(" Punkte\n");
        sb.append("**").append(12).append(".** ").append("logatoboot").append(" - ").append("15").append(" Punkte\n");

        DiscordMessageSender.sendSplitTextMessages(interaction.getChannel().asTextChannel(), sb.toString(), i -> {

        });
    }
}

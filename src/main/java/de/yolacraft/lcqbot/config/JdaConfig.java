package de.yolacraft.lcqbot.config;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class JdaConfig {

    @Value("${discord.bot.token}")
    private String botToken;

    private JDA jdaInstance;

    @Bean
    public JDA jda(ObjectProvider<List<ListenerAdapter>> listenersProvider) throws InterruptedException {
        jdaInstance = JDABuilder.createDefault(botToken)
                .build()
                .awaitReady();

        listenersProvider.ifAvailable(listeners -> listeners.forEach(jdaInstance::addEventListener));
        return jdaInstance;
    }

    public void shutdownJda() {
        if (jdaInstance != null) {
            jdaInstance.shutdown();
        }
    }
}

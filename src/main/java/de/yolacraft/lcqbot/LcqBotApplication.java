package de.yolacraft.lcqbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import de.yolacraft.lcqbot.config.JdaConfig;

@SpringBootApplication
public class LcqBotApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(LcqBotApplication.class, args);
        JdaConfig jdaConfig = context.getBean(JdaConfig.class);

        Runtime.getRuntime().addShutdownHook(new Thread(jdaConfig::shutdownJda));
    }

}

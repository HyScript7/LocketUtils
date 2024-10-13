package io.github.hyscript7.locketutils.config;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

@Slf4j
@Configuration
public class JDAConfiguration {
    private final LocketutilsConfiguration locketutilsConfiguration;
    private final ListenerAdapter[] listeners;
    private JDA jda;

    public JDAConfiguration(LocketutilsConfiguration locketutilsConfiguration, ListenerAdapter[] listeners) {
        this.listeners = listeners;
        this.locketutilsConfiguration = locketutilsConfiguration;
    }

    @Bean
    public JDABuilder jdaBuilder() {
        JDABuilder jdaBuilder = JDABuilder
                                .createDefault(locketutilsConfiguration.getBot().getToken())
                                .setActivity(Activity.watching("the whitelist"))
                                .enableIntents(EnumSet.allOf(GatewayIntent.class));
        Arrays.stream(listeners).forEach(jdaBuilder::addEventListeners);
        return jdaBuilder;
    }

    @Bean
    @Autowired
    public JDA jda(JDABuilder jdaBuilder) {
        this.jda = jdaBuilder.build();
        return this.jda;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (jda != null) {
            log.info("Shutting down JDA...");
            jda.shutdown();
            try {
                if (!jda.awaitShutdown(10, TimeUnit.SECONDS)) {
                    log.warn("JDA did not shut down gracefully within 10 seconds!");
                }
            } catch (InterruptedException e) {
                log.error("Thread interrupt received while shutting down JDA!", e);
                throw new InterruptedException(e.getMessage());
            }
        }
    }
}

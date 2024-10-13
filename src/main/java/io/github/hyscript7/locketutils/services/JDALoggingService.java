package io.github.hyscript7.locketutils.services;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import io.github.hyscript7.locketutils.config.LocketutilsConfiguration;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;

@Slf4j
@Service
public class JDALoggingService {
    long logGuildId;
    long logChannelId;
    public JDALoggingService(LocketutilsConfiguration locketutilsConfiguration) {
        this.logGuildId = locketutilsConfiguration.getBot().getGuildid();
        this.logChannelId = locketutilsConfiguration.getBot().getLogchannelid();
    }
    public void log(JDA jda, String title, String message, Field... fields) {
        log(jda, title, message, 0xFFFFFF, fields);
    }

    public void log(JDA jda, String title, String message, int color, Field... fields) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(message);
        embedBuilder.setColor(color);
        Arrays.stream(fields).forEach(field -> embedBuilder.addField(field));

        jda.getGuildById(logGuildId).getTextChannelById(logChannelId).sendMessageEmbeds(embedBuilder.build()).queue();
    }
}

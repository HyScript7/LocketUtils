package io.github.hyscript7.locketutils.listeners;

import org.springframework.stereotype.Component;

import io.github.hyscript7.locketutils.config.LocketutilsConfiguration;
import io.github.hyscript7.locketutils.services.JDAWhitelistService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
@Component
public class OnMemberJoinListener extends ListenerAdapter {
    private final JDAWhitelistService jdaWhitelistService;
    private final long whitelistedRoleId;

    public OnMemberJoinListener(JDAWhitelistService jdaWhitelistService, LocketutilsConfiguration locketutilsConfiguration) {
        this.jdaWhitelistService = jdaWhitelistService;
        this.whitelistedRoleId = locketutilsConfiguration.getBot().getWhitelistedrole();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        log.info("User {} joined the server.", event.getMember().getUser().getName());
        jdaWhitelistService.authenticate(event.getMember(), event.getJDA(), event.getGuild(), whitelistedRoleId);
    }
}

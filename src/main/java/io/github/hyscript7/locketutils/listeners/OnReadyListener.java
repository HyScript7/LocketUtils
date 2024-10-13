package io.github.hyscript7.locketutils.listeners;

import org.springframework.stereotype.Component;

import io.github.hyscript7.locketutils.commands.ICommand;
import io.github.hyscript7.locketutils.config.LocketutilsConfiguration;
import io.github.hyscript7.locketutils.services.JDAWhitelistService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

@Slf4j
@Component
public class OnReadyListener extends ListenerAdapter {
    private ICommand[] commands;
    private final JDAWhitelistService jdaWhitelistService;
    private final LocketutilsConfiguration locketutilsConfiguration;

    public OnReadyListener(ICommand[] commands, JDAWhitelistService jdaWhitelistService, LocketutilsConfiguration locketutilsConfiguration) {
        this.commands = commands;
        this.jdaWhitelistService = jdaWhitelistService;
        this.locketutilsConfiguration = locketutilsConfiguration;
    }

    @Override
    public void onReady(ReadyEvent event) {
        log.info("JDA is ready!");
        log.info("Retrospectively re-authorizing all members");
        jdaWhitelistService.authenticateAll(event.getJDA(), event.getJDA().getGuildById(locketutilsConfiguration.getBot().getGuildid()), locketutilsConfiguration.getBot().getWhitelistedrole());
        log.info("Building command update action");
        CommandListUpdateAction action = event.getJDA().updateCommands();
        for (ICommand command : commands) {
            action.addCommands(command.getSlashCommandData());
        }
        action.queue();
        log.info("Command update action sent");
    }
}

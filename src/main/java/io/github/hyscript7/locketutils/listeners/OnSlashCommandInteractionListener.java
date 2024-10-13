package io.github.hyscript7.locketutils.listeners;

import org.springframework.stereotype.Component;

import io.github.hyscript7.locketutils.commands.ICommand;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Slf4j
@Component
public class OnSlashCommandInteractionListener extends ListenerAdapter {

    private ICommand[] commands;
    public OnSlashCommandInteractionListener(ICommand[] commands) {
        this.commands = commands;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        for (ICommand command : commands) {
            if (command.getSlashCommandData().getName().equals(event.getName())) {
                log.info("Executing command {}", event.getName());
                command.execute(event);
                return;
            }
        }
        log.warn("Command {} not found!", event.getName());
        event.getInteraction().getHook().editOriginal("Command not found!").queue();
    }
}

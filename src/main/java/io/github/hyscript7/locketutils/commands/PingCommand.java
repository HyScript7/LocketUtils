package io.github.hyscript7.locketutils.commands;

import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Component
public class PingCommand implements ICommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.getHook().editOriginal("Pong!").queue();
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("ping", "Replies with Pong!");
    }
} 

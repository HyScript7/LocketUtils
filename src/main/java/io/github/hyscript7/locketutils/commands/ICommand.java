package io.github.hyscript7.locketutils.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ICommand {
    void execute(SlashCommandInteractionEvent event);

    SlashCommandData getSlashCommandData();
}

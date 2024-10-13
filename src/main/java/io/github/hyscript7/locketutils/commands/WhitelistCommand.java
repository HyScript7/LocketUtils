package io.github.hyscript7.locketutils.commands;

import org.springframework.stereotype.Component;

import io.github.hyscript7.locketutils.config.LocketutilsConfiguration;
import io.github.hyscript7.locketutils.data.services.WhitelistService;
import io.github.hyscript7.locketutils.services.JDALoggingService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Slf4j
@Component
public class WhitelistCommand implements ICommand {
    private WhitelistService whitelistService;
    private JDALoggingService jdaLoggingService;
    private long adminRoleId;

    public WhitelistCommand(WhitelistService whitelistService, JDALoggingService jdaLoggingService,
            LocketutilsConfiguration locketutilsConfiguration) {
        this.whitelistService = whitelistService;
        this.jdaLoggingService = jdaLoggingService;
        this.adminRoleId = locketutilsConfiguration.getBot().getAdminroleid();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Role adminRole = event.getGuild().getRoleById(adminRoleId);
        if (adminRole == null) {
            log.error("Admin role with id {} not found!", adminRoleId);
        }
        if (!(event.getMember().getRoles().contains(adminRole)
                || event.getMember().hasPermission(Permission.ADMINISTRATOR)
                || event.getMember().hasPermission(Permission.MODERATE_MEMBERS))) {
            event.getHook().sendMessage("You do not have permission to use this command!").queue();
            return;
        }
        switch (event.getSubcommandName()) {
            case "add":
                User user = event.getOption("user").getAsUser();
                OptionMapping reasonOption = event.getOption("reason");
                String reason = reasonOption != null ? reasonOption.getAsString() : "No reason specified.";
                log.info("Reason: {}", reason);
                if (whitelistService.isWhitelisted(user)) {
                    event.getHook().sendMessage("User is already whitelisted").queue();
                    return;
                }
                whitelistService.addToWhitelist(user, event.getUser());
                jdaLoggingService.log(event.getJDA(), "Whitelist Add",
                        "User " + user.getAsMention() + " (" + user.getId() + ")" + " was added to the whitelist by "
                                + event.getUser().getAsMention() + "(" + event.getUser().getId() + ")");
                event.getHook().sendMessage("User added to the whitelist").queue();
                break;
            case "remove":
                user = event.getOption("user").getAsUser();
                if (!whitelistService.isWhitelisted(user)) {
                    event.getHook().sendMessage("User is not whitelisted").queue();
                    return;
                }
                whitelistService.removeFromWhitelist(user);
                jdaLoggingService.log(event.getJDA(), "Whitelist Remove",
                        "User " + user.getAsMention() + " (" + user.getId() + ")"
                                + " was removed from the whitelist by " + event.getUser().getAsMention() + "("
                                + event.getUser().getId() + ")");
                event.getHook().sendMessage("User removed from the whitelist").queue();
                event.getGuild().kick(user).reason("Removed from whitelist").queue();
                break;
            case "list":
                // TODO: Implement
                int page = event.getOption("page") != null ? event.getOption("page").getAsInt() : 1;
                event.getHook().sendMessage("Not yet implemented! Your specified page: " + page).queue();
                break;
            case "reason":
                user = event.getOption("user").getAsUser();
                OptionMapping newReasonOption = event.getOption("reason");
                if (newReasonOption == null) {
                    String oldReason = whitelistService.getReason(user);
                    event.getHook()
                            .sendMessage(user.getAsMention() + " has been whitelisted by "
                                    + event.getJDA().getUserById(whitelistService.getAddedBy(user)).getAsMention()
                                    + " with reason: `" + oldReason + "`.")
                            .queue();
                    return;
                }
                String newReason = newReasonOption.getAsString();
                whitelistService.setReason(user, newReason);
                event.getHook().sendMessage("Reason set to: " + newReason).queue();
                break;
            default:
                event.getHook().sendMessage("Unknown subcommand: " + event.getSubcommandName()).queue();
                break;
        }
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("whitelist", "Manages the whitelist").addSubcommands(
                new SubcommandData("add", "Adds a user to the whitelist").addOption(OptionType.USER, "user",
                        "The user to add", true).addOption(OptionType.STRING, "reason", "The whitelist reason", false),
                new SubcommandData("remove", "Removes a user from the whitelist").addOption(OptionType.USER, "user",
                        "The user to remove", true),
                new SubcommandData("list", "Lists all whitelisted users").addOption(OptionType.INTEGER, "page",
                        "The page number", false),
                new SubcommandData("reason", "Let's you view or change the reason someone was whitelisted")
                        .addOption(OptionType.USER, "user", "The user to work with", true)
                        .addOption(OptionType.STRING, "reason", "The new reason", false));
    }
}

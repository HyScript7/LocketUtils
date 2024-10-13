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
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Slf4j
@Component
public class RoleCommand implements ICommand {
    private WhitelistService whitelistService;
    private JDALoggingService jdaLoggingService;
    private long adminRoleId;
    private long operatorRoleId;

    public RoleCommand(WhitelistService whitelistService, JDALoggingService jdaLoggingService,
            LocketutilsConfiguration locketutilsConfiguration) {
        this.whitelistService = whitelistService;
        this.jdaLoggingService = jdaLoggingService;
        this.adminRoleId = locketutilsConfiguration.getBot().getAdminroleid();
        this.operatorRoleId = locketutilsConfiguration.getBot().getOperatorroleid();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Role adminRole = event.getGuild().getRoleById(adminRoleId);
        Role operatorRole = event.getGuild().getRoleById(operatorRoleId);
        if (adminRole == null) {
            log.error("Admin role with id {} not found!", adminRoleId);
        }
        if (operatorRole == null) {
            log.error("Operator role with id {} not found!", operatorRoleId);
        }
        if (!((event.getMember().getRoles().contains(adminRole)
                || event.getMember().hasPermission(Permission.ADMINISTRATOR)
                || event.getMember().hasPermission(Permission.MODERATE_MEMBERS)
                || event.getMember().getRoles().contains(operatorRole))
                && event.getMember().hasPermission(Permission.MANAGE_ROLES))) {
            event.getHook().sendMessage("You do not have permission to use this command!").queue();
            return;
        }
        switch (event.getSubcommandName()) {
            case "add":
                User user = event.getOption("user").getAsUser();
                Role role = event.getOption("role").getAsRole();
                if (!whitelistService.isWhitelisted(user)) {
                    event.getHook().sendMessage("User is not whitelisted").queue();
                    return;
                }
                if (whitelistService.hasRole(user, role)) {
                    event.getHook().sendMessage("User already has this role").queue();
                    return;
                }
                whitelistService.addRole(user, role);
                jdaLoggingService.log(event.getJDA(), "Whitelist Update",
                        "User " + user.getAsMention() + " (" + user.getId() + ")" + " was granted the role "
                                + role.getAsMention() + " by "
                                + event.getUser().getAsMention() + "(" + event.getUser().getId() + ")");
                event.getHook().sendMessage("Role successfully granted to user!").queue();
                if (event.getGuild().getMember(user) != null) {
                    try {
                        event.getGuild().addRoleToMember(user, role).queue();
                    } catch (Exception e) {
                        log.warn("Could not add role {} ({}) to user {} ({})", role.getName(), role.getId(),
                                user.getName(), user.getId(), e);
                    }
                }
                break;
            case "remove":
                user = event.getOption("user").getAsUser();
                role = event.getOption("role").getAsRole();
                if (!whitelistService.isWhitelisted(user)) {
                    event.getHook().sendMessage("User is not whitelisted").queue();
                    return;
                }
                jdaLoggingService.log(event.getJDA(), "Whitelist Update",
                        "User " + user.getAsMention() + " (" + user.getId() + ")" + " was rvoked the role "
                                + role.getAsMention() + " by " + event.getUser().getAsMention() + "("
                                + event.getUser().getId() + ")");
                whitelistService.removeRole(user, role);
                event.getHook().sendMessage("Role successfully revoked from the user!").queue();
                if (event.getGuild().getMember(user) != null
                        && event.getGuild().getMember(user).getRoles().contains(role)) {
                    try {

                        event.getGuild().removeRoleFromMember(user, role).queue();
                    } catch (Exception e) {
                        log.warn("Could not revoke role {} ({}) from user {} ({})", role.getName(), role.getId(),
                                user.getName(), user.getId(), e);
                    }
                }
                break;
            case "list":
                // TODO: Implement
                int page = event.getOption("page") != null ? event.getOption("page").getAsInt() : 1;
                user = event.getOption("user").getAsUser();
                event.getHook().sendMessage("Not yet implemented! You want to view user " + user.getAsMention()
                        + "'s roles on your specified page: " + page).queue();
                break;
            default:
                event.getHook().sendMessage("Unknown subcommand: " + event.getSubcommandName()).queue();
                break;
        }
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("role", "Manages the assigned roles").addSubcommands(
                new SubcommandData("add", "Grants a role to a user").addOption(OptionType.USER, "user",
                        "The user to modify", true).addOption(OptionType.ROLE, "role", "The role to grant", true),
                new SubcommandData("remove", "Revokes a role from a user").addOption(OptionType.USER, "user",
                        "The user to modify", true).addOption(OptionType.ROLE, "role", "The role to revoke", true),
                new SubcommandData("list", "Lists all roles the user has").addOption(OptionType.INTEGER, "page",
                        "The page number", false));
    }
}

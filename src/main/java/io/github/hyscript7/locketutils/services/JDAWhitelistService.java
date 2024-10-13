package io.github.hyscript7.locketutils.services;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import io.github.hyscript7.locketutils.data.services.WhitelistService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

/**
 * Behold, horrors beyond programmer comprehension:
 * @author hyscript7
 */
@Slf4j
@Service
public class JDAWhitelistService {
    private final WhitelistService whitelistService;
    private final JDALoggingService jdaLoggingService;

    public JDAWhitelistService(WhitelistService whitelistService, JDALoggingService jdaLoggingService) {
        this.whitelistService = whitelistService;
        this.jdaLoggingService = jdaLoggingService;
    }

    public void authenticate(Member member, JDA jda, Guild guild, long whitelistedRoleId) {
        if (!guild.getMember(jda.getSelfUser()).canInteract(member)) {
            log.warn("Cannot interact with user {} ({}), skipping authentication.", member.getUser().getName(),
                    member.getUser().getId());
            if (!whitelistService.isWhitelisted(member.getUser())) {
                jdaLoggingService.log(jda, "Whitelist Authenticator",
                        "User " + member.getUser().getAsMention() + " (" + member.getUser().getId()
                                + ")"
                                + " is not whitelisted, but the authenticator cannot interact with them.\nPlease, resolve this issue or kick the user manually.",
                        0xFFFF00);
            }
            return;
        }
        Role whitelistedRole = guild.getRoleById(whitelistedRoleId);
        if (whitelistService.isWhitelisted(member.getUser())) {
            if (member.getRoles().contains(whitelistedRole)) {
                log.info("User already has the whitelist role", member.getUser().getName());
                return;
            }
            log.info("User {} was whitelisted by {}.", member.getUser().getName());
            User whitelistedBy = guild
                    .getMemberById(whitelistService.getAddedBy(member.getUser())).getUser();
            jdaLoggingService.log(jda, "Whitelist Authenticator",
                    "User " + member.getUser().getAsMention() + " (" + member.getUser().getId()
                            + ")" + " authenticated in acordance with the whitelist.",
                    0x00FF00,
                    new Field("Whitelisted By", whitelistedBy.getAsMention() + " ("
                            + whitelistedBy.getId() + ")", false),
                    new Field("Whitelisted Since", whitelistService.getAddedAt(member.getUser()).toString(),
                            false));
            Arrays.stream(whitelistService.getRoleIds(member.getUser()))
                    .forEach(roleId -> guild
                            .addRoleToMember(member, guild.getRoleById(roleId)).queue());
            guild.addRoleToMember(member, whitelistedRole).queue();
            try {
                member.getUser().openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage("").addEmbeds(new EmbedBuilder()
                            .setTitle("Whitelist Authenticator").setAuthor(guild.getName(), guild.getIconUrl())
                            .setDescription("# Whitelist authentication successfull!\nYou have been whitelisted by "
                                    + whitelistedBy.getAsMention() + "(" + whitelistedBy.getId() + ")"
                                    + "with reason:\n```" + whitelistService.getReason(member.getUser()).stripTrailing()
                                    + "\n```")
                            .setColor(0x00FF00).setFooter("LocketUtils Whitelist Authenticator").build()))
                    .queue();
            } catch (Exception e) {
                log.warn("Cannot send message to user {}", member.getUser().getName(), e);
            }
        } else {
            if (member.getRoles().contains(whitelistedRole)) {
                log.info("User had the whitelist role, but is no longer whitelisted", member.getUser().getName());
                return;
            } else {
                log.info("User {} was not whitelisted.", member.getUser().getName());
            }
            jdaLoggingService.log(jda, "Whitelist Authenticator",
                    "User " + member.getUser().getAsMention() + " (" + member.getUser().getId()
                            + ")" + " was denied access in acordance with the whitelist.",
                    0xFF0000);
            try {
                member.getUser().openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage("").addEmbeds(new EmbedBuilder()
                            .setTitle("Whitelist Authenticator").setAuthor(guild.getName(), guild.getIconUrl())
                            .setDescription(
                                    "# You are not whitelisted!\nIf you wish to join this guild, please have an existing member ask an administrator to whitelist you!")
                            .setColor(0xFF0000).setFooter("LocketUtils Whitelist Authenticator").build()))
                    .queue();
            } catch (Exception e) {
                log.warn("Cannot send message to user {}", member.getUser().getName(), e);
            }
            member.kick().reason("Not whitelisted").queue();
        }
    }

    public void authenticateAll(JDA jda, Guild guild, long whitelistedRoleId) {
        guild.loadMembers();
        guild.getMembers().forEach(member -> authenticate(member, jda, guild, whitelistedRoleId));
    }
}

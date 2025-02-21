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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

/**
 * Behold, horrors beyond programmer comprehension:
 * 
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
        if (!canInteractWithMember(member, jda, guild)) {
            handleNonInteractableMember(member, jda);
            return;
        }

        Role whitelistedRole = guild.getRoleById(whitelistedRoleId);
        if (whitelistService.isWhitelisted(member.getUser())) {
            authenticateWhitelistedMember(member, jda, guild, whitelistedRole);
        } else {
            handleNonWhitelistedMember(member, jda, guild, whitelistedRole);
        }
    }

    private boolean canInteractWithMember(Member member, JDA jda, Guild guild) {
        return guild.getMember(jda.getSelfUser()).canInteract(member);
    }

    private void handleNonInteractableMember(Member member, JDA jda) {
        log.warn("Cannot interact with user {} ({}), skipping authentication.",
                member.getUser().getName(), member.getUser().getId());
        if (!whitelistService.isWhitelisted(member.getUser())) {
            jdaLoggingService.log(jda, "Whitelist Authenticator",
                    "User " + member.getUser().getAsMention() + " (" + member.getUser().getId() + ")"
                            + " is not whitelisted, but the authenticator cannot interact with them.\n"
                            + "Please, resolve this issue or kick the user manually.",
                    0xFFFF00);
        }
    }

    private void authenticateWhitelistedMember(Member member, JDA jda, Guild guild, Role whitelistedRole) {
        if (member.getRoles().contains(whitelistedRole)) {
            log.info("User {} already has the whitelist role", member.getUser().getName());
            return;
        }

        User whitelistedBy = getWhitelistedByUser(member, guild);

        if (whitelistedBy == null) {
            log.info("User {} was whitelisted by an unknown member. (Failed to fetch)", member.getUser().getName());

            jdaLoggingService.log(jda, "Whitelist Authenticator",
                    "User " + member.getUser().getAsMention() + " (" + member.getUser().getId() + ")"
                            + " authenticated in accordance with the whitelist.",
                    0x00FF00,
                    new Field("Whitelisted By", "Error fetching member", false),
                    new Field("Whitelisted Since", whitelistService.getAddedAt(member.getUser()).toString(), false));

        } else {
            log.info("User {} was whitelisted by {}.", member.getUser().getName(), whitelistedBy.getName());

            jdaLoggingService.log(jda, "Whitelist Authenticator",
                    "User " + member.getUser().getAsMention() + " (" + member.getUser().getId() + ")"
                            + " authenticated in accordance with the whitelist.",
                    0x00FF00,
                    new Field("Whitelisted By", whitelistedBy.getAsMention() + " (" + whitelistedBy.getId() + ")",
                            false),
                    new Field("Whitelisted Since", whitelistService.getAddedAt(member.getUser()).toString(), false));

        }

        assignRoles(member, guild);
        sendWhitelistedMessage(member, guild, whitelistedBy);
    }

    private User getWhitelistedByUser(Member member, Guild guild) {
        long addedById = whitelistService.getAddedBy(member.getUser());
        log.warn("Possibly blocking thread " + Thread.currentThread().getName());
        Member whitelistedByGuildMember = guild.retrieveMemberById(addedById).complete();
        return whitelistedByGuildMember != null ? whitelistedByGuildMember.getUser() : null;
    }

    private void assignRoles(Member member, Guild guild) {
        Arrays.stream(whitelistService.getRoleIds(member.getUser()))
                .forEach(roleId -> guild.addRoleToMember(member, guild.getRoleById(roleId)).queue());
    }

    private void sendWhitelistedMessage(Member member, Guild guild, User whitelistedBy) {
        try {
            member.getUser().openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage("")
                            .addEmbeds(new EmbedBuilder()
                                    .setTitle("Whitelist Authenticator")
                                    .setAuthor(guild.getName(), guild.getIconUrl())
                                    .setDescription("# Whitelist authentication successful!\n"
                                            + "You have been whitelisted by " + whitelistedBy.getAsMention()
                                            + "(" + whitelistedBy.getId() + ") with reason:\n```"
                                            + whitelistService.getReason(member.getUser()).stripTrailing() + "\n```")
                                    .setColor(0x00FF00)
                                    .setFooter("LocketUtils Whitelist Authenticator")
                                    .build()))
                    .queue();
        } catch (Exception e) {
            log.warn("Cannot send message to user {}", member.getUser().getName(), e);
        }
    }

    private void handleNonWhitelistedMember(Member member, JDA jda, Guild guild, Role whitelistedRole) {
        if (member.getRoles().contains(whitelistedRole)) {
            log.info("User {} had the whitelist role, but is no longer whitelisted", member.getUser().getName());
            return;
        }

        log.info("User {} was not whitelisted.", member.getUser().getName());
        jdaLoggingService.log(jda, "Whitelist Authenticator",
                "User " + member.getUser().getAsMention() + " (" + member.getUser().getId() + ")"
                        + " was denied access in accordance with the whitelist.",
                0xFF0000);

        sendNotWhitelistedMessage(member, guild);
        member.kick().reason("Not whitelisted").queue();
    }

    private void sendNotWhitelistedMessage(Member member, Guild guild) {
        try {
            member.getUser().openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage("")
                            .addEmbeds(new EmbedBuilder()
                                    .setTitle("Whitelist Authenticator")
                                    .setAuthor(guild.getName(), guild.getIconUrl())
                                    .setDescription("# You are not whitelisted!\n"
                                            + "If you wish to join this guild, please have an existing member ask an administrator to whitelist you!")
                                    .setColor(0xFF0000)
                                    .setFooter("LocketUtils Whitelist Authenticator")
                                    .build()))
                    .queue();
        } catch (Exception e) {
            log.warn("Cannot send message to user {}", member.getUser().getName(), e);
        }
    }

    public void authenticateAll(JDA jda, Guild guild, long whitelistedRoleId) {
        guild.loadMembers();
        guild.getMembers().forEach(member -> authenticate(member, jda, guild, whitelistedRoleId));
    }
}

package io.github.hyscript7.locketutils.data.services;

import java.util.Date;

import io.github.hyscript7.locketutils.data.models.WhitelistModel;
import io.github.hyscript7.locketutils.data.repositories.WhitelistRepository;

import org.springframework.stereotype.Service;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

@Service
public class WhitelistService {
    private WhitelistRepository whitelistRepository;

    public WhitelistService(WhitelistRepository whitelistRepository) {
        this.whitelistRepository = whitelistRepository;
    }

    public boolean isWhitelisted(User user) {
        return whitelistRepository.existsById(user.getIdLong());
    }

    public void addToWhitelist(User user, User auditor) {
        WhitelistModel whitelist = new WhitelistModel(user.getIdLong(), auditor.getIdLong());
        whitelistRepository.save(whitelist);
    }

    public void addToWhitelistWithReason(User user, User auditor, String reason) {
        WhitelistModel whitelist = new WhitelistModel(user.getIdLong(), auditor.getIdLong());
        whitelist.setReason(reason);
        whitelistRepository.save(whitelist);
    }

    public void removeFromWhitelist(User user) {
        WhitelistModel whitelist = whitelistRepository.findById(user.getIdLong());
        if (whitelist != null) {
            whitelistRepository.delete(whitelist);
        }
    }

    public void setReason(User user, String reason) {
        WhitelistModel whitelist = whitelistRepository.findById(user.getIdLong());
        if (whitelist != null) {
            whitelist.setReason(reason);
            whitelistRepository.save(whitelist);
        }
    }

    public void addRole(User user, Role role) {
        WhitelistModel whitelist = whitelistRepository.findById(user.getIdLong());
        if (whitelist != null) {
            whitelist.addRoleId(role.getIdLong());
            whitelistRepository.save(whitelist);
        }
    }

    public void removeRole(User user, Role role) {
        WhitelistModel whitelist = whitelistRepository.findById(user.getIdLong());
        if (whitelist != null) {
            whitelist.removeRoleId(role.getIdLong());
            whitelistRepository.save(whitelist);
        }
    }

    public boolean hasRole(User user, Role role) {
        WhitelistModel whitelist = whitelistRepository.findById(user.getIdLong());
        if (whitelist != null) {
            return whitelist.getRoleIds().contains(role.getIdLong());
        }
        return false;
    }

    public String getReason(User user) {
        WhitelistModel whitelist = whitelistRepository.findById(user.getIdLong());
        if (whitelist != null) {
            return whitelist.getReason();
        }
        return null;
    }

    public long[] getRoleIds(User user) {
        WhitelistModel whitelist = whitelistRepository.findById(user.getIdLong());
        if (whitelist != null) {
            return whitelist.getRoleIds().stream().mapToLong(Long::longValue).toArray();
        }
        return new long[0];
    }

    public Date getAddedAt(User user) {
        WhitelistModel whitelist = whitelistRepository.findById(user.getIdLong());
        if (whitelist != null) {
            return whitelist.getAddedAt();
        }
        return null;
    }

    public Date getUpdatedAt(User user) {
        WhitelistModel whitelist = whitelistRepository.findById(user.getIdLong());
        if (whitelist != null) {
            return whitelist.getUpdatedAt();
        }
        return null;
    }

    public long getAddedBy(User user) {
        WhitelistModel whitelist = whitelistRepository.findById(user.getIdLong());
        if (whitelist != null) {
            return whitelist.getAddedBy();
        }
        return 0;
    }

    public long count() {
        return whitelistRepository.count();
    }
}

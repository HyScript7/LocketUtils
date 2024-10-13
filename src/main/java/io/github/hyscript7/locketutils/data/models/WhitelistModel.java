package io.github.hyscript7.locketutils.data.models;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;

@Entity
@Getter
public class WhitelistModel {
    @Id
    private long id;

    private long addedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date addedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    private @Nullable String reason;

    // private long[] roleIds;
    private List<Long> roleIds;

    public WhitelistModel() {
    };

    public WhitelistModel(long id, long addedBy) {
        this.id = id;
        this.addedBy = addedBy;
        this.addedAt = new Date();
        this.updatedAt = (Date) this.addedAt.clone();
        // this.roleIds = new long[0];
        this.roleIds = List.of();
        this.reason = null;
    }

    public void setReason(String reason) {
        this.reason = reason;
        this.updatedAt = new Date();
    }

    /*
    public void addRoleId(long roleId) {
        List<Long> roleIdsList = Arrays.stream(this.roleIds).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        roleIdsList.add(roleId);
        this.roleIds = roleIdsList.stream().mapToLong(l -> l).toArray();
        this.updatedAt = new Date();
    }

    public void removeRoleId(long roleId) {
        this.roleIds = Arrays.stream(this.roleIds).filter(l -> l != roleId).toArray();
        this.updatedAt = new Date();
    }
    */

    public void addRoleId(long roleId) {
        this.roleIds.add(roleId);
    }

    public void removeRoleId(long roleId) {
        this.roleIds.remove(Long.valueOf(roleId));
    }
}

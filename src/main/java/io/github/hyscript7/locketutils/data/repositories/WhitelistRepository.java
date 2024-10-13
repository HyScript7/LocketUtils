package io.github.hyscript7.locketutils.data.repositories;

import java.util.List;

import org.springframework.data.repository.Repository;

import io.github.hyscript7.locketutils.data.models.WhitelistModel;
import jakarta.annotation.Nullable;

public interface WhitelistRepository extends Repository<WhitelistModel, Long> {
    WhitelistModel save(WhitelistModel whitelistModel);

    @Nullable WhitelistModel findById(long id);

    List<WhitelistModel> findByAddedBy(long addedBy);

    Iterable<WhitelistModel> findAll();

    long count();

    void delete(WhitelistModel whitelistModel);

    boolean existsById(long id);
}

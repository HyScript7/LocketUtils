package io.github.hyscript7.locketutils.data.repositories;

import java.util.List;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import io.github.hyscript7.locketutils.data.models.WhitelistModel;

public interface PagingWhitelistRepository extends PagingAndSortingRepository<WhitelistModel, Long> {
}

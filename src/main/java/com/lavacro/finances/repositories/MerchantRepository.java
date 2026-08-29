package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.EntityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<EntityEntity, Integer> {
}

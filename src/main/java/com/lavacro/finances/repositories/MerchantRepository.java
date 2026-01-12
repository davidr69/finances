package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.EntityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<EntityEntity, Integer> {
	@Query(value = """
			SELECT id, acct, description, address
			FROM entities
			ORDER BY LOWER(description)
	""", nativeQuery = true)
	List<EntityEntity> findAllOrderByDescriptionAsc();
}

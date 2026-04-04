package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.ActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<ActionEntity, Integer> {
	@Modifying
	@Transactional
	@Query(value = "UPDATE action SET visible = 't' WHERE sequence IN :visible_list", nativeQuery = true)
	void setVisibleTrue(@Param("visible_list") final List<Integer> visibleList);

	@Modifying
	@Transactional
	@Query(value = "UPDATE action SET visible = 'f' WHERE sequence IN :visible_list", nativeQuery = true)
	void removeVisibleTrue(@Param("visible_list") final List<Integer> visibleList);

	@Modifying
	@Transactional
	@Query(value = "UPDATE action SET reconciled = 't', visible = 't' WHERE sequence IN :reconcile_list", nativeQuery = true)
	void reconcile(@Param("reconcile_list") final List<Integer> reconcileList);
}

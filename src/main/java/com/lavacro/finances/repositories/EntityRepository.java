package com.lavacro.finances.repositories;

import java.util.List;

import com.lavacro.finances.entities.EntityByYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityRepository extends JpaRepository<EntityByYear, Integer> {
	@Query(value = """
		SELECT a."sequence", amount, mydate, description, reference,
			SUM(amount) OVER(PARTITION BY entity ORDER BY description, mydate) AS total
		FROM action a
		JOIN entities e ON a.entity = e.id
		WHERE date_part('year', mydate) = :year AND account = :account
		ORDER BY description, mydate
	""", nativeQuery = true)
	List<EntityByYear> byEntity(final Integer year, final Integer account);
}

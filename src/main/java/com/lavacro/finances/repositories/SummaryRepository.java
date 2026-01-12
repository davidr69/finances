package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.EntitySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SummaryRepository extends JpaRepository<EntitySummary, Integer> {
	@Query(value = """
			WITH x AS (
				SELECT e.description AS entity, SUM(a.amount) AS amount,
					TO_CHAR(SUM(a.amount), '999,999,999D99') AS money,
					CAST(DATE_PART('year', a.mydate) AS INTEGER) AS theyear
				FROM action a
				JOIN entities e ON a.entity = e.id
				WHERE a.account = :account AND DATE_PART('year', a.mydate) >= :year
				GROUP BY description, theyear
			)
			SELECT ROW_NUMBER() OVER(ORDER BY entity) id, entity, amount, money, theyear
			FROM x
	""", nativeQuery = true)
	List<EntitySummary> getSummaries(
			@Param(value = "account") final Integer account,
			@Param(value = "year") final Integer year
	);
}

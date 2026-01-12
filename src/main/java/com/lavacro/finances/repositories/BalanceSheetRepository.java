package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.BalanceSheetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceSheetRepository extends JpaRepository<BalanceSheetEntity, Integer> {
	String BALANCE_QUERY = """
			SELECT row_number() OVER (ORDER BY yr) AS rownum, withdrawals, deposits, deposits + withdrawals AS surp_def, yr, mo
			FROM (
				SELECT
					SUM(CASE WHEN amount < 0 THEN amount ELSE 0 END) AS withdrawals,
					SUM(CASE WHEN amount >=0 THEN amount ELSE 0 END) AS deposits,
					DATE_PART('year', mydate) AS yr, DATE_PART('month', mydate) AS mo
				FROM action
				WHERE account = :account
				GROUP BY yr, mo
			) AS balances
			ORDER BY yr, mo;
	""";

	@Query(value = BALANCE_QUERY, nativeQuery = true)
	List<BalanceSheetEntity> balanceSheet(final Integer account);
}

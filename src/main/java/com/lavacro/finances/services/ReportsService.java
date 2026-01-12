package com.lavacro.finances.services;

import com.lavacro.finances.entities.YearEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ReportsService {
	private final YearRepository yearRepository;

	ReportsService(YearRepository yearRepository) {
		this.yearRepository = yearRepository;
	}

	public List<Integer> getAllYears(final Integer account) {
		log.info("getAllYears: {}", account);
		final List<YearEntity> yearEntityList = yearRepository.getAllYearsForAccount(account);
		final List<Integer> years = new ArrayList<>();

		yearEntityList.forEach(it -> years.add(it.getYear()) );
		return years;
	}

	public List<Integer> getFilteredYears(final Integer account, final Integer start) {
		log.info("getFilteredYears: {}, {}", account, start);
		final List<YearEntity> yearEntityList = yearRepository.getFilteredYears(account, start);
		final List<Integer> years = new ArrayList<>();

		yearEntityList.forEach(it -> years.add(it.getYear()) );
		return years;
	}
}

@Repository
interface YearRepository extends JpaRepository<YearEntity, Integer> {
	@Query(value = """
		SELECT DISTINCT CAST(date_part('year', mydate) AS INTEGER) AS year
		FROM action
		WHERE account = :account
		ORDER BY year
	""", nativeQuery = true)
	List<YearEntity> getAllYearsForAccount(final Integer account);

	@Query(value = """
		SELECT DISTINCT CAST(DATE_PART('year', mydate) AS INTEGER) AS year
		FROM action
		WHERE account = :account AND DATE_PART('year', mydate) >= :first
		ORDER BY year
	""", nativeQuery = true)
	List<YearEntity> getFilteredYears(@Param("account") final Integer account, @Param("first") final Integer first);
}

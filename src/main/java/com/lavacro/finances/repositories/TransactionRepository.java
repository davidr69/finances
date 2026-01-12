package com.lavacro.finances.repositories;

import com.lavacro.finances.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionRepository {
	private final TransactionInterface transactionInterface;

	TransactionRepository(TransactionInterface transactionInterface) {
		this.transactionInterface = transactionInterface;
	}
	public List<TransactionEntity> findForOneAccount(final Integer account, final LocalDate dateStart, final LocalDate dateEnd) {
		return transactionInterface.findForOneAccount(account, dateStart, dateEnd);
	}
}

@Repository
interface TransactionInterface extends JpaRepository<TransactionEntity, Integer> {
	@Query(value = """
			SELECT act.amount, act.mydate, act.reference, act.reconciled, act.visible,
				e.description AS entity, trn.description AS method, act.sequence
			FROM action act
			JOIN entities e ON act.entity = e.id
			JOIN trans_type trn on act.method = trn.id
			WHERE act.account = :account AND act.mydate BETWEEN :dateStart AND :dateEnd
			ORDER BY mydate, amount DESC, e.description
	""", nativeQuery = true)
	List<TransactionEntity> findForOneAccount(final Integer account, final LocalDate dateStart, final LocalDate dateEnd);
}

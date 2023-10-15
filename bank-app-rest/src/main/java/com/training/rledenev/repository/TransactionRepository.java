package com.training.rledenev.repository;

import com.training.rledenev.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("select distinct t from Transaction t " +
            "where t.creditAccount.number = :accountNumber " +
            "or t.debitAccount.number = :accountNumber " +
            "order by t.createdAt desc")
    List<Transaction> getAllTransactionsWithAccountNumber(@Param("accountNumber") String accountNumber);
}
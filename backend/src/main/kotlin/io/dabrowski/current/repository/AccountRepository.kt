package io.dabrowski.current.repository

import io.dabrowski.current.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    fun findByName(name: String): Account?

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.transactions WHERE a.id = :id")
    fun findByIdWithTransactions(id: Long): Account?
}

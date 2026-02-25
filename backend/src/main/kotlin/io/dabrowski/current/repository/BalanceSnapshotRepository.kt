package io.dabrowski.current.repository

import io.dabrowski.current.entity.BalanceSnapshot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BalanceSnapshotRepository : JpaRepository<BalanceSnapshot, Long> {
    @Query("SELECT s FROM BalanceSnapshot s WHERE s.account.id = :accountId ORDER BY s.snapshotDate ASC")
    fun findByAccountIdOrderByDateAsc(accountId: Long): List<BalanceSnapshot>

    @Query(
        "SELECT s FROM BalanceSnapshot s WHERE s.account.id = :accountId " +
            "ORDER BY s.snapshotDate DESC LIMIT 1",
    )
    fun findLatestByAccountId(accountId: Long): BalanceSnapshot?

    fun deleteByAccountId(accountId: Long)
}

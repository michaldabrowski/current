package io.dabrowski.current.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    val account: Account,
    @Column(nullable = false)
    val symbol: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TransactionType,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val assetType: AssetType,
    @Column(nullable = false, precision = 19, scale = 8)
    val quantity: BigDecimal,
    @Column(nullable = false, precision = 19, scale = 2)
    val price: BigDecimal,
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    val totalAmount: BigDecimal,
    @Column(name = "transaction_date")
    val transactionDate: LocalDateTime = LocalDateTime.now(),
    @Column
    val notes: String? = null,
)

enum class TransactionType {
    BUY,
    SELL,
}

enum class AssetType {
    STOCK,
    CRYPTO,
}

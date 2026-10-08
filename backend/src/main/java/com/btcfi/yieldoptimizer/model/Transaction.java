package com.btcfi.yieldoptimizer.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_pool", referencedColumnName = "id")
    private Pool fromPool;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_pool", referencedColumnName = "id")
    private Pool toPool;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal amount;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal fee;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}

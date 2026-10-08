package com.btcfi.yieldoptimizer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "wallet_address", nullable = false, unique = true, length = 42)
    private String walletAddress;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_pool", referencedColumnName = "id")
    private Pool currentPool;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }
}

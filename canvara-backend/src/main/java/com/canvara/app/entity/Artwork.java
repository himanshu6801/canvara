package com.canvara.app.entity;

import com.canvara.app.enums.ArtworkStatus;
import com.canvara.app.enums.Category;
import com.canvara.app.enums.Medium;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "artworks", indexes = {
    @Index(name = "idx_artwork_status",   columnList = "status"),
    @Index(name = "idx_artwork_category", columnList = "category"),
    @Index(name = "idx_artwork_supplier", columnList = "supplier_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Medium medium;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    // e.g. "60 × 80 cm"
    @Column(length = 100)
    private String dimensions;

    // Stored filename (e.g. "abc123.jpg"); resolved to full URL via StorageService
    @Column(nullable = false, length = 300)
    private String imageFilename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ArtworkStatus status = ArtworkStatus.AVAILABLE;

    // Many artworks → one supplier (User with role=SUPPLIER)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private User supplier;

    // One artwork → many purchase requests
    @OneToMany(mappedBy = "artwork", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseRequest> purchaseRequests = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

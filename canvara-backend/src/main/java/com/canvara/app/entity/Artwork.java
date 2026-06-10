package com.canvara.app.entity;

import com.canvara.app.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "artworks")
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

    // e.g. "60 × 80 cm"
    @Column(length = 100)
    private String dimensions;

    @Column(length = 50)
    private Size size;

    // Stored filename (e.g. "abc123.jpg"); resolved to full URL via StorageService
    @Column(nullable = false, length = 300)
    private String imageFilename;

    @Column
    private String status;

    @Column(length = 200)
    private String storyTitle;

    @Column(length = 20)
    private String storyType;

    @Column(columnDefinition = "TEXT")
    private String storyContent;

    // Many artworks → one supplier (User with role=SUPPLIER)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private User supplier;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "artwork_categories", joinColumns = @JoinColumn(name = "artwork_id"))
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private Set<Category> categories = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "artwork_styles", joinColumns = @JoinColumn(name = "artwork_id"))
    @Column(name = "style")
    @Enumerated(EnumType.STRING)
    private Set<Style> styles = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "artwork_mediums", joinColumns = @JoinColumn(name = "artwork_id"))
    @Column(name = "medium")
    @Enumerated(EnumType.STRING)
    private Set<Medium> mediums = new HashSet<>();

}

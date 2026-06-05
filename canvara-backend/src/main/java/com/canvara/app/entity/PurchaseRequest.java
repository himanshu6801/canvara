package com.canvara.app.entity;

import com.canvara.app.enums.PurchaseRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_requests", indexes = {
    @Index(name = "idx_pr_artwork",  columnList = "artwork_id"),
    @Index(name = "idx_pr_visitor",  columnList = "visitor_id"),
    @Index(name = "idx_pr_status",   columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Visitor details — stored directly so unauthenticated visitors can submit
    @Column(nullable = false, length = 100)
    private String visitorName;

    @Column(nullable = false, length = 100)
    private String visitorEmail;

    @Column(length = 20)
    private String visitorPhone;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PurchaseRequestStatus status = PurchaseRequestStatus.PENDING;

    // Optional: if visitor is a logged-in user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id")
    private User visitor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork;

    // Supplier's optional reply message (e.g. "Happy to proceed, here is payment info")
    @Column(columnDefinition = "TEXT")
    private String supplierReply;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

package com.canvara.app.repository;

import com.canvara.app.entity.PurchaseRequest;
import com.canvara.app.enums.PurchaseRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {

    // Supplier: all requests across their artworks
    @Query("SELECT pr FROM PurchaseRequest pr JOIN pr.artwork a WHERE a.supplier.id = :supplierId ORDER BY pr.createdAt DESC")
    List<PurchaseRequest> findBySupplierId(@Param("supplierId") Long supplierId);

    // Supplier: requests for one specific artwork
    List<PurchaseRequest> findByArtworkId(Long artworkId);

    // Count pending requests per artwork (shown on supplier dashboard)
    long countByArtworkIdAndStatus(Long artworkId, PurchaseRequestStatus status);

    // Auto-cancel remaining PENDING requests when artwork is marked SOLD
    @Modifying
    @Query("""
        UPDATE PurchaseRequest pr
        SET pr.status = com.canvara.app.enums.PurchaseRequestStatus.CANCELLED
        WHERE pr.artwork.id = :artworkId
          AND pr.status     = com.canvara.app.enums.PurchaseRequestStatus.PENDING
        """)
    int cancelPendingForArtwork(@Param("artworkId") Long artworkId);
}

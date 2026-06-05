package com.canvara.app.repository;

import com.canvara.app.entity.Artwork;
import com.canvara.app.enums.ArtworkStatus;
import com.canvara.app.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    // Gallery: paginated, filterable by status + optional category + optional keyword
    @Query("""
        SELECT a FROM Artwork a
        JOIN FETCH a.supplier s
        WHERE a.status = :status
          AND (:category IS NULL OR a.category = :category)
          AND (:keyword IS NULL
               OR LOWER(a.title)        LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.fullName)     LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Artwork> findAllPublic(
        @Param("status")   ArtworkStatus status,
        @Param("category") Category category,
        @Param("keyword")  String keyword,
        Pageable pageable
    );

    // Supplier dashboard: all artworks by a specific supplier
    List<Artwork> findBySupplierId(Long supplierId);

    // Ownership check used before update / delete
    Optional<Artwork> findByIdAndSupplierId(Long id, Long supplierId);

    // Count by status for dashboard stats
    long countBySupplierId(Long supplierId);
    long countBySupplierIdAndStatus(Long supplierId, ArtworkStatus status);
}

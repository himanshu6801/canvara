package com.canvara.app.repository;

import com.canvara.app.entity.Artwork;
import com.canvara.app.enums.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ArtworkRepository extends JpaRepository<Artwork, Long> {

    // Gallery: paginated, filterable by status + optional category + optional keyword
    @Query("""
    SELECT DISTINCT a FROM Artwork a
    JOIN FETCH a.supplier s
    WHERE (:status IS NULL OR a.status = :status)
      AND (:keyword IS NULL
           OR LOWER(a.title)    LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:#{#categories == null or #categories.empty} = true
           OR EXISTS (SELECT 1 FROM Artwork a2 JOIN a2.categories c
                      WHERE a2 = a AND c IN :categories))
      AND (:#{#mediums == null or #mediums.empty} = true
           OR EXISTS (SELECT 1 FROM Artwork a3 JOIN a3.mediums m
                      WHERE a3 = a AND m IN :mediums))
      AND (:#{#styles == null or #styles.empty} = true
           OR EXISTS (SELECT 1 FROM Artwork a4 JOIN a4.styles st
                      WHERE a4 = a AND st IN :styles))
      AND (:size IS NULL OR a.size = :size)
""")
    Page<Artwork> findAllPublic(
            @Param("status")     String status,
            @Param("keyword")    String keyword,
            @Param("categories") Set<Category> categories,
            @Param("mediums")    Set<Medium>   mediums,
            @Param("styles")     Set<Style>    styles,
            @Param("size")       Size          size,
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

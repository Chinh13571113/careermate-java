package com.fpt.careermate.repository;

import com.fpt.careermate.domain.BlogRating;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRatingRepo extends JpaRepository<BlogRating, Long> {
    Optional<BlogRating> findByBlogIdAndUserId(Long blogId, Long userId);

    @Query("SELECT AVG(r.rating) FROM blog_rating r WHERE r.blog.id = :blogId")
    Double findAverageRatingByBlogId(@Param("blogId") Long blogId);

    Long countByBlogId(Long blogId);

    // Admin methods for rating management
    Long countByRating(Integer rating);

    Long countByBlogIdAndRating(Long blogId, Integer rating);

    @Query("SELECT AVG(r.rating) FROM blog_rating r")
    Double findOverallAverageRating();

    @Query("SELECT b.id, b.title, AVG(r.rating) as avgRating, COUNT(r.id) as ratingCount " +
            "FROM blog_rating r JOIN r.blog b " +
            "GROUP BY b.id, b.title " +
            "ORDER BY avgRating DESC")
    List<Object[]> findTopRatedBlogs(Pageable pageable);

    List<BlogRating> findByBlogIdOrderByCreatedAtDesc(Long blogId, Pageable pageable);
}

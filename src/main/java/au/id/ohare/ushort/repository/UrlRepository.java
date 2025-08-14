package au.id.ohare.ushort.repository;

import au.id.ohare.ushort.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    /**
     * Find URL entity by shortened URL code
     * @param shortenedUrl the shortened URL code
     * @return Optional URL entity
     */
    Optional<UrlEntity> findByShortenedUrl(String shortenedUrl);

    /**
     * Find URLs that were last accessed before the given date (for expiry cleanup)
     * @param cutoffDate the cutoff date
     * @return List of URL entities
     */
    List<UrlEntity> findByLastAccessedBefore(LocalDateTime cutoffDate);

    /**
     * Check if a shortened URL already exists
     * @param shortenedUrl the shortened URL code
     * @return true if exists, false otherwise
     */
    boolean existsByShortenedUrl(String shortenedUrl);
}
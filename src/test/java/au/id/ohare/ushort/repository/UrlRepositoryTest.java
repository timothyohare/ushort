package au.id.ohare.ushort.repository;

import au.id.ohare.ushort.entity.UrlEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UrlRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UrlRepository urlRepository;

    @Test
    @DisplayName("Should save and find URL entity by ID")
    void shouldSaveAndFindById() {
        // Given
        UrlEntity urlEntity = createUrlEntity(
                "https://www.example.com",
                "abc123",
                0,
                LocalDateTime.now()
        );

        // When
        UrlEntity savedEntity = urlRepository.save(urlEntity);
        Optional<UrlEntity> foundEntity = urlRepository.findById(savedEntity.getId());

        // Then
        assertTrue(foundEntity.isPresent(), "Entity should be found by ID");
        assertEquals("https://www.example.com", foundEntity.get().getOriginalUrl());
        assertEquals("abc123", foundEntity.get().getShortenedUrl());
        assertEquals(0, foundEntity.get().getAccessCount());
        assertNotNull(foundEntity.get().getLastAccessed());
    }

    @Test
    @DisplayName("Should find URL entity by shortened URL")
    void shouldFindByShortenedUrl() {
        // Given
        UrlEntity urlEntity = createUrlEntity(
                "https://www.google.com",
                "xyz789",
                5,
                LocalDateTime.now().minusHours(2)
        );
        entityManager.persistAndFlush(urlEntity);

        // When
        Optional<UrlEntity> foundEntity = urlRepository.findByShortenedUrl("xyz789");

        // Then
        assertTrue(foundEntity.isPresent(), "Entity should be found by shortened URL");
        assertEquals("https://www.google.com", foundEntity.get().getOriginalUrl());
        assertEquals("xyz789", foundEntity.get().getShortenedUrl());
        assertEquals(5, foundEntity.get().getAccessCount());
    }

    @Test
    @DisplayName("Should return empty when shortened URL not found")
    void shouldReturnEmptyForNonExistentShortenedUrl() {
        // When
        Optional<UrlEntity> foundEntity = urlRepository.findByShortenedUrl("nonexistent");

        // Then
        assertFalse(foundEntity.isPresent(), "Should not find non-existent shortened URL");
    }

    @Test
    @DisplayName("Should find URLs accessed before given date")
    void shouldFindUrlsAccessedBefore() {
        // Given
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1);
        LocalDateTime oldDate = LocalDateTime.now().minusDays(2);
        LocalDateTime recentDate = LocalDateTime.now().minusHours(1);

        UrlEntity oldUrlEntity = createUrlEntity(
                "https://www.old.com",
                "old123",
                10,
                oldDate
        );
        UrlEntity recentUrlEntity = createUrlEntity(
                "https://www.recent.com",
                "recent456",
                3,
                recentDate
        );

        entityManager.persistAndFlush(oldUrlEntity);
        entityManager.persistAndFlush(recentUrlEntity);

        // When
        List<UrlEntity> oldUrls = urlRepository.findByLastAccessedBefore(cutoffDate);

        // Then
        assertEquals(1, oldUrls.size(), "Should find one old URL");
        assertEquals("old123", oldUrls.get(0).getShortenedUrl());
    }

    @Test
    @DisplayName("Should update access count and last accessed time")
    void shouldUpdateAccessInfo() {
        // Given
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);
        UrlEntity urlEntity = createUrlEntity(
                "https://www.test.com",
                "test123",
                0,
                initialTime
        );
        UrlEntity savedEntity = entityManager.persistAndFlush(urlEntity);

        // When
        savedEntity.setAccessCount(savedEntity.getAccessCount() + 1);
        savedEntity.setLastAccessed(LocalDateTime.now());
        urlRepository.save(savedEntity);

        // Then
        Optional<UrlEntity> updatedEntity = urlRepository.findById(savedEntity.getId());
        assertTrue(updatedEntity.isPresent());
        assertEquals(1, updatedEntity.get().getAccessCount());
        assertTrue(updatedEntity.get().getLastAccessed().isAfter(initialTime));
    }

    @Test
    @DisplayName("Should enforce unique constraint on shortened URL")
    void shouldEnforceUniqueShortenedUrl() {
        // Given
        UrlEntity firstEntity = createUrlEntity(
                "https://www.first.com",
                "unique123",
                0,
                LocalDateTime.now()
        );
        entityManager.persistAndFlush(firstEntity);

        UrlEntity duplicateEntity = createUrlEntity(
                "https://www.second.com",
                "unique123",  // Same shortened URL
                0,
                LocalDateTime.now()
        );

        // When & Then
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicateEntity);
        }, "Should throw exception for duplicate shortened URL");
    }

    @Test
    @DisplayName("Should count total URLs in database")
    void shouldCountTotalUrls() {
        // Given
        UrlEntity url1 = createUrlEntity("https://www.one.com", "one123", 1, LocalDateTime.now());
        UrlEntity url2 = createUrlEntity("https://www.two.com", "two456", 2, LocalDateTime.now());
        UrlEntity url3 = createUrlEntity("https://www.three.com", "three789", 3, LocalDateTime.now());

        entityManager.persistAndFlush(url1);
        entityManager.persistAndFlush(url2);
        entityManager.persistAndFlush(url3);

        // When
        long count = urlRepository.count();

        // Then
        assertEquals(3, count, "Should count 3 URLs in database");
    }

    @Test
    @DisplayName("Should delete URLs by ID list")
    void shouldDeleteUrlsByIdList() {
        // Given
        UrlEntity url1 = createUrlEntity("https://www.delete1.com", "delete1", 0, LocalDateTime.now());
        UrlEntity url2 = createUrlEntity("https://www.delete2.com", "delete2", 0, LocalDateTime.now());
        UrlEntity url3 = createUrlEntity("https://www.keep.com", "keep123", 0, LocalDateTime.now());

        url1 = entityManager.persistAndFlush(url1);
        url2 = entityManager.persistAndFlush(url2);
        url3 = entityManager.persistAndFlush(url3);

        List<Long> idsToDelete = List.of(url1.getId(), url2.getId());

        // When
        urlRepository.deleteAllById(idsToDelete);
        entityManager.flush();

        // Then
        assertEquals(1, urlRepository.count(), "Should have 1 URL remaining");
        assertTrue(urlRepository.findById(url3.getId()).isPresent(), "url3 should still exist");
        assertFalse(urlRepository.findById(url1.getId()).isPresent(), "url1 should be deleted");
        assertFalse(urlRepository.findById(url2.getId()).isPresent(), "url2 should be deleted");
    }

    private UrlEntity createUrlEntity(String originalUrl, String shortenedUrl, int accessCount, LocalDateTime lastAccessed) {
        return UrlEntity.builder()
                .originalUrl(originalUrl)
                .shortenedUrl(shortenedUrl)
                .accessCount(accessCount)
                .lastAccessed(lastAccessed)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
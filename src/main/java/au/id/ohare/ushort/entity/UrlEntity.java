package au.id.ohare.ushort.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Entity
@Table(name = "urls", indexes = {
    @Index(name = "idx_shortened_url", columnList = "shortened_url", unique = true),
    @Index(name = "idx_last_accessed", columnList = "last_accessed")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class UrlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Original URL cannot be blank")
    @Size(max = 2048, message = "Original URL cannot exceed 2048 characters")
    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @NotBlank(message = "Shortened URL cannot be blank")
    @Size(min = 6, max = 16, message = "Shortened URL must be between 6-16 characters")
    @Column(name = "shortened_url", nullable = false, unique = true, length = 16)
    private String shortenedUrl;

    @NotNull(message = "Access count cannot be null")
    @PositiveOrZero(message = "Access count must be positive or zero")
    @Column(name = "access_count", nullable = false)
    @Builder.Default
    private Integer accessCount = 0;

    @NotNull(message = "Last accessed time cannot be null")
    @Column(name = "last_accessed", nullable = false)
    private LocalDateTime lastAccessed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastAccessed == null) {
            lastAccessed = now;
        }
        if (accessCount == null) {
            accessCount = 0;
        }
        log.debug("Creating URL entity: shortenedUrl={}, originalUrl={}", shortenedUrl, originalUrl);
    }

    @PreUpdate
    protected void onUpdate() {
        log.debug("Updating URL entity: id={}, shortenedUrl={}, accessCount={}", id, shortenedUrl, accessCount);
    }

    /**
     * Increments the access count and updates last accessed time
     */
    public void recordAccess() {
        this.accessCount++;
        this.lastAccessed = LocalDateTime.now();
        log.debug("Recorded access for URL: shortenedUrl={}, newAccessCount={}", shortenedUrl, accessCount);
    }
}
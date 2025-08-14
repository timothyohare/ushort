package au.id.ohare.ushort.repository;

import au.id.ohare.ushort.entity.AdminEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AdminRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdminRepository adminRepository;

    @Test
    @DisplayName("Should save and find admin entity by ID")
    void shouldSaveAndFindById() {
        // Given
        AdminEntity adminEntity = createAdminEntity("testadmin", "hashedpassword123456");

        // When
        AdminEntity savedEntity = adminRepository.save(adminEntity);
        Optional<AdminEntity> foundEntity = adminRepository.findById(savedEntity.getId());

        // Then
        assertTrue(foundEntity.isPresent(), "Admin entity should be found by ID");
        assertEquals("testadmin", foundEntity.get().getUsername());
        assertEquals("hashedpassword123456", foundEntity.get().getPasswordHash());
    }

    @Test
    @DisplayName("Should find admin entity by username")
    void shouldFindByUsername() {
        // Given
        AdminEntity adminEntity = createAdminEntity("admin", "$2a$10$hashedBcryptPassword123");
        entityManager.persistAndFlush(adminEntity);

        // When
        Optional<AdminEntity> foundEntity = adminRepository.findByUsername("admin");

        // Then
        assertTrue(foundEntity.isPresent(), "Admin entity should be found by username");
        assertEquals("admin", foundEntity.get().getUsername());
        assertEquals("$2a$10$hashedBcryptPassword123", foundEntity.get().getPasswordHash());
    }

    @Test
    @DisplayName("Should return empty when username not found")
    void shouldReturnEmptyForNonExistentUsername() {
        // When
        Optional<AdminEntity> foundEntity = adminRepository.findByUsername("nonexistent");

        // Then
        assertFalse(foundEntity.isPresent(), "Should not find non-existent username");
    }

    @Test
    @DisplayName("Should enforce unique constraint on username")
    void shouldEnforceUniqueUsername() {
        // Given
        AdminEntity firstAdmin = createAdminEntity("uniqueuser", "password123456");
        entityManager.persistAndFlush(firstAdmin);

        AdminEntity duplicateAdmin = createAdminEntity("uniqueuser", "password789012");

        // When & Then
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicateAdmin);
        }, "Should throw exception for duplicate username");
    }

    @Test
    @DisplayName("Should update admin password hash")
    void shouldUpdatePasswordHash() {
        // Given
        AdminEntity adminEntity = createAdminEntity("changepassword", "oldpasshash123456");
        AdminEntity savedEntity = entityManager.persistAndFlush(adminEntity);

        // When
        savedEntity.setPasswordHash("newpasshash123456");
        adminRepository.save(savedEntity);

        // Then
        Optional<AdminEntity> updatedEntity = adminRepository.findById(savedEntity.getId());
        assertTrue(updatedEntity.isPresent());
        assertEquals("newpasshash123456", updatedEntity.get().getPasswordHash());
    }

    @Test
    @DisplayName("Should check if admin exists by username")
    void shouldCheckAdminExistsByUsername() {
        // Given
        AdminEntity adminEntity = createAdminEntity("existscheck", "somepassword123456");
        entityManager.persistAndFlush(adminEntity);

        // When & Then
        assertTrue(adminRepository.existsByUsername("existscheck"), "Should find existing username");
        assertFalse(adminRepository.existsByUsername("doesnotexist"), "Should not find non-existent username");
    }

    private AdminEntity createAdminEntity(String username, String passwordHash) {
        return AdminEntity.builder()
                .username(username)
                .passwordHash(passwordHash)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
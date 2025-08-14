package au.id.ohare.ushort.repository;

import au.id.ohare.ushort.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, Long> {

    /**
     * Find admin entity by username
     * @param username the username
     * @return Optional admin entity
     */
    Optional<AdminEntity> findByUsername(String username);

    /**
     * Check if an admin with the given username exists
     * @param username the username
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);
}
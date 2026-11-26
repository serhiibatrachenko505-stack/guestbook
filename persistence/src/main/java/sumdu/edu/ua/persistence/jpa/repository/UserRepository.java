package sumdu.edu.ua.persistence.jpa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sumdu.edu.ua.persistence.jpa.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
}

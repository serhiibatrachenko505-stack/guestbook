package sumdu.edu.ua.persistence.jpa.adapter;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.core.domain.User;
import sumdu.edu.ua.core.port.UserRepositoryPort;
import sumdu.edu.ua.persistence.jpa.entity.UserEntity;
import sumdu.edu.ua.persistence.jpa.repository.UserRepository;

/** Реалізація порту користувачів через Spring Data JPA. */
@Component
@Transactional(readOnly = true)
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository repo;

    public JpaUserRepositoryAdapter(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<User> findById(long id) {
        return repo.findById(id).map(Mappers::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username).map(Mappers::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repo.findAll(Sort.by("username")).stream().map(Mappers::toDomain).toList();
    }

    @Override
    @Transactional
    public User create(String username, String role) {
        // пароль не задається: користувача створено автоматично з відгуку
        return Mappers.toDomain(repo.save(new UserEntity(username, null, role)));
    }
}

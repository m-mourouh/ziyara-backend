package ma.enset.ziyara.auth.repository;

import ma.enset.ziyara.auth.entity.User;
import ma.enset.ziyara.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findByRole(UserRole role);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %?1% OR u.lastName LIKE %?1% OR u.email LIKE %?1%")
    List<User> searchUsers(String keyword);

    List<User> findByEnabledTrue();

    Optional<User> findByEmailAndEnabledTrue(String email);
}
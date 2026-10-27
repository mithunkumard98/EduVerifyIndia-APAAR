package in.eduverify;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByRegisterNoIgnoreCase(String registerNo);
    Optional<Student> findByEmailIgnoreCaseAndPassword(String email, String password);
    Optional<Student> findByApaarIdIgnoreCaseAndPassword(String apaarId, String password);
    Optional<Student> findByEmailIgnoreCase(String email);
}

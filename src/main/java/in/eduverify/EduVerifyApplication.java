package in.eduverify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EduVerifyApplication {
    public static void main(String[] args) { SpringApplication.run(EduVerifyApplication.class, args); }

    @Bean
    CommandLineRunner seedReturningStudent(StudentRepository repo) {
        return args -> {
            // Clearly synthetic demo account used to demonstrate a returning student with an existing demo ID.
            if (repo.count() == 0) {
                Student old = new Student();
                old.setFullName("Meera Sample (Returning Demo Student)");
                old.setEmail("meera.returning@example.com");
                old.setPassword("returning123");
                old.setRegisterNo("DEMO-OLD-001");
                old.setApaarId("DEMO-APAAR-2025-000001");
                old.setCollege("Demo College");
                old.setCourse("B.E. Computer Science");
                old.setStatus("VERIFIED");
                repo.save(old);
            }
        };
    }
}

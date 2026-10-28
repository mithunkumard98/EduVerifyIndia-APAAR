package in.eduverify;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students", uniqueConstraints = {
    @UniqueConstraint(name="uk_student_email", columnNames="email"),
    @UniqueConstraint(name="uk_student_regno", columnNames="register_no"),
    @UniqueConstraint(name="uk_student_apaar", columnNames="apaar_id")
})
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="full_name", nullable=false) private String fullName;
    @Column(nullable=false) private String email;
    @Column(nullable=false) private String password;
    @Column(name="register_no", nullable=false) private String registerNo;
    @Column(name="apaar_id", nullable=false) private String apaarId;
    private String college;
    private String course;
    @Column(nullable=false) private String status = "PENDING REVIEW";
    @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
    @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="student_document_names", joinColumns=@JoinColumn(name="student_id")) @Column(name="document_name") private List<String> documentNames = new ArrayList<>();
    @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="student_document_keys", joinColumns=@JoinColumn(name="student_id")) @Column(name="document_key") private List<String> documentKeys = new ArrayList<>();

    public Student() {}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public String getRegisterNo(){return registerNo;} public void setRegisterNo(String v){registerNo=v;}
    public String getApaarId(){return apaarId;} public void setApaarId(String v){apaarId=v;}
    public String getCollege(){return college;} public void setCollege(String v){college=v;}
    public String getCourse(){return course;} public void setCourse(String v){course=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public List<String> getDocumentNames(){return documentNames;} public void setDocumentNames(List<String> v){documentNames=v;}
    public List<String> getDocumentKeys(){return documentKeys;} public void setDocumentKeys(List<String> v){documentKeys=v;}
}

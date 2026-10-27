package in.eduverify;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StudentRepository repo;
    private final AtomicLong sequence = new AtomicLong(System.currentTimeMillis() % 100000);
    private static final Set<String> OFFICER_TOKENS = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> STUDENT_TOKENS = new ConcurrentHashMap<>();
    public ApiController(StudentRepository repo) { this.repo = repo; }

    public record RegisterRequest(String fullName, String email, String password, String registerNo, String college, String course) {}
    public record LoginRequest(String loginId, String password) {}
    public record OfficerLoginRequest(String email, String password) {}
    public record OfficerLoginResponse(String message, String token, String officerName) {}
    public record StudentView(Long id, String fullName, String email, String registerNo, String apaarId, String college, String course, String status, List<String> documents) {
        static StudentView from(Student s) { return new StudentView(s.getId(),s.getFullName(),s.getEmail(),s.getRegisterNo(),s.getApaarId(),s.getCollege(),s.getCourse(),s.getStatus(),s.getDocumentNames()); }
    }
    public record LoginResponse(String message, String token, StudentView student) {}
    public record ReviewRequest(String status, String note) {}

    @PostMapping(value="/register", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,Object> register(@RequestParam String fullName, @RequestParam String email,
            @RequestParam String password, @RequestParam String registerNo,
            @RequestParam(required=false) String college, @RequestParam(required=false) String course,
            @RequestParam(required=false) MultipartFile[] documents) {
        if (blank(fullName) || blank(email) || blank(password) || blank(registerNo))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name, email, password and register number are required.");
        if (!email.contains("@")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter a valid email address.");
        if (repo.existsByEmailIgnoreCase(email.trim())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered. Please log in.");
        if (repo.existsByRegisterNoIgnoreCase(registerNo.trim())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Register number already exists. Try logging in or check the number.");
        List<MultipartFile> selected = documents == null ? List.of() : Arrays.stream(documents).filter(f -> f != null && !f.isEmpty()).toList();
        if (selected.size() > 5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Upload up to 5 files only.");
        long totalBytes = selected.stream().mapToLong(MultipartFile::getSize).sum();
        if (totalBytes > 20L * 1024 * 1024 || selected.stream().anyMatch(f -> f.getSize() > 5L * 1024 * 1024))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Each file must be 5 MB or less, and total upload must be 20 MB or less.");
        Student s = new Student();
        s.setFullName(fullName.trim()); s.setEmail(email.trim().toLowerCase()); s.setPassword(password);
        s.setRegisterNo(registerNo.trim()); s.setCollege(safe(college)); s.setCourse(safe(course));
        s.setApaarId("DEMO-APAAR-" + java.time.Year.now().getValue() + "-" + String.format("%06d", sequence.incrementAndGet()));
        s.setStatus("PENDING REVIEW");
        try {
            Path uploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "eduverify-uploads");
            Files.createDirectories(uploadDir);
            for (MultipartFile file : selected) {
                String original = Paths.get(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename()).getFileName().toString();
                String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
                Set<String> allowedExtensions = Set.of("pdf", "png", "jpg", "jpeg", "doc", "docx");
                if (!allowedExtensions.contains(ext)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported document type. Upload PDF, PNG, JPG, JPEG, DOC or DOCX files only.");
                }
                String key = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
                Files.copy(file.getInputStream(), uploadDir.resolve(key), StandardCopyOption.REPLACE_EXISTING);
                s.getDocumentNames().add(original); s.getDocumentKeys().add(key);
            }
        } catch (IOException ex) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Could not save uploaded demo documents."); }
        repo.save(s);
        Map<String,Object> out = new LinkedHashMap<>(); out.put("message","Registration successful. Your demo APAAR ID has been generated."); out.put("student",StudentView.from(s)); return out;
    }

    @GetMapping("/students/{id}/documents/{index}")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable Long id, @PathVariable int index,
            @RequestHeader(value="X-Officer-Token", required=false) String token) throws IOException {
        requireOfficer(token);
        Student student = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Student not found."));
        if (index < 0 || index >= student.getDocumentKeys().size()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Document not found.");
        Path file = Paths.get(System.getProperty("java.io.tmpdir"), "eduverify-uploads", student.getDocumentKeys().get(index));
        if (!Files.exists(file)) throw new ResponseStatusException(HttpStatus.GONE,"This demo file is no longer available. Render temporary storage may reset after restart.");
        String name = student.getDocumentNames().get(index).replaceAll("[\\r\\n\"]", "_");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachment; filename=\"" + name + "\"")
            .body(new InputStreamResource(Files.newInputStream(file)));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest r) {
        if (blank(r.loginId()) || blank(r.password())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Enter your email or APAAR ID and password.");
        String id = r.loginId().trim();
        Optional<Student> found = id.contains("@") ? repo.findByEmailIgnoreCaseAndPassword(id,r.password()) : repo.findByApaarIdIgnoreCaseAndPassword(id,r.password());
        Student s = found.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Login details not found. New students must register first to receive a demo APAAR ID."));
        String token = UUID.randomUUID().toString();
        STUDENT_TOKENS.put(token, s.getId());
        return new LoginResponse("Login successful.", token, StudentView.from(s));
    }

    @PostMapping(value="/student/documents", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentView uploadStudentDocument(@RequestHeader(value="X-Student-Token", required=false) String token,
            @RequestParam String documentType, @RequestParam MultipartFile document) {
        Long studentId = token == null ? null : STUDENT_TOKENS.get(token);
        if (studentId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in as a student before uploading documents.");
        Set<String> allowedTypes = Set.of("Birth Certificate", "Aadhaar Card / Aadhaar Proof", "Address Proof", "Identity Proof");
        if (!allowedTypes.contains(documentType)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid document type.");
        if (document == null || document.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a file to upload.");
        if (document.getSize() > 5L * 1024 * 1024) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each file must be 5 MB or less.");
        String original = Paths.get(document.getOriginalFilename() == null ? "document" : document.getOriginalFilename()).getFileName().toString();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!Set.of("pdf", "png", "jpg", "jpeg", "doc", "docx").contains(ext)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload PDF, PNG, JPG, JPEG, DOC or DOCX files only.");
        Student student = repo.findById(studentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Student account not found. Please log in again."));
        if (student.getDocumentKeys().size() >= 5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Up to 5 documents can be uploaded per student in this demo.");
        Path uploadDir = Paths.get(System.getProperty("java.io.tmpdir"), "eduverify-uploads");
        try {
            Files.createDirectories(uploadDir);
            String key = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Files.copy(document.getInputStream(), uploadDir.resolve(key), StandardCopyOption.REPLACE_EXISTING);
            student.getDocumentNames().add(documentType + " - " + original);
            student.getDocumentKeys().add(key);
            student.setStatus("PENDING REVIEW");
            repo.save(student);
            return StudentView.from(student);
        } catch (IOException ex) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save uploaded demo document."); }
    }

    @PostMapping("/student/logout")
    public Map<String,String> studentLogout(@RequestHeader(value="X-Student-Token", required=false) String token) {
        if (token != null) STUDENT_TOKENS.remove(token);
        return Map.of("message", "Student logged out.");
    }

    @PostMapping("/officer/login")
    public OfficerLoginResponse officerLogin(@RequestBody OfficerLoginRequest r) {
        String expectedEmail = System.getenv().getOrDefault("OFFICER_EMAIL", "officer@eduverify.demo");
        String expectedPassword = System.getenv().getOrDefault("OFFICER_PASSWORD", "OfficerDemo2026!");
        if (r == null || blank(r.email()) || blank(r.password()) || !expectedEmail.equalsIgnoreCase(r.email().trim()) || !expectedPassword.equals(r.password()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Officer login details are incorrect.");
        String token = UUID.randomUUID().toString(); OFFICER_TOKENS.add(token);
        return new OfficerLoginResponse("Officer login successful.", token, "Verification Officer");
    }

    @PostMapping("/officer/logout")
    public Map<String,String> officerLogout(@RequestHeader(value="X-Officer-Token", required=false) String token) {
        if (token != null) OFFICER_TOKENS.remove(token);
        return Map.of("message","Officer logged out.");
    }

    @GetMapping("/students")
    public List<StudentView> students(@RequestHeader(value="X-Officer-Token", required=false) String token){ requireOfficer(token); return repo.findAll().stream().map(StudentView::from).toList(); }

    @PatchMapping("/students/{id}/review")
    public StudentView review(@PathVariable Long id, @RequestBody ReviewRequest r, @RequestHeader(value="X-Officer-Token", required=false) String token) {
        requireOfficer(token);
        Student s = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Student not found."));
        Set<String> allowed = Set.of("PENDING REVIEW","VERIFIED","NEEDS CORRECTION");
        if (r.status()==null || !allowed.contains(r.status().toUpperCase())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose PENDING REVIEW, VERIFIED or NEEDS CORRECTION.");
        s.setStatus(r.status().toUpperCase()); repo.save(s); return StudentView.from(s);
    }
    private static void requireOfficer(String token) { if (token == null || !OFFICER_TOKENS.contains(token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please log in as a verification officer first."); }
    private static boolean blank(String s){return s==null || s.isBlank();}
    private static String safe(String s){return s==null?"":s.trim();}
}

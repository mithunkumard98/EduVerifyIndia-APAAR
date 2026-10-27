# EduVerify India — APAAR Flow Prototype (Demo)

## Run on Windows using VS Code

### Requirements
- JDK 17 or newer (JDK 21 is fine).
- Apache Maven 3.9 or newer available on PATH.
- Internet connection on first run so Maven can download dependencies.

### Steps
1. Extract the ZIP completely.
2. In VS Code, select **File → Open Folder** and open the extracted project folder that directly contains `pom.xml`.
3. Select **Terminal → New Terminal**. Check the prompt/path ends in `\EduVerifyIndia`.
4. Run `java -version` and `mvn -version`.
5. Run `mvn spring-boot:run`. Alternatively, double-click `run-windows.bat` from File Explorer.
6. Wait for `Started EduVerifyApplication` (or similar).
7. Open `http://localhost:8080`. Keep the terminal running. Press Ctrl+C to stop.

If `dir pom.xml` says the file is not found, run `cd` into the folder that contains `pom.xml`. Do not run Maven from `src`, `src\main\java`, or a different project folder.

## Demo flow
- New student: choose New Student Registration, complete the form, and register. A unique `DEMO-APAAR-YYYY-XXXXXX` identifier is generated.
- Returning student: log in using the registered email or generated demo ID and the same password.
- Returning demo student: email `meera.returning@example.com`; password `returning123`.
- Officer Review: view demo records and update their status.

## Database behavior
This version uses an **in-memory H2 database**. No MySQL installation or database setup is needed. Demo records reset when the app is stopped. H2 console: `http://localhost:8080/h2-console`; JDBC URL `jdbc:h2:mem:eduverifydb`, username `sa`, blank password.

## Important limitations
This is a local hackathon prototype. Generated identifiers are demo identifiers only, not official APAAR IDs. The app is not connected to APAAR, DigiLocker, Aadhaar, or government records. Use fictional data only. Password handling and officer access are not production-secure.

## Student supporting-file upload (demo)
- The New Student Registration form has separate optional fields for Birth Certificate, Aadhaar Proof (sample only), Address Proof, and Other ID Proof.
- Up to 5 files total can be selected; each file must be 5 MB or less (20 MB total maximum). The four labelled fields are Birth Certificate, Aadhaar Card/Proof, Address Proof, and Identity Proof. The category appears in the filename shown to the officer. Accepted formats: PDF, PNG, JPG/JPEG, DOC/DOCX.
- After registration, the Verification Officer can log in and download uploaded files from the Uploaded files column.
- Files are stored in the service's temporary filesystem. Render restarts/redeploys may remove them, and this demo does not provide permanent document storage.
- Use fictional/sample documents only. Do not upload real Aadhaar details, real certificates, or sensitive personal information.

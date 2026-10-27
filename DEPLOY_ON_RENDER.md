# Deploy EduVerify India on Render

This is a demonstration prototype only. Do not enter real student information or official APAAR IDs. The H2 database is in-memory, so demo data resets when the service restarts or redeploys. This project is not production-secure for real student records.

## Before deployment
- A GitHub account
- A Render account
- Internet access for the build to download Maven dependencies and base images

## Steps
1. Extract this ZIP.
2. Create a GitHub repository.
3. Upload the **contents of the extracted project folder** to the repository root (Dockerfile, pom.xml, src/, etc.). Do not upload only the outer folder as a nested directory.
4. In Render, choose **New + → Web Service**, connect the GitHub repository, and select the branch.
5. Set **Language/Runtime: Docker**. Leave Dockerfile path as `./Dockerfile` if it is at the repository root.
6. Create/deploy the service. The Dockerfile builds the project with Java 17 and uses Render's `PORT` environment variable.
7. When Render says the service is live, open the `https://<service-name>.onrender.com` URL shown on the service page.
8. Test registration/login using fictional demo data only. Copy the public URL into PowerPoint with Ctrl+K.

## Notes
- Free web services may sleep after inactivity and take time to wake on the next visit.
- The H2 database is in-memory and not persistent. Do not use this prototype for real student records, passwords, or government identity data.
- If deployment fails, inspect the Render service's **Logs** and share the first actual error, excluding secrets.

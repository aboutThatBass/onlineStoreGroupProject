# Viral Vault

Viral Vault is the Code Goats' CS 3773 online shopping project for trending social-media products.

## Current implementation

The repository contains the smallest useful starting point for the group's shopping website. It renders one page with Spring Boot, Thymeleaf, and Bootstrap. The example product is **hard-coded** to demonstrate the layout; there is no database, login, search, cart, checkout, or admin panel yet.

## Technology decisions

| Area | Current code | Submitted proposal | Decision |
| --- | --- | --- | --- |
| Backend | Java Spring Boot | Java Spring Boot | Keep Spring Boot |
| Frontend | Thymeleaf and Bootstrap | Svelte | Team agreement pending |
| Database | None configured | SQLite | Team agreement pending; the starter's original next steps suggested PostgreSQL |

The starter's use of Thymeleaf and its PostgreSQL suggestion are not yet team decisions. Record the agreed choices and rationale here before developing those layers.

The project targets Java 17. JDK 21 can build this target; teammates do not need to raise the target to match their installed JDK.

## Run locally

1. Install JDK 17 or newer and ensure Java is available. JDK 21 is suitable.
2. Open the repository folder containing `pom.xml` in VS Code. The VS Code Java Extension Pack is useful.
3. Check Java: `java -version`.
4. In the VS Code terminal, start the application using the Maven Wrapper:

   Windows PowerShell:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   macOS/Linux:
   ```sh
   ./mvnw spring-boot:run
   ```

5. Open <http://localhost:8080/> in a browser. Keep the terminal running; press Ctrl+C to stop.

The wrapper downloads Maven on its first run, and Maven downloads project dependencies. Internet access is needed for those downloads. A separate Maven installation is optional; if you have one, `mvn spring-boot:run` also works.

If Maven reports a Java configuration error, ensure `JAVA_HOME` points to the installed JDK folder rather than its `bin` folder. If port 8080 is occupied, stop the other server or run with `-Dspring-boot.run.arguments=--server.port=8081` and open port 8081.

## Build and check

```powershell
.\mvnw.cmd verify
```

This builds the project and runs any tests present. There are no automated tests in the starter yet; a successful build does not prove shopping functionality exists.

## What each file does

| File | Role |
| --- | --- |
| `pom.xml` | Java version, Spring Boot version, and two web dependencies |
| `mvnw`, `mvnw.cmd`, `.mvn/wrapper/` | Maven Wrapper: runs a consistent Maven version without a separate installation |
| `src/main/java/com/codegoats/viralvault/ViralVaultApplication.java` | Starts the Spring Boot server |
| `src/main/java/com/codegoats/viralvault/web/HomeController.java` | Handles a request to `/` and supplies a message to the page |
| `src/main/resources/templates/index.html` | Thymeleaf page; Bootstrap classes style the HTML |
| `src/main/resources/static/images/product-placeholder.svg` | Local example image |
| `BACKLOG.md` | Requirements, ownership, status, and acceptance criteria |

## Next group steps

1. Agree on the frontend and database, and record the decisions above.
2. Add the agreed database, Spring Data JPA (or equivalent), and a repeatable schema/seed setup together. Do not add the JPA starter alone: without a configured data source, startup can fail.
3. Replace the static product card with products queried from the database. Keep image, price, and available quantity on the main sale page.
4. Build a complete catalog-to-cart-to-order flow before expanding the administrative features.
5. Add customer pages, cart/checkout, and admin features in separate reviewed pull requests.
6. Add Spring Security when implementing login and admin permissions. Enforce authorization on the server; do not treat a hidden Admin link as authorization.

## Team workflow

- Use [BACKLOG.md](BACKLOG.md) as the shared feature checklist. Assign owners during planning.
- Create a feature branch from an up-to-date `main`, implement a focused change, and open a pull request for review.
- Link implementation issues and pull requests from the backlog. Mark a feature Done only after its acceptance criteria have been demonstrated and reviewed.
- Record sprint decisions, testing evidence, and architectural changes for the final project report.
- Commit source and wrapper files, not generated `target/` output or OS files.
- Do not commit real database passwords or other credentials. Document local configuration when the database is added.

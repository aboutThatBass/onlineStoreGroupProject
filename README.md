# Viral Vault starter

This is the smallest useful starting point for the group's shopping website. It renders one page with Spring Boot, Thymeleaf, and Bootstrap. The example product is **hard-coded** to demonstrate the layout; there is no database, login, cart, admin panel, or checkout yet.

## Run in VS Code

1. Install JDK 17 or newer, Maven, and the VS Code Java Extension Pack.
3. In the VS Code terminal, run `mvn spring-boot:run`.
4. Open <http://localhost:8080/>.



## What each file does

| File | Role |
| --- | --- |
| `pom.xml` | Java version, Spring Boot version, and two web dependencies |
| `ViralVaultApplication.java` | Starts the Spring Boot server |
| `web/HomeController.java` | Handles a request to `/` and supplies a message to the page |
| `templates/index.html` | Thymeleaf page; Bootstrap classes style the HTML |
| `static/images/product-placeholder.svg` | Local example image |

## Next group steps

1. Agree on the tech stack and merge this starter into the team's repository.
2. Add PostgreSQL, Spring Data JPA, and a repeatable schema/seed setup together. Do not add the JPA starter alone: without a configured data source, startup can fail.
3. Replace the static product card with products queried from the database. Keep image, price, and available quantity on the main sale page.
4. Add customer pages, cart/checkout, and admin features in separate reviewed pull requests.
5. Add Spring Security when implementing login and admin permissions. Do not treat a hidden Admin link as authorization.

Do not commit real database passwords. Document local configuration when the database is added.

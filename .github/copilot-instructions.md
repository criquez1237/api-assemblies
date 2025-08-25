<!-- .github/copilot-instructions.md - Guidance for AI coding agents working on this repository -->

Purpose: Provide focused, actionable instructions so an AI assistant can be immediately productive in the Assemblies Store `api-rest` Java Spring Boot project.

High-level context
- This is a Spring Boot 3.5.x REST API (Java 21) with modular packages under `src/main/java/com/assembliestore/api/`.
- Major responsibilities: user/auth, product/catalog, sale/orders, payment (Stripe), storage (Cloudinary), email (Resend), realtime (WebSocket).
- Entrypoint: `ApiRestApplication` (enables `ResendConfig` properties). Main config lives in `src/main/resources/application.yml`.

Quick commands (developer flows)
- Build & run (local): `./mvnw clean package && ./mvnw spring-boot:run` (the app default port is 8081 and context-path is `/api`).
- Run tests: `./mvnw test` (project uses JUnit via Spring Boot Starter Test). Use `-Dtest=...` to run individual tests.
- Generate OpenAPI UI: available automatically at `http://localhost:8081/api/swagger-ui.html` (see `src/main/java/.../SwaggerConfig.java`).

Project-specific conventions & patterns
- Package layout: features are grouped by module under `module/*` for domain logic (e.g. `module/product`, `module/user`, `module/sale`). Use the module package when adding new domain services/controllers.
- Configuration properties: classes annotated with `@ConfigurationProperties(prefix = "...")` (examples: `ResendConfig`, `CloudinaryConfig`) read values from `application.yml`. When adding new properties, bind them with a similar config class and enable via `@EnableConfigurationProperties` if needed.
- Beans & wiring: most services use Spring stereotypes (`@Service`, `@Configuration`, `@Controller`) and constructor or field `@Autowired` injection. Prefer creating `@Service` classes under the module package and keep stateless logic there.
- External keys in `application.yml`: Stripe, Resend, Cloudinary secrets are present in the repo for convenience but should be replaced by environment variables in real deployments. Use `@Value` (e.g. `StripeConfig`) or `@ConfigurationProperties` consistently for new integrations.

Integration points & important files
- Stripe: `src/main/java/.../service/payment/StripeConfig.java` sets `Stripe.apiKey` from `stripe.api.key.secret`.
- Resend (email): `src/main/java/.../config/ResendConfig.java` and templates in `src/main/resources/templates/email/` (see `EMAIL_SERVICE_README.md` for endpoints and variable names).
- Cloudinary (storage): `src/main/java/.../service/storage/config/CloudinaryConfig.java` creates a `Cloudinary` bean. Respect `allowed-formats` and `max-file-size` from `application.yml` when validating uploads.
- WebSocket: `src/main/java/.../service/realtime/config/WebSocketConfig.java` registers handlers at `/ws/stock`, `/ws/notifications`, `/ws/general`. Client docs in `WEBSOCKET_README.md` show message shapes and test REST endpoints under `/api/realtime/*`.
- Security/JWT: `application.yml` contains `application.security.jwt.*` values and `SwaggerConfig` declares the `bearerAuth` security scheme. Most controllers or classes use role-based access; review `module/user/infrastructure/config/SecurityConfig.java` for guard behavior.

Code patterns to follow when editing or adding files
- Controllers: annotate with `@RestController` and, when endpoints require auth, add `@SecurityRequirement(name = "bearerAuth")` or follow existing controllers' annotations.
- DTOs: located under module `dto` or `service/*/dto`; prefer plain POJOs with Lombok where used in repository. Keep JSON shape stable — controllers expect DTO field names matching templates (email, websocket payloads).
- Exception handling: use project `common/error` classes to create consistent error responses (check `src/main/java/com/assembliestore/api/common/error/`). Reuse existing response wrappers in `common/response`.
- Logging: use SLF4J logger instances (existing classes use `LoggerFactory`). Avoid printing secrets to logs.

Testing hints
- There are unit/integration tests under `src/test/java/...`. Tests use Spring Boot testing harness; use `@SpringBootTest` for integration tests when needing the full context.
- For fast feedback, mock external integrations (Stripe, Resend, Cloudinary, WebSocket) using Mockito or by replacing beans in test configuration.

Files/places to check for examples
- `src/main/resources/application.yml` — central runtime properties (ports, JWT config, 3rd-party keys, cloudinary limits).
- `src/main/java/com/assembliestore/api/config/*` — global config examples: `CorsConfig`, `SwaggerConfig`, `JacksonConfig`, `ResendConfig`.
- `src/main/java/com/assembliestore/api/service/*` — storage, email, payment, realtime service implementations.
- `src/main/java/com/assembliestore/api/module/*` — domain modules (product, sale, user) showing patterns for controllers, services, DTOs.
- `WEBSOCKET_README.md`, `EMAIL_SERVICE_README.md`, `SWAGGER_AUTH_GUIDE.md` — valuable, project-specific integration docs; mirror examples when implementing features.

Important constraints & gotchas (discovered)
- application.yml currently contains real-looking secret keys; do not copy them into public logs or new commits. Prefer environment variables or CI secrets for production.
- CORS: `CorsConfig` allows all origins with credentials — when targeting production, narrow `allowedOriginPatterns` to specific domains.
- WebSocket endpoints use `setAllowedOrigins("*")` — tests can use `ws://localhost:8081/api/...` as in `WEBSOCKET_README.md`.

Small assist tasks you can do automatically
- When adding a new external client/config, add a `@ConfigurationProperties` class, a unit test that injects the properties, and document the new keys in `application.yml.example` (if maintained).
- For endpoints that send emails or use Stripe, add a mock-based unit test that verifies the interaction and a small integration test that runs with test keys.

If you make a change, run the quick quality gates locally
1. `./mvnw -q -DskipTests clean package` (build) — ensure compile errors are fixed.
2. `./mvnw test -Dtest=...` (run targeted tests). Prefer running a focused test file during development.

If anything is unclear or you'd like specific examples added (e.g., how to mock Stripe, or a working test that fakes Resend), tell me which area and I will expand this file with concrete code snippets and tests.

# Repository Guidelines

## Project Structure & Module Organization
- Source code lives in `src/main/java/com/tanner/...`, organized by feature (for example `devconfig`, `datadictionary`, `module`, `patcher`).
- IntelliJ plugin metadata and assets live under `src/main/resources/`:
  - `META-INF/plugin.xml` defines actions, extensions, and tool windows.
  - `template/` and `config/` hold XML templates and YAML configuration files.
  - `img/` contains plugin icons and logos.
- Tests live in `src/test/java/com/tanner/` and follow the same package root.
- Build outputs go to `build/`.

## Build, Test, and Development Commands
- Use the Gradle wrapper from the repo root:
  - `./gradlew test` runs unit tests (JUnit 4).
  - `./gradlew build` compiles, tests, and packages the plugin.
  - `./gradlew runIde` launches a sandbox IntelliJ instance with the plugin.
  - `./gradlew verifyPlugin` runs IntelliJ plugin verification checks.
- If needed once on a fresh clone: `chmod +x gradlew`.
- Requirements: JDK 17 and IntelliJ Platform `2022.2` (see `build.gradle.kts`).

## Coding Style & Naming Conventions
- Follow standard Java conventions with 4-space indentation and UTF-8 source files.
- Keep packages under `com.tanner`.
- Use descriptive suffixes consistently:
  - Actions: `*Action` (for example `ExportPatcherAction`).
  - Dialogs: `*Dialog` / `*Dlg`.
  - Utilities: `*Util`.
- Prefer small, focused classes per feature area rather than cross-cutting “god” utils.

## Testing Guidelines
- Tests use JUnit 4 (`@org.junit.Test`).
- Name test classes `*Test` and place them alongside the relevant package root.
- Run `./gradlew test` before opening a PR.
- Add regression tests when fixing parsing, path handling, or database-specific logic.

## Commit & Pull Request Guidelines
- Match the existing history: short, direct, scoped messages (often in Chinese).
  - Good examples: `devconfig: 处理 oceanbase URL` or `patcher: 修复导出路径`.
- PRs should include:
  - What changed and why.
  - How to verify (commands run, sample inputs, or IDE steps).
  - Screenshots/GIFs for UI updates (`.form` dialogs, tool windows, actions).
- Avoid mixing unrelated refactors with behavior changes.

## Security & Configuration Tips
- Plugin publishing/signing uses environment variables (`CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`, `PUBLISH_TOKEN`). Never commit secrets.
- Treat files in `src/main/resources/template/` and `src/main/resources/config/` as user-facing contracts; change them carefully and document impacts in the PR.

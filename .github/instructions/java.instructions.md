---
applyTo: "runtime/**/*.java,deployment/**/*.java,integration-tests/**/*.java,**/pom.xml"
---

When editing Java or Maven files in this repository:

- Keep runtime CDI behavior, deployment/build-time integration, and integration-test coverage separate by module responsibility.
- Prefer configuration and extension wiring that matches normal Quarkus extension patterns already present in the repository.
- Keep parent POM changes compatible with all modules, not just one submodule.
- If user-facing extension setup changes, update the README in the same change.

# NeoForge 1.21.1 Agent Rules

## Build (JDK 21)

./gradlew build runClient runServer genIntellijRuns

CI: .github/workflows/

## Code Style

- Java 21, preserve existing formatting.
- PascalCase classes, camelCase methods/fields, UPPER_SNAKE_CASE constants.
- Use @NotNull/@Nullable; prefer existing dependencies; proper imports (no FQCN in bodies).

### Mixins

- Under mixin/, use @Mixin, match target package layout.
- Update <namespace>.mixins.json on changes.
- @Unique members with gt$ prefix.
- Prefer @Inject(cancellable=true) over @Overwrite (latter needs @author + @reason).
- Helper classes stay out of mixin/.
- Fallback: reflection → VarHandle → ASM (last resort). Mixins may target any dependency.

## Agent Constraints

- Target: NeoForge 1.21.1 only. No Fabric/multi-loader.
- Don't touch build.gradle/settings.gradle unless asked.
- Surgical edits only — no import reordering, reformatting, or unrelated fixes.
- Configuration goes in config/, not scattered constants.
- Smallest working implementation — no speculative abstractions, no unfinished replacements.
- Keep modules separated; check existing APIs before adding new libs.
- If working directory vanishes, create a new one.

## Localization Encoding

- Keep `src/main/resources/assets/neoguanniao/lang/*.json` as UTF-8 without BOM.
- Never write Chinese localization files through a shell command or API that uses the system code page (for example PowerShell `Set-Content` without an explicit UTF-8 mode).
- Prefer `apply_patch` for small localization edits, or write with an explicit UTF-8 encoder.
- After changing localization, validate both JSON and encoding with:
  `@'\nimport json\nfrom pathlib import Path\nfor path in Path("src/main/resources/assets/neoguanniao/lang").glob("*.json"):\n    data = path.read_bytes()\n    json.loads(data.decode("utf-8"))\n    assert data.count(b"?") == 0 or path.name != "zh_cn.json"\n'@ | python -`

## Testing

No formal suite. Verify with:
- ./gradlew build
- ./gradlew runClient for gameplay
- Logs: run/logs/latest.log, run/logs/debug.log, run/crash-reports/
- For compat fixes: test with and without target mod.

## Git

- Short descriptive commits.
- PRs: explain what/why; screenshots for visuals.

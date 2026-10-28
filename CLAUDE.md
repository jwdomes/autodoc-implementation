# autodoc-implementation

The reference app for [autodoc](../autodoc), the flow-docs library. It's a Spring Boot router
(`main-app`) that calls up to three partner services (`partner-a/b/c`), each with its own seeded
Postgres database. It also has a standalone WireMock container and a Bruno collection for calling the
mocks.

main-app uses autodoc as a third-party jar, `com.example:autodoc:0.1.0`, read from `~/.m2`. Run
`./gradlew publishToMavenLocal` in `../autodoc` first, and again after any autodoc change.

## Layout

```
Dockerfile        wiremock/wiremock:3.13.1 with mappings/ baked in, --verbose, plain HTTP/2 off
mappings/         stub mappings loaded at startup, one feature per file
bruno/            Bruno collection (open this folder in Bruno, pick the "local" environment)
  calls/          one request per mapping behaviour, each with a docs block saying what to expect
  admin/          WireMock admin API: list, create, reset mappings; request journal
  mocks/          original examples (Get user, Get order)
  search/         POST /search against the router, one request per routing case
  environments/   local.bru -> baseUrl http://localhost:8080 (WireMock), appUrl http://localhost:8090
compose.yaml      Postgres 17 on localhost:5442 (automockit uses the same port: run one at a time)
docker/           init script creating partner_a, partner_b, partner_c databases
partner-clients/  stand-in for the separate client repo: PartnerXClient and New/LegacyIdProviderClient
                  interfaces + RestClient implementations (com.example.partnerclients), wired by
                  auto-configuration
main-app/         router, com.example.router, port 8090; flow docs in main-app/docs/
partner-a/        com.example.partner.a, port 8091, POST /partner-a/matches
partner-b/        com.example.partner.b, port 8092, POST /partner-b/matches
partner-c/        com.example.partner.c, port 8093, POST /partner-c/matches
```

## Router and partners

`POST /search` takes `{header, documents[], person}`. `SearchController` creates a `ResponseDto`.

`Router` first resolves a canonical `personId` through `IdProviderService`. It uses the new ID
provider if `id-provider.use-new` is true, and the legacy one otherwise (the default). This is an
inline if/else in `route()`.

It then runs A, B, then C. For each partner, its `PartnerXService.shouldCall` decides whether
to call it, and `PartnerXService.call` makes the call through the `PartnerXClient` interface.

| Partner | Called when | Sent |
| --- | --- | --- |
| A | SSN and a valid `PASSPORT` | firstName, lastName, documentNumber, personId (=SSN) |
| B | PNumber and a valid `DRIVER_LICENSE` | firstName, lastName, birthDate, documentNumber, pNumber |
| C | SSN | lastName, birthDate, personIdentifier (=SSN) |

- "Valid" means the number matches the regex on `DocumentType`. The patterns are placeholders.
- `results` has one entry per partner that was called. If a partner call fails, the router
  returns 502.
- Partners find candidates by identifier or document number and score them with weighted field
  matches, highest first. Ties are broken by name so the order is deterministic.
- Seed data lives in each partner's `db/migration/V2__seed.sql` (Flyway).
- There is deliberately no shared module, because each partner owns its contract.
- **Client code.** The clients live in `partner-clients`, mirroring the enterprise setup:
  - Client interfaces and implementations sit together in a separate repo the team owns. The
    partners own their APIs.
  - main-app only sees the interfaces and converts `PartnerMatch` into its own `PersonMatch`.
  - `PartnerClientsAutoConfiguration` registers one client per partner at `partners.x.base-url`.
  - The module has no autodoc dependency.

## Running

```bash
docker build -t wiremock-local .
docker run -d --rm --name wiremock -p 8080:8080 wiremock-local
docker stop wiremock          # container removes itself
```

Router and partners (Java 21; Gradle comes from the wrapper):

```bash
docker compose up -d                 # Postgres; Flyway seeds each partner DB on first start
./gradlew build                      # compiles and runs unit tests
./gradlew :partner-a:bootRun         # likewise partner-b, partner-c, main-app (separate terminals)
```

Mappings are baked into the image, so changes under `mappings/` need a rebuild and restart.
Mappings added through the admin API live in memory only and are lost on restart.

Useful admin endpoints: `GET /__admin/mappings`, `POST /__admin/mappings`,
`POST /__admin/mappings/reset` (back to the baked-in set), `POST /__admin/scenarios/reset`,
`GET /__admin/requests`, `GET /__admin/health`.

## Current mappings

| File | Endpoint(s) | Demonstrates |
| --- | --- | --- |
| `get-user.json` | `GET /api/users/1` | Plain static stub |
| `greeting-templated.json` | `GET /api/greeting?name=` | Response templating from the request |
| `create-user.json` | `POST /api/users` | JSON body matching; priority fallback to 400 |
| `secure.json` | `GET /api/secure/profile` | Header matching (`Bearer let-me-in`); fallback 401 |
| `faults.json` | `/api/slow`, `/api/unavailable`, `/api/broken` | Random delay, 503 + Retry-After, connection reset |
| `cart-scenario.json` | `GET /api/cart`, `POST /api/cart/items` | Stateful scenario |
| `id-providers.json` | `POST /new-id-provider/lookup`, `POST /legacy-id-provider/lookup` | Stand-ins for the two ID providers; templated `personId`; priority 10 so scenario stubs win |

## Conventions

- A file holds either one mapping or `{"mappings": [...]}` for a related group.
- Fallbacks use `priority`: the specific match gets 1, the catch-all gets 10.
- Templated responses need `"transformers": ["response-template"]`.
- Stubs meant for the flow scenarios or Gatling must match on a request key no other stub
  uses, since Gatling loads every stub at once.
- Every new mapping gets a matching request in `bruno/calls/` with a `docs` block.
- WireMock can't run arbitrary scripts. Custom logic (e.g. decrypting a request) means a Java
  extension jar copied into `/var/wiremock/extensions/` in the image.

## Verifying changes

Rebuild, start the container, and curl each affected endpoint (and `/__admin/mappings` for the
loaded count) before calling a change done. The Bruno CLI (`bru`) is not installed; the desktop
app is.

## Flow docs (autodoc)

The full guide is autodoc's README. Here's how main-app uses it:

- **Annotations.** `@FlowStep` only, on the controller, `Router.route`, `IdProviderService`'s two
  lookups, `PartnerXService.call`, and `SearchExceptionHandler`.
  - Participants are constants in `Participants`, and step actions are constants in `Steps`.
  - Functional code is never changed for the docs.
- **On/off.** `autodoc.observations.enabled=true` turns recording on. Unset, autodoc registers
  nothing, and `FlowObservationsOffTest` checks this.
- **Scenarios.** `main-app/src/test/resources/flow-scenarios/*.json` hold `description`, `method`,
  `path`, optional `properties`, `request`, `expectedStatus`, `expect` and `stubs`.
  - **`expect`** uses WireMock syntax. Each `calls` entry is a WireMock request pattern plus `count`
    (exact; omitted means at least once) and `why`. `response.bodyPatterns` checks the router's
    response.
  - **Partner decisions live in the expectations.** Each scenario states which partners and ID
    provider are called or not, with a `why` taken from the partner rules above.
  - **Labels** for endpoints and fields are in `main-app/src/test/resources/autodoc-glossary.json`.
  - **One test class per property set.** `FlowScenarioDocsTest` runs the defaults, and
    `NewIdProviderScenarioDocsTest` runs `id-provider.use-new=true`. Shared wiring is in
    `ScenarioDocsTestSupport`.
  - **Unique keys.** Give each scenario its own SSN or PNumber, and match stubs on it: `$.personId`
    (A), `$.pNumber` (B), `$.personIdentifier` (C). The ID provider stub matches the scenario's
    `$.lastName`.
  - **Stub responses** need `Content-Type: application/json`. Don't use random delays.
- **Docs.** `main-app/docs/` is written by every test run and committed.
  - `-Pautodoc.docs.formats=` limits the formats (`markdown`, `markdown-image`, `mermaid`).
  - `-Pautodoc.docs.verify=true` fails on stale docs instead of writing them.
  - `markdown-image/` is the Confluence set, with diagrams as SVG images.
- **Rules.** `AutodocRulesTest` includes `AutodocRules`, plus
  `downstreamCallsAreSteps("com.example.partnerclients..")`.
- **WireMock and HTTP/2.** Spring's RestClient uses the JDK HttpClient, which tries an h2c upgrade
  that WireMock's Jetty resets. It's turned off on the WireMock side: `http2PlainDisabled(true)` in
  tests, and `--disable-http2-plain` in the Dockerfile.

Gatling load test (`main-app/src/gatling/java/.../FlowScenariosSimulation.java`):

```bash
docker run -d --rm --name wiremock -p 8080:8080 wiremock-local
java -jar main-app/build/libs/main-app.jar --spring.profiles.active=wiremock --autodoc.observations.enabled=true
./gradlew :main-app:gatlingRun        # env: APP_URL, WIREMOCK_URL, PARTNER_DELAY_MS, PEAK_USERS_PER_SEC, RAMP_SECONDS, GATLING_PROPERTIES
curl 'localhost:8090/actuator/metrics/autodoc.step?tag=autodoc.participant:PARTNER_A'
```

`before()` resets WireMock and loads every scenario's stubs. `GATLING_PROPERTIES`, for example
`id-provider.use-new=true`, selects a property set's scenarios. Start the app with the same settings.

## Open questions

- **Decryption in WireMock.** Proposed approach: a Java extension jar, either a template helper
  (`{{decrypt request.body}}`) to put plain values in responses, or a request matcher to choose a
  response by decrypted content. Waiting on the user: which of the two is needed, and the
  algorithm and key source.
- **`bruno/mocks/` vs `bruno/calls/`.** Undecided whether to move Get user / Get order into
  `calls/` and drop `mocks/`.

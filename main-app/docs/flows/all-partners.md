# all-partners

SSN, PNumber, valid passport and license: A, B and C are all called

**Status:** 200 (expected 200) · **Called:** Legacy ID Provider, Partner A, Partner B, Partner C

## Expected behavior

- **Legacy ID provider lookup** is called once — The new ID provider is switched off, which is the default.
  - Last name is Ames
- **New ID provider lookup** is not called.
- **Partner A match search** is called once — Partner A needs an SSN and a valid passport.
  - Person ID is 900000001
  - Document number is FA0000001
- **Partner B match search** is called once — Partner B needs a PNumber and a valid driver license.
  - PNumber is PN900001
  - Document number is F00000001
  - Birth date is 1980-01-01
- **Partner C match search** is called once — Partner C needs an SSN.
  - Person identifier (SSN) is 900000001
  - First name is not provided
- **The response:**
  - Person ID is LEGACY-AMES
  - Results has 3 entries
  - Partner (Results #1) is PARTNER_A
  - Partner (Results #2) is PARTNER_B
  - Partner (Results #3) is PARTNER_C
  - Matches (Results #2) has 2 entries

## Request

<details><summary>POST /search</summary>

```json
{
  "header" : {
    "requestId" : "all-partners",
    "source" : "flow-scenario",
    "timestamp" : "2026-10-20T12:00:00Z"
  },
  "documents" : [ {
    "documentType" : "PASSPORT",
    "documentNumber" : "FA0000001"
  }, {
    "documentType" : "DRIVER_LICENSE",
    "documentNumber" : "F00000001"
  } ],
  "person" : {
    "first" : "Fiona",
    "last" : "Ames",
    "ssn" : "900000001",
    "pNumber" : "PN900001",
    "birthDate" : "1980-01-01"
  }
}
```

</details>

## Flow

```mermaid
sequenceDiagram
    %% SSN, PNumber, valid passport and license: A, B and C are all called
    participant UPSTREAM as Upstream
    participant ROUTER as Router
    participant LEGACY_ID_PROVIDER as Legacy ID Provider
    participant PARTNER_A as Partner A
    participant PARTNER_B as Partner B
    participant PARTNER_C as Partner C
    UPSTREAM->>ROUTER: Receive search request
    ROUTER->>ROUTER: Determine downstream partners
    ROUTER->>LEGACY_ID_PROVIDER: Call Legacy ID Provider
    LEGACY_ID_PROVIDER-->>ROUTER: response
    Note over ROUTER,PARTNER_A: Using passport FA0000001
    ROUTER->>PARTNER_A: Call Partner A
    PARTNER_A-->>ROUTER: response
    Note over ROUTER,PARTNER_B: Using driver license F00000001
    ROUTER->>PARTNER_B: Call Partner B
    PARTNER_B-->>ROUTER: response
    ROUTER->>PARTNER_C: Call Partner C
    PARTNER_C-->>ROUTER: response
    ROUTER-->>UPSTREAM: 200 OK
```

## Downstream calls

### POST /legacy-id-provider/lookup → 200

Matched `stubs[0]`.

<details><summary>Request sent</summary>

```json
{
  "firstName" : "Fiona",
  "lastName" : "Ames",
  "birthDate" : "1980-01-01",
  "ssn" : "900000001",
  "pNumber" : "PN900001"
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "personId" : "LEGACY-AMES"
}
```

</details>

### POST /partner-a/matches → 200

Matched `stubs[1]`.

<details><summary>Request sent</summary>

```json
{
  "firstName" : "Fiona",
  "lastName" : "Ames",
  "documentNumber" : "FA0000001",
  "personId" : "900000001"
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "matches" : [ {
    "firstName" : "Fiona",
    "lastName" : "Ames",
    "birthDate" : "1980-01-01",
    "matchProbability" : 1.0
  } ]
}
```

</details>

### POST /partner-b/matches → 200

Matched `stubs[2]`.

<details><summary>Request sent</summary>

```json
{
  "firstName" : "Fiona",
  "lastName" : "Ames",
  "birthDate" : "1980-01-01",
  "documentNumber" : "F00000001",
  "pNumber" : "PN900001"
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "matches" : [ {
    "firstName" : "Fiona",
    "lastName" : "Ames",
    "birthDate" : "1980-01-01",
    "matchProbability" : 1.0
  }, {
    "firstName" : "Fiona",
    "lastName" : "Amos",
    "birthDate" : "1980-01-01",
    "matchProbability" : 0.7
  } ]
}
```

</details>

### POST /partner-c/matches → 200

Matched `stubs[3]`.

<details><summary>Request sent</summary>

```json
{
  "lastName" : "Ames",
  "birthDate" : "1980-01-01",
  "personIdentifier" : "900000001"
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "matches" : [ {
    "firstName" : "Fiona",
    "lastName" : "Ames",
    "birthDate" : "1980-01-01",
    "matchProbability" : 1.0
  } ]
}
```

</details>

## Response

<details><summary>200 OK</summary>

```json
{
  "requestId" : "all-partners",
  "personId" : "LEGACY-AMES",
  "results" : [ {
    "partner" : "PARTNER_A",
    "matches" : [ {
      "firstName" : "Fiona",
      "lastName" : "Ames",
      "birthDate" : "1980-01-01",
      "matchProbability" : 1.0
    } ]
  }, {
    "partner" : "PARTNER_B",
    "matches" : [ {
      "firstName" : "Fiona",
      "lastName" : "Ames",
      "birthDate" : "1980-01-01",
      "matchProbability" : 1.0
    }, {
      "firstName" : "Fiona",
      "lastName" : "Amos",
      "birthDate" : "1980-01-01",
      "matchProbability" : 0.7
    } ]
  }, {
    "partner" : "PARTNER_C",
    "matches" : [ {
      "firstName" : "Fiona",
      "lastName" : "Ames",
      "birthDate" : "1980-01-01",
      "matchProbability" : 1.0
    } ]
  } ]
}
```

</details>

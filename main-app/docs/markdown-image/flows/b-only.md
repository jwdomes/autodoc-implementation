# b-only

PNumber and valid license but no SSN: only B is called

**Status:** 200 (expected 200) · **Called:** Legacy ID Provider, Partner B

## Expected behavior

- **Legacy ID provider lookup** is called once — The new ID provider is switched off, which is the default.
  - Last name is Cole
- **New ID provider lookup** is not called.
- **Partner A match search** is not called — Partner A needs an SSN, and this person has none.
- **Partner B match search** is called once — Partner B needs a PNumber and a valid driver license.
  - PNumber is PN900003
  - Document number is F00000003
  - Birth date is 1990-09-09
- **Partner C match search** is not called — Partner C needs an SSN, and this person has none.
- **The response:**
  - Person ID is LEGACY-COLE
  - Results has 1 entry
  - Partner (Results #1) is PARTNER_B

## Request

<details><summary>POST /search</summary>

```json
{
  "header" : {
    "requestId" : "b-only",
    "source" : "flow-scenario",
    "timestamp" : "2026-10-20T12:00:00Z"
  },
  "documents" : [ {
    "documentType" : "DRIVER_LICENSE",
    "documentNumber" : "F00000003"
  } ],
  "person" : {
    "first" : "Hana",
    "last" : "Cole",
    "pNumber" : "PN900003",
    "birthDate" : "1990-09-09"
  }
}
```

</details>

## Flow

![Sequence diagram for b-only](b-only.svg)

## Downstream calls

### POST /legacy-id-provider/lookup → 200

Matched `stubs[0]`.

<details><summary>Request sent</summary>

```json
{
  "firstName" : "Hana",
  "lastName" : "Cole",
  "birthDate" : "1990-09-09",
  "ssn" : null,
  "pNumber" : "PN900003"
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "personId" : "LEGACY-COLE"
}
```

</details>

### POST /partner-b/matches → 200

Matched `stubs[1]`.

<details><summary>Request sent</summary>

```json
{
  "firstName" : "Hana",
  "lastName" : "Cole",
  "birthDate" : "1990-09-09",
  "documentNumber" : "F00000003",
  "pNumber" : "PN900003"
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "matches" : [ {
    "firstName" : "Hana",
    "lastName" : "Cole",
    "birthDate" : "1990-09-09",
    "matchProbability" : 1.0
  } ]
}
```

</details>

## Response

<details><summary>200 OK</summary>

```json
{
  "requestId" : "b-only",
  "personId" : "LEGACY-COLE",
  "results" : [ {
    "partner" : "PARTNER_B",
    "matches" : [ {
      "firstName" : "Hana",
      "lastName" : "Cole",
      "birthDate" : "1990-09-09",
      "matchProbability" : 1.0
    } ]
  } ]
}
```

</details>

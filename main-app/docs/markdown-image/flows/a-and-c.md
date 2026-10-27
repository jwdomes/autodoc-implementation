# a-and-c

SSN and valid passport but no PNumber: A and C are called, B is skipped

**Status:** 200 (expected 200) · **Called:** Legacy ID Provider, Partner A, Partner C

## Expected behavior

- **Legacy ID provider lookup** is called once — The new ID provider is switched off, which is the default.
  - Last name is Baker
- **New ID provider lookup** is not called.
- **Partner A match search** is called once — Partner A needs an SSN and a valid passport.
  - Person ID is 900000002
  - Document number is FA0000002
- **Partner B match search** is not called — Partner B needs a PNumber, and this person has none.
- **Partner C match search** is called once — Partner C needs an SSN.
  - Person identifier (SSN) is 900000002
  - First name is not provided
- **The response:**
  - Person ID is LEGACY-BAKER
  - Results has 2 entries
  - Partner (Results #1) is PARTNER_A
  - Partner (Results #2) is PARTNER_C

## Request

<details><summary>POST /search</summary>

```json
{
  "header" : {
    "requestId" : "a-and-c",
    "source" : "flow-scenario",
    "timestamp" : "2026-10-20T12:00:00Z"
  },
  "documents" : [ {
    "documentType" : "PASSPORT",
    "documentNumber" : "FA0000002"
  }, {
    "documentType" : "DRIVER_LICENSE",
    "documentNumber" : "F00000002"
  } ],
  "person" : {
    "first" : "Gil",
    "last" : "Baker",
    "ssn" : "900000002",
    "birthDate" : "1975-05-05"
  }
}
```

</details>

## Flow

![Sequence diagram for a-and-c](a-and-c.svg)

## Downstream calls

### POST /legacy-id-provider/lookup → 200

Matched `stubs[0]`.

<details><summary>Request sent</summary>

```json
{
  "firstName" : "Gil",
  "lastName" : "Baker",
  "birthDate" : "1975-05-05",
  "ssn" : "900000002",
  "pNumber" : null
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "personId" : "LEGACY-BAKER"
}
```

</details>

### POST /partner-a/matches → 200

Matched `stubs[1]`.

<details><summary>Request sent</summary>

```json
{
  "firstName" : "Gil",
  "lastName" : "Baker",
  "documentNumber" : "FA0000002",
  "personId" : "900000002"
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "matches" : [ {
    "firstName" : "Gil",
    "lastName" : "Baker",
    "birthDate" : "1975-05-05",
    "matchProbability" : 1.0
  } ]
}
```

</details>

### POST /partner-c/matches → 200

Matched `stubs[2]`.

<details><summary>Request sent</summary>

```json
{
  "lastName" : "Baker",
  "birthDate" : "1975-05-05",
  "personIdentifier" : "900000002"
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "matches" : [ {
    "firstName" : "Gil",
    "lastName" : "Baker",
    "birthDate" : "1975-05-05",
    "matchProbability" : 1.0
  } ]
}
```

</details>

## Response

<details><summary>200 OK</summary>

```json
{
  "requestId" : "a-and-c",
  "personId" : "LEGACY-BAKER",
  "results" : [ {
    "partner" : "PARTNER_A",
    "matches" : [ {
      "firstName" : "Gil",
      "lastName" : "Baker",
      "birthDate" : "1975-05-05",
      "matchProbability" : 1.0
    } ]
  }, {
    "partner" : "PARTNER_C",
    "matches" : [ {
      "firstName" : "Gil",
      "lastName" : "Baker",
      "birthDate" : "1975-05-05",
      "matchProbability" : 1.0
    } ]
  } ]
}
```

</details>

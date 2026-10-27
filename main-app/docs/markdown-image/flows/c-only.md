# c-only

SSN with a malformed passport: only C is called

**Status:** 200 (expected 200) · **Called:** Legacy ID Provider, Partner C

## Expected behavior

- **Legacy ID provider lookup** is called once — The new ID provider is switched off, which is the default.
  - Last name is Diaz
- **New ID provider lookup** is not called.
- **Partner A match search** is not called — Partner A needs a valid passport, and BAD isn't a valid passport number.
- **Partner B match search** is not called — Partner B needs a PNumber, and this person has none.
- **Partner C match search** is called once — Partner C needs an SSN.
  - Person identifier (SSN) is 900000004
  - First name is not provided
- **The response:**
  - Person ID is LEGACY-DIAZ
  - Results has 1 entry
  - Partner (Results #1) is PARTNER_C
  - Matches (Results #1) has 0 entries

## Request

<details><summary>POST /search</summary>

```json
{
  "header" : {
    "requestId" : "c-only",
    "source" : "flow-scenario",
    "timestamp" : "2026-10-20T12:00:00Z"
  },
  "documents" : [ {
    "documentType" : "PASSPORT",
    "documentNumber" : "BAD"
  } ],
  "person" : {
    "first" : "Ivan",
    "last" : "Diaz",
    "ssn" : "900000004",
    "birthDate" : "1985-03-03"
  }
}
```

</details>

## Flow

![Sequence diagram for c-only](c-only.svg)

## Downstream calls

### POST /legacy-id-provider/lookup → 200

Matched `stubs[0]`.

<details><summary>Request sent</summary>

```json
{
  "firstName" : "Ivan",
  "lastName" : "Diaz",
  "birthDate" : "1985-03-03",
  "ssn" : "900000004",
  "pNumber" : null
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "personId" : "LEGACY-DIAZ"
}
```

</details>

### POST /partner-c/matches → 200

Matched `stubs[1]`.

<details><summary>Request sent</summary>

```json
{
  "lastName" : "Diaz",
  "birthDate" : "1985-03-03",
  "personIdentifier" : "900000004"
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "matches" : [ ]
}
```

</details>

## Response

<details><summary>200 OK</summary>

```json
{
  "requestId" : "c-only",
  "personId" : "LEGACY-DIAZ",
  "results" : [ {
    "partner" : "PARTNER_C",
    "matches" : [ ]
  } ]
}
```

</details>

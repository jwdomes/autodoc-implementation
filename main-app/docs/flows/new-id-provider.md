# new-id-provider

New ID provider switched on: the person id comes from the new provider; no identifiers, so no partner is called

**Status:** 200 (expected 200) · **Called:** New ID Provider

## Expected behavior

- **New ID provider lookup** is called once — id-provider.use-new is switched on.
  - Last name is Grant
- **Legacy ID provider lookup** is not called.
- **Partner A match search** is not called — Partner A needs an SSN.
- **Partner B match search** is not called — Partner B needs a PNumber.
- **Partner C match search** is not called — Partner C needs an SSN.
- **The response:**
  - Person ID is NEW-GRANT
  - Results has 0 entries

## Request

<details><summary>POST /search</summary>

```json
{
  "header" : {
    "requestId" : "new-id-provider",
    "source" : "flow-scenario",
    "timestamp" : "2026-10-20T12:00:00Z"
  },
  "documents" : [ ],
  "person" : {
    "first" : "Mo",
    "last" : "Grant",
    "birthDate" : "1988-08-08"
  }
}
```

</details>

## Flow

```mermaid
sequenceDiagram
    %% New ID provider switched on: the person id comes from the new provider; no identifiers, so no partner is called
    participant UPSTREAM as Upstream
    participant ROUTER as Router
    participant NEW_ID_PROVIDER as New ID Provider
    UPSTREAM->>ROUTER: Receive search request
    ROUTER->>ROUTER: Determine downstream partners
    ROUTER->>NEW_ID_PROVIDER: Call New ID Provider
    NEW_ID_PROVIDER-->>ROUTER: response
    ROUTER-->>UPSTREAM: 200 OK
```

## Downstream calls

### POST /new-id-provider/lookup → 200

Matched `stubs[0]`.

<details><summary>Request sent</summary>

```json
{
  "firstName" : "Mo",
  "lastName" : "Grant",
  "birthDate" : "1988-08-08",
  "ssn" : null,
  "pNumber" : null
}
```

</details>

<details><summary>Response returned</summary>

```json
{
  "personId" : "NEW-GRANT"
}
```

</details>

## Response

<details><summary>200 OK</summary>

```json
{
  "requestId" : "new-id-provider",
  "personId" : "NEW-GRANT",
  "results" : [ ]
}
```

</details>

# Timezone ID and Display name converter (Sample in Java)

REST API to convert between Timezone ID and Display Name.

## Prerequisites

- Java 11
- Azure (this API is hosted on Azure Functions.)
- Azure CLI or PowerShell
- Azure Functions Core tools

## Syntax

~~~
GET https://<azure functions URL>/api/timezone-conversion?{short=TimeZoneID|long=Display Name}
~~~

- Both TimeZone ID and Display Name are case-sensitive (case ignorance is not implemented).

### Query parameter

- short : Timezone ID (e.g. `Asia/Tokyo`)
- long  : Display Name (e.g. `Japan Time`)

### Response example

#### HTTP 200 (OK)

```
GET /api/timezone-conversion?short=Asia/Tokyo
{
    "shortId":"Asia/Tokyo",
    "displayName":"Japan Time",
    "description":"Timezone display name mapped to Short Id Asia/Tokyo is Japan Time."
}

GET /api/timezone-conversion?long=Japan Time
{
    "shortId":"Asia/Tokyo",
    "displayName":"Japan Time",
    "description":"Short Id mapped to Timezone display name Japan Time is Asia/Tokyo."
}
```

### HTTP 403 (Forbidden)

```
GET /api/timezone-conversion
{
    "shortId": "",
    "displayName": "",
    "description": "No query parameter (Short Id or Timezone Display Name) is specified."
}

GET /api/timezone-conversion?long=Japan Time&short=Asia/Tokyo
StatusCode: 403
{
    "shortId":"Asia/Tokyo",
    "displayName":"Japan Time",
    "description":"Both query parameters (Short Id and Timezone Display Name) are specified."
}
```

### HTTP 404 (Not found)

```
GET /api/timezone-conversion?long=Asia/Tokyo
{
    "shortId": "",
    "displayName": "Asia/Tokyo",
    "description": "Short Id mapped to Timezone display name Asia/Tokyo is not found."
}

GET /api/timezone-conversion?short=Japan Time
{
    "shortId": "Japan Time",
    "displayName": "",
    "description": "Timezone display name mapped to Short Id Japan Time is not found."
}

GET /api/timezone-conversion?long
{
    "shortId":"",
    "displayName":"",
    "description":"No query parameter for Timezone display name is specified."
}

GET /api/timezone-conversion?short
{
    "shortId":"",
    "displayName":"",
    "description":"No query parameter for Short Id is specified."
}
```
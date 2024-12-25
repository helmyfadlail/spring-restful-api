# User API Spec

## Register User

Endpoint : POST /api/users

Request Body :

```json
{
    "username": "helmy_fadlail",
    "password": "rahasia",
    "name": "helmy Fadlail"
}
```

Response Body (Success) :

```json
{
    "data": "OK"
}
```

Response Body (Failed) :

```json
{
    "errors": "Username must not blank, ???"
}
```

## Login User

Endpoint : POST /api/auth/login

Request Body :

```json
{
    "username": "helmy_fadlail",
    "password": "rahasia"
}
```

Response Body (Success) :

```json
{
    "data": {
        "token": "TOKEN",
        "expiredAt": 2342342423423 // milliseconds
    }
}
```

Response Body (Failed, 401) :

```json
{
    "errors": "Username or password wrong"
}
```

## Get User

Endpoint : GET /api/users/current

Request Header :

-   X-API-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
    "data": {
        "username": "helmy_fadlail",
        "name": "Helmy Fadlail"
    }
}
```

Response Body (Failed, 401) :

```json
{
    "errors": "Unauthorized"
}
```

## Update User

Endpoint : PATCH /api/users/current

Request Header :

-   X-API-TOKEN : Token (Mandatory)

Request Body :

```json
{
    "name": "Helmy Fadlail Albab", // put if only want to update name
    "password": "new_rahasia" // put if only want to update password
}
```

Response Body (Success) :

```json
{
    "data": {
        "name": "Helmy Fadlail Albab",
        "password": "new_rahasia"
    }
}
```

Response Body (Failed, 401) :

```json
{
    "errors": "Unauthorized"
}
```

## Logout User

Endpoint : DELETE /api/auth/logout

Request Header :

-   X-API-TOKEN : Token (Mandatory)

Response Body (Success) :

```json
{
    "data": "OK"
}
```

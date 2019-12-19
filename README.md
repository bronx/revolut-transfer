# Revolut Transfer Service

Built with Kotlin ❤️, Javalin & Jetbrains Exposed 


## Account

### Create account
POST /accounts
```
{
    "name": "Jane Doe"
}
```

Expected response:
```
{
    "id": "4a39fc97-12c0-4c53-8aa0-82ba28996657",
    "name": "Jane Doe",
    "balance": 0
}
```

### Retrieve account
GET /accounts/:id

Expected response:
```
{
    "id": "4a39fc97-12c0-4c53-8aa0-82ba28996657",
    "name": "Jane Doe",
    "balance": 0
}
```

## Transactions

### Add funds (deposit) to an account
POST /transactions
```
{
    "type": "DEPOSIT",
    "amount": 8231.23,
    "destination": "4a39fc97-12c0-4c53-8aa0-82ba28996657"
}
```

Expected response: 
```
{
    "id": "1057affc-71a0-40bb-9164-f2ddeff7e408",
    "amount": 8231.23,
    "originAccount": null,
    "destinationAccount": "4a39fc97-12c0-4c53-8aa0-82ba28996657",
    "type": "DEPOSIT",
    "timestamp": "2019-12-18T11:37:09.057+01:00"
}
```

### Transfer funds from origin to destination accounts 
POST /transactions
```
{
    "type": "TRANSFER",
    "amount": 123.45,
    "origin": "4a39fc97-12c0-4c53-8aa0-b5aceff7e92e",
    "destination": "4a39fc97-12c0-9164-8aa0-82ba28996657"
}
```

```
{
    "id": "1057affc-71a0-4c53-9164-f2ddeff7e408",
    "amount": 123.45,
    "originAccount": "4a39fc97-12c0-4c53-8aa0-b5aceff7e92e",
    "destinationAccount": "4a39fc97-12c0-9164-8aa0-82ba28996657",
    "type": "TRANSFER",
    "timestamp": "2019-12-18T11:43:19.002+01:00"
}
```

## Tests
The following integration tests were implemented:

| Test                                                                                                             | Context     | Result |
|------------------------------------------------------------------------------------------------------------------|-------------|--------|
| Should get a 400 (Bad Request) when trying to create an account with an invalid JSON object()                    | Account     | Passed |
| Should successfully create an account()                                                                          | Account     | Passed |
| Should get a 404 (Not Found) when trying to get a non-existent account()                                        | Account     | Passed |
| Should successfully get an existent account()                                                                    | Account     | Passed |
| Should get a 400 (Bad Request) when trying to create a transaction with an invalid JSON object()                 | Transaction | Passed |
| Should get a 400 (Bad Request) when trying to create a transfer but not providing an origin account()            | Transaction | Passed |
| Should get a 400 (Bad Request) when trying to create a transfer from a non-existent origin account()             | Transaction | Passed |
| Should get a 400 (Bad Request) when trying to create a transfer from an origin account with insufficient funds() | Transaction | Passed |
| Should get a 400 (Bad Request) when trying to create a transfer from same origin and destination()               | Transaction | Passed |
| Should get a 400 (Bad Request) when trying to create a transfer to a non-existent destination account()          | Transaction | Passed |
| Should get a 400 (Bad Request) when trying to create a transfer with a negative value()                          | Transaction | Passed |
| Should successfully create a debit transaction()                                                                 | Transaction | Passed |
| Should successfully create a deposit transaction()                                                               | Transaction | Passed |
| Should successfully create a transfer()                                                                          | Transaction | Passed |


## Build
To build, simply run the following command: 

`/gradlew clean build`

This will run the tests and generate the jar file in the `build/libs/` folder.

## Executing

The jar file is located in the project's root folder. It required Java 13. 

`java -jar transfer-0.0.1-SNAPSHOT.jar`
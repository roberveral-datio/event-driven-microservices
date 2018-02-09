# User Service

This service manages users in the system.

## REST API

It registers new users and allow to query the registered users. For the sake of this, 
it has a REST API for user management:

Method | Endpoint | Description
--- | --- | ---
GET | /users | Get all registered users.
GET | /users/:id | Get the user with the given id.
POST | /users | Registers a new user.

A user is defined with the following JSON:

```json
{ 
    "id": 1,
    "firstName": "Mr.", 
    "lastName": "Meeseeks",
    "age": 2018,
    "email": "meeseeks@rick.me"
}
```

## Event driven

When a user is registered, the service writes a `UserCreated` event in the *users* topic. It doesn't store 
anything in a local DB yet, it only writes the event in Kafka. This is called **Event Sourcing**: using the 
stream of events as the source of truth.

Obviously, we need to fetch the user information for performing queries and validations (for instance, that the
user id is not repeated). To achieve this, there is a pattern known as **Materialized Views**. The service is 
consuming user events from the Kafka topics and with the events generates a table in an in-memory DB with all the
user information. When a UserCreated is read, a user is inserted, and when a UserNotified is read, a "notified" flag
is set to true for the affected user.

This separation between writes and reads is also known as **CQRS** pattern.

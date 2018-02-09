# Notification Service

This service sends a notification to a configured Slack webhook every time a new user
is registered.

This is an example of a small reactive microservice, which only reacts to an event and 
produces a new event after some processing.

In this case, the service will consume the 'users' Kafka topic and every time a `UserCreated` 
event is read, it will send the notification to Slack and generate a `UserNotified` event. This violates 
the *single writer principle* for the users topic, but it makes the example easier to understand.

There's no coupling with the User service, the services doesn't know about each other, they collaborate
by only reacting to events.

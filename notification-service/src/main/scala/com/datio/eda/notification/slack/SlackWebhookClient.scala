package com.datio.eda.notification.slack

import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Client to send messages to a Slack Webhook.
  *
  * @param wsClient    http client to use.
  * @param webhookUrl  url of the webhook. (xxxx/yyyy/zzzz)
  * @param channel     channel where messages are sent.
  * @param botUsername username of the bot (appears in the sent message).
  * @param ec          execution context.
  */
case class SlackWebhookClient(wsClient: WSClient,
                              webhookUrl: String,
                              channel: String,
                              botUsername: String = "eda-notification")
                             (implicit ec: ExecutionContext) {

  /**
    * Sends a message to the configured webhook.
    *
    * https://api.slack.com/incoming-webhooks
    *
    * @param msg message to send.
    * @return HTTP status code of the Slack response.
    */
  def sendMessage(msg: String): Future[Int] =
    wsClient.url(s"https://hooks.slack.com/services/$webhookUrl")
      .post(Json.obj("channel" -> channel, "username" -> botUsername, "text" -> msg))
      .map(_.status)
}

package com.datio.eda.users.utils

import java.util.Date

import akka.event.LoggingAdapter
import org.apache.kafka.clients.producer.RecordMetadata

trait ProducerUtils {

  /**
    * Method for logging the result of a kafka producer storing an event
    * in Kafka.
    *
    * @param metadata metadata of the inserted event (if success, null otherwise).
    * @param exception exception thrown (if error, null otherwise)
    */
  def printSendResult(metadata: RecordMetadata,
                      exception: Exception)
                     (implicit logger: LoggingAdapter): Unit = {
    if (metadata != null)
      logger.info(s"Written!! Topic: ${metadata.topic} | Partition: ${metadata.partition} |" +
        s" Offset: ${metadata.offset} | Timestamp: ${new Date(metadata.timestamp)}")
    if (exception != null)
      logger.error(exception, "Error writting a message in Kafka")
  }
}

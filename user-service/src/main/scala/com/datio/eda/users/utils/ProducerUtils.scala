package com.datio.eda.users.utils

import java.util.Date

import org.apache.kafka.clients.producer.RecordMetadata

trait ProducerUtils {
  def printSendResult(metadata: RecordMetadata, exception: Exception): Unit = {
    if (metadata != null)
      println(s"Written!! Topic: ${metadata.topic} | Partition: ${metadata.partition} |" +
        s" Offset: ${metadata.offset} | Timestamp: ${new Date(metadata.timestamp)}")
    if (exception != null)
      println(exception)
  }
}

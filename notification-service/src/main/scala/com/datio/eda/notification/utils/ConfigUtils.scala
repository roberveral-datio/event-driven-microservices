package com.datio.eda.notification.utils

import java.util.Properties

import com.typesafe.config.Config

trait ConfigUtils {

  /**
    * Converts a Typesafe Config object to Java Properties.
    *
    * @param config config to convert.
    * @return Properties containing all the parameters in config.
    */
  def propsFromConfig(config: Config): Properties = {
    import scala.collection.JavaConverters._

    val props = new Properties()

    val map: Map[String, Object] = config.entrySet().asScala.map({ entry =>
      entry.getKey -> entry.getValue.unwrapped()
    })(collection.breakOut)

    props.putAll(map.asJava)
    props
  }
}

package com.datio.eda.users.utils

import java.util.Properties

import com.typesafe.config.Config

trait ConfigUtils {

  /**
    * Converts a Typesafe Config object into Java Properties.
    *
    * @param config config to convert.
    * @return properties containing all the keys and values of
    *         the given config.
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

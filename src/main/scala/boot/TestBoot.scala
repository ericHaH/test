package boot

import com.typesafe.config.ConfigFactory

object TestBoot {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("application.conf")

    println("config"+config.toString)
    println("configs"+config.getString("mode"))
  }
}

package qianhub.libs.boot

import java.io.File
import java.lang.management.ManagementFactory

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import org.apache.commons.io.FileUtils

import scala.util.{Failure, Success, Try}

// 用于启动系统
trait Boot {

  val logger = Logger(this.getClass)

  // 进程 ID 的名字
  def pidKey: String = "pid.file"

  // 加载系统, configName 是加载文件的名称
  def initWith()(f: Config => Unit): Try[Unit] = {
    val fTry = Try {
      val config = ConfigFactory.load()
      logger.info("Server is starting with config {}", config)
      // 3 写入 PID
      serverPID.foreach { pid =>
        logger.info("Writing PID {} to {}", pid, pidFile)
        FileUtils.writeStringToFile(pidFile, pid, "utf-8")
        // 系统退出时再删除
        scala.sys.addShutdownHook {
          FileUtils.deleteQuietly(pidFile)
          logger.info("Delete PIDFile {}", pidFile)
        }
      }
      f(config)
    }
    fTry match {
      case Success(_)  => logger.info("Server Started")
      case Failure(ex) => logger.warn("InitSystemError:", ex)
    }
    fTry
  }

  // 获取当前进程的 ID
  def serverPID: Option[String] = ManagementFactory.getRuntimeMXBean.getName.split('@').headOption

  // 获取进程 ID 文件
  def pidFile: File = new File(Option(System.getProperty(pidKey)).getOrElse("RUNNING_PID")).getCanonicalFile

}

package qianhub.libs.model

// 版本信息
final case class RunningMode(value: String) extends AnyVal with StringMID {
  def isDev: Boolean = this == RunningMode.Dev
  def isProd: Boolean = this == RunningMode.Prod
  def isTest: Boolean = this == RunningMode.Test
  def isDemo: Boolean = this == RunningMode.Demo
}

object RunningMode {
  val Dev = RunningMode("Dev")
  val Test = RunningMode("Test")
  val Demo = RunningMode("Demo")
  val Prod = RunningMode("Prod")

  val VersionModels = Seq(Demo, Dev, Prod, Test)

  def valueOf(str: String): RunningMode = str.toUpperCase match {
    case "TEST" => Test
    case "DEMO" => Demo
    case "PROD" => Prod
    case _      => Dev
  }
}

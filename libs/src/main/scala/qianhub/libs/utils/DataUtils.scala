package qianhub.libs.utils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

/**
 * 数据处理
 */
trait DataUtils {

  // 重试操作是普通方法
  def retry[O](max: Int)(f: => O): Try[O] = {
    Try(f) match {
      case Failure(_) if max > 1 => retry(max - 1)(f)
      case r                     => r
    }
  }

  // 重试操作是 Future
  def retry2[O](max: Int)(f: => Future[O])(implicit ec: ExecutionContext): Future[O] = {
    f.andThen {
      case Failure(_) if max > 1 => retry2(max - 1)(f)
    }
  }

  /** 字符串星化 */
  def withStar(str: String) = {
    def star(pre: Int, post: Int) = str.take(pre) + "*" * (str.length - pre - post) + str.takeRight(post)
    str.length match {
      case i if i > 10 => star(3, 4)
      case i if i > 7  => star(2, 2)
      case i if i > 4  => star(1, 1)
      case i           => star(0, 0)
    }
  }

  /** 对字符串编码，主要是在 mysql 导出时能够正常导入 */
  private[this] val CharMap = Map(
    '\\' -> """\\""",
    '\u0000' -> """\0""",
    '\n' -> """\n""",
    '\r' -> """\r""",
    '\u001A' -> """\Z""",
    '\'' -> """\'""",
    ';' -> """\;""",
    '"' -> "\\\"")

  def escapeSql(str: String) = {
    if (str.exists(CharMap.contains)) {
      val sb = new StringBuilder(str.length)
      str.foreach { c =>
        sb.append(CharMap.getOrElse(c, c))
      }
      sb.toString
    } else {
      str
    }
  }

  // 简单计时
  def timing[T](f: => T)(h: Long => Unit): T = {
    val begin = System.currentTimeMillis
    val ret = f
    h(System.currentTimeMillis - begin)
    ret
  }
}

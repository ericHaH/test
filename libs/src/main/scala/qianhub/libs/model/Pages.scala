package qianhub.libs.model

/**
 * 分页对象: 总页数，当前页数，上一页，下一页，如果为 None 则没有， 页数都从 0 开始的
 * 需要显示第一页，最后一页，中间 9 页
 */
final case class Pages(total: Int, current: Int, pageSize: Int = 20, showingPage: Int = 9) {
  // 最大页数(从 0 开始)
  def max = total match {
    case 0                      => 0
    case i if i % pageSize == 0 => (i - 1) / pageSize // 数量为 20 时，页数为 1， 即 max = 0
    case i                      => i / pageSize // 页数
  }

  def previous: Option[Int] = if (current == 0) None else Some(current - 1)
  def next: Option[Int] = if (current == max) None else Some(current + 1)
  // 显示的页面, 如果为 None，则显示 ... 否则显示数字
  def showing: Seq[Option[Int]] = {
    val n = showingPage / 2
    val left = current - n match {
      case i if i > 1  => Seq(Some(0), None) ++ ((current - n) to current).map(Some(_))
      case i if i <= 1 => (0 to current).map(Some(_))
    }
    val right = current + n match {
      case i if i < max - 1  => ((current + 1) to (current + n)).map(Some(_)) ++ Seq(None, Some(max))
      case i if i >= max - 1 => ((current + 1) to max).map(Some(_))
    }
    left ++ right
  }
}

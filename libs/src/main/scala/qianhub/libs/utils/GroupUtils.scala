package qianhub.libs.utils

/**
 * 简单的聚合方法
 */
// 二维数组
class Group2[A, B](val keys: Seq[A], maps: Map[A, Seq[B]]) {
  def value(key: A): Option[Seq[B]] = maps.get(key)
}

// 三维数组
class Group3[A, B, C](val keys: Seq[A], maps: Map[A, Group2[B, C]]) {
  def value(key: A): Option[Group2[B, C]] = maps.get(key)
}

object Groups {
  def apply[A, B](in: Seq[(A, B)]): Group2[A, B] = {
    // 保证顺序
    val keys = in.map(_._1).distinct
    val maps = in.groupBy(_._1).view.mapValues(_.map(_._2))
    new Group2(keys, maps.toMap)
  }

  def apply[A, B, C](in: Seq[(A, B, C)]): Group3[A, B, C] = {
    val keys = in.map(_._1).distinct
    val maps = in.groupBy(_._1).view.mapValues(_.map(r => (r._2, r._3))).mapValues(Groups(_))
    new Group3(keys, maps.toMap)
  }
}

trait GroupUtils {
  import scala.collection.mutable.ListBuffer

  /**
   * 二维数组聚合: (1, 2), (1, 3), (2, 3), (2, 4) => (1, (2, 3)), (2, (3, 4))
   */
  def group2[A, B](in: Seq[(A, B)]): Seq[(A, Seq[B])] = {
    val ret = ListBuffer.empty[(A, ListBuffer[B])]
    in.foreach {
      case (i, j) =>
        ret.find(_._1 == i) match {
          case Some((i, list)) => list += j
          case None            => ret += ((i, ListBuffer(j)))
        }
    }
    ret.map { case (i, l) => (i, l.toList) }.toList
  }

  /**
   * 三维数组聚合
   */
  def group3[A, B, C](in: Seq[(A, B, C)]): Seq[(A, Seq[(B, Seq[C])])] = {
    val ret = ListBuffer.empty[(A, ListBuffer[(B, ListBuffer[C])])]
    in.foreach {
      case (i, j, k) =>
        ret.find(_._1 == i) match {
          case Some((_, l)) =>
            l.find(_._1 == j) match {
              case Some((_, ll)) => ll += k
              case None          => l += ((j, ListBuffer(k)))
            }
          case None => ret += ((i, ListBuffer((j, ListBuffer(k)))))
        }
    }
    ret.map { case (i, l) => (i, l.map { case (j, ll) => (j, ll.toList) }.toList) }.toList
  }
}

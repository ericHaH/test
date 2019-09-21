package qianhub.libs

package object actor {
  // 内部通知处理
  case object EndWaiting
  // Tick 事件
  case object Tick
}

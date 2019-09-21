package qianhub.libs.http

import com.typesafe.scalalogging.Logger

trait LoggerSupport {
  // 记录日志
  final val logger = Logger(this.getClass)
}

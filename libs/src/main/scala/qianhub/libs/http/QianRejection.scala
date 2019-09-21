package qianhub.libs.http

import akka.http.scaladsl.server.Rejection

final case class QianRejection(ex: Throwable) extends Rejection

package qianhub.libs.client.oss

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.directives.ContentTypeResolver
import akka.stream.scaladsl.FileIO
import org.apache.commons.io.FileUtils
import qianhub.libs.client.ClientTrait
import qianhub.libs.model.ApiErrors
import qianhub.libs.model.Magic._
import qianhub.libs.utils

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

final class OssClient(val website: String)(implicit val system: ActorSystem, val ec: ExecutionContext)
    extends ClientTrait {

  def methodOf(obj: OssSigned): HttpMethod = {
    obj.verb match {
      case "PUT"    => HttpMethods.PUT
      case "POST"   => HttpMethods.POST
      case "DELETE" => HttpMethods.DELETE
      case "GET"    => HttpMethods.GET
      case "PATCH"  => HttpMethods.PATCH
    }
  }

  // 上传文件, ossFile 必须是全路径，即以 / 开头, 返回在 OSS 上的路径
  def putFile(sourceFile: File, ossFile: String)(implicit meta: OssSetting): Future[String] = {
    wrap(ossFile) { url =>
      logger.debug("OSS.PutObject {} {} {}", url, sourceFile, ossFile)
      val fMD5 = utils.hashFile(sourceFile, "MD5").map(_.base64)
      Future.fromTry(fMD5).flatMap { md5 =>
        val ct = ContentTypeResolver.Default(sourceFile.getName)
        val obj = OssSigned.putObject(sourceFile, ossFile, md5, ct.toString())
        val headers = obj.headers.map { case (k, v) => RawHeader(k, v) }.toSeq
        val entity = HttpEntity.fromFile(ct, sourceFile)
        val request = HttpRequest(methodOf(obj), url, headers = immutable.Seq(headers: _*), entity = entity)
        handle(request)(_ => Future.successful(meta.toUrl(ossFile)))
      }
    }
  }

  // 下载文件 ossFile 必须是全路径，即以 / 开头
  def getFile(ossFile: String, target: File)(implicit meta: OssSetting): Future[File] = {
    wrap(ossFile) { url =>
      logger.debug("OSS.GutObject {} {} {}", url, ossFile, target)
      val obj = OssSigned.getObject(ossFile)
      val headers = obj.headers.map { case (k, v) => RawHeader(k, v) }.toSeq
      val request = HttpRequest(methodOf(obj), url, headers = immutable.Seq(headers: _*))
      handle(request) { resp =>
        val parent = target.getParentFile
        if (!parent.isDirectory) {
          FileUtils.forceMkdir(parent)
        }
        resp.entity.dataBytes.runWith(FileIO.toPath(target.toPath)).map(_ => target)
      }
    }
  }

  // 下载文件 ossFile 必须是全路径，即以 / 开头
  def getString(ossFile: String)(implicit meta: OssSetting): Future[String] = {
    wrap(ossFile) { url =>
      logger.debug("OSS.GutObject {} {}", url, ossFile)
      val obj = OssSigned.getObject(ossFile)
      val headers = obj.headers.map { case (k, v) => RawHeader(k, v) }.toSeq
      val request = HttpRequest(methodOf(obj), url, headers = immutable.Seq(headers: _*))
      handle(request) { resp =>
        resp.entity.dataBytes.runFold("")(_ ++ _.utf8String)
      }
    }
  }
  // 瞬客里面加的方法
  @deprecated("开发后请弃用掉,测试使用")
  def getStringWithOutAuth(ossFile: String)(implicit meta: OssSetting): Future[String] = {
    wrap(ossFile) { url =>
      logger.debug("OSS.GutObject {} {}", url, ossFile)
      val obj = OssSigned.getObject(ossFile)
      val request = HttpRequest(methodOf(obj), url)
      handle(request) { resp =>
        resp.entity.dataBytes.runFold("")(_ ++ _.utf8String)
      }
    }
  }

  // 删除文件 ossFile 必须是全路径，即以 / 开头
  def delObject(ossFile: String)(implicit meta: OssSetting): Future[Boolean] = {
    wrap(ossFile) { url =>
      logger.debug("OSS.DelObject {} {}", url, ossFile)
      val obj = OssSigned.delObject(ossFile)
      val headers = obj.headers.map { case (k, v) => RawHeader(k, v) }.toSeq
      val request = HttpRequest(methodOf(obj), url, headers = immutable.Seq(headers: _*))
      handle(request)(_ => Future.successful(true))
    }
  }

  // 图片缩放处理 ossFile 必须是全路径，即以 / 开头
  def dealObject(ossFile: String, target: String)(implicit meta: OssSetting): Future[Boolean] = {
    val obj = OssSigned.dealObject(ossFile, target)
    wrap(obj.fullResource) { url =>
      val headers = obj.headers.map { case (k, v) => RawHeader(k, v) }.toSeq
      val entity = HttpEntity(obj.body.get)
      val request = HttpRequest(methodOf(obj), url, headers = immutable.Seq(headers: _*), entity = entity)
      handle(request)(_ => Future.successful(true))
    }
  }

  def handle[Out](request: HttpRequest)(f: HttpResponse => Future[Out])(implicit meta: OssSetting): Future[Out] = {
    singleRequest(request).flatMap { resp =>
      resp.status.intValue() match {
        case 200 => f(resp)
        case other =>
          resp.entity.dataBytes.runFold("")(_ ++ _.utf8String).flatMap { out =>
            logger.error("AliyunError: {} {} {} {}", request.method, request.uri, request.headers, out)
            other match {
              case 404 => Future.failed(ApiErrors.NotFound.extra(request.uri.toString()))
              case 403 => Future.failed(ApiErrors.Forbidden.extra(request.uri.toString()))
              case _   => Future.failed(ApiErrors.SystemError.extra(request.uri.toString()))
            }
          }
      }
    }
  }
}

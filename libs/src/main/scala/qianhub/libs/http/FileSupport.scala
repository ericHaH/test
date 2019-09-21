package qianhub.libs.http

import java.io.File
import java.util.zip.{ZipEntry, ZipOutputStream}

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpEntity, MediaTypes, Multipart}
import akka.http.scaladsl.server.{Directive1, Route}
import akka.stream.scaladsl.{FileIO, Sink, StreamConverters}
import org.apache.commons.io.FileUtils
import qianhub.libs.model.Magic._
import qianhub.libs.utils

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

// 针对文件的 HTTP 操作: 下载 压缩下载
trait FileSupport { self: ServerSupport =>

  // 附件名字, 兼容不同浏览器
  final def attachName(name: String): RawHeader = {
    val encode = name.encodeUrl("utf-8")
    val attach = s"""attachment; filename="$name"; filename*=utf-8''$encode"""
    RawHeader("Content-Disposition", attach)
  }

  // 下载文件
  final def downloadFile(file: File, name: String): Route = {
    respondWithHeader(attachName(name)) {
      val responseEntity = HttpEntity.fromFile(MediaTypes.`application/octet-stream`, file)
      complete(responseEntity)
    }
  }

  // 批量压缩文件用于下载, 在指定线程池中压缩
  final def zipFiles(files: Seq[File], targetName: String)(implicit ec: ExecutionContext): Route = {
    val withNames = files.map(r => (r, r.getName))
    zipFilesWithName(withNames, targetName)
  }

  // 批量压缩文件并下载
  final def zipFilesWithName(files: Seq[(File, String)], targetName: String)(implicit ec: ExecutionContext): Route = {
    respondWithHeader(attachName(targetName)) {
      val source = StreamConverters.asOutputStream().mapMaterializedValue { out =>
        Future {
          utils.use(new ZipOutputStream(out)) { writer =>
            files.foreach {
              case (file, fileName) =>
                writer.putNextEntry(new ZipEntry(fileName))
                FileUtils.copyFile(file, writer)
                writer.closeEntry()
            }
          }
        }(ec)
      }
      val responseEntity = HttpEntity(MediaTypes.`application/zip`, source)
      complete(responseEntity)
    }
  }

  // 上载文件, 是一个 Directive,  传入的 fFile 获取目标文件路径，注意，此时文件还未上传
  final def uploadFiles(fFile: FormField => File): Directive1[immutable.Seq[FormField]] = {
    entity(as[Multipart.FormData]).flatMap { form =>
      extractRequestContext.flatMap { ctx =>
        import ctx._
        val parts = form.parts
          .mapAsync(1) {
            case b if b.filename.isEmpty =>
              b.entity.dataBytes.runFold("")(_ ++ _.utf8String).map(s => FormField.fromString(b.name, s))
            case b =>
              val field = FormField.fromFile(b.name, b.filename.get, b.entity.contentType)
              val file = fFile(field)
              if (!file.getParentFile.isDirectory) {
                FileUtils.forceMkdir(file.getParentFile)
              }
              b.entity.dataBytes.runWith(FileIO.toPath(file.toPath)).map(_ => field.copy(target = Some(file)))
          }
          .runWith(Sink.seq[FormField])
        onSuccess(parts)
      }
    }
  }
}

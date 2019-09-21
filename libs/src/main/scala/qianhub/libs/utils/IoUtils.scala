package qianhub.libs.utils

import java.io._
import java.util.zip._

import scala.annotation.tailrec
import scala.io.Source

/**
 * IO 通用函数
 */
trait IoUtils {

  @throws(classOf[IOException])
  def use[C <: Closeable, R](c: C)(f: C => R): R =
    try { f(c) } finally { close(c) }

  def close[C <: Closeable](closes: Closeable*): Unit = closes.foreach { c =>
    try { if (c != null) c.close() } catch { case _: Throwable => }
  }

  @throws(classOf[IOException])
  def useSource[C <: Source, R](c: C)(f: C => R): R =
    try { f(c) } finally { closeSource(c) }

  def closeSource[C <: Source](closes: Source*): Unit = closes.foreach { c =>
    try { if (c != null) c.close() } catch { case _: Throwable => }
  }

  def ext(file: File): String = ext(file.getName)
  def ext(filename: String): String = filename.split('.').last
  // 修改名字（不修改扩展名）
  def rename(nameAndExt: String, name: String): String = name + '.' + ext(nameAndExt)

  // copy 流
  def copy(in: InputStream, out: OutputStream, bufSize: Int = 4096): Int = {
    var count = 0
    var n = 0
    val buffer = new Array[Byte](bufSize)
    while (-1 != { n = in.read(buffer); n }) {
      out.write(buffer, 0, n)
      count += n
    }
    count
  }

  @tailrec
  final def joinPath(home: File, paths: String*): File = {
    paths.toList match {
      case Nil       => home
      case p :: Nil  => new File(home, p)
      case p :: rest => joinPath(new File(home, p), rest: _*)
    }
  }

  // 解压 zip 文件, 返回解压后的文件列表
  def unzip(file: File, todir: File): Seq[File] = {
    todir.mkdirs
    val buffer = new Array[Byte](1024)
    val fin = new FileInputStream(file)
    val zin = new ZipInputStream(fin)
    var ze: ZipEntry = null
    var seq = Seq.empty[File]
    while ({ ze = zin.getNextEntry; ze } != null) {
      val target = new File(todir, ze.getName)
      val fout = new FileOutputStream(target)
      var len: Int = 0
      while ({ len = zin.read(buffer); len } > 0) {
        fout.write(buffer, 0, len)
      }
      seq :+= target
      zin.closeEntry()
      close(fout)
    }
    close(fin, zin)
    seq
  }
}

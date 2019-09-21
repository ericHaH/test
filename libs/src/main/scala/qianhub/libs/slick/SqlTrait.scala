package qianhub.libs.slick

import java.io.{File, FileWriter}

import qianhub.libs.model.DBType

import scala.util.Try

/**
 * SQL 语句
 */
trait SqlTrait {
  self: SlickTrait =>

  import profile.api._

  type TQ = TableQuery[_ <: Table[_]]

  implicit class RichTable(table: TQ) {

    def tableName: String = table.baseTableRow.tableName

    def schemaName: String = table.baseTableRow.schemaName.map(_ + ".").getOrElse("")

    // 创建表的语句
    def createSql: String = {
      val extraSql = dbType match {
        case DBType.PostgreSQL =>
          val name = table.baseTableRow.tableName
          val schema = table.baseTableRow.schemaName.map(_ + ".").getOrElse("")
          s"""
             |ALTER TABLE $schema$name ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL;
             |ALTER TABLE $schema$name ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL;
        """.stripMargin
        case _ => ""
      }
      table.schema.createStatements.mkString(";\n").split(",").mkString(",\n  ") ++ ";" ++ extraSql + "\n\n"
    }

    // 删除表的语句
    def dropSql: String =
      table.schema.dropStatements.mkString("", ";\n", ";\n\n").split(",").mkString(",\n  ")

    def triggerSql(f: String => Option[String]): Option[String] =
      f(createSql).map { t =>
        s"CREATE TRIGGER ${tableName}_$t BEFORE UPDATE ON $schemaName$tableName FOR EACH ROW EXECUTE PROCEDURE $t();\n"
      }
  }

  // 生成 Create Sql
  def createSql(tables: Seq[TQ], root: File, schema: String): Unit =
    dumpSql(tables, root, schema, "create") { table =>
      Some(table.createSql)
    }

  // 生成 Drop Sql
  def dropSql(tables: Seq[TQ], root: File, schema: String): Unit =
    dumpSql(tables, root, schema, "drop") { table =>
      Some(table.dropSql)
    }

  def dumpTrigger(tables: Seq[TQ], root: File, schema: String)(f: String => Option[String]): Unit =
    dumpSql(tables, root, schema, "trigger")(_.triggerSql(f))

  // 按照字母顺序生成 Sql
  private[this] def dumpSql(tables: Seq[TQ], root: File, schema: String, tpe: String)(f: TQ => Option[String]): Unit = {
    val home = new File(root, schema).getCanonicalFile
    // 创建和删除 schema 的目录
    val target = new File(home, tpe)
    if (!target.isDirectory) target.mkdirs()
    // 删除旧文件
    target.listFiles().foreach(_.delete())
    val seq = tables.flatMap { table =>
      f(table).map(r => table.tableName -> r)
    }
    // 每张表保存一个 SQL 文件
    seq.foreach { case (name, sql) => writeFile(new File(target, s"$name.$tpe.sql"), sql) }
    // 所有 SQL 再保存一个文件
    val totalSql = seq.map(_._2).mkString("")
    writeFile(new File(home, s"$schema.$tpe.sql"), wrap(totalSql))
  }

  private[this] def wrap(f: => String): String = {
    dbType match {
      case DBType.MySQL =>
        "START TRANSACTION;\nSET FOREIGN_KEY_CHECKS=0;\n\n" + f + "\nSET FOREIGN_KEY_CHECKS=1;\n" + "COMMIT;\n"
      case _ => f
    }
  }

  private[this] def writeFile(target: File, str: String) = {
    val fw = new FileWriter(target)
    try { fw.write(str) } finally {
      Try(fw.close())
    }
  }
}

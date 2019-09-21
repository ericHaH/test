package qianhub.libs.slick

import com.github.tminglei.slickpg._
import io.circe._
import io.circe.parser.parse
import io.circe.syntax._
import qianhub.libs.model.{ApiErrors, MID}
import slick.basic.Capability
import slick.jdbc.{JdbcCapabilities, JdbcType}

import scala.reflect.ClassTag

// 扩展 PG 能力
trait PgProfile
    extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgRangeSupport
    with PgHStoreSupport
    with PgCirceJsonSupport
    with PgSearchSupport
    with PgPostGISSupport
    with PgNetSupport
    with PgLTreeSupport {

  import PgHelper._

  def pgjson = "jsonb"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api = MyAPI

  object MyAPI
      extends API
      with ArrayImplicits
      with DateTimeImplicits
      with JsonImplicits // JSON 带有空格, 要重载
      with NetImplicits
      with LTreeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SearchAssistants {

    // 不需要 null 值
    private val noSpaces = Printer.noSpaces.copy(dropNullValues = true)

    val TimestampWithZone = "TIMESTAMPTZ DEFAULT NOW()" // PG 当前时间
    val TimestampWithLocal = "TIMESTAMP DEFAULT NOW()" // PG 当前时间

    implicit val SimpleSeqOfString = SeqTypeOfBasic[String]
    implicit val SimpleSeqOfShort = SeqTypeOfBasic[Short]
    implicit val SimpleSeqOfInt = SeqTypeOfBasic[Int]
    implicit val SimpleSeqOfLong = SeqTypeOfBasic[Long]
    implicit val SimpleSeqOfFloat = SeqTypeOfBasic[Float]
    implicit val SimpleSeqOfDouble = SeqTypeOfBasic[Double]
    implicit val SimpleSeqOfBoolean = SeqTypeOfBasic[Boolean]

    implicit override val circeJsonTypeMapper: JdbcType[Json] =
      new GenericJdbcType[Json](
        pgjson,
        v =>
          parse(v) match {
            case Right(value) => value
            case Left(ex) =>
              logger.warn("PgProfile.fromString CirceJsonTypeMapper Error $ex with $v", ex)
              throw ApiErrors.InvalidJson.extra(ex)
        },
        v => noSpaces.print(v.asJson),
        hasLiteralForm = false)

    implicit val CirceArrayTypeMapper =
      new AdvancedArrayJdbcType[Json](
        pgjson,
        s =>
          utils.SimpleArrayUtils
            .fromString[Json] { r =>
              parse(r) match {
                case Right(value) => value
                case Left(ex) =>
                  logger.warn("PgProfile.fromString CirceArrayTypeMapper Error $ex with $v", ex)
                  throw ApiErrors.InvalidJson.extra(ex)
              }
            }(s)
            .orNull,
        v => utils.SimpleArrayUtils.mkString[Json](noSpaces.print)(v)).to(_.toList)

    // 单个对象映射
    def TypeOfJson[T](implicit c: ClassTag[T], encode: Encoder[T], decode: Decoder[T]): JdbcType[T] = {
      def tcomap(json: Json): T = {
        json.as[T] match {
          case Right(v) => v
          case Left(ex) =>
            logger.warn(s"PgProfile.Json2ModelError: $ex with ${json.noSpaces}", ex)
            throw ApiErrors.InvalidJson.extra(ex)
        }
      }
      MappedJdbcType.base[T, Json](_.asJson, tcomap)
    }

    // 集合对象映射
    def SeqTypeOfJson[T](implicit c: ClassTag[T], encode: Encoder[T], decode: Decoder[T]): JdbcType[Seq[T]] = {
      new AdvancedArrayJdbcType[T](
        pgjson,
        s =>
          utils.SimpleArrayUtils
            .fromString[T] { r =>
              parse(r).flatMap(_.as[T]) match {
                case Right(value) => value
                case Left(ex) =>
                  logger.warn(s"PgProfile.fromString SeqTypeOfJson Error $ex with $r", ex)
                  throw ApiErrors.InvalidJson.extra(ex)
              }
            }(s)
            .orNull,
        v => utils.SimpleArrayUtils.mkString[T](b => noSpaces.print(b.asJson))(v)).to(_.toSeq)
    }

    // List[MID] 类型
    def ListTypeOfMID[B, T <: MID[B]](f1: B => T)(
        f2: T => B)(implicit c: ClassTag[T], b: ClassTag[B], w: ElemWitness[B]): JdbcType[List[T]] = {
      new SimpleArrayJdbcType[B](TypeOfBasic[B]).mapTo[T](f1, f2).to(_.toList)
    }

    def SeqTypeOfMID[B, T <: MID[B]](f1: B => T)(
        f2: T => B)(implicit c: ClassTag[T], b: ClassTag[B], w: ElemWitness[B]): JdbcType[Seq[T]] = {
      new SimpleArrayJdbcType[B](TypeOfBasic[B]).mapTo[T](f1, f2).to(_.toSeq)
    }

    // Seq[Int] 基础类型
    def SeqTypeOfBasic[B](implicit b: ClassTag[B], w: ElemWitness[B]): JdbcType[Seq[B]] = {
      new SimpleArrayJdbcType[B](TypeOfBasic[B]).mapTo[B](identity, identity).to(_.toSeq)
    }

  }
}

object PgHelper {

  val ClassOfString = classOf[String]
  val ClassOfShort = classOf[Short]
  val ClassOfInt = classOf[Int]
  val ClassOfLong = classOf[Long]
  val ClassOfFloat = classOf[Float]
  val ClassOfDouble = classOf[Double]
  val ClassOfBoolean = classOf[Boolean]

  def TypeOfBasic[B](implicit b: ClassTag[B]): String = {
    b.runtimeClass match {
      case ClassOfString  => "text"
      case ClassOfShort   => "int2"
      case ClassOfInt     => "int4"
      case ClassOfLong    => "int8"
      case ClassOfFloat   => "float4"
      case ClassOfDouble  => "float8"
      case ClassOfBoolean => "bool"
    }
  }
}

object PgProfile extends PgProfile

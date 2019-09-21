/*package qianhub.libs.json.test

import io.circe.syntax._
import org.scalatest.FunSuite
import qianhub.libs.json.Jsons

class CommonJsonSpec extends FunSuite {

  test("String 序列化可以支持数字") {
    val number = 123456
    Jsons.from[String](number.toString).get == "123456"
  }

  test("Long 序列化可以支持字符串") {
    val number = 123451231231231L
    val str = number.toString.asJson.slim
    val v1 = Jsons.from[Long](number.toString).get
    val v2 = Jsons.from[Long](str).get
    assert(v1 == number)
    assert(v2 == number)
  }

  test("Short 序列化可以支持字符串") {
    val number = 12345
    val str = number.toString.asJson.slim
    val v1 = Jsons.from[Short](number.toString).get
    val v2 = Jsons.from[Short](str).get
    assert(v1 == number)
    assert(v2 == number)
  }

  test("Int 序列化可以支持字符串") {
    val number = 123451231
    val str = number.toString.asJson.slim
    val v1 = Jsons.from[Int](number.toString).get
    val v2 = Jsons.from[Int](str).get
    assert(v1 == number)
    assert(v2 == number)
  }

  test("Double 序列化可以支持字符串") {
    val number = 12345.678
    val str = number.toString.asJson.slim
    math.abs(Jsons.from[Double](str).get - number) < 0.000001
  }

  test("Boolean 序列化可以支持字符串") {
    val number = 1
    val str = number.toString.asJson.slim
    val v1 = Jsons.from[Boolean](number.toString).get
    val v2 = Jsons.from[Boolean](str).get
    assert(v1)
    assert(v2)
  }

}*/

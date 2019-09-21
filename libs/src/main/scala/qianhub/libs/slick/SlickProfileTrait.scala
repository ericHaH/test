package qianhub.libs.slick

import slick.jdbc.JdbcProfile

trait SlickProfileTrait {

  type SlickProfile <: JdbcProfile

  val profile: SlickProfile
}

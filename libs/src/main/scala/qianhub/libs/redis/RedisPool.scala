package qianhub.libs.redis

import redis.clients.jedis._

// Redis 连接池, 当不用时，需要调用 close 关闭
object RedisPool {

  def from(host: String, port: Int, maxTotal: Int = 32, password: Option[String] = None): RedisPool = {
    require(maxTotal > 1)
    val conf = new JedisPoolConfig()
    conf.setMaxTotal(maxTotal)
    val underlying = password match {
      case Some(pwd) => new JedisPool(conf, host, port, 5000, pwd)
      case None      => new JedisPool(conf, host, port, 5000)
    }
    new RedisPool(underlying)
  }
}

final class RedisPool(underlying: JedisPool) extends AutoCloseable {

  def withClient[T](f: Jedis => T): T = {
    val client = underlying.getResource
    try {
      f(client)
    } finally {
      client.close()
    }
  }

  override def close(): Unit = {
    underlying.close()
  }
}

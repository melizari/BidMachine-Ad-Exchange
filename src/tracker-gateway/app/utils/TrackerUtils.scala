package utils

import org.joda.time.DateTime

object TrackerUtils {

  /**
   * Linear latency level
   *
   * @param value
   * @param interval
   * @return string representation of interval for value
   */
  def linearLevel(value: Double, interval: Long): String = {
    val c = math.floor(value / interval.toDouble).toLong
    s"${interval * c}-${interval * (c + 1)}"
  }

  def latencyLevel(start: DateTime, end: DateTime): String = {
    val latency = (end.getMillis - start.getMillis) / 1000D
    latency match {
      case _ if latency < 10D                   => linearLevel(latency, 2)
      case _ if latency >= 10D && latency < 60D => linearLevel(latency, 5)
      case _ if latency >= 60D                  => linearLevel(latency, 60)
      case _                                    => "Unexpected"
    }
  }

}

package models

/**
  * Impression count for current session
  */
case class SessionMetrics(banner: Option[Long] = None,
                          interstitial: Option[Long] = None,
                          `rewarded_video`: Option[Long] = None,
                          video: Option[Long] = None,
                          mrec: Option[Long] = None,
                          native: Option[Long] = None)

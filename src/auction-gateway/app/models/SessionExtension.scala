package models


/**
  * Sdk extension object (from request url parameters)
  *
  * @param session_id Session number.
  * @param imp        Impression in this session
  */
case class SessionExtension(`session_id`: Option[Long] = None, imp: Option[SessionMetrics])

package zhttp.http.sse

sealed trait ServerSentEvent {
  def toStringRepresentation: String
}

object ServerSentEvent {

  private[sse] val LF = "\n"

  val empty: ServerSentEvent = EventHeartbeat

  def withData(data: String, eventF: Option[String] = None, idF: Option[String] = None, retryF: Option[Int] = None) =
    Event(Some(data), eventF, idF, retryF)

  case class Event private (dataF: Option[String], eventF: Option[String], idF: Option[String], retryF: Option[Int]) extends ServerSentEvent {

    /**
     * @return A String representation of the SSE, structured for serialization and transport.
     */
    override def toStringRepresentation: String = {
      val _data = dataF.map(_.split(LF).map(line => s"data: $line").mkString(LF).concat(LF))
      val _event = eventF.map(str => s"event: $str".concat(LF))
      val _id = idF.map(str => s"id: $str".concat(LF))
      val _retry = retryF.map(str => s"retry: $str".concat(LF))

      // As per specification each field and every event as a whole are each followed by an `end-of-line` character.
      Array[Option[String]](_data, _event, _id, _retry).flatten.mkString.concat(LF)
    }
  }

  /**
   * As per specification an `end-of-line` is a complete event which in turn can be used as a heartbeat.
   */
  private case object EventHeartbeat extends ServerSentEvent {
    override def toStringRepresentation: String = LF
  }

}

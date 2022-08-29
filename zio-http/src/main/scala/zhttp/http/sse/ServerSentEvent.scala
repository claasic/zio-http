package zhttp.http.sse

sealed trait ServerSentEvent {
  def toStringRepresentation: String
}

object ServerSentEvent {

  val empty: ServerSentEvent =
    EventHeartbeat

  private[sse] val LF =
    "\n"

  def withData(data: String, eventF: Option[String] = None, idF: Option[String] = None, retryF: Option[Int] = None): Event =
    Event(Some(data), eventF, idF, retryF)

  // Odd pattern was chosen due to leaking private constructors.
  // See: https://users.scala-lang.org/t/ending-the-confusion-of-private-case-class-constructor-in-scala-2-13-or-2-14/2915/8
  private[sse] sealed abstract case class Event private(
                                                         dataF: Option[String],
                                                         eventF: Option[String],
                                                         idF: Option[String],
                                                         retryF: Option[Int]
                                                       ) extends ServerSentEvent {

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

  private[sse] object Event {
    def apply(dataF: Option[String], eventF: Option[String], idF: Option[String], retryF: Option[Int]): Event =
      new Event(dataF, eventF, idF, retryF) {}
  }

  /**
   * As per specification an `end-of-line` is a complete event which in turn can be used as a heartbeat.
   */
  private[sse] case object EventHeartbeat extends ServerSentEvent {
    override def toStringRepresentation: String = LF
  }

}

package zio.http.sse

sealed trait ServerSentEvent {
  def toStringRepresentation: String
}

object ServerSentEvent {

  import Fields._

  private object Fields {
    val Field      = """([^:]+):? ?(.*)""".r
    val DataField  = "data"
    val IdField    = "id"
    val EventField = "event"
    val RetryField = "retry"
  }

  def parse(eventLines: List[String]): ServerSentEvent = {
    val result = eventLines.foldLeft(Event(None, None, None, None)) { (sse, line) =>
      line match {
        case Field(DataField, data)   =>
          sse.copy(dataF = sse.dataF.map(s => s.concat(LF ++ data)).orElse(Some(data)))
        case Field(IdField, id)       => sse.copy(idF = Some(id))
        case Field(EventField, event) => sse.copy(eventF = Some(event))
        case Field(RetryField, retry) => sse.copy(retryF = retry.toIntOption)
        case _                        => sse
      }
    }
    if (result.isEmpty) ServerSentEvent.empty else result
  }

  val empty: EventHeartbeat.type =
    EventHeartbeat

  private[sse] val LF =
    "\n"

  def withData(
    data: String,
    eventF: Option[String] = None,
    idF: Option[String] = None,
    retryF: Option[Int] = None,
  ): Event =
    Event(Some(data), eventF, idF, retryF)

  private[sse] case class Event private[sse] (
    dataF: Option[String],
    eventF: Option[String],
    idF: Option[String],
    retryF: Option[Int],
  ) extends ServerSentEvent {

    /**
     * @return
     *   A String representation of the SSE, structured for serialization and
     *   transport.
     */
    override def toStringRepresentation: String = {
      val _data  = dataF.map(_.split(LF).map(line => s"${DataField}: $line").mkString(LF).concat(LF))
      val _event = eventF.map(str => s"${EventField}: $str".concat(LF))
      val _id    = idF.map(str => s"${IdField}: $str".concat(LF))
      val _retry = retryF.map(str => s"${RetryField}: $str".concat(LF))

      // As per specification each field and every event as a whole are each followed by an `end-of-line` character.
      Array[Option[String]](_data, _event, _id, _retry).flatten.mkString.concat(LF)
    }

    private[sse] val isEmpty = dataF.isEmpty && eventF.isEmpty && idF.isEmpty && retryF.isEmpty
  }

  private[sse] object Event {
    def apply(dataF: Option[String], eventF: Option[String], idF: Option[String], retryF: Option[Int]): Event =
      new Event(dataF, eventF, idF, retryF) {}
  }

  /**
   * As per specification an `end-of-line` is a complete event which in turn can
   * be used as a heartbeat.
   */
  private[sse] case object EventHeartbeat extends ServerSentEvent {
    override def toStringRepresentation: String = LF
  }

}

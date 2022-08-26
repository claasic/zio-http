package zhttp.http.sse


sealed trait ServerSentEvent {
  def toStringRepresentation: String
}

object ServerSentEvent {

  //  private val crlf = "\r\n"
  private val LF = "\n"
  //  private val cr   = "\r"
  //
  //  private val end_of_line_regex: Regex = raw"""(^$crlf|^$lf|^$cr)""".r

  case class Event(dataF: Option[String], eventF: Option[String], idF: Option[String], retryF: Option[Int]) extends ServerSentEvent {

    /**
     * @return A String representation of the SSE, structured for serialization and transport.
     */
    override def toStringRepresentation: String = {
      val _data = dataF.map(_.split(LF).map(line => s"data: $line").mkString(LF))
      val _event = eventF.map(str => s"event: $str")
      val _id = idF.map(str => s"id: $str")
      val _retry = retryF.map(str => s"retry: $str")

      // As per specification each field and every event as a whole are each followed by an `end-of-line` character.
      Array[Option[String]](_data, _event, _id, _retry).flatten.mkString("", LF, LF.concat(LF))
    }
  }

  case object EventHeartbeat extends ServerSentEvent {
    override def toStringRepresentation: String = ???

  }
}

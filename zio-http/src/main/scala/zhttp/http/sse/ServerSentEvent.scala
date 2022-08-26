package zhttp.http.sse


sealed trait ServerSentEvent {
  def toStringRepresentation: String
}

object ServerSentEvent {

  private val eol = "\n"

  case class Event(dataF: Option[String], eventF: Option[String], idF: Option[String], retryF: Option[Int]) extends ServerSentEvent {
    override def toStringRepresentation: String = {
      val _data = dataF.map(_.split(eol).map(line => s"data: $line").mkString(eol))
      val _event = eventF.map(str => s"event: $str")
      val _id = idF.map(str => s"id: $str")
      val _retry = retryF.map(str => s"id: $str")

      Array[Option[String]](_data, _event, _id, _retry).flatten.mkString("", eol, eol.concat(eol))
    }
  }

  case object EventHeartbeat extends ServerSentEvent {
    override def toStringRepresentation: String = ???
      
  }
}

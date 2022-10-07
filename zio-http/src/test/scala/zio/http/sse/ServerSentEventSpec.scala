package zio.http.sse

import zio.Scope
import zio.http.sse.ServerSentEvent.Event
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object ServerSentEventSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ServerSentEvent")(
      suite("encoding")(
        test("#withData#toStringRepresentation correctly serializes a ServerSentEvent with all fields set") {
          val (data, event, id, retry) = ("dataValue", "eventValue", "idValue", 1)
          val expectedValue            =
            s"""data: $data
               |event: $event
               |id: $id
               |retry: $retry
               |
               |""".stripMargin
          assertTrue(
            ServerSentEvent.withData(data, Some(event), Some(id), Some(retry)).toStringRepresentation == expectedValue,
          )
        },
        test("#withData#toStringRepresentation correctly serializes a ServerSentEvent with omitted fields (id)") {
          val (data, event, _, retry) = ("dataValue", "eventValue", "idValue", 1)
          val expectedValue           =
            s"""data: $data
               |event: $event
               |retry: $retry
               |
               |""".stripMargin
          assertTrue(
            ServerSentEvent.withData(data, Some(event), None, Some(retry)).toStringRepresentation == expectedValue,
          )
        },
        test("#withData#toStringRepresentation correctly serializes a ServerSentEvent with omitted fields (event)") {
          val (data, _, id, retry) = ("dataValue", "eventValue", "idValue", 1)
          val expectedValue        =
            s"""data: $data
               |id: $id
               |retry: $retry
               |
               |""".stripMargin
          assertTrue(
            ServerSentEvent.withData(data, None, Some(id), Some(retry)).toStringRepresentation == expectedValue,
          )
        },
        test("#withData#toStringRepresentation correctly serializes a ServerSentEvent with multiline data") {
          val (data, event, id, retry) = ("dataValue1\ndataValue2", "eventValue", "idValue", 1)
          val expectedValue            =
            s"""data: ${data.split("\n").head}
               |data: ${data.split("\n").tail.head}
               |event: $event
               |id: $id
               |retry: $retry
               |
               |""".stripMargin
          assertTrue(
            ServerSentEvent.withData(data, Some(event), Some(id), Some(retry)).toStringRepresentation == expectedValue,
          )
        },
        test(
          "#impl#toStringRepresentation correctly serializes an all optionally empty ServerSentEvent to an end-of-line",
        ) {
          assertTrue(Event(None, None, None, None).toStringRepresentation == "\n")
        },
        test("#empty#toStringRepresentation correctly serializes an heartbeat as an end-of-line") {
          assertTrue(ServerSentEvent.empty.toStringRepresentation == "\n")
        },
        test("#empty#toStringRepresentation is identical to an stringified Event with empty optional parameters") {
          assertTrue(
            ServerSentEvent.empty.toStringRepresentation == Event(None, None, None, None).toStringRepresentation,
          )
        },
      ),
      suite("decoding")(
        test("#parse correctly deserializes a Server-Sent Event with all fields set") {
          val (data, event, id, retry) = ("dataValue", "eventValue", "idValue", 1)
          val inputValue               =
            s"""data: $data
               |event: $event
               |id: $id
               |retry: $retry
               |
               |""".stripMargin
          val expectedValue            = Event(Some(data), Some(event), Some(id), Some(retry))
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
        test("#parse correctly deserializes a Server-Sent Event with omitted fields (id)") {
          val (data, event, _, retry) = ("dataValue", "eventValue", "idValue", 1)
          val inputValue              =
            s"""data: $data
               |event: $event
               |retry: $retry
               |
               |""".stripMargin
          val expectedValue           = Event(Some(data), Some(event), None, Some(retry))
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
        test("#parse correctly deserializes a Server-Sent Event with omitted fields (event)") {
          val (data, _, id, retry) = ("dataValue", "eventValue", "idValue", 1)
          val inputValue           =
            s"""data: $data
               |id: $id
               |retry: $retry
               |
               |""".stripMargin
          val expectedValue        = Event(Some(data), None, Some(id), Some(retry))
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
        test("#parse correctly deserializes a Server-Sent Event with multiline data") {
          val (data, event, id, retry) = ("dataValue1\ndataValue2", "eventValue", "idValue", 1)
          val inputValue               =
            s"""data: ${data.split("\n").head}
               |data: ${data.split("\n").tail.head}
               |event: $event
               |id: $id
               |retry: $retry
               |
               |""".stripMargin
          val expectedValue            = Event(Some(data), Some(event), Some(id), Some(retry))
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
        test("#parse correctly deserializes a Server-Sent Event with no field data (no semicolon)") {
          val inputValue    = "data"
          val expectedValue = ServerSentEvent.withData("")
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
        test("#parse correctly deserializes a Server-Sent Event with no field data (with semicolon)") {
          val inputValue    = "data:"
          val expectedValue = ServerSentEvent.withData("")
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
        test("#parse correctly deserializes a Server-Sent Event ignoring the first trailing space after semicolon") {
          val inputValueSpace   = "data: myData"
          val inputValueNoSpace = "data:myData"
          assertTrue(
            ServerSentEvent.parse(inputValueSpace.split(ServerSentEvent.LF).toList) == ServerSentEvent.parse(
              inputValueNoSpace.split(ServerSentEvent.LF).toList,
            ),
          )
        },
        test("#parse correctly deserializes a Server-Sent Event with no data (newline)") {
          val inputValue    = ServerSentEvent.LF
          val expectedValue = ServerSentEvent.empty
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
        test(
          "#parse correctly deserializes a Server-Sent Event with values following a valid field name without semicolon",
        ) {
          val inputValue    = "data myData"
          val expectedValue = ServerSentEvent.empty
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
        // TODO CTL: Might need to handle this differently?
        test("#parse correctly deserializes a Server-Sent Event with leading semicolon (1)(ignores line)") {
          val inputValue    = ":"
          val expectedValue = ServerSentEvent.empty
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
        test("#parse correctly deserializes a Server-Sent Event with leading semicolon (2)(ignores line)") {
          val inputValue    = ":data: myData"
          val expectedValue = ServerSentEvent.empty
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
        // TODO CTL: Is this the correct behaviour?
        test("#parse correctly deserializes a Server-Sent Event with partially invalid lines") {
          val (data, event, id, retry) = ("dataValue", "eventValue", "idValue", 1)
          val inputValue               =
            s"""data: $data
               |event: $event
               |id $id
               |retry: $retry
               |
               |""".stripMargin
          val expectedValue            = ServerSentEvent.withData(data, Some(event), None, Some(retry))
          assertTrue(ServerSentEvent.parse(inputValue.split(ServerSentEvent.LF).toList) == expectedValue)
        },
      ),
    )
}

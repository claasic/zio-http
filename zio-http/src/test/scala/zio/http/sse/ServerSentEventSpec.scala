package zhttp.http.sse

import zhttp.http.sse.ServerSentEvent.Event
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object ServerSentEventSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ServerSentEvent")(
      test("#withData#toStringRepresentation correctly serializes a ServerSentEvent with all fields set") {
        val (data, event, id, retry) = ("dataValue", "eventValue", "idValue", 1)
        val expectedValue =
          s"""data: $data
             |event: $event
             |id: $id
             |retry: $retry
             |
             |""".stripMargin
        assertTrue(ServerSentEvent.withData(data, Some(event), Some(id), Some(retry)).toStringRepresentation == expectedValue)
      },
      test("#withData#toStringRepresentation correctly serializes a ServerSentEvent with omitted fields (id)") {
        val (data, event, _, retry) = ("dataValue", "eventValue", "idValue", 1)
        val expectedValue =
          s"""data: $data
             |event: $event
             |retry: $retry
             |
             |""".stripMargin
        assertTrue(ServerSentEvent.withData(data, Some(event), None, Some(retry)).toStringRepresentation == expectedValue)
      },
      test("#withData#toStringRepresentation correctly serializes a ServerSentEvent with omitted fields (event)") {
        val (data, _, id, retry) = ("dataValue", "eventValue", "idValue", 1)
        val expectedValue =
          s"""data: $data
             |id: $id
             |retry: $retry
             |
             |""".stripMargin
        assertTrue(ServerSentEvent.withData(data, None, Some(id), Some(retry)).toStringRepresentation == expectedValue)
      },
      test("#withData#toStringRepresentation correctly serializes a ServerSentEvent with multiline data") {
        val (data, event, id, retry) = ("dataValue1\ndataValue2", "eventValue", "idValue", 1)
        val expectedValue =
          s"""data: ${data.split("\n").head}
             |data: ${data.split("\n").tail.head}
             |event: $event
             |id: $id
             |retry: $retry
             |
             |""".stripMargin
        assertTrue(ServerSentEvent.withData(data, Some(event), Some(id), Some(retry)).toStringRepresentation == expectedValue)
      },
      test("#impl#toStringRepresentation correctly serializes an all optionally empty ServerSentEvent to an end-of-line") {
        assertTrue(Event(None, None, None, None).toStringRepresentation == "\n")
      },
      test("#empty#toStringRepresentation correctly serializes an heartbeat as an end-of-line") {
        assertTrue(ServerSentEvent.empty.toStringRepresentation == "\n")
      },
      test("#empty#toStringRepresentation is identical to an stringified Event with empty optional parameters") {
        assertTrue(ServerSentEvent.empty.toStringRepresentation == Event(None, None, None, None).toStringRepresentation)
      },
    )
}

package zhttp.http.sse

import zhttp.http.sse.ServerSentEvent.Event
import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object ServerSentEventSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ServerSentEvent")(
      test("#toStringRepresentation correctly serializes an empty ServerSentEvent as an end-of-line") {
        assertTrue(Event(None, None, None, None).toStringRepresentation == "\n\n")
      },
      test("#toStringRepresentation correctly serializes a ServerSentEvent with all fields set") {
        val (data, event, id, retry) = ("dataValue", "eventValue", "idValue", 1)
        val expectedValue =
          s"""data: $data
             |event: $event
             |id: $id
             |retry: $retry
             |
             |""".stripMargin
        assertTrue(Event(Some(data), Some(event), Some(id), Some(retry)).toStringRepresentation == expectedValue)
      },
      test("#toStringRepresentation correctly serializes a ServerSentEvent with omitted fields") {
        val (event, id, retry) = ("eventValue", "idValue", 1)
        val expectedValue =
          s"""event: $event
             |id: $id
             |retry: $retry
             |
             |""".stripMargin
        assertTrue(Event(None, Some(event), Some(id), Some(retry)).toStringRepresentation == expectedValue)
      },
      test("#toStringRepresentation correctly serializes a ServerSentEvent with multiline data") {
        val (data, event, id, retry) = ("dataValue1\ndataValue2", "eventValue", "idValue", 1)
        val expectedValue =
          s"""data: ${data.split("\n").head}
             |data: ${data.split("\n").tail.head}
             |event: $event
             |id: $id
             |retry: $retry
             |
             |""".stripMargin
        assertTrue(Event(Some(data), Some(event), Some(id), Some(retry)).toStringRepresentation == expectedValue)
      },
    )
}

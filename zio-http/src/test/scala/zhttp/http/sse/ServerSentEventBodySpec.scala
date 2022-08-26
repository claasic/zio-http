package zhttp.http.sse

import zhttp.http.HTTP_CHARSET
import zio.Scope
import zio.stream.ZStream
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object ServerSentEventBodySpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ServerSentEventBody")(
      test("#serializeEventStream correctly serializes a ServerSentEvent.empty as a LF") {
        val eventStream = ZStream(ServerSentEvent.empty)
        val newlineBytes = "\n".getBytes(HTTP_CHARSET).toList
        ServerSentEventBody.serializeEventStream(eventStream).runCollect.map { byteChunk =>
          assertTrue(byteChunk.toList == newlineBytes)
        }
      },
      test("#serializeEventStream correctly serializes a SSE with minimal data followed by a LF") {
        val eventStream = ZStream(ServerSentEvent.withData("myData"))
        val expectedString =
          """data: myData
            |
            |""".stripMargin
        val expectedBytes  = expectedString.getBytes(HTTP_CHARSET).toList
        ServerSentEventBody.serializeEventStream(eventStream).runCollect.map { byteChunk =>
          assertTrue(byteChunk.toList == expectedBytes)
        }
      },
      test("#serializeEventStream correctly serializes a sequence of minimal SSEs") {
        val eventStream = ZStream(ServerSentEvent.withData("myData"), ServerSentEvent.withData("myData"))
        val expectedString =
          """data: myData
            |
            |data: myData
            |
            |""".stripMargin
        val expectedBytes = expectedString.getBytes(HTTP_CHARSET).toList
        ServerSentEventBody.serializeEventStream(eventStream).runCollect.map { byteChunk =>
          assertTrue(byteChunk.toList == expectedBytes)
        }
      },
      test("#serializeEventStream correctly serializes heartbeats between minimal SSEs ") {
        val eventStream = ZStream(ServerSentEvent.withData("myData"), ServerSentEvent.empty, ServerSentEvent.withData("myData"))
        val expectedString =
          """data: myData
            |
            |
            |data: myData
            |
            |""".stripMargin
        val expectedBytes = expectedString.getBytes(HTTP_CHARSET).toList
        ServerSentEventBody.serializeEventStream(eventStream).runCollect.map { byteChunk =>
          assertTrue(byteChunk.toList == expectedBytes)
        }
      }
    )
}

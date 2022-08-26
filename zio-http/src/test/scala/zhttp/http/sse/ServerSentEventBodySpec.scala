package zhttp.http.sse

import zhttp.http.HTTP_CHARSET
import zhttp.http.sse.ServerSentEvent.Event
import zio.Scope
import zio.stream.ZStream
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object ServerSentEventBodySpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ServerSentEventBody")(
      test("#serializeEventStream serializes an empty ServerSentEvent correctly as a newline") {
        val eventStream = ZStream(Event(None, None, None, None))
        val newlineBytes = "\n".getBytes(HTTP_CHARSET).toList
        ServerSentEventBody.serializeEventStream(eventStream).runCollect.map { byteChunk =>
          assertTrue(byteChunk.toList == newlineBytes)
        }
      }
    )
}

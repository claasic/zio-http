package zhttp.http.sse

import zhttp.http.Response.Attribute
import zhttp.http.{Body, Headers, Response, Status}
import zio.stream.ZStream
import zio.{Schedule, durationInt}

object ServerSentEventResponse {

  private[sse] val requiredHeaders =
    Headers.contentType("text/event-stream; charset=utf-8") ++
      Headers.cacheControl("no-cache") ++
      Headers.connection("keep-alive")

  def fromEventStream(status: Status = Status.Ok,
                      additionalHeaders: Headers = Headers.empty,
                      stream: ZStream[Any, Throwable, ServerSentEvent]): Response =
    fromBody(status, additionalHeaders, ServerSentEventBody.fromEventStream(stream))

  private[sse] def fromBody(
                             status: Status = Status.Ok,
                             additionalHeaders: Headers = Headers.empty,
                             body: Body = Body.empty,
                           ): Response =
    Response(status, requiredHeaders.combine(additionalHeaders), body, Attribute.empty)

  def fromEventStreamWithHeartbeat(
                                    status: Status = Status.Ok,
                                    additionalHeaders: Headers = Headers.empty,
                                    stream: ZStream[Any, Throwable, ServerSentEvent],
                                    schedule: Schedule[Any, Any, Any] = Schedule.spaced(2.seconds)
                                  ): Response =
    fromBody(status, additionalHeaders, ServerSentEventBody.fromEventStream(stream.mergeLeft(heartbeatWith(schedule))))

  private def heartbeatWith(schedule: Schedule[Any, Any, Any]) =
    ZStream.repeat(ServerSentEvent.empty).schedule(schedule)



}

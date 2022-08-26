package zhttp.http.sse

import zhttp.http.Response.Attribute
import zhttp.http.{Body, Headers, Response, Status}
import zio.{Schedule, durationInt}
import zio.stream.ZStream

object ServerSentEventResponse {

  private[sse] val requiredHeaders =
    Headers.contentType(s"text/event-stream; charset=utf-8") ++
      Headers.cacheControl("no-cache") ++
      Headers.connection("keep-alive")

  private def heartbeat(schedule: Schedule[Any, Any, Any]) =
    ZStream.repeat(ServerSentEvent.empty).schedule(schedule)

  def fromEventStream(status: Status = Status.Ok,
                      additionalHeaders: Headers = Headers.empty,
                      stream: ZStream[Any, Throwable, ServerSentEvent]): Response =
    fromBody(status, additionalHeaders, ServerSentEventBody.fromEventStream(stream))

  def fromEventStreamWithHeartbeat(
                               status: Status = Status.Ok,
                               additionalHeaders: Headers = Headers.empty,
                               stream: ZStream[Any, Throwable, ServerSentEvent],
                               heartBeatSchedule: Schedule[Any, Any, Any] = Schedule.spaced(2.seconds)
                             ): Response =
    fromBody(status, additionalHeaders, ServerSentEventBody.fromEventStream(stream.mergeHaltLeft(heartbeat(heartBeatSchedule))))


  private[sse] def fromBody(
                             status: Status = Status.Ok,
                             additionalHeaders: Headers = Headers.empty,
                             body: Body = Body.empty,
                           ): Response =
    Response(status, requiredHeaders.combine(additionalHeaders), body, Attribute.empty)


}

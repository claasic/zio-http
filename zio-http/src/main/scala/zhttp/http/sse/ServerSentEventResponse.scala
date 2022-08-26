package zhttp.http.sse

import zhttp.http.Response.Attribute
import zhttp.http.{Body, Headers, Response, Status}
import zio.stream.ZStream

object ServerSentEventResponse {

  private[sse] val requiredHeaders =
    Headers.contentType(s"text/event-stream; charset=utf-8") ++
      Headers.cacheControl("no-cache") ++
      Headers.connection("keep-alive")

  def fromEventStream(status: Status = Status.Ok,
                      headers: Headers = Headers.empty,
                      stream: ZStream[Any, Throwable, ServerSentEvent]): Response =
    fromBody(status, headers, ServerSentEventBody.fromEventStream(stream))

  private[sse] def fromBody(
                             status: Status = Status.Ok,
                             headers: Headers = Headers.empty,
                             body: Body = Body.empty,
                           ): Response =
    Response(status, requiredHeaders.combine(headers), body, Attribute.empty)


}

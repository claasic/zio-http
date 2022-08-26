package example

import zhttp.http._
import zhttp.service.Server
import zio._
import zio.stream.ZStream
import zhttp.http.sse.ServerSentEvent._
import zhttp.http.sse._

/**
 * Example to encode content using a ZStream
 */
object ServerSentEventEndpoint extends ZIOAppDefault {
  // Starting the server (for more advanced startup configuration checkout `HelloWorldAdvanced`)
  def run = Server.start(8090, app)

  // Create a stream of Server-Sent-Events
  def stream = ZStream.repeat(Event(Some("my-data"), Some("message"), Some("my-id"), None)).schedule(Schedule.spaced(1.seconds) && Schedule.recurs(10))

  // Use `Http.collect` to match on route
  def app: HttpApp[Any, Nothing] = Http.collect[Request] {

    // Simple (non-stream) based route
    case Method.GET -> !! / "health" => Response.ok

    // ZStream powered response
    case Method.GET -> !! / "stream" =>
      ServerSentEventResponse.fromEventStream(
        status = Status.Ok,
        headers = Headers.accessControlAllowOrigin("*"),
        stream
      )
  }
}

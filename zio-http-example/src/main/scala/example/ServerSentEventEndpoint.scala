package example

import zio._
import zio.http._
import zio.http.model._
import zio.http.sse._
import zio.stream.ZStream

/**
 * Example to provide content as Server-Sent Events.
 */
object ServerSentEventEndpoint extends ZIOAppDefault {

  // Create a stream of Server-Sent-Events
  val eventStream =
    ZStream.repeatWithSchedule(ServerSentEvent.withData("myData"), Schedule.spaced(1.seconds) && Schedule.recurs(10))

  // Starting the server (for more advanced startup configuration checkout `HelloWorldAdvanced`)
  def run = Server.serve(app).provide(Server.default)

  // Use `Http.collect` to match on route
  def app: HttpApp[Any, Nothing] = Http.collect[Request] {

    // Simple (non-stream) based route
    case Method.GET -> !! / "health" => Response.ok

    // ZStream powered response
    case Method.GET -> !! / "stream" =>
      ServerSentEventResponse.fromEventStreamWithHeartbeat(
        status = Status.Ok,
        additionalHeaders = Headers.accessControlAllowOrigin("*"),
        stream = eventStream,
      )
  }
}

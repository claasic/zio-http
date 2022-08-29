package example

import zhttp.http._
import zhttp.http.sse._
import zhttp.service.Server
import zio._
import zio.stream.ZStream

/**
 * Example to provide content as Server-Sent Events.
 */
object ServerSentEventEndpoint extends ZIOAppDefault {

  // Create a stream of Server-Sent-Events
  val eventStream = ZStream.repeatWithSchedule(ServerSentEvent.withData("myData"), Schedule.spaced(1.seconds) && Schedule.recurs(10))

  // Starting the server (for more advanced startup configuration checkout `HelloWorldAdvanced`)
  def run = Server.start(8090, app)

  // Use `Http.collect` to match on route
  def app: HttpApp[Any, Nothing] = Http.collect[Request] {

    // Simple (non-stream) based route
    case Method.GET -> !! / "health" => Response.ok

    // ZStream powered response
    case Method.GET -> !! / "stream" =>
      ServerSentEventResponse.fromEventStreamWithHeartbeat(
        status = Status.Ok,
        additionalHeaders = Headers.accessControlAllowOrigin("*"),
        stream = eventStream
      )
  }
}

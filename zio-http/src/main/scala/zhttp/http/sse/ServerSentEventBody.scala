package zhttp.http.sse

import zhttp.http.{Body, HTTP_CHARSET}
import zio.Chunk
import zio.stream.ZStream

object ServerSentEventBody {

  def fromEventStream(stream: ZStream[Any, Throwable, ServerSentEvent]): Body =
    Body.fromStream(serializeEventStream(stream))

  private[sse] def serializeEventStream(stream: ZStream[Any, Throwable, ServerSentEvent]): ZStream[Any, Throwable, Byte] =
    stream.map(seq => Chunk.fromArray(seq.toStringRepresentation.getBytes(HTTP_CHARSET))).flattenChunks
}

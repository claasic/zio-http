package zio.http.sse

import zio.Chunk
import zio.http.Body
import zio.http.model.HTTP_CHARSET
import zio.stream.ZStream

object ServerSentEventBody {

  def fromEventStream(stream: ZStream[Any, Throwable, ServerSentEvent]): Body =
    Body.fromStream(serializeEventStream(stream))

  private[sse] def serializeEventStream(
    stream: ZStream[Any, Throwable, ServerSentEvent],
  ): ZStream[Any, Throwable, Byte] =
    stream.map(seq => Chunk.fromArray(seq.toStringRepresentation.getBytes(HTTP_CHARSET))).flattenChunks
}

package zhttp.http.sse

import zhttp.http.Body.fromStream
import zhttp.http.{Body, HTTP_CHARSET}
import zio.Chunk
import zio.stream.ZStream

object ServerSentEventBody {
  private[sse] def serializeEventStream(stream: ZStream[Any, Throwable, ServerSentEvent]): ZStream[Any, Throwable, Byte] =
    stream.map(seq => Chunk.fromArray(seq.toStringRepresentation.getBytes(HTTP_CHARSET))).flattenChunks

  def fromEventStream(stream: ZStream[Any, Throwable, ServerSentEvent]): Body =
    fromStream(serializeEventStream(stream))
}

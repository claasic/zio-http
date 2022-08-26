package zhttp.http.sse

import zhttp.http.{Body, HTTP_CHARSET}
import zhttp.http.Body.fromStream
import zio.Chunk
import zio.stream.ZStream

object ServerSentEventBody {
  def fromEventStream(stream: ZStream[Any, Throwable, ServerSentEvent]): Body =
    fromStream(stream.map(seq => Chunk.fromArray(seq.toStringRepresentation.getBytes(HTTP_CHARSET))).flattenChunks)
}

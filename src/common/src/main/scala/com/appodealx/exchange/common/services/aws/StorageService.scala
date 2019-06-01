package com.appodealx.exchange.common.services.aws

import akka.http.scaladsl.model.ContentType
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.Future

trait StorageService {

  def get(hash: String): Future[Option[(ContentType, Array[Byte])]]

  def check(hash: String): Future[Boolean]

  def put(hash: String, contentType: ContentType, content: Array[Byte], contentSha256: Option[String] = None): Future[Boolean]

  def putSource(hash: String,
                contentLength: Long,
                contentType: ContentType,
                content: Source[ByteString, Any],
                contentSha256: Option[String] = None): Future[Boolean]
}
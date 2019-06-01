package com.appodealx.exchange.common.services

import com.appodealx.exchange.common.services.crypto.Signer

import scala.collection.immutable.ListMap

class ParamsSigner(signer: Signer, secureParams: Set[String]) {
  import com.appodealx.exchange.common.models.CallbackParams._

  private val Separator = "|"

  private def paramsString(params: Map[String, String]) =
    params.toList.sortBy(_._1).map(_._2).mkString(Separator)

  private def signParams(params: Map[String, String]): String = signer.sign(paramsString(params))

  def sign(params: Map[String, String]): Map[String, String] = {
    val paramsToSign = params.filterKeys(secureParams.contains)
    params + (TokenParam -> signParams(paramsToSign))
  }

  def verify(params: Map[String, String]): Boolean = {
    val paramsToVerify = params.filterKeys(secureParams.contains)
    val verifier = signer.verify(paramsString(paramsToVerify), _)
    val token = params.get(TokenParam)

    paramsToVerify.nonEmpty && token.exists(verifier)
  }

}

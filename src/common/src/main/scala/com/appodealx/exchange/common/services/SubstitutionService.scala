package com.appodealx.exchange.common.services

import org.apache.commons.lang3.StringUtils


class SubstitutionService {

  def substitute(params: Map[String, String])(s: String): String = substitute(s, params.toSeq: _*)

  def substitute(str: String, macros: (String, String)*): String = {
    val searchList = macros.map("${" + _._1 + "}").toArray[String]
    val replaceList = macros.map(_._2).toArray[String]

    StringUtils.replaceEachRepeatedly(str, searchList, replaceList)
  }
}
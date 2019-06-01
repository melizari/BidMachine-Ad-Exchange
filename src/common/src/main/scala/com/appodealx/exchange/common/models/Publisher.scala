package com.appodealx.exchange.common.models

case class Publisher(id: Option[PublisherId] = None,
                     name: String,
                     autoAppCreation: Boolean = false)

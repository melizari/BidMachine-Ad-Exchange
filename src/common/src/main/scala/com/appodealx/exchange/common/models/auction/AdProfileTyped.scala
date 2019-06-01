package com.appodealx.exchange.common.models.auction

trait AdProfileTyped[A] extends AdProfile {
  type Repr = A
}

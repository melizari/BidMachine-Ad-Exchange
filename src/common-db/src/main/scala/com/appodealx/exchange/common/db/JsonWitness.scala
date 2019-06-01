package com.appodealx.exchange.common.db

trait JsonWitness[A]

object JsonWitness {
  def apply[A] = new JsonWitness[A] {}
}
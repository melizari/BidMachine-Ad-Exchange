package com.appodealx.exchange.common.utils

import scala.language.higherKinds


abstract class TypeClassSelector[Inst[_] <: TypeClassInst[_]](inst: Seq[Inst[_]]) { self =>

  protected def selectInst[A](implicit tag: TypeTag[A]): Inst[A] = {
    inst.find(_.tag == tag)
      .map(_.asInstanceOf[Inst[A]])
      .getOrElse(throw new InstNotFound(self.getClass.getSimpleName))
  }

}

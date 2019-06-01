package com.appodealx.exchange.common.utils.xtract

import scala.xml.{Attribute, MetaData, NodeSeq}

trait XmlWriter[A] {

  def write(a: A): NodeSeq

}

object XmlWriter {

  def of[A](implicit w: XmlWriter[A]) = w

  def apply[A](f: A => NodeSeq): XmlWriter[A] = (a: A) => f(a)

  implicit def seqXmlWriter[A: XmlWriter] = {
    val w = implicitly[XmlWriter[A]]
    XmlWriter[List[A]] { _.foldLeft(NodeSeq.Empty)((n, a) => n ++ w.write(a)) }
  }

  implicit def optXmlWriter[A: XmlWriter] = {
    val w = implicitly[XmlWriter[A]]
    XmlWriter[Option[A]] { _.fold(NodeSeq.Empty)(w.write)  }
  }

  object syntax {

    implicit class HasXmlWriter[A](a: A) {
      def toXml(implicit w: XmlWriter[A]) = w.write(a)
    }

    implicit class ElemOps(e: xml.Elem) {
      def addAttrs(seq: List[(String, String)]): xml.Elem = {
        e % seq.foldLeft[MetaData](xml.Null) { (attr, t) =>
          Attribute(None, t._1, xml.Text(t._2), attr)
        }
      }

      def addAttrOpt(attr: Option[(String, String)]): xml.Elem = {
        attr.fold(e)(a => e % Attribute(None, a._1, xml.Text(a._2), xml.Null))
      }
    }
  }

}
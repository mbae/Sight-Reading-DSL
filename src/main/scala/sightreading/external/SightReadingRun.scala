package sightreading.external
import java.io.File
import scalafx.application.JFXApp

import sightreading.external.parser._

object Sightreading {
  def main(args: Array[String]) {
    val source = scala.io.Source.fromFile(args(0))
    val lines = try source.mkString finally source.close()
    semantics.eval(SightReadingParser(lines).get)
  }


}
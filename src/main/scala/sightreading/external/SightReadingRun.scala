package sightreading.external
import java.io.File
import sys.process._
import sightreading.external.parser._

object Sightreading {
  def main(args: Array[String]) {
    if (args.length < 1 || args.length > 3) {
      println("Not the right number of arguments; Requires 1 or 2 arguments.")
      return
    }
    
    // I should have flags, but I have no time for now
    var name = "default"
    if (args.length >= 2) {
      name = args(1)
    }
    var numberOfCopies = "1"
    if (args.length == 3) {
      numberOfCopies = args(2)
    }
    val source = scala.io.Source.fromFile(args(0))
    val lines = try source.mkString finally source.close()
    semantics.eval(SightReadingParser(lines).get)
    
    // Runs the python program on the output from Scala
    val command = "python sheetMusicMaker.py " + name + " " + numberOfCopies
    command !
  }
}
package sightreading.external

import sightreading.external.ir._
import java.io._
import sys.process._
package object semantics {
  def eval(rules: List[Bars])= {
    val pw = new PrintWriter(new FileOutputStream("exportToPython.sr", false));
    for (e <- rules) {
      val stringRepresentation = convertBars(e)
      pw.println(stringRepresentation)
    }
    pw.close()
    // Runs the python program on the output from Scala
    "python sheetMusicMaker.py" !
  }
  
  def convertBars(x: Bars): String = x match {
    case Bars(MajorScale(k),n) =>
        k + " " + n
    case Bars(MinorScale(k),n) =>
        k + " " + n
  }
}
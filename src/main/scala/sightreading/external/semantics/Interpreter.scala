package sightreading.external

import sightreading.external.ir._
import java.io._
import sys.process._

object tableOfVars {
  var defMap:Map[String,List[Statement]] = Map()
  var globalKey:Scale = null;
  var globalTime:TimeSignature = null;
}

package object semantics {
  def eval(rules: List[Statement])= {
    // Setup
    putDefinitions(rules)
    checkForCycles(tableOfVars.defMap)
    
    // Go through statements once more and write them out to file
    val pw = new PrintWriter(new FileOutputStream("exportToPython.sr", false))
    for (e <- rules) {
      val stringRepresentation = convertBars(e)
      pw.println(stringRepresentation)
    }
    pw.close()
    // Runs the python program on the output from Scala
    "python sheetMusicMaker.py" !
  }
  
  def convertBars(x: Bars): String = x match {
    case Bars(MajorScale(k),TimeSignature(a,b),n) =>
        k + " " + a.toString() + " " + b.toString() + " " + n
    case Bars(MinorScale(k),TimeSignature(a,b),n) =>
        k + " " + a.toString() + " " + b.toString() + " " + n
  }
  
  // Passes through rules to put the appropriate definitions first
  def putDefinitions(rules: List[Statement])= {
    rules.foreach { rule => 
       rule match {
         case Definition(name,x) => tableOfVars.defMap += (name -> x)
         case globalKey(k) => tableOfVars.globalKey = k
         case globalTime(t) => tableOfVars.globalTime = t
         case _ =>  // Do nothing otherwise (the underscore is invisible in eclipse)
       }
    }
  }
  
  def checkForCycles(tableOfDefs: Map[String,List[Statement]]): Boolean= {
    var unvisitedDefs: collection.mutable.Set[String] = collection.mutable.Set[String](tableOfDefs.keySet.toSeq:_*)
    while (!unvisitedDefs.isEmpty) {
      val element = unvisitedDefs.head
      unvisitedDefs -= element
      if (performDFS(element, unvisitedDefs))
        true
    }
    false
  }
  
  // Returns false if there is no cycle
  def performDFS(element: String, unvisitedDefs: collection.mutable.Set[String]): Boolean= {
    var finalBool = false
    tableOfVars.defMap(element).foreach { x =>
      x match {
        case Variable(n) => if (unvisitedDefs contains n) {unvisitedDefs -= n; performDFS(n, unvisitedDefs);} else finalBool = true
        case _ =>
      }
    }
    finalBool
  }
}
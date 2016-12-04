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
    // Set up the map of definintions and do some error checking
    putDefinitions(rules)
    checkForCycles(tableOfVars.defMap)
    
    // Go through statements once more and write them out to file
    val pw = new PrintWriter(new FileOutputStream("exportToPython.sr", false))
    for (e <- rules) {
      val stringRepresentation = convertStatement(e)
      if (stringRepresentation != null) {
        pw.println(stringRepresentation.trim())
      }
    }
    pw.close()
    // Runs the python program on the output from Scala
    "python sheetMusicMaker.py" !
  }
  
  def convertStatement(x: Statement): String = x match {
    case Bars(scale,time,num) =>
      convertBars(scale,time,num) // k + " " + a.toString() + " " + b.toString() + " " + n
    case Variable(name) =>
      convertVariable(name)
    case VariableWithRepetition(name, num) =>
      convertVariableWithRepetition(name, num)
    case _ =>
      null
  }
  
  /*
   * Ugly helper function to help with different cases of what user can write for Bars
   * Does error checking based on if global key and/or time signature was set in the program
   */
  def convertBars(key: Option[Scale], time: Option[TimeSignature], num: Int): String = (key,time,num) match {
    case (Some(Scale(k,q)),Some(TimeSignature(n1,n2)),num) =>
      k + " " + q + " " + n1.toString() + " " + n2.toString() + " " + num.toString()
    case (None,Some(TimeSignature(n1,n2)),num) =>
      if (tableOfVars.globalKey == null) {
        throw new NoSuchFieldException("Global key doesn't exist; Need to specify a global key") // Want to let the user know where this error occurs
      } else {
        val Scale(k,q) = tableOfVars.globalKey
        k + " " + q + " " + n1.toString() + " " + n2.toString() + " " + num.toString()
      }
    case (Some(Scale(k,q)),None,num) =>
      if (tableOfVars.globalTime == null) {
        throw new NoSuchFieldException("Global time doesn't exist; Need to specify a global time") // Want to let the user know where this error occurs
      } else {
        val TimeSignature(n1,n2) = tableOfVars.globalTime
        k + " " + q + " " + n1.toString() + " " + n2.toString() + " " + num.toString()
      }
    case (None, None, num) =>
      if ((tableOfVars.globalTime == null) && (tableOfVars.globalTime == null)) {
        throw new NoSuchFieldException("Global key and global time doesn't exist")
      } else if(tableOfVars.globalTime == null) {
        throw new NoSuchFieldException("Global time doesn't exist; Need to specify a global time")
      } else if(tableOfVars.globalKey == null) {
        throw new NoSuchFieldException("Global key doesn't exist; Need to specify a global key")
      } else {
        val Scale(k,q) = tableOfVars.globalKey
        val TimeSignature(n1,n2) = tableOfVars.globalTime
        k + " " + q + " " + n1.toString() + " " + n2.toString() + " " + num.toString()
      }
  }
  
  def convertVariableWithRepetition(name: String, num: Int): String= {
    if (num <= 1) {
      return convertVariable(name)
    }
    convertVariable(name) + convertVariableWithRepetition(name, num - 1)
  }
  
  def convertVariable(name: String): String= {
    val getMap = tableOfVars.defMap get name
    if (getMap.equals(None)) {
      throw new NoSuchFieldException("No definition is found for variable " + name)
    }
    // After checking if variable is in dictionary, evaluate it
    val Some(listOfRules) = getMap
    var finalString: String = ""
    for (e <- listOfRules) {
      e match {
        case Variable(n) =>
          finalString += convertVariable(n)
        case Bars(k,t,n) =>
          finalString += convertBars(k,t,n) + "\n"
        case _ =>
          
      }
    }
    finalString
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
  
  def checkForCycles(tableOfDefs: Map[String,List[Statement]])= {
    var unvisitedDefs: collection.mutable.Set[String] = collection.mutable.Set[String](tableOfDefs.keySet.toSeq:_*)
    while (!unvisitedDefs.isEmpty) {
      val element = unvisitedDefs.head
      unvisitedDefs -= element
      if (performDFS(element, unvisitedDefs)) {
        throw cycleDetectedException("Cycle found definitions. Will lead to infinite loop")
      }
    }
  }
  
  // Returns false if there is no cycle
  def performDFS(element: String, unvisitedDefs: collection.mutable.Set[String]): Boolean= {
    var finalBool = false
    tableOfVars.defMap(element).foreach { x =>
      x match {
        case Variable(n) => if (unvisitedDefs contains n) {unvisitedDefs -= n; performDFS(n, unvisitedDefs);} else {finalBool = true}
        case _ =>
      }
    }
    finalBool
  }
}
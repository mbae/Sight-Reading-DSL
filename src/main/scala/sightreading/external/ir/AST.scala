package sightreading.external.ir

// A generic set of note and time signature
case class Scale(key: String, quality: String)
case class TimeSignature(top: Int, bot: Int)

// Something users can say to create music
sealed abstract class Statement
case class Bars(key: Option[Scale], time: Option[TimeSignature], numBars: Int) extends Statement
case class BarsWithVariance(key: Option[Scale], time: Option[TimeSignature], minBars: Int, maxBars: Int) extends Statement
case class Variable(varName: String) extends Statement
case class VariableWithRepetition(varName: String, rep: Int) extends Statement

// Assign statements to a variable for convenience
case class Definition(name: String, defs: List[Statement]) extends Statement
// Users can write stuff that is global to the whole sheet music
sealed abstract class globalVar extends Statement
case class globalTime(value: TimeSignature) extends globalVar
case class globalKey(value: Scale) extends globalVar


case class cycleDetectedException(smth:String)  extends Exception(smth)
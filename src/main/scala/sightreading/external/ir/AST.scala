package sightreading.external.ir

// A generic set of Notes
sealed abstract class Scale
case class MajorScale(k: String) extends Scale
case class MinorScale(k: String) extends Scale

case class TimeSignature(top: Int, bot: Int)

// Something users can say to create music
sealed abstract class Statement
case class Bars(key: Option[Scale], time: Option[TimeSignature], numBars: Int) extends Statement
case class Variable(varName: String) extends Statement

// Assign statements to a variable for convenience
case class Definition(name: String, defs: List[Statement]) extends Statement
// Users can write stuff that is global to the whole sheet music
sealed abstract class globalVar extends Statement
case class globalTime(value: TimeSignature) extends globalVar
case class globalKey(value: Scale) extends globalVar
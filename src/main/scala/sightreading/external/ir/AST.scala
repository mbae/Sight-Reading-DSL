package sightreading.external.ir

// A generic set of Notes
sealed abstract class Scale

// Support for only two kinds of scales for now
case class MajorScale(k: String) extends Scale
case class MinorScale(k: String) extends Scale

// A group of measures and only takes in a key for now
case class Bars(key: Scale, numBars: Int)

// A group of bars forms the music
// This may change into phrases, sections, etc.
//case class groupOfBars(bars: List[Bar])
package sightreading.external.parser
import scala.util.parsing.combinator._
import sightreading.external.ir._
import scala.collection.mutable.Map

object keyHolder {
  val majorKeys = """[A,B,C,D,E,F,G,Bb,Eb,Ab,Db,Gb,Cb,F#,C#]""".r
  val minorKeys = """[a,e,b,c#,g#,ab,d#,eb,a#,bb,f,c,g,d]""".r
  var defMaps:Map[String,List[Bars]] = Map()
}

object SightReadingParser extends JavaTokenParsers with RegexParsers{
    def apply(s: String): ParseResult[List[Bars]] = {
      parseAll(thebars, s)
    }
    
    def thebars: Parser[List[Bars]] = {
      (   keyParser~","~timeParser~"for"~wholeNumber~"bars"~";"~thebars ^^ {case k~","~t~"for"~n~"bars"~";"~more => List(Bars(k,t,n.toInt)) ++ more}
        | keyParser~","~timeParser~"for"~wholeNumber~"bars"~";" ^^ {case k~","~t~"for"~n~"bars"~";" => List(Bars(k,t,n.toInt))})
        
    }
    
    def timeParser: Parser[TimeSignature] = {
      (   wholeNumber~"/"~wholeNumber ^^ {case n1~"/"~n2 => TimeSignature(n1.toInt,n2.toInt)})
    }
    
    def keyParser: Parser[Scale] = {
      (   keyHolder.majorKeys~"major" ^^ {case k~"major" => MajorScale(k)}
        | keyHolder.minorKeys~"minor" ^^ {case k~"minor" => MinorScale(k)})
    }  
}


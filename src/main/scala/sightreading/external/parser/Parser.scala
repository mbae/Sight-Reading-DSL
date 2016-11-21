package sightreading.external.parser
import scala.util.parsing.combinator._
import sightreading.external.ir._

object keyHolder {
  val majorKeys = """[A,B,C,D,E,F,G,Bb,Eb,Ab,Db,Gb,Cb,F#,C#]""".r
  val minorKeys = """[a,e,b,c#,g#,ab,d#,eb,a#,bb,f,c,g,d]""".r
  
}

object SightReadingParser extends JavaTokenParsers with RegexParsers{
    def apply(s: String): ParseResult[List[Bars]] = {
      parseAll(thebars, s)
    }
    
    def thebars: Parser[List[Bars]] = {
      (   "Bars"~"for"~wholeNumber~"in"~keyParser~thebars ^^ {case "Bars"~"for"~n~"in"~k~more => (List(Bars(k,n.toInt))) ++ more}
        | "Bars"~"for"~wholeNumber~"in"~keyParser ^^ {case "Bars"~"for"~n~"in"~k => (List(Bars(k,n.toInt)))})
    }
    
    def keyParser: Parser[Scale] = {
      (   keyHolder.majorKeys~"major" ^^ {case k~"major" => MajorScale(k)}
        | keyHolder.minorKeys~"minor" ^^ {case k~"minor" => MinorScale(k)})
    }  
}


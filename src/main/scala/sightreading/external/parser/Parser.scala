package sightreading.external.parser
import scala.util.parsing.combinator._
import sightreading.external.ir._
import scala.collection.mutable.Map

object keyHolder {
  val majorKeys = """[A,B,C,D,E,F,G,Bb,Eb,Ab,Db,Gb,Cb,F#,C#]""".r
  val minorKeys = """[a,e,b,c#,g#,ab,d#,eb,a#,bb,f,c,g,d]""".r
}

object SightReadingParser extends JavaTokenParsers with RegexParsers{
    def apply(s: String): ParseResult[List[Statement]] = {
      parseAll(allStatementsParser, s)
    }
    
    def allStatementsParser: Parser[List[Statement]] = {
      (   statementParser ~ allStatementsParser ^^ {case a ~ more => a ++ more}
        | statementParser ^^ {case a => a}
      )
    }
    
    def statementParser: Parser[List[Statement]] = {
      (   actionParser ^^ {case a => a}
        | defParser ^^ {case d => d}
      )
    }
    
    // Parses a definition of a variable a user has made regardless of whether
    // the variable is global or specific to the program
    def defParser: Parser[List[Statement]] = {
      (   "def" ~ stringLiteral ~ "{" ~ actionParser ~ "}" ^^ {case "def" ~ s ~ "{" ~ a ~ "}" => List(Definition(s,a))}
        | "time" ~ ":" ~ timeParser ^^ {case "time" ~ ":" ~ t => List(globalTime(t))}
        | "key" ~ ":" ~ keyParser ^^ {case "key" ~ ":" ~ k => List(globalKey(k))}
      )
    }
    
    // Parses things that are supposed to be written to sheet music
    def actionParser: Parser[List[Statement]] = {
      (   barParser ^^ {case b => b}
        | stringLiteral~";" ^^ {case s~";" => List(Variable(s))}
      )
    }
    
    // Basic units of music that can be specified
    def barParser: Parser[List[Statement]] = {
      (   keyParser~","~timeParser~"for"~wholeNumber~"bars"~";" ^^ {case k~","~t~"for"~n~"bars"~";" => List(Bars(Some(k),Some(t),n.toInt))}
        | keyParser~"for"~wholeNumber~"bars"~";" ^^ {case k~"for"~n~"bars"~";" => List(Bars(Some(k),None,n.toInt))}
        | timeParser~"for"~wholeNumber~"bars"~";" ^^ {case t~"for"~n~"bars"~";" => List(Bars(None,Some(t),n.toInt))}
        | wholeNumber~"bars"~";" ^^ {case n~"bars"~";" => List(Bars(None, None,n.toInt))}
      )
    }
    
    // Parses a time signature
    def timeParser: Parser[TimeSignature] = {
      (   wholeNumber~"/"~wholeNumber ^^ {case n1~"/"~n2 => TimeSignature(n1.toInt,n2.toInt)})
    }
    
    // Parses a key signature
    def keyParser: Parser[Scale] = {
      (   keyHolder.majorKeys~"major" ^^ {case k~"major" => MajorScale(k)}
        | keyHolder.minorKeys~"minor" ^^ {case k~"minor" => MinorScale(k)})
    }  
}


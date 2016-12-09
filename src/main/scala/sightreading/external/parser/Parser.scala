package sightreading.external.parser
import scala.util.parsing.combinator._
import sightreading.external.ir._
import scala.collection.mutable.Map

object keyHolder {
  val majorKeys = "[CF]#|[BEADGC]b|[CDEFGAB]".r
  val minorKeys = "[cgda]#|[aeb]b|[aebfcgd]".r
  val aWord = "\\w+".r // Used to parse a single, general word
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
      (   "def" ~ keyHolder.aWord ~ "{" ~ actionParsers ~ "}" ^^ {case "def" ~ s ~"{" ~ a ~ "}" => List(Definition(s,a))}
        | "time" ~ ":" ~ timeParser ~ ";" ^^ {case "time" ~ ":" ~ t ~ ";" => List(globalTime(t))}
        | "key" ~ ":" ~ keyParser ~ ";" ^^ {case "key" ~ ":" ~ k ~ ";" => List(globalKey(k))} withErrorMessage "Could not parse definition"
      )
    }
    
    // Helper function for defParser to recursively parse actions 
    def actionParsers: Parser[List[Statement]] = {
      (   actionParser ~ actionParsers ^^ {case a ~ more => a ++ more}
        | actionParser ^^ {case a => a}
      )
    }
    
    // Parses things that are supposed to be written to sheet music
    def actionParser: Parser[List[Statement]] = {
      (   barParser ^^ {case b => b}
        | keyHolder.aWord ~ wholeNumber ~ "times" ~ ";" ^^ {case s~n~"times"~";" => List(VariableWithRepetition(s,n.toInt))}
        | keyHolder.aWord ~ ";" ^^ {case s~";" => List(Variable(s))}
      )
    }
    
    // Basic units of music that can be specified
    def barParser: Parser[List[Statement]] = {
      (   keyParser~","~timeParser~"for"~rangeOrNumParser~"bars"~";" ^^ {case k~","~t~"for"~n~"bars"~";" => List(Bars(Some(k),Some(t),n))}
        | timeParser~","~keyParser~"for"~rangeOrNumParser~"bars"~";" ^^ {case t~","~k~"for"~n~"bars"~";" => List(Bars(Some(k),Some(t),n))}
        | keyParser~"for"~rangeOrNumParser~"bars"~";" ^^ {case k~"for"~n~"bars"~";" => List(Bars(Some(k),None,n))}
        | timeParser~"for"~rangeOrNumParser~"bars"~";" ^^ {case t~"for"~n~"bars"~";" => List(Bars(None,Some(t),n))}
        | rangeOrNumParser~"bars"~";" ^^ {case n~"bars"~";" => List(Bars(None, None,n))}
      )
    }
    
    def rangeOrNumParser: Parser[Int] = {
      (   wholeNumber ~ "-" ~ wholeNumber ^^ {case start ~ "-" ~ end => getRandomNumber(start.toInt,end.toInt)}
        | wholeNumber ^^ {case n => n.toInt}
      )
    }
    
    // Parses a time signature
    def timeParser: Parser[TimeSignature] = {
      (   wholeNumber~"/"~wholeNumber ^^ {case n1~"/"~n2 => TimeSignature(n1.toInt,n2.toInt)} withErrorMessage "Could not parse time signature")
    }
    
    // Parses a key signature
    def keyParser: Parser[Scale] = {
      (   keyHolder.majorKeys~"major" ^^ {case k~"major" => Scale(k, "major")}
        | keyHolder.minorKeys~"minor" ^^ {case k~"minor" => Scale(k, "minor")} withErrorMessage "Could not parse the key signature")
    }
    
    def getRandomNumber(start: Int,end: Int): Int = {
      val rnd = new scala.util.Random
      return start + rnd.nextInt( (end - start) + 1 )
    }
}


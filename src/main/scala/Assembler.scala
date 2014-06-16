import scala.util.parsing.combinator._

class RedCode extends JavaTokenParsers {
  def instruction: Parser[Any] = opcode~operand~","~operand
  def opcode: Parser[Any] = "ADD" | "MOV" | "JMP" | "DAT"
  def operand: Parser[Any] = opt(addressing)~wholeNumber
  def addressing: Parser[Any] = "@" | "<" | "#"

  def parse(s: String) = parseAll(instruction, s.toUpperCase)
}
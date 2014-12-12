import VirtualMachine._
import org.parboiled2._
import scala.util._

class Assembler(val input: ParserInput) extends Parser {
  def InputLine = rule {
    zeroOrMore(NewLine) ~ oneOrMore(Expression).separatedBy(oneOrMore(NewLine)) ~ zeroOrMore(NewLine) ~ EOI
  }

  def Expression = rule {
    WhiteSpace ~ (Opcode ~ Operand ~ Separator ~ Operand) ~ WhiteSpace ~> { Instruction(_: Opcode, _: Operand, _: Operand) }
  }

  def WhiteSpace = rule { zeroOrMore(ch(' ') | ch('\t')) }
  def NewLine    = rule { WhiteSpace ~ '\n'}
  def Separator  = rule { WhiteSpace ~ optional(',') ~ WhiteSpace}
  def Opcode     = rule { valueMap(Map("add" → Add,
                                       "mov" → Mov,
                                       "jmp" → Jmp,
                                       "dat" → Dat,
                                       "sub" → Sub,
                                       "cmp" → Cmp,
                                       "jmz" → Jmz,
                                       "slt" → Slt,
                                       "jmn" → Jmn,
                                       "djn" → Djn,
                                       "spl" → Spl)) }
  def Operand   = rule { WhiteSpace ~ AddrMode ~ WhiteSpace ~ Number ~> VirtualMachine.Operand }
  def AddrMode  = rule { valueMap(Map("@" → Indirect,
                                      "#" → Immediate,
                                      "<" → PreDecrement,
                                      ""  → Direct)) }
  def Number    = rule { capture(Digits) ~> (_.toInt) }
  def Digits    = rule { optional("-") ~ oneOrMore(CharPredicate.Digit) }

  def compile   = InputLine.run() match {
    case Success(x: Vector[Instruction]) => x.toList
    case _                               => List()
  }
}

object RedCode {
  def apply(code: String) = new Assembler(code.stripMargin.toLowerCase).compile
}
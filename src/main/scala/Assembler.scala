import VirtualMachine._
import org.parboiled2._
import scala.util._

class Assembler(val input: ParserInput) extends Parser {
  def InputLine = rule {
    zeroOrMore(NewLine) ~ oneOrMore(Expression).separatedBy(oneOrMore(NewLine)) ~ zeroOrMore(NewLine) ~ EOI
  }

  def Expression: Rule1[Instruction] = rule {
    WhiteSpace ~ (Opcode ~ Operand ~ Separator ~ Operand) ~ WhiteSpace ~>
      (Instruction(_: Opcode, _: Operand, _: Operand))
  }

  def WhiteSpace = rule { zeroOrMore(ch(' ') | ch('\t')) }
  def NewLine    = rule { WhiteSpace ~ '\n'}
  def Separator  = rule { (WhiteSpace ~ "," ~ WhiteSpace) | WhiteSpace}
  def Opcode     = rule { (capture(ignoreCase("add")) ~> (_ => Add) |
                           capture(ignoreCase("mov")) ~> (_ => Mov) |
                           capture(ignoreCase("jmp")) ~> (_ => Jmp) |
                           capture(ignoreCase("dat")) ~> (_ => Dat)) ~
                           WhiteSpace }
  def Operand   = rule { AddrMode ~ WhiteSpace ~ Number ~> VirtualMachine.Operand }
  def AddrMode  = rule { capture("@") ~> (_ => Indirect) |
                         capture("#") ~> (_ => Immediate) |
                         capture("<") ~> (_ => PreDecrement) |
                         capture("")  ~> (_ => Direct) }
  def Number    = rule { capture(Digits) ~> (_.toInt) }
  def Digits    = rule { optional("-") ~ oneOrMore(CharPredicate.Digit) }

  def compile: List[Instruction] = InputLine.run() match {
    case Success(x: Vector[Instruction]) => x.toList
    case _                               => List()
  }
}
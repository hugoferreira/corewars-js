import org.parboiled2._

import VirtualMachine._
import scala.util.Try._

class Assembler(val input: ParserInput) extends Parser {
  def InputLine = rule {
    oneOrMore(Expression).separatedBy(oneOrMore(NewLine)) ~ EOI
  }

  def Expression: Rule1[Instruction] = rule {
    WhiteSpace ~ (Opcode ~ Operand ~ Separator ~ Operand) ~ WhiteSpace ~>
      (Instruction(_: Opcode, _: Operand, _: Operand))
  }

  def WhiteSpace = rule { zeroOrMore(ch(' ') | ch('\t')) }
  def NewLine    = rule { WhiteSpace ~ '\n'}
  def Separator  = rule { (WhiteSpace ~ "," ~ WhiteSpace) | WhiteSpace}
  def Opcode     = rule { (capture(ignoreCase("add") ~> (_ => Add) |
    capture("MOV") ~> (_ => Mov) |
    capture("JMP") ~> (_ => Jmp) |
    capture("DAT") ~> (_ => Dat)) ~
    WhiteSpace }
  def Operand   = rule { AddrMode ~ WhiteSpace ~ Number ~> VirtualMachine.Operand }
  def AddrMode  = rule { capture("@") ~> (_ => Indirect) |
    capture("#") ~> (_ => Immediate) |
    capture("<") ~> (_ => PreDecrement) |
    capture("")  ~> (_ => Direct) }
  def Number    = rule { capture(Digits) ~> (_.toInt) }
  def Digits    = rule { optional("-") ~ oneOrMore(CharPredicate.Digit) }
}

new Assembler(" MOV <3  #-5").InputLine.run()
new Assembler(" MOV <3, #-5").InputLine.run()
new Assembler(" MOV  3, @-5").InputLine.run()
new Assembler(" DAT #0  # 0").InputLine.run()

new Assembler(
  """MOV <3 #-5
    |
    |DAT #0 #0""".stripMargin).InputLine.run()


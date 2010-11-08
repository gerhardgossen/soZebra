package de.ovgu.findke.gossen.sozebra.features

import de.ovgu.findke.gossen.sozebra._
import Implicits._
import scala.collection.JavaConversions._
import java.util.regex.Pattern
import scala.util.matching.Regex
import cc.mallet.types.{TokenSequence}

class FragmentContainsSenderName extends LinesPipe {
    def lineValues (m: Message) = {
        val senderName = {
            val senderNameTokens = m.sender.name.split("\\s+")
            val senderNameRE = (senderNameTokens map { Pattern.quote(_) }) mkString( ".*(", "|", ").*" )
            new Regex(senderNameRE)
        }
        withTokenizedLines(m) { l =>
            if (l.iterator exists { case senderName() => true })
                1.0
            else
                0.0
        }
    }
}

class PriorFragmentContainsSenderName extends LinesPipe {
    def lineValues (m: Message) = {
        val senderName = {
            val senderNameTokens = m.sender.name.split("\\s+")
            val senderNameRE = (senderNameTokens map { Pattern.quote(_) }) mkString( ".*(", "|", ").*" )
            new Regex(senderNameRE)
        }
        val lines = m.lines
        val tailSize = lines.dropWhile { case senderName() => false; case _ => true }.length
        List.fill(lines.length - tailSize + 1)(0.0) ++ List.fill(tailSize - 1)(1.0)
    }
}

class FragmentContainsSenderInitials extends LinesPipe {
    def lineValues (m: Message) = {
        val ContainsInitials = new Regex(getRE(getInitials(m.sender.name)))
        withLines(m) {
            case ContainsInitials() => 1.0
            case _                  => 0.0
        }
    }

    private def getRE (initials: List[String]) = {
        var patterns = List(
            initials.mkString(""), // AB
            initials.mkString("", ".", "."), // A.B.
            initials.mkString("", ". ", ".") // A. B.
        )
        if (initials.size > 2) {
            val shortInitials = initials.head :: initials.get( initials.size - 1) :: Nil
            patterns ++ List(
                shortInitials.mkString(""), // AB
                shortInitials.mkString("", ".", "."), // A.B.
                shortInitials.mkString("", ". ", ".") // A. B.
            )
        }
        patterns.map { Pattern.quote(_) }.mkString("(?:", "|", ")")
    }

    private def getInitials (senderName: String) = {
            var name = senderName
            val commaPos = name.indexOf(",")
            if (commaPos >= 0) {
                val name = senderName.substring(commaPos + 1 ) +
                    " " + senderName.substring(0, commaPos)
            }
            name.split("\\s+").map { _.substring(0,1) }.toList
    }
}

class FragmentContainsRecipientName extends LinesPipe {
    def lineValues (m: Message) = {
        val ContainsName = new ContainsNameMatcher(m.recipients map { _.name })
        withTokenizedLines(m) {
            case ContainsName() => 1.0
            case _                   => 0.0
        }
    }

    private def getRE (names: List[String]) = {
        val parts = names flatMap { _.split("\\s+") } map { _.replaceAll(",", "") }
        parts.mkString( "(?:", "|", ")")
    }

    class ContainsNameMatcher(names: List[String]) {
        val re = Pattern.compile(getRE(names))
        def unapply(ts: TokenSequence) = {
            ts exists { t => re.matcher(t getText).matches }
        }
    }
}

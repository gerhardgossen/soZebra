package de.ovgu.findke.gossen.sozebra.features

import de.ovgu.findke.gossen.sozebra.Message
import cc.mallet.pipe._
import cc.mallet.types._

/**
 * Count the number of Words in each fragment.
 */
class NumberOfWords extends LinesPipe {
    def lineValues (m: Message) = {
        withTokenizedLines(m) { _.size }
    }
}

/**
 * Count the number of Unicode code points in each fragment.
 */
class NumberOfCodePoints extends LinesPipe {
    def lineValues (m: Message) = {
        withLines(m) { _.length }
    }
}

/**
 * Add the start position of each fragment (1..length)/length
 */
class StartPosition extends LinesPipe {
    def lineValues (m: Message) = {
        (1 to m.lines.size).toList map { _*1.0 / m.lines.size }
    }
}

/**
 * Add the end position of each fragment.
 * @see{StartPosition}
 */
class EndPosition extends LinesPipe {
    def lineValues (m: Message) = {
        (1 to m.lines.size).toList.reverse map { _*1.0 / m.lines.size }
    }
}

/**
 * Add the average line length.
 */
class AverageLineLength extends LinesPipe {
    def lineValues(m: Message) = {
        withLines(m) { l => 
            val lines = l.split("""\r?\n""")
            ( lines map { _.length } sum ) * 1.0 / lines.size
        }
    }
}

/**
 * Length of the fragment in characters relative to the previous one.
 */
class LengthRelativeToPrevious extends LinesPipe {
    def lineValues(m: Message) = {
        val lines = m.lines map { _.length }
        val prevLines = 0::lines
        (lines zip prevLines) map { case (cur, prev) => prev * 1.0/cur }
    }
}

/**
 * Length of the fragment in characters relative to the following one.
 */
class LengthRelativeToFollowing extends LinesPipe {
    def lineValues(m: Message) = {
        val lines = m.lines map { _.length }
        val nextLines = (lines drop 1 ) :+ 0
        (lines zip nextLines) map { case (cur, next) => next * 1.0/cur }
    }
}

private object EmptyLine {
    def unapply(s: String): Boolean = {
        s matches """^\s*\r?\n$"""
    }
}

class PreceedingEmptyLines extends LinesPipe {
    def lineValues (m: Message) = {
        val lines = m.lines
        lines.scanLeft(0.0){ (prevLines, curLine) => 
            curLine match {
                case EmptyLine() => prevLines + 1
                case _           => 0.0
            }
        }
    }
}

package de.ovgu.findke.gossen.sozebra.features

import de.ovgu.findke.gossen.sozebra.{Message,Implicits}
import Implicits._
import scala.collection.JavaConversions._
import cc.mallet.pipe._
import cc.mallet.types._
import scala.util.matching.Regex

class PriorFragmentContainsRepeatedPunctuation extends LinesPipe {
    def lineValues (m: Message) = {
        val lines = m.lines
        val tailSize = lines.dropWhile { ! _.matches("[.,;:!?-_]{2,}") }.length
        List.fill(lines.length - tailSize + 1)(0.0) ++ List.fill(tailSize - 1)(1.0)
    }
}

class FragmentContainsURL extends LinesPipe {
    // regex below is from http://daringfireball.net/2010/07/improved_regex_for_matching_urls
    val Url = new Regex(""".*(?i)\b((?:[a-z][\w-]+:(?:/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’])).*""")
    def lineValues (m: Message) = {
        withLines(m) { case Url() => 1.0; case _ => 0.0 }
    }
}

class FragmentContainsEmailAddress extends LinesPipe {
    val EmailAddress = new Regex(""".*[\w\d]+@[\w\d+]{2,}.*""")
    def lineValues(m: Message) = {
        withLines(m) { case EmailAddress() => 1.0; case _ => 0.0 }
    }
}

class FragmentContainsFourOrMoreDigits extends LinesPipe {
    val FourDigits = new Regex(".*\\d{4,}.*")
    def lineValues (m:Message) = {
        withLines(m) { case FourDigits => 1.0; case _ => 0.0 }
    }
}

abstract class CapitalisedWords extends LinesPipe {
    def numberOfCapitalisedWords(ts: TokenSequence) = {
        ts.iterator filter { t => t.getText matches "^[A-Z]" } length
    }
}

class NumberOfCapitalisedWords extends CapitalisedWords {
    def lineValues(m: Message) = {
        withTokenizedLines(m)(numberOfCapitalisedWords)
    }
}

class PercentageOfCapitalisedWords extends CapitalisedWords {
    def lineValues(m: Message) = {
        withTokenizedLines(m) { ts =>
            numberOfCapitalisedWords(ts) / ts.size
        }
    }
}

abstract class NonAlphanumericChars extends LinesPipe {
    def numberOfNonAlphanumerics (l: String) = {
        l.filterNot {_.isLetterOrDigit} length
    }
}

class NumberOfNonAlphanumerics extends NonAlphanumericChars {
    def lineValues(m: Message) = {
        withLines(m)(numberOfNonAlphanumerics)
    }
}

class PercentageOfNonAlphanumerics extends NonAlphanumericChars {
    def lineValues (m: Message) = {
        withLines(m) { l =>
            numberOfNonAlphanumerics(l) / l.length
        }
    }
}

abstract class NumericChars extends LinesPipe {
    def numberOfNumerics (l: String) = {
        l.filterNot {_.isDigit} length
    }
}

class NumberOfNumerics extends NumericChars {
    def lineValues(m: Message) = {
        withLines(m)(numberOfNumerics)
    }
}

class PercentageOfNumerics extends NumericChars {
    def lineValues (m: Message) = {
        withLines(m) { l =>
            numberOfNumerics(l) / l.length
        }
    }
}

class SubjectContainsReplyMarker extends LinesPipe {
    def lineValues (m: Message) = {
        val hasMarker = m.subject.matches("^[rR][eR]:?.*")
        val hasMarkerAsDouble = if (hasMarker) 1.0 else 0.0
        List.fill(m.lines.length) { hasMarkerAsDouble }
    }
}

class SubjectContainsForwardMarker extends LinesPipe {
    def lineValues (m: Message) = {
        val hasMarker = m.subject.matches("^[fF][wW][dD]?:?.*")
        val hasMarkerAsDouble = if (hasMarker) 1.0 else 0.0
        List.fill(m.lines.length) { hasMarkerAsDouble }
    }
}

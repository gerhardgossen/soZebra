package de.ovgu.findke.gossen.sozebra

import cc.mallet.pipe._
import scala.collection.JavaConversions._
import de.ovgu.findke.gossen.sozebra.features._

object Pipeline {
    def getPipeline: SerialPipes = {
        val pipes = List(
            new NumberOfWords,
            new NumberOfCodePoints,
            new StartPosition,
            new EndPosition,
            new AverageLineLength,
            new LengthRelativeToPrevious,
            new LengthRelativeToFollowing,
            new PreceedingEmptyLines,
            new PriorFragmentContainsRepeatedPunctuation,
            new FragmentContainsURL,
            new FragmentContainsEmailAddress,
            new FragmentContainsFourOrMoreDigits,
            new NumberOfCapitalisedWords,
            new PercentageOfCapitalisedWords,
            new NumberOfNonAlphanumerics,
            new PercentageOfNonAlphanumerics,
            new NumberOfNumerics,
            new PercentageOfNumerics,
            new SubjectContainsReplyMarker,
            new SubjectContainsForwardMarker,
            new FragmentContainsSenderName,
            new PriorFragmentContainsSenderName,
            new FragmentContainsSenderInitials,
            new FragmentContainsRecipientName
        )

        new SerialPipes(pipes)
    }
}

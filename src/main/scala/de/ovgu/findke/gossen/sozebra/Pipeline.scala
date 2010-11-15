package de.ovgu.findke.gossen.sozebra

import cc.mallet.pipe._
import cc.mallet.types.{FeatureVectorSequence,Instance,LabelSequence}
import scala.collection.JavaConversions._
import de.ovgu.findke.gossen.sozebra.features._
import java.util.Iterator

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
            new FragmentContainsRecipientName,
            new SplitSequenceIntoInstances
        )

        new SerialPipes(pipes)
    }
}

class SplitSequenceIntoInstances extends Pipe {
    class InstancesIterator(source: Iterator[Instance]) extends Iterator[Instance] {
        var currentInstance = splitInstance(source.next)

        def remove = throw new UnsupportedOperationException

        def hasNext = currentInstance.hasNext || source.hasNext

        def next = {
            if (currentInstance hasNext) {
                currentInstance.next
            } else if (source hasNext) {
                currentInstance = splitInstance(source next)
                currentInstance.next
            } else {
                null
            }
        }

        private def splitInstance(inst: Instance): Iterator[Instance] = {
            val data = inst.getData.asInstanceOf[FeatureVectorSequence]
            val target = inst.getTarget.asInstanceOf[LabelSequence]
            (data.iterator zip target.iterator) map { case (d, t) => 
                new Instance(d, t, inst.getName, inst.getSource)
            }
        }
    }

    override def newIteratorFrom(source: Iterator[Instance]): Iterator[Instance] = {
        if (source hasNext) {
            new InstancesIterator(source)
        } else {
            new Iterator[Instance] {
                def hasNext = false
                def next = null
                def remove = throw new UnsupportedOperationException
            }
        }
    }
}

package de.ovgu.findke.gossen.sozebra.features

import de.ovgu.findke.gossen.sozebra._
import Implicits._
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

abstract class LinesPipe extends Pipe {
    private val idx = getAlphabet lookupIndex(getClass, true)
    override def pipe (inst: Instance) : Instance = {
        val message = inst.getSource.asInstanceOf[Message]

        val data = inst.getData.asInstanceOf[FeatureVectorSequence]
        data.iterator zip lineValues(message) foreach { case (v, c) =>
            v.setValue( idx, c )
        }

        inst
    }

    def lineValues (message: Message): List[Double]

    protected def withLines (message: Message)(func: (String) => Double): List[Double] = {
        message.lines map { func(_) }
    }

    protected def withTokenizedLines (message: Message)(func: (TokenSequence) => Double): List[Double] = {
        message.tokenizedLines map { func(_) }
    }
}

package de.ovgu.findke.gossen.sozebra.features

import cc.mallet.pipe.{Pipe}
import cc.mallet.types.{FeatureVectorSequence, Instance, TokenSequence}
import de.ovgu.findke.gossen.sozebra.{Implicits, Message}
import Implicits._

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
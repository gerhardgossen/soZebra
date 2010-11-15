package de.ovgu.findke.gossen.sozebra

object Implicits {
    import cc.mallet.types.{FeatureVector, FeatureVectorSequence}
    class RichIter(iter: FeatureVectorSequence#Iterator) extends Iterable[FeatureVector] {
        def iterator = new Iterator[FeatureVector] {
            def next = iter.next
            def hasNext = iter.hasNext
        }
    }
    implicit def iter2RichIter(iter: FeatureVectorSequence#Iterator) = new RichIter(iter)
}

object Config {
    def modelLocation: String = "models/classification"
    def dataSplit: Array[Double] = Array(0.9, 0.1, 0.0)
}

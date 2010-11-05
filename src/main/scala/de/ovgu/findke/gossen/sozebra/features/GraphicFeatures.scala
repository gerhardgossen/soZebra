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


        }
    }

    }

    }
}

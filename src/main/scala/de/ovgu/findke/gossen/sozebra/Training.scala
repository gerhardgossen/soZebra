package de.ovgu.findke.gossen.sozebra

import cc.mallet.pipe._
import cc.mallet.types._
import cc.mallet.classify._
import cc.mallet.util.Randoms
import java.io.{FileOutputStream,ObjectOutputStream}

object ClassifierFactory {
    def getClassifierTrainer = new DecisionTreeTrainer
}

object Training extends Application {
    val instances = new InstanceList(Pipeline.getPipeline);
    val iter = new MessageIterator(true)
    instances.addThruPipe(iter)
    instances.getPipe.setTargetAlphabet(iter.labelAlphabet)

    val Array(trainSet, testSet, _) = instances.split(new Randoms, Config.dataSplit)
    val trainer = ClassifierFactory.getClassifierTrainer
    trainer.train(trainSet)

    val trial = new Trial(trainer getClassifier, testSet)
    printStatistics(trial)

    val oos = new ObjectOutputStream(new FileOutputStream(Config.modelLocation));
    oos.writeObject(trainer getClassifier);
    oos.close;

    def printStatistics(trial: Trial) {
        val alphabet = trial.getClassifier.getLabelAlphabet

        val labels = (0 until alphabet.size) map alphabet.lookupLabel

        println("\tPrecision\tRecall\t\tF1")
        labels foreach { l =>
            printf("%s\t%f\t%f\t%f\n", l, trial.getPrecision(l), trial.getRecall(l), trial.getF1(l))
        }
        println("Accuracy: " + trial.getAccuracy)
    }
}

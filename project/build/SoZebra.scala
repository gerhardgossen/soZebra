import sbt._

class EmailClassificationProject(info: ProjectInfo) extends DefaultProject(info) {
    // Mallet dependencies
    val trove = "trove" % "trove"  % "1.0.2"
    //val specs = "org.scala-tools.testing" % "specs" % "1.6.5" % "test"
    val mail = "javax.mail" % "mail" % "1.4"
}

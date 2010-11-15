import sbt._

class EmailClassificationProject(info: ProjectInfo) extends DefaultProject(info) {
    // Mallet dependencies
    val trove = "trove" % "trove"  % "1.0.2"
    //val specs = "org.scala-tools.testing" % "specs" % "1.6.5" % "test"
    val postgres = "postgresql" % "postgresql" % "8.4-702.jdbc4"
}

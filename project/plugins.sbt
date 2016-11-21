// Actually IDE specific settings belong into ~/.sbt/,
// but in order to ease the setup for the training we put the following here:

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

//lazy val root = (project in file(".")).dependsOn(picolib)
//lazy val picolib = uri("git://github.com/hmc-cs111-fall2014/picolib.git")

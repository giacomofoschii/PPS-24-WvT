ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "Wizards_vs_Trolls_PPS",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32",
    fork := true,
    Compile / mainClass := Some("it.unibo.pps.wvt.main")
  )

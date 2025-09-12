ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "Wizards_vs_Trolls_PPS",
    libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
    // add scala test
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32",
    fork := true,
    javaOptions ++= Seq(
    "--add-modules", "javafx.controls,javafx.fxml,javafx.web",
    "--add-exports", "javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED",
    "--add-exports", "javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED",
    "--add-exports", "javafx.base/com.sun.javafx.binding=ALL-UNNAMED",
    "--add-exports", "javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED",
    "--add-exports", "javafx.base/com.sun.javafx.event=ALL-UNNAMED"
    )
  )

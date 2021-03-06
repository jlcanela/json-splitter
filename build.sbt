import BuildHelper._

inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://github.com/jlcanela/json-splitter/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "jlcanela",
        "Jean-Luc CANELA",
        "na",
        url("https://github.com/jlcanela")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/jlcanela/json-splitter/"),
        "scm:git:git@github.com:jlcanela/json-splitter.git"
      )
    )
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

//sonarUseExternalConfig := true

//scapegoatVersion in ThisBuild := "1.3.9"
//scapegoatReports := Seq("xml")

coverageMinimum := 85
coverageFailOnMinimum := true

libraryDependencies ++= Dependencies.jsonSplitter ++ Dependencies.awsSdk
scalaVersion := customScalaVersion
scalacOptions := customScalacOptions

assemblyMergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties"  => MergeStrategy.first
  case "codegen-resources/customization.config" => MergeStrategy.first
  case "codegen-resources/paginators-1.json"    => MergeStrategy.first
  case "codegen-resources/service-2.json"       => MergeStrategy.first
  case "scala/annotation/nowarn$.class"         => MergeStrategy.first
  case "scala/annotation/nowarn.class"          => MergeStrategy.first
  case "module-info.class"                      => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

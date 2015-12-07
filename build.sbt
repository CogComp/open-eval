lazy val root = (project in file(".")).
  aggregate(core, learner)

lazy val commonSettings = Seq(
  organization := "edu.illinois.cs.cogcomp",
  name := "open-eval",
  scalaVersion := "2.11.7",
  version := "0.1",
  resolvers ++= Seq(
    Resolver.mavenLocal,
    "CogcompSoftware" at "http://cogcomp.cs.illinois.edu/m2repo/"
  ), 
  javaOptions += "-Xmx1G"
)

lazy val core = (project in file("core")).
  enablePlugins(PlayJava).
  settings(commonSettings: _*).
  settings(
    name := "core",
      libraryDependencies ++= Seq(
      "edu.illinois.cs.cogcomp" % "illinois-core-utilities" % "3.0.0",
      "org.webjars" %% "webjars-play" % "2.4.0-1",
      "org.webjars" % "bootstrap" % "3.1.1-2",
      javaJdbc,
      cache,
      javaWs
    ),
    routesGenerator := InjectedRoutesGenerator
  )

lazy val learner = (project in file("learner")).
  settings(commonSettings: _*).
  settings(
    name := "learner"
  )

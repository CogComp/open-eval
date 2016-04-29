val cogcompNLPVersion = "3.0.30"
lazy val root = (project in file(".")).
  aggregate(core, learner)

lazy val commonSettings = Seq(
  organization := "edu.illinois.cs.cogcomp",
  name := "open-eval",
  scalaVersion := "2.11.7",
  version := "0.1.2",
  resolvers ++= Seq(
    Resolver.mavenLocal,
    "CogcompSoftware" at "http://cogcomp.cs.illinois.edu/m2repo/"
  ),
  publishTo := Some(Resolver.sftp("CogcompSoftwareRepo", "bilbo.cs.illinois.edu", "/mounts/bilbo/disks/0/www/cogcomp/html/m2repo/"))
)

lazy val core = (project in file("core")).
  enablePlugins(PlayJava).
  settings(commonSettings: _*).
  settings(
    name := "openeval-core",
      libraryDependencies ++= Seq(
      "edu.illinois.cs.cogcomp" % "illinois-core-utilities" % cogcompNLPVersion,
      "edu.illinois.cs.cogcomp" % "illinois-corpusreaders" % cogcompNLPVersion,
      "org.webjars" %% "webjars-play" % "2.4.0-1",
      "org.webjars" % "bootstrap" % "3.1.1-2",
      "org.json" % "json" % "20140107",
      "mysql" % "mysql-connector-java" % "5.1.37",
      "com.novocode" % "junit-interface" % "0.11" % "test->default",
      javaJdbc,
      cache,
      javaWs
    ),
    routesGenerator := InjectedRoutesGenerator
  )

lazy val learner = (project in file("learner")).
  settings(commonSettings: _*).
  settings(
    name := "openeval-client",
    libraryDependencies ++= Seq(
      "edu.illinois.cs.cogcomp" % "illinois-core-utilities" % cogcompNLPVersion,
      "org.nanohttpd" % "nanohttpd" % "2.2.0",
      "org.nanohttpd" % "nanohttpd-nanolets" % "2.2.0",
      "org.mockito" % "mockito-core" % "1.10.19",
      "org.apache.httpcomponents" % "httpclient" % "4.5.1",
      "com.novocode" % "junit-interface" % "0.11" % "test"
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
  )

lazy val crossTest = (project in file("crossTest")).
  settings(commonSettings: _*).
  settings(
    name := "openeval-core-learner-test",
    libraryDependencies ++= Seq(
      "edu.illinois.cs.cogcomp" % "saul-examples_2.11" % "0.1" exclude("com.typesafe.play", "play_2.11"),
      "com.typesafe.play" % "play-test_2.11" % "2.4.3"
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")
  ).dependsOn(core, learner)
lazy val root = (project in file("."))
  .settings(
    organization := "fbi",
    name := "keyword-finder",
    version := "0.0.0",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Dependencies.all,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-feature",
  "-Xfatal-warnings",
)

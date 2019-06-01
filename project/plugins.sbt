addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.1.0-M11")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.4.9")

addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.4")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.19")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.8.3"
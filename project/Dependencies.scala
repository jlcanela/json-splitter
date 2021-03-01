import sbt._

object Dependencies {
  object Versions {
    val http4s         = "0.21.18"
    val jaeger         = "1.5.0"
    val sttp           = "2.2.9"
    val opentracing    = "0.33.0"
    val opentelemetry  = "0.7.1"
    val opencensus     = "0.28.3"
    val zipkin         = "2.16.3"
    val zio            = "1.0.4-2"
    val zioInteropCats = "2.2.0.1"
    val spark          = "3.1.0"
  }

  lazy val zio = Seq(
    "dev.zio" %% "zio-nio"        % "1.0.0-RC10",
    "dev.zio" %% "zio"            % Versions.zio,
    "dev.zio" %% "zio-macros"     % Versions.zio,
    "dev.zio" %% "zio-test"       % Versions.zio % Test,
    "dev.zio" %% "zio-test-sbt"   % Versions.zio % Test,
    "dev.zio" %% "zio-test-junit" % Versions.zio % Test
  )

  lazy val spark = Seq(
    "org.apache.spark" %% "spark-core" % Versions.spark,
    "org.apache.spark" %% "spark-sql"  % Versions.spark
  )

  lazy val datavault = zio ++ spark ++ Seq(
    "org.rogach"                      %% "scallop"                % "4.0.2",
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.8.8",
    "org.json4s"                      %% "json4s-native"          % "3.7.0-M8"
  )

  lazy val awsSdk = Seq(
    "dev.zio"      %% "zio-s3"          % "0.3.0",
    "com.amazonaws" % "aws-java-sdk-s3" % "1.11.964"
  )

  lazy val datagen = Seq(
    "com.github.azakordonets" %% "fabricator" % "2.1.5",
    "com.github.javafaker"     % "javafaker"  % "0.15",
    "org.postgresql"           % "postgresql" % "42.2.18"
  )

}

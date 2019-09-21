
import Dependencies._
import ProjectBuild._

lazy val test = project.init(file("."),Some("test"),isLib= false,commonDependencies)
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(
    //resourceDirectory := (sourceDirectory.value / "resources"),
    //mappings in Universal ++= (resourceDirectory.value * "*" get).map(x => x->("conf/"+x.getName)),
    mappings in Universal ++= (baseDirectory.value / "dist" / "conf" * "*" get).map(x => x -> ("conf/" + x.getName)),
    mappings in Universal ++= (baseDirectory.value / "dist" / "bin" * "*" get).map(x => x -> ("bin/" + x.getName)),
    //mainClass in Compile := Some("boot.ShunkeSysBoot")
    //mappings in Universal ++= ((resourceDirectory in Compile).value / "*" get).map(x => x->("conf/"+x.getName))
   /* sbt -Denv=test  universal:packageBin
      sbt -Denv=stage universal:packageBin
      sbt -Denv=prod  universal:packageBin*/
    mainClass in Compile := Some("boot.TestBoot"),
    mappings in Universal += {
      val confFile = buildEnv.value match {
        case BuildEnv.Developement => "dev.conf"
        case BuildEnv.Test => "test.conf"
        case BuildEnv.Stage => "stage.conf"
        case BuildEnv.Production => "prod.conf"
      }
      ((resourceDirectory in Compile).value / confFile) -> "conf/application.conf"
    }
  )
  //.dependsOn(libs)
  //.aggregate(libs)

//lazy val libs = project.init(file("libs"),Some("libs"),isLib=true,libsDependencies)
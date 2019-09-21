import sbt._
import Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.universal.UniversalPlugin

object ProjectBuild {

  val nexusServer = "nexus.sanyitest.com"
  val nexus = s"https://$nexusServer/nexus/content/repositories"
  val snapshots = Some("snapshots".at(nexus + "/snapshots"))
  val releases = Some("Sanyi".at(nexus + "/sanyi"))
  val auth = Credentials("Sonatype Nexus Repository Manager", nexusServer, "sanyi", "sanyi")
  val publish = Seq(publishTo := { if (isSnapshot.value) snapshots else releases }, credentials += auth)

  implicit class init(val project: Project) {
    def init(
        file: File,
        projectName: Option[String] = None,
        isLib:Boolean,
        dependencies: Seq[ModuleID] = Seq.empty,
        projectSettings: Seq[Def.Setting[
          _ >: String with Task[Seq[String]] <: java.io.Serializable]] =
          Seq.empty,
        depends: Seq[Project] = Seq.empty
    ): Project = {
      val defineName = projectName match {
        case None    => file.getName
        case Some(n) => n
      }
      val defineSettings = projectSettings++Seq(
        libraryDependencies ++= dependencies,
        sources in (Compile, doc) := Seq.empty,
        publishArtifact in (Compile, packageDoc) := false) ++ { if (isLib) publish else Seq.empty }
      project
        .in(file)
        .settings(
          name := defineName,
          defineSettings
        )
    }
  }
}

/*
 * Copyright 2016-2020 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package microsites

import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.site.jekyll.JekyllPlugin
import com.typesafe.sbt.site.SitePlugin.autoImport._
import sbt.Keys._
import sbt._
import sbt.plugins.IvyPlugin
import tut.TutPlugin
import tut.TutPlugin.autoImport._
import mdoc.MdocPlugin
import mdoc.MdocPlugin.autoImport._

import scala.util.control.NonFatal

object MicrositesPlugin extends AutoPlugin {

  object autoImport extends MicrositeAutoImportSettings

  import MicrositesPlugin.autoImport._
  import com.typesafe.sbt.site.jekyll.JekyllPlugin.autoImport._

  override def requires: Plugins =
    IvyPlugin && MdocPlugin && TutPlugin && JekyllPlugin && GhpagesPlugin

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] =
    micrositeDefaultSettings ++
      micrositeTasksSettings ++
      Seq(
        git.remoteRepo := s"git@github.com:${micrositeGithubOwner.value}/${micrositeGithubRepo.value}.git",
        sourceDirectory in Jekyll := resourceManaged.value / "main" / "jekyll",
        tutSourceDirectory := sourceDirectory.value / "main" / "tut",
        tutTargetDirectory := resourceManaged.value / "main" / "jekyll",
        mdocIn := baseDirectory.value / "docs",
        mdocOut := resourceManaged.value / "main" / "jekyll"
      )

  lazy val micrositeDefaultSettings = Seq(
    micrositeName := name.value,
    micrositeDescription := description.value,
    micrositeAuthor := organizationName.value,
    micrositeHomepage := homepage.value.map(_.toString).getOrElse(""),
    micrositeOrganizationHomepage := {
      if (organizationHomepage.value.map(_.toString).isEmpty)
        homepage.value.map(_.toString).getOrElse("")
      else
        organizationHomepage.value.map(_.toString).getOrElse("")
    },
    micrositeUrl := "",
    micrositeBaseUrl := "",
    micrositeDocumentationUrl := "",
    micrositeDocumentationLabelDescription := "Documentation",
    micrositeTwitter := "",
    micrositeTwitterCreator := "",
    micrositeShareOnSocial := true,
    micrositeHighlightTheme := "vs",
    micrositeHighlightLanguages := Seq("scala", "java", "bash"),
    micrositeConfigYaml := ConfigYml(
      yamlPath = Some((resourceDirectory in Compile).value / "microsite" / "_config.yml")
    ),
    micrositeImgDirectory := (resourceDirectory in Compile).value / "microsite" / "img",
    micrositeCssDirectory := (resourceDirectory in Compile).value / "microsite" / "css",
    micrositeSassDirectory := (resourceDirectory in Compile).value / "microsite" / "sass",
    micrositeJsDirectory := (resourceDirectory in Compile).value / "microsite" / "js",
    micrositeCDNDirectives := CdnDirectives(),
    micrositeExternalLayoutsDirectory := (resourceDirectory in Compile).value / "microsite" / "layouts",
    micrositeExternalIncludesDirectory := (resourceDirectory in Compile).value / "microsite" / "includes",
    micrositeDataDirectory := (resourceDirectory in Compile).value / "microsite" / "data",
    micrositeStaticDirectory := (resourceDirectory in Compile).value / "microsite" / "static",
    micrositeExtraMdFiles := Map.empty,
    micrositeExtraMdFilesOutput := (resourceManaged in Compile).value / "jekyll" / "extra_md",
    micrositePluginsDirectory := (resourceDirectory in Compile).value / "microsite" / "plugins",
    micrositeTheme := "light",
    micrositePalette := {

      val theme = (Compile / micrositeTheme).value

      if (theme == "pattern")
        Map(
          "brand-primary"   -> "#02B4E5",
          "brand-secondary" -> "#1C2C52",
          "brand-tertiary"  -> "#162341",
          "gray-dark"       -> "#453E46",
          "gray"            -> "#837F84",
          "gray-light"      -> "#E3E2E3",
          "gray-lighter"    -> "#F4F3F4",
          "white-color"     -> "#FFFFFF"
        )
      else
        Map(
          "brand-primary"   -> "#013567",
          "brand-secondary" -> "#009ADA",
          "white-color"     -> "#FFFFFF"
        )
    },
    micrositeFavicons := Seq(),
    micrositeVersionList := Seq(),
    micrositeGithubOwner := gitRemoteInfo._1,
    micrositeGithubRepo := gitRemoteInfo._2,
    micrositeGithubToken := None,
    micrositeGitHostingService := GitHub,
    micrositeGitHostingUrl := "",
    micrositePushSiteWith := GHPagesPlugin,
    micrositeAnalyticsToken := "",
    micrositeGitterChannel := true,
    micrositeGitterChannelUrl := s"${micrositeGithubOwner.value}/${micrositeGithubRepo.value}",
    micrositeFooterText := Some(layouts.Layout.footer.toString),
    micrositeEditButton := None,
    micrositeGithubLinks := true,
    micrositeCompilingDocsTool := WithMdoc,
    includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.jpeg" | "*.gif" | "*.js" | "*.swf" | "*.md" | "*.webm" | "*.ico" | "CNAME" | "*.yml" | "*.svg" | "*.json",
    includeFilter in Jekyll := (includeFilter in makeSite).value || "LICENSE",
    commands ++= Seq(publishMicrositeCommand),
    javaOptions += "-Djava.awt.headless=true"
  )

  /** Gets the Github user and repository from the git remote info */
  private val gitRemoteInfo = {
    import scala.sys.process._

    val identifier = """([^\/]+)"""

    val GitHubHttps   = s"https://github.com/$identifier/$identifier".r
    val SSHConnection = s"git@github.com:$identifier/$identifier.git".r

    try {
      val remote = List("git", "ls-remote", "--get-url", "origin").!!.trim()

      remote match {
        case GitHubHttps(user, repo)   => (user, repo)
        case SSHConnection(user, repo) => (user, repo)
        case _                         => ("", "")
      }
    } catch {
      case NonFatal(_) => ("", "")
    }
  }

}

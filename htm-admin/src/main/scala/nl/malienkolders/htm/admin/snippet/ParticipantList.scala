package nl.malienkolders.htm.admin
package snippet

import nl.malienkolders.htm.lib.model._
import net.liftweb._
import common._
import util._
import Helpers._
import http._
import mapper._
import js._
import JsCmds._
import nl.malienkolders.htm.admin.lib.exporter._
import nl.malienkolders.htm.admin.lib.Utils.PimpedParticipant
import scala.xml.Text

object ParticipantList {

  def render = {
    val orderField: MappedField[_, Participant] = Participant.name
    val ps = Participant.findAll(OrderBy(orderField, Ascending))
    val cs = Country.findAll.map(c => c -> c.name.is)
    var selectedParticipant: Box[Participant] = Empty

    def changeCountry(p: Participant, c: Country) = {
      p.country(c).save
      var cmd = "$('#flag" + p.id.is + "').attr('title', '" + c.name.is + "'); $('#flag" + p.id.is + "').attr('src', '/images/flags/" + (if (c.hasFlag.get) c.code2.get.toLowerCase() else "unknown") + ".png'); $('#flag" + p.id.is + "');"
      if (c.hasViewerFlag.is)
        cmd += "$('#flag" + p.id.is + "').addClass('viewerFlagAvailable');"
      else
        cmd += "$('#flag" + p.id.is + "').removeClass('viewerFlagAvailable');"
      Run(cmd)
    }

    def registerAll(register: Boolean) = {
      ps foreach { p =>
        p.isPresent(register)
        p.subscriptions foreach (_.gearChecked(register))
        p.save
      }
      S.redirectTo("/participants/list")
    }

    def createParticipant() = {
      Participant.create.externalId("new").country(Country.findAll().find(_.code2.is == "NL").get).save

      S.redirectTo("/participants/register/new")
    }

    def registerAllSubmit = SHtml.submit("register all", () => registerAll(true), "class" -> "btn btn-default")

    def unregisterAllSubmit = SHtml.submit("unregister all", () => registerAll(false), "class" -> "btn btn-default")

    def createParticipantSubmit = SHtml.submit("create participant", createParticipant, "class" -> "btn btn-default")

    ".downloadButton *" #> Seq(
      SHtml.link("/download/participants", () => throw new ResponseShortcutException(downloadParticipantList), Text("Participants")),
      SHtml.link("/download/details", () => throw new ResponseShortcutException(downloadDetailsList), Text("Details")),
      SHtml.link("/download/clubs", () => throw new ResponseShortcutException(downloadClubsList), Text("Clubs"))) &
      "#countrySelect *" #> SHtml.ajaxSelectObj(cs, Empty, { c: Country =>
        val cmd = selectedParticipant.map(p => changeCountry(p, c)) openOr (Noop)
        selectedParticipant = Empty
        cmd & Run("$('#countrySelect').hide();")
      }, "id" -> "countrySelectDropdown") &
      ".participant" #> (ps.map { p =>
        val c = p.country.obj.getOrElse(Country.findAll.head)
        ".participant [onclick]" #> SHtml.ajaxInvoke(() => JsCmds.RedirectTo("/participants/register/" + p.externalId.is)) &
          ".participant [class]" #> (if (p.isPresent.is) "success" else "default") &
          ".photo [class+]" #> (if (p.subscriptions.size > 0) (if (p.hasAvatar) "glyphicon-check" else "glyphicon-unchecked") else "") &
          ".id *" #> p.externalId.is &
          ".name *" #> p.name.is &
          ".shortName *" #> p.shortName.is &
          ".club *" #> p.club.is &
          ".clubCode *" #> p.clubCode.is &
          ".tournament" #> p.subscriptions.sortBy(_.primary.get).reverse.map { sub =>
            val tournament = sub.tournament.foreign.get
            <span class={ "label " + tournament.identifier.get } title={ tournament.name.get }>{ tournament.mnemonic.get + " " + sub.fighterNumber.get }</span>
          } &
          ".flag" #> (
            "img [src]" #> (if (c.hasFlag.is) "/images/flags/" + c.code2.get.toLowerCase() + ".png" else "/images/flags/unknown.png") &
            "img [class]" #> (if (c.hasViewerFlag.is) "viewerFlagAvailable" else "") &
            "img [id]" #> ("flag" + p.id.is) &
            "img [title]" #> ("%s (click to change)" format c.name.is) &
            "img [onclick]" #> SHtml.ajaxInvoke { () =>
              selectedParticipant = Full(p)
              Run("document.getElementById('countrySelectDropdown').selectedIndex = 0;$('#countrySelect').show();$('#countrySelect').offset($('#flag" + p.id.is + "').offset());") &
                Focus("countrySelectDropdown")

            }) &
            ".action" #> <a href={ "/participants/register/" + p.externalId.is } style="margin-right: 10px">register</a>
      }) &
      ".totals" #> (
        ".people *" #> ps.size &
        ".countries *" #> ps.groupBy(_.country.is).size &
        ".clubs *" #> ps.groupBy(_.clubCode.is).size &
        ".actions *" #> Seq(createParticipantSubmit, registerAllSubmit, unregisterAllSubmit))
  }

  def downloadParticipantList() = {
    OutputStreamResponse(ParticipantsExporter.doExport _, List("content-disposition" -> "inline; filename=\"participants.xls\""))
  }

  def downloadClubsList() = {
    OutputStreamResponse(ClubsExporter.doExport _, List("content-disposition" -> "inline; filename=\"clubs.xls\""))
  }

  def downloadDetailsList() = {
    OutputStreamResponse(DetailsExporter.doExport _, List("content-disposition" -> "inline; filename=\"details.xls\""))
  }

}
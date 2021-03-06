package nl.malienkolders.htm.admin.snippet
import net.liftweb._
import http._
import util.Helpers._
import scala.xml.NodeSeq
import nl.malienkolders.htm.lib.model._
import net.liftweb.common.Full
import net.liftweb.http.js.JsCmd
import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Loggable
import nl.malienkolders.htm.admin.lib.PhotoImporterBackend
import java.io.FileOutputStream

object PhotoImporter extends Loggable {

  def render = {
    var upload: Box[FileParamHolder] = Empty

    def processForm() = upload match {
      case Full(FileParamHolder(_, mime, fileName, file)) if mime.startsWith("application/") =>
        val participants = Participant.findAll.sortBy(_.externalId.get.toInt).toList

        PhotoImporterBackend.doImport(file, participants.toIterator)
        S.notice("Imported " + fileName)

      case Full(FileParamHolder(_, mime, _, _)) => S.notice("Invalid mime-type: " + mime)
      case _ => S.notice("No file?")
    }

    "#file" #> SHtml.fileUpload(f => upload = Full(f)) &
      "type=submit" #> SHtml.onSubmitUnit(processForm)
  }

}
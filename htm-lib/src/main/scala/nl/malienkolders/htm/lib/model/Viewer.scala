package nl.malienkolders.htm.lib
package model

import net.liftweb._
import mapper._
import common._
import util._
import Helpers._
import scala.xml._
import dispatch._
import Http._
import net.liftweb.json._
import scala.concurrent._
import ExecutionContext.Implicits.global

case class ViewerMessage(message: String, duration: Long)

case class Screen(id: String, width: Int, height: Int, fullscreenSupported: Boolean)

class Viewer extends LongKeyedMapper[Viewer] with IdPK with CreatedUpdated with ManyToMany {
  def getSingleton = Viewer

  object alias extends MappedPoliteString(this, 32)
  object url extends MappedString(this, 255)
  object screen extends MappedInt(this)
  object arenas extends MappedManyToMany(ArenaViewers, ArenaViewers.viewer, ArenaViewers.arena, Arena)

  object rest {
    var state = "empty"

    def baseRequest = :/(url.get) / "api"
    implicit val formats = Serialization.formats(NoTypeHints)

    def ping = {
      val req = baseRequest / "ping"
      Http(req OK as.String).fold(
        _ => false,
        success => true).apply
    }

    private def update(arena: Arena, screen: String, data: String): Boolean = {
      state = screen
      update(arena, data)
    }

    private def update(arena: Arena, data: String): Boolean = {
      val req = dispatch.url("http://" + url.get + "/api/update/text/" + (arena.id.get) + "/" + state).POST.setBody(data).addHeader("Content-Type", "text/plain")
      Http(req).fold[Boolean](
        _ => false,
        resp => resp.getResponseBody().toBoolean).apply
    }

    private def fightUpdate(arena: Arena, data: String): Boolean = {
      update(arena, "fight", data)
    }

    def fightUpdate(arena: Arena, f: Fight): Boolean = fightUpdate(arena, Serialization.write(f.toMarshalled))

    def message(arena: Arena, message: String): Boolean = update(arena, Serialization.write(
      Map("message" -> message)))

    object timer {
      def start(arena: Arena, time: Long) = fightUpdate(arena, Serialization.write(
        Map(
          "timer" -> Map(
            "action" -> "start",
            "time" -> time.toString))))

      def stop(arena: Arena, time: Long) = fightUpdate(arena, Serialization.write(
        Map(
          "timer" -> Map(
            "action" -> "stop",
            "time" -> time.toString))))
    }
  }
}

object Viewer extends Viewer with LongKeyedMetaMapper[Viewer] with CRUDify[Long, Viewer] {
  override def dbTableName = "viewers"
}
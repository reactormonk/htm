package nl.malienkolders.htm.admin

import net.liftweb._
import http._
import rest._
import json._
import util.Helpers._
import nl.malienkolders.htm.lib.model._
import nl.malienkolders.htm.admin.comet._
import net.liftweb.json.JValue

object AdminRest extends RestHelper {

  override implicit val formats = Serialization.formats(NoTypeHints)

  serve {
    case "api" :: "tournaments" :: Nil JsonGet _ =>
      Extraction.decompose(Tournament.findAll.map(_.toMarshalled))

    case "api" :: "tournament" :: AsLong(tournamentId) :: Nil JsonGet _ =>
      Tournament.findByKey(tournamentId).map(t => Extraction.decompose(t.toMarshalled)).getOrElse[JValue](JBool(false))

    case "api" :: "tournament" :: AsLong(tournamentId) :: "rounds" :: Nil JsonGet _ =>
      Extraction.decompose(Tournament.findByKey(tournamentId).get.rounds.map(_.toMarshalled))

    case "api" :: "round" :: AsLong(roundId) :: Nil JsonGet _ =>
      Extraction.decompose(Round.findByKey(roundId).map(_.toMarshalled).getOrElse(false))

    case "api" :: "round" :: AsLong(roundId) :: "pools" :: Nil JsonGet _ =>
      Extraction.decompose(Round.findByKey(roundId).map(_.pools.map(_.toMarshalled)).getOrElse(false))

    case "api" :: "pool" :: AsLong(poolId) :: Nil JsonGet _ =>
      Extraction.decompose(Pool.findByKey(poolId).map(_.toMarshalled).getOrElse(false))

    case "api" :: "pool" :: AsLong(poolId) :: "viewer" :: Nil JsonGet _ =>
      val p = Pool.findByKey(poolId)
      var j = p.map(p => Extraction.decompose(p.toViewer)).getOrElse[JValue](JBool(false))
      JsonResponse(
        j,
        ("Content-Type", "application/json; charset=utf-8") :: Nil,
        Nil,
        200)

    case "api" :: "pool" :: AsLong(poolId) :: "fight" :: "pop" :: Nil JsonGet _ =>
      (FightServer !! PopFight(Pool.findByKey(poolId).get)).map {
        case FightMsg(f) => Extraction.decompose(f.toMarshalled)
        case _ => JBool(false)
      }.getOrElse[JValue](JBool(false))

    case "api" :: "pool" :: AsLong(poolId) :: "fight" :: "peek" :: Nil JsonGet _ =>
      (FightServer !! PeekFight(Pool.findByKey(poolId).get)).map {
        case FightMsg(f) => Extraction.decompose(f.toMarshalled)
        case _ => JBool(false)
      }.getOrElse[JValue](JBool(false))

    case "api" :: "pool" :: AsLong(poolId) :: "ranking" :: Nil JsonGet _ =>
      val res = Pool.findByKey(poolId).get.toMarshalledRanking
      Extraction.decompose(res)

    case "api" :: "fight" :: "confirm" :: Nil JsonPost json -> _ =>
      val m = Extraction.extract[MarshalledFight](json)
      JBool((FightServer !! FightResult(Fight.findByKey(m.id).get.fromMarshalled(m), true)).map {
        case FightMsg(_) => true
        case _ => false
      }.getOrElse[Boolean](false))

    case "api" :: "fight" :: "cancel" :: Nil JsonPost json -> _ =>
      val m = Extraction.extract[MarshalledFight](json)
      JBool((FightServer !! FightResult(Fight.findByKey(m.id).get.fromMarshalled(m), false)).map {
        case FightMsg(_) => true
        case _ => false
      }.getOrElse[Boolean](false))

    case "api" :: "fight" :: AsLong(id) :: Nil JsonGet _ =>
      Fight.findByKey(id).map(f => Extraction.decompose(f.toMarshalled)).getOrElse[JValue](JBool(false))

    case "api" :: "ping" :: Nil JsonGet _ =>
      JString("pong")
  }

}
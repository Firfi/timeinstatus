package ru.megaplan.jira.plugin.util

import com.atlassian.jira.config.properties.ApplicationProperties
import com.atlassian.jira.ComponentManager
import java.lang.Float
import scala.Float
import com.atlassian.core.util.DateUtils

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 01.10.12
 * Time: 19:27
 * To change this template use File | Settings | File Templates.
 */
object textUtil {
  lazy val ap: ApplicationProperties = ComponentManager.getComponentInstanceOfType(classOf[ApplicationProperties])
  lazy val secondsPerDay: Long = 86400 //((ap.getDefaultBackedString("jira.timetracking.hours.per.day")).toFloat * 3600).toLong
  lazy val secondsPerWeek: Long = 604800 //((ap.getDefaultBackedString("jira.timetracking.days.per.week")).toFloat * secondsPerDay).toLong

  def getPrettyDuration(seconds: Number) = DateUtils.getDurationStringSeconds(seconds.longValue(), secondsPerDay, secondsPerWeek)

}

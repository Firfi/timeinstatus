package ru.megaplan.jira.plugin.util

import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 9/9/12
 * Time: 9:49 PM
 * To change this template use File | Settings | File Templates.
 */
trait LogHelper {
  private val loggerName = this.getClass.getName
  lazy val log = Logger.getLogger(loggerName)
}

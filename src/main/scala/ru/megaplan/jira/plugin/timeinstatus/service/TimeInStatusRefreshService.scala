package ru.megaplan.jira.plugin.timeinstatus.service

import job.TimeInStatusRefreshJob
import org.springframework.beans.factory.{DisposableBean, InitializingBean}
import com.atlassian.sal.api.scheduling.PluginScheduler
import ru.megaplan.jira.plugin.timeinstatus.manager.TimeInStatusManager
import scala.collection.JavaConversions.collectionAsScalaIterable
import scala.collection.JavaConversions.mapAsJavaMap
import java.util.{Calendar, Date}
import com.atlassian.jira.config.StatusManager
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.jql.builder.JqlQueryBuilder
import com.atlassian.jira.user.util.UserManager
import com.atlassian.crowd.embedded.api.User
import ru.megaplan.jira.plugin.util.{textUtil, LogHelper}
import com.atlassian.jira.extension.Startable
import com.atlassian.sal.api.lifecycle.LifecycleAware

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 11.09.12
 * Time: 11:49
 * To change this template use File | Settings | File Templates.
 */
class TimeInStatusRefreshService
  (pluginScheduler: PluginScheduler,
   timeInStatusManager: TimeInStatusManager,
   statusManager: StatusManager,
   searchService: SearchService,
   userManager: UserManager) extends LifecycleAware with LogHelper {

  val JOB_NAME: String = classOf[TimeInStatusRefreshService].getName + ":job"
  val interval: Long = 1000 * 60 * 60 * 24
  val project = "MPS"
  val statusesNot = Set("6", "10061") //jira statuses ids is string everywhere
  val initiator = Set("megaplan","admin")

  def onStart() {
    var initiatorUser: User = null
    initiator.find(login => userManager.getUserObject(login) match {
      case null => false
      case user: User => {
        initiatorUser = user
        true
      }
    })
    if (initiatorUser == null) {
      log.error("can't find neither user : " + initiator.mkString)
      return
    }
    val statuses = statusManager.getStatuses.filter(s => !statusesNot.contains(s.getId)).map(s => s.getId)
    val query = JqlQueryBuilder.newBuilder().where().project(project).and().status(statuses.toSeq:_*).buildQuery()

    val params = Map(
      "timeInStatusManager" -> timeInStatusManager,
      "searchService" -> searchService,
      "query" -> query,
      "initiatorUser" -> initiatorUser)

    pluginScheduler.scheduleJob(
    JOB_NAME,
    classOf[TimeInStatusRefreshJob],
    params,
    {
      val now = new Date()
      val r = Calendar.getInstance()
      import Calendar._
      r.setTime(now);r.set(HOUR_OF_DAY, 0);r.set(MINUTE, 0);r.set(DAY_OF_MONTH, r.get(DAY_OF_MONTH)+1)
      r.getTime
    },
    interval
    )
  }

  def start() {

  }

}

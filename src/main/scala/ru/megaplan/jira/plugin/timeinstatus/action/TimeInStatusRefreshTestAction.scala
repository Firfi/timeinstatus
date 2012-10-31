package ru.megaplan.jira.plugin.timeinstatus.action

import com.atlassian.jira.web.action.JiraWebActionSupport
import ru.megaplan.jira.plugin.timeinstatus.manager.TimeInStatusManager
import com.atlassian.jira.jql.builder.JqlQueryBuilder
import com.atlassian.jira.config.StatusManager
import scala.collection.JavaConversions.mapAsJavaMap
import scala.collection.JavaConversions.collectionAsScalaIterable
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.web.bean.PagerFilter

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 01.10.12
 * Time: 17:28
 * To change this template use File | Settings | File Templates.
 */
class TimeInStatusRefreshTestAction(timeInStatusManager : TimeInStatusManager, statusManager: StatusManager, searchService: SearchService) extends JiraWebActionSupport {

  val project = "MPS"
  val statusesNot = Set("6", "10061")

  @scala.reflect.BeanProperty
  var res = "ok"

  override def doExecute = {
    val statuses = statusManager.getStatuses.filter(s => !statusesNot.contains(s.getId)).map(s => s.getId)
    val query = JqlQueryBuilder.newBuilder().where().project(project).and().status(statuses.toSeq:_*).buildQuery()
    timeInStatusManager.updateIssues(searchService.search(getLoggedInUser, query, PagerFilter.getUnlimitedFilter).getIssues.toSet)
    super.doExecute()
  }
}

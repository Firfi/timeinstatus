package ru.megaplan.jira.plugin.timeinstatus.service.job

import com.atlassian.sal.api.scheduling.PluginJob
import java.util
import ru.megaplan.jira.plugin.timeinstatus.manager.TimeInStatusManager
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.query.Query
import com.atlassian.crowd.embedded.api.User
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.jira.issue.search.SearchResults
import scala.collection.JavaConversions._
import ru.megaplan.jira.plugin.util.LogHelper

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 11.09.12
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
class TimeInStatusRefreshJob extends PluginJob with LogHelper {
  def execute(params: util.Map[String, AnyRef]) {
    log.warn("executing refresh timeinstatus job")
    var timeInStatusManager: TimeInStatusManager = null
    params.get("timeInStatusManager") match {
      case t: TimeInStatusManager => timeInStatusManager = t
    }
    val searchService = params.get("searchService").asInstanceOf[SearchService]
    val query = params.get("query").asInstanceOf[Query]
    val initiatorUser = params.get("initiatorUser").asInstanceOf[User]
    val searchResults: SearchResults = searchService.search(initiatorUser, query, PagerFilter.getUnlimitedFilter)
    timeInStatusManager.updateIssues(searchResults.getIssues.toSet)
  }
}

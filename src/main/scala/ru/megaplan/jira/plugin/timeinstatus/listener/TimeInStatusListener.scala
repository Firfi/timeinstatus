package ru.megaplan.jira.plugin.timeinstatus.listener

import org.springframework.beans.factory.{DisposableBean, InitializingBean}
import com.atlassian.event.api.{EventListener, EventPublisher}
import ru.megaplan.jira.plugin.timeinstatus.manager.TimeInStatusManager
import com.atlassian.jira.issue.{ModifiedValue, CustomFieldManager}
import com.atlassian.jira.event.issue.IssueEvent
import ru.megaplan.jira.plugin.util.LogHelper
import ru.megaplan.jira.plugins.history.search.HistorySearchManager
import org.ofbiz.core.entity.GenericValue
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
import scala.collection.JavaConversions._
import com.atlassian.jira.workflow.WorkflowManager
import com.atlassian.jira.issue.history.ChangeItemBean


/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 10.09.12
 * Time: 13:57
 * To change this template use File | Settings | File Templates.
 */
class TimeInStatusListener
  (
    timeInStatusManager: TimeInStatusManager,
    historySearchManager: HistorySearchManager,
    changeHistoryManager: ChangeHistoryManager,
    eventPublisher: EventPublisher,
    customFieldManager: CustomFieldManager,
    workflowManager: WorkflowManager
  )
  extends InitializingBean with DisposableBean with LogHelper {

  private val changeLogSearchFunction: com.atlassian.plugin.util.collect.Function[HistorySearchManager.ChangeLogRequest, String] =
    historySearchManager.getFindInChangeLogFunction

  @EventListener
  def changeStatusEvent(event: IssueEvent) {
    if (event.getChangeLog == null) return
    val statusFrom = getStatus(event.getChangeLog, from = true)
    if (statusFrom == null) return
    val issue = event.getIssue
    timeInStatusManager.updateIssues(Set(issue), statusFrom)
  }

  private def getStatus(changeLog: GenericValue, from: Boolean) = {
    val changeLogRequest: HistorySearchManager.ChangeLogRequest = new HistorySearchManager.ChangeLogRequest(changeLog, TimeInStatusManager.STATUS , from, true)
    changeLogRequest.setLog(log)
    changeLogSearchFunction.get(changeLogRequest)
  }

  def afterPropertiesSet() {eventPublisher.register(this)}
  def destroy() {eventPublisher.unregister(this)}

}



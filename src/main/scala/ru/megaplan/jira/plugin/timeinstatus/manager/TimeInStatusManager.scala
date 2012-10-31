package ru.megaplan.jira.plugin.timeinstatus.manager

import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.propertyset.JiraPropertySetFactory
import ru.megaplan.jira.plugin.util.LogHelper
import javax.annotation.Nullable
import com.atlassian.jira.issue.{ModifiedValue, CustomFieldManager, Issue}
import collection.JavaConversions._
import scala.collection.immutable.Map
import com.atlassian.jira.issue.history.ChangeItemBean
import ru.megaplan.jira.plugin.timeinstatus.customfield.TimeInStatusCFType
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
import com.atlassian.jira.issue.index.{IndexException, IssueIndexManager}
import com.atlassian.jira.util.ImportUtils
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.sal.api.scheduling.PluginScheduler
import org.springframework.beans.factory.{DisposableBean, InitializingBean}
import collection.mutable.ListBuffer
import java.util.Date
import collection.mutable
import com.atlassian.jira.config.StatusManager
import com.atlassian.jira.workflow.WorkflowManager

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 9/9/12
 * Time: 7:25 PM
 * To change this template use File | Settings | File Templates.
 */

trait TimeInStatusManager  {

  def setStatusId(customField: CustomField, id: Long)
  def getStatusId(customField: CustomField) : Long
  def getCustomFieldsByIssueAndType(cfType: java.lang.Class[_], @Nullable issue: Issue) : Map[Long, CustomField]
  def updateIssues(issues: Set[Issue])
  def updateIssues(issues: Set[Issue], statusFrom: String)
}

object TimeInStatusManager {
  val STATUS = "Status"
}

class TimeInStatusManagerImpl
  (jiraPropertySerFactory: JiraPropertySetFactory,
   customFieldManager: CustomFieldManager,
   changeHistoryManager: ChangeHistoryManager,
   issueIndexManager: IssueIndexManager,
   pluginScheduler: PluginScheduler,
   statusManager: StatusManager,
   workflowManager: WorkflowManager)
  extends TimeInStatusManager with LogHelper {

  val ENTITY = "timeinstatus"
  val STATUS_ID = "statusId"

  def setStatusId(customField: CustomField, id: Long) {
    buildPropertySet.setLong(getKey(customField, STATUS_ID), id)
  }

  def getStatusId(customField: CustomField) = buildPropertySet.getLong(getKey(customField, STATUS_ID))

  private def buildPropertySet = jiraPropertySerFactory.buildNoncachingPropertySet(ENTITY)

  private def getKey(customField: CustomField, key: Object) = {
    String.format("%s_%s", key, customField.getId)
  }

  def getCustomFieldsByIssueAndType(cfType: java.lang.Class[_], @Nullable issue: Issue) = {
    getCustomFieldObjects(issue).toSet.filter(cf => cfType.isAssignableFrom(cf.getCustomFieldType.getClass)).
      map(cf => (getStatusId(cf), cf)).toMap
  }

  private[this] def getCustomFieldObjects(@Nullable issue: Issue) = {
    if ((null == issue)) customFieldManager.getCustomFieldObjects else customFieldManager.getCustomFieldObjects(issue)
  }

  def updateIssues(issues: Set[Issue]) {
    issues.foreach(i => {
      val times = getTimesInStatus(i)
      updateTimes(i, times)
    })
    indexIssues(issues)
  }

  def updateIssues(issues: Set[Issue], statusFrom: String) {
    issues.foreach(i => {
      val times = getTimesInStatus(i)
      updateTimes(i, times.filter(t => (t._1 == statusFrom.toLong)))
    })
  }

  private def updateTimes(issue: Issue, times: Map[Long, Double]) {
    val fields = getStatusCfMap(issue)
    fields.filter(e => times.contains(e._1)).foreach(e => {
      val time = times.get(e._1).getOrElse(0d) / 1000 // seconds
      updateCustomField(e._2, issue, time)
    })
  }

  private def updateCustomField(cf: CustomField, i: Issue, v: Double) {
    cf.updateValue(null, i, new ModifiedValue(Option(i.getCustomFieldValue(cf)).getOrElse(0d).toString.toDouble, v), new DefaultIssueChangeHolder)
  }

  def getTimesInStatus(issue: Issue) = {

    val facts = {

      val maybeWithFirst = changeHistoryManager.getChangeItemsForField(issue,"status").foldLeft(List.empty[(Long, Long)])((list, ci) => {
        list match {
          case Nil => {
            val statusId: Long = ci.getFrom.toLong
            (ci.getTo.toLong, ci.getCreated.getTime) :: (statusId, issue.getCreated.getTime) :: list
          }
          case _ => {
            (ci.getTo.toLong, ci.getCreated.getTime) :: list
          }
        }
      })

      val withFirst = if (maybeWithFirst.isEmpty) {
        List((issue.getStatusObject.getId.toLong, issue.getCreated.getTime))
      } else {
        maybeWithFirst
      }

      val lastFictiveFact = (withFirst.head._1 -> new Date().getTime)

      lastFictiveFact :: withFirst

    }

    val factIntervals = facts.zip(facts.tail)

    val result = factIntervals.
      map(twoTuples => (twoTuples._2._1, twoTuples._1._2, twoTuples._2._2)).
      foldRight(Map.empty[Long, Double])((tuple, map) => {
        val id = tuple._1
        val addTime = tuple._2 - tuple._3
        val newTime = map.get(id).getOrElse(0d) + addTime
        map.updated(id, newTime)
      })

    result
  }


  private def getStatusCfMap(i: Issue) = {
    getCustomFieldsByIssueAndType(classOf[TimeInStatusCFType], i)
  }

  private def indexIssues(issues: Set[Issue]) {
    val oldValue: Boolean = ImportUtils.isIndexIssues
    ImportUtils.setIndexIssues(true)
    try {
      issueIndexManager.reIndexIssueObjects(issues)
    }
    catch {
      case e: IndexException => {
        log.error("Unable to index issues: " + issues.mkString, e)
      }
    }
    ImportUtils.setIndexIssues(oldValue)
  }


}

package ru.megaplan.jira.plugin.timeinstatus.customfield

import com.atlassian.jira.issue.customfields.impl.{AbstractSingleFieldType, CalculatedCFType}
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager
import com.atlassian.jira.issue.customfields.persistence.{PersistenceFieldType, CustomFieldValuePersister}
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.Issue
import java.lang

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 9/9/12
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem
import com.atlassian.jira.util.velocity.NumberTool
import collection.JavaConversions._
import com.atlassian.jira.issue.fields.config.{FieldConfig, FieldConfigItemType}
import ru.megaplan.jira.plugin.util.{textUtil, LogHelper}

class TimeInStatusCFType
  (customFieldValuePersister: CustomFieldValuePersister, genericConfigManager: GenericConfigManager)
    extends AbstractSingleFieldType[lang.Double] (customFieldValuePersister, genericConfigManager)
  with LogHelper {

  def getDatabaseType = PersistenceFieldType.TYPE_DECIMAL

  def getDbValueFromObject(obj: lang.Double) = obj

  def getObjectFromDbValue(value: Any) = {
    value match {
      case doubleVal: lang.Double => doubleVal
      case null => null
      case _ => throw new ClassCastException
    }
  }

  def getStringFromSingularObject(obj: lang.Double) = {
    if (obj eq null) null else obj.toString
  }

  override def getVelocityParameters(issue: Issue, customField: CustomField, fieldLayoutItem: FieldLayoutItem) = {
    Map("textUtil" -> textUtil)
  }

  override def getConfigurationItemTypes = {
    new TimeInStatusConfiguration :: super.getConfigurationItemTypes.toList
  }

  def getSingularObjectFromString(str: String) = {
    if (str eq null) null else lang.Double.parseDouble(str)
  }

}
class TimeInStatusConfiguration extends FieldConfigItemType {

  val KEY = "timeinstatusconfiguration"

  def getDisplayName = "Status ID"

  def getDisplayNameKey = "ru.megaplan.jira.plugin.timeinstatus.timeinstatusconfig"

  def getViewHtml(fieldConfig: FieldConfig, fieldLayoutItem: FieldLayoutItem) = {
    "<p>to be implemented</p>"
  }

  def getObjectKey = KEY

  def getBaseEditUrl = "ConfigureTimeInStatusFieldAction!default.jspa"

  def getConfigurationObject(p1: Issue, p2: FieldConfig) = null

}

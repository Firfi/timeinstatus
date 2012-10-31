package ru.megaplan.jira.plugin.timeinstatus.action

import com.atlassian.jira.web.action.admin.customfields.AbstractEditConfigurationItemAction
import ru.megaplan.jira.plugin.timeinstatus.manager.TimeInStatusManager
import org.apache.log4j.Logger
import ru.megaplan.jira.plugin.util.LogHelper

/**
 * Created with IntelliJ IDEA.
 * User: Firfi
 * Date: 9/9/12
 * Time: 7:11 PM
 * To change this template use File | Settings | File Templates.
 */
class ConfigureTimeInStatusFieldAction(timeInStatusManager: TimeInStatusManager) extends AbstractEditConfigurationItemAction {

  @scala.reflect.BeanProperty
  var statusId: java.lang.Long = 0

  override def doDefault = {
    statusId = timeInStatusManager.getStatusId(getCustomField)
    super.doDefault()
  }
  override def doExecute = {
    timeInStatusManager.setStatusId(getCustomField, statusId)
    super.doExecute()
  }
}

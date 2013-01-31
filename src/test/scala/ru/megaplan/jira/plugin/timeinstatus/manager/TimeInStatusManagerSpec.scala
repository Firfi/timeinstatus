package ru.megaplan.jira.plugin.timeinstatus.manager

import org.specs2.Specification
import org.specs2.mock._

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 07.11.12
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */
class TimeInStatusManagerSpec extends Specification { override def is =

  "TimeInStatusManager controls configuration and main business logic of issue cf's updating" ^
  "dsa"                                                                                       ! success^
                                                                                              end
   case class manager() extends Mockito {
      val m = mock[TimeInStatus]
   }

}

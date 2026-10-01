package org.basex.modules;

import org.basex.http.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the Sessions Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionsModuleTest extends HTTPTest {
  /**
   * Start server.
   * @throws Exception exception
   */
  @BeforeAll public static void start() throws Exception {
    init(REST_ROOT, true);
  }

  /**
   * Function test.
   * @throws Exception exception
   */
  @Test public void listDetails() throws Exception {
    get("1 true true true", "", "query", "let $id := session:id() "
        + "let $session := sessions:list-details($id) "
        + "return string-join((count($session), $session/@id = $id, "
        + "xs:dateTime($session/@created) <= xs:dateTime($session/@accessed), "
        + "$id = sessions:list-details()/@id), ' ')");
    get("0", "", "query", "count(sessions:list-details('unknown'))");
  }

  /**
   * Checks if jobs report the client that started them.
   * @throws Exception exception
   */
  @Test public void jobOrigin() throws Exception {
    get("true true", "", "query", "let $id := session:id() "
        + "let $job := job:eval('prof:sleep(1000)') "
        + "let $details := job:list-details($job) "
        + "return (string-join(($details/@session = $id, exists($details/@address)), ' '), "
        + "job:remove($job))");
  }
}

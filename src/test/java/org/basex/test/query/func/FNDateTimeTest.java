package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests some XQuery dateTime functions prefixed with "datetime".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dirk Kirsten
 */
public final class FNDateTimeTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void currentTime() {
    check(_DATETIME_CURRENT_TIME);
  }

  /** Test method. */
  @Test
  public void currentDate() {
    check(_DATETIME_CURRENT_DATE);
  }

  /** Test method. */
  @Test
  public void currentDateTime() {
    check(_DATETIME_CURRENT_DATETIME);
  }

  /** Test method. */
  @Test
  public void currentTimestamp() {
    check(_DATETIME_TIMESTAMP);
  }
}

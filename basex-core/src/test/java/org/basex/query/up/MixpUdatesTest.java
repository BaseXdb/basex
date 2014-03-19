package org.basex.query.up;

import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.junit.*;

/**
 * Tests for the {@link MainOptions#MIXUPDATES} flag.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class MixpUdatesTest extends AdvancedQueryTest {
  /**
   * Prepare tests.
   * @throws BaseXException database exception
   */
  @BeforeClass
  public static void beforeClass() throws BaseXException {
    new Set(MainOptions.MIXUPDATES, true).execute(context);
  }

  /**
   * Finalize tests.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void afterClass() throws BaseXException {
    new Set(MainOptions.MIXUPDATES, false).execute(context);
  }

  /** Transform expression containing a simple expression. */
  @Test
  public void transSimple() {
    error("<a/> update ('')", Err.BASEX_MOD);
    error("copy $a := <a/> modify ('') return $a", Err.BASEX_MOD);
  }

  /** Update test. */
  @Test
  public void list() {
    query("delete node <a/>, 1, db:output('2')", "1 2");
  }

  /** Update test. */
  @Test
  public void update() {
    query(_XQUERY_UPDATE.args("1"), "1");
    query(_XQUERY_UPDATE.args("1") + ",2", "1 2");
  }

  /** Test method. */
  @Test
  public void output() {
    query(_DB_OUTPUT.args("x") + ",1", "1 x");
  }

  /** Annotations. */
  @Test
  public void annotations() {
    query("declare %updating function local:x() { 1 }; local:x()", "1");
  }
}

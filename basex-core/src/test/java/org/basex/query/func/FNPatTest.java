package org.basex.query.func;

import org.basex.query.util.*;
import org.basex.query.*;
import org.junit.*;

/**
 * This class tests XQuery functions placed in the {@link FNPat} class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class FNPatTest extends AdvancedQueryTest {
  /** Tests for the {@code fn:replate} function. */
  @Test
  public void replace() {
    // tests for issue GH-573:
    query("replace('aaaa bbbbbbbb ddd ','(.{6,15}) ','$1@')", "aaaa bbbbbbbb@ddd ");
    query("replace(' aaa AAA 123','(\\s+\\P{Ll}{3,280}?)','$1@')", " aaa AAA@ 123@");
    error("replace('asdf','a{12,3}','')", Err.REGPAT);
  }
}

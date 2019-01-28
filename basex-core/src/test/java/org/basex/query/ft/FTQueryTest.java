package org.basex.query.ft;

import org.basex.*;
import org.junit.*;

/**
 * Full-text query tests.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FTQueryTest extends SandboxTest {
  /** Checks optimizations of full-text operations. */
  @Test public void ftOptimize() {
    query("let $a := <a>x</a> "
        + "let $b := $a[. contains text '.*' all using wildcards] "
        + "let $c := $b "
        + "return $c", "<a>x</a>");
  }

}

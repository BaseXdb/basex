package org.basex.query.simple;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * XQuery extensions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class XQueryExtensionsTest extends SandboxTest {
  /** If without else. */
  @Test public void ifWithoutElse() {
    query("if(1) then 2", 2);
    query("if(1) then if(2) then 3", 3);
    query("if(1) then 2 else if(3) then 4", 2);
    query("if(()) then 2", "");
    query("if(()) then if(2) then 3", "");
    query("if(()) then 2 else if(3) then 4", 4);
  }

  /** $err:additional. */
  @Test public void errAdditional() {
    query("try { error() } catch * { count($err:additional) }", 1);
    query("let $f := function () { error() } " +
        "return try { $f() } catch * { count($err:additional) }", 2);
  }
}

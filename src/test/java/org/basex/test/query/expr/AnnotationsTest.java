package org.basex.test.query.expr;

import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;

/**
 * Annotations tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class AnnotationsTest extends AdvancedQueryTest {
  /** Parsing of function declaration. */
  @Test
  public void functionDecl() {
    query("declare %public function local:x() { 1 }; local:x()", "1");
    query("declare %fn:public function local:x() { 1 }; local:x()", "1");
    query("declare %private function local:x() { 1 }; local:x()", "1");
    query("declare %fn:private function local:x() { 1 }; local:x()", "1");
    //error("declare %unknown function local:x() { 1 }; local:x()", Err.WHICHANN);
    //error("declare %err:public function local:x() { 1 }; local:x()", Err.WHICHANN);
    //error("declare %pfff:public function local:x() { 1 }; local:x()", Err.NOURI);
  }

  /** Parsing of variable declaration. */
  @Test
  public void varDecl() {
    query("declare %public variable $x := 1; $x", "1");
    query("declare %fn:public variable $x := 1; $x", "1");
    query("declare %private variable $x := 1; $x", "1");
    query("declare %fn:private variable $x := 1; $x", "1");
    //error("declare %unknown variable $x := 1; $x", Err.WHICHANN);
    //error("declare %err:public variable $x := 1; $x", Err.WHICHANN);
    //error("declare %pfff:public variable $x := 1; $x", Err.NOURI);
  }

  /**  Test for empty-sequence() as function item. */
  @Test
  public void emptyFunTest() {
    error("()()", Err.XPEMPTY);
  }
}

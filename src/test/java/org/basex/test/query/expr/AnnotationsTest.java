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
  /** Parsing of function declarations. */
  @Test
  public void functionDecl() {
    query("declare namespace a='a';declare %a:a function local:x() {1}; local:x()", "1");
    query("declare %public function local:x() { 1 }; local:x()", "1");
    query("declare %fn:public function local:x() { 1 }; local:x()", "1");
    query("declare %private function local:x() { 1 }; local:x()", "1");
    query("declare %fn:private function local:x() { 1 }; local:x()", "1");
    query("declare namespace a='a';declare %a:a function local:x() {1}; local:x()", "1");
  }

  /** Parsing of variable declarations. */
  @Test
  public void varDecl() {
    query("declare %public variable $x := 1; $x", "1");
    query("declare %fn:public variable $x := 1; $x", "1");
    query("declare %private variable $x := 1; $x", "1");
    query("declare %fn:private variable $x := 1; $x", "1");
    query("declare namespace a='a';declare %a:a variable $x := 1; $x", "1");
    query("declare namespace a='a';declare %a:a(1) %a:b(2) variable $x:=1; $x", "1");
  }

  /** Parsing errors and conflicts. */
  @Test
  public void conflicts() {
    error("declare namespace a='a';declare %a:a() variable $x:=1; $x", Err.ANNVALUE);
    error("declare namespace a='a';declare %a:a() variable $x:=1; $x", Err.ANNVALUE);
    error("declare %unknown local:x() { 1 }; local:x(); $x", Err.ANNRES);
    error("declare %pfff:public variable $x := 1; $x", Err.NOURI);
    error("declare %public %public variable $x := 1; $x", Err.ANNVIS);
  }

  /**  Test for empty-sequence() as function item. */
  @Test
  public void emptyFunTest() {
    error("()()", Err.XPEMPTY);
  }
}

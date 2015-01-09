package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.junit.*;

/**
 * Annotations tests.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class AnnotationsTest extends AdvancedQueryTest {
  /** Parsing of function declarations. */
  @Test
  public void functionDecl() {
    query("declare namespace a='a';declare %a:a function local:x() {1}; local:x()", "1");
    query("declare %public function local:x() { 1 }; local:x()", "1");
    query("declare %private function local:x() { 1 }; local:x()", "1");
    query("declare namespace a='a';declare %a:a function local:x() {1}; local:x()", "1");
  }

  /** Parsing of variable declarations. */
  @Test
  public void varDecl() {
    query("declare %public variable $x := 1; $x", "1");
    query("declare %private variable $x := 1; $x", "1");
    query("declare namespace a='a';declare %a:a variable $x := 1; $x", "1");
    query("declare namespace a='a';declare %a:a(1) %a:b(2) variable $x:=1; $x", "1");
  }

  /** Parsing errors and conflicts. */
  @Test
  public void conflicts() {
    error("declare namespace a='a';declare %a:a() variable $x:=1; $x", ANNVALUE);
    error("declare namespace a='a';declare %a:a() variable $x:=1; $x", ANNVALUE);
    error("declare %pfff:public variable $x := 1; $x", NOURI_X);
    error("declare %public %public variable $x := 1; $x", DUPLVARVIS);
    error("declare %public %private variable $x := 1; $x", DUPLVARVIS);
    error("declare %updating variable $x := 1; $x", UPDATINGVAR);
    error("declare %updating updating function local:x() " +
        "{ insert node <a/> into <b/> }; local:x()", DUPLUPD);
    error("declare updating %updating function local:x() " +
        "{ insert node <a/> into <b/> }; local:x()", DUPLUPD);
    error("declare %updating function local:x() { 1 }; local:x()", UPEXPECTF);
  }

  /** Parsing errors and conflicts. */
  @Test
  public void unknown() {
    // ignore prefixes with no annotation definitions
    query("declare %db:xx function local:x() { 1 }; 1");
    // check unit annotations
    error("declare %unit:xyz function local:x() { 1 }; 1", BASX_ANNOT_X_X);
    // check restxq annotations
    error("declare %rest:xx function local:x() { 1 }; 1", BASX_ANNOT_X_X);
    // check output annotations
    error("declare %output:xx function local:x() { 1 }; 1", BASX_ANNOT_X_X);
    error("declare %output:method function local:x() { 1 }; 1", BASX_ANNOTARGS_X_X);
  }

  /**  Test for empty-sequence() as function item. */
  @Test
  public void emptyFunTest() {
    error("()()", EMPTYFOUND);
  }
}

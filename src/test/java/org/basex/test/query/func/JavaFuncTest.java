package org.basex.test.query.func;

import org.basex.test.query.AdvancedQueryTest;
import org.junit.Test;

/**
 * This class tests the Java bindings.
 * [LW] add tests for argument conversions
 *
 * @author Leo Woerteler
 */
public class JavaFuncTest extends AdvancedQueryTest {

  /** Tests calling some Java constructors from XQuery. */
  @Test
  public void constr() {
    query("Q{java:java.lang.Integer}new('123')", 123);
    query("declare namespace rand='java:java.util.Random';" +
        "rand:nextInt(rand:new())");
    query("declare namespace ctx='java:org.basex.core.Context';" +
        "ctx:new()");
  }

  /** Tests calling some Java static fields from XQuery. */
  @Test
  public void staticField() {
    query("Q{java:java.lang.Math}PI()", Math.PI);
    query("Q{java:org.basex.core.Prop}gui()");
  }

  /** Tests calling some Java object fields from XQuery. */
  @Test
  public void field() {
    query("declare namespace point='java:java.awt.Point';" +
        "point:x(point:new())", 0);
  }

  /** Tests calling some Java static methods from XQuery. */
  @Test
  public void staticMethod() {
    query("Q{java:java.lang.Math}sqrt(xs:double(9.0))", 3);
  }

  /** Tests calling some Java static methods from XQuery. */
  @Test
  public void method() {
    query("declare namespace rect = 'java:java.awt.Rectangle';" +
        "rect:contains(rect:new(xs:int(2), xs:int(2)), xs:int(1), xs:int(1))",
        true);
  }

  /** Tests importing a Java class. */
  @Test
  public void javaImport() {
    query("import module namespace sb='java:java.lang.StringBuilder';" +
        "let $a := (sb:append('a'), sb:append('b')) return sb:to-string()", "ab");

    query("import module namespace qm='java:org.basex.test.query.func.QueryModuleTest';" +
        "qm:fast(0)", "Apple");
    query("import module namespace qm='java:org.basex.test.query.func.QueryModuleTest';" +
        "qm:convenient(xs:int(1))", "Banana");
    query("import module namespace qm='java:org.basex.test.query.func.QueryModuleTest';" +
        "qm:functionNS()", "http://www.w3.org/2005/xpath-functions");
  }

  /** Tests importing a Java class and throwing errors. */
  @Test
  public void javaImportError() {
    query("declare namespace qm='java:org.basex.test.query.func.QueryModuleTest';" +
        "try { qm:error(qm:new()) } catch * { $err:code }", "BASX0000");
    query("import module namespace qm='java:org.basex.test.query.func.QueryModuleTest';" +
        "try { qm:error() } catch * { $err:code }", "BASX0000");
  }
}

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
    query("'java:java.lang.Integer':new('123')", 123);
    query("declare namespace rand='java:java.util.Random';" +
        "rand:nextInt(rand:new())");
    query("declare namespace ctx='java:org.basex.core.Context';" +
        "ctx:new()");
  }

  /** Tests calling some Java static fields from XQuery. */
  @Test
  public void staticField() {
    query("'java:java.lang.Math':PI()", Math.PI);
    query("'java:org.basex.core.Prop':gui()");
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
    query("'java:java.lang.Math':sqrt(xs:double(9.0))", 3.0);
  }

  /** Tests calling some Java static methods from XQuery. */
  @Test
  public void method() {
    query("declare namespace rect = 'java:java.awt.Rectangle';" +
        "rect:contains(rect:new(xs:int(2), xs:int(2)), xs:int(1), xs:int(1))",
        true);
  }
}

package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the numeric-less-than() function.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpNumericLessThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-1                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:integer(-1) lt xs:integer(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT1() {
    final XQuery query = new XQuery(
      "xs:integer(-1) lt xs:integer(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-10                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:float("-INF") lt 0`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT10() {
    final XQuery query = new XQuery(
      "xs:float(\"-INF\") lt 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-11                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:float(-1) lt xs:float(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT11() {
    final XQuery query = new XQuery(
      "xs:float(-1) lt xs:float(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-12                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:float(-1) le xs:float(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT12() {
    final XQuery query = new XQuery(
      "xs:float(-1) le xs:float(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-13                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(xs:double("NaN") lt 1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT13() {
    final XQuery query = new XQuery(
      "not(xs:double(\"NaN\") lt 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-14                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(xs:float("NaN") lt 1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT14() {
    final XQuery query = new XQuery(
      "not(xs:float(\"NaN\") lt 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-15                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(xs:double("NaN") le 1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT15() {
    final XQuery query = new XQuery(
      "not(xs:double(\"NaN\") le 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-16                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(xs:float("NaN") le 1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT16() {
    final XQuery query = new XQuery(
      "not(xs:float(\"NaN\") le 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-17                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(1 lt xs:double("NaN"))`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT17() {
    final XQuery query = new XQuery(
      "not(1 lt xs:double(\"NaN\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-18                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(1 lt xs:float("NaN"))`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT18() {
    final XQuery query = new XQuery(
      "not(1 lt xs:float(\"NaN\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-19                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(1 le xs:double("NaN"))`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT19() {
    final XQuery query = new XQuery(
      "not(1 le xs:double(\"NaN\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-2                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:integer(-1) le xs:integer(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT2() {
    final XQuery query = new XQuery(
      "xs:integer(-1) le xs:integer(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-20                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `not(1 le xs:float("NaN"))`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT20() {
    final XQuery query = new XQuery(
      "not(1 le xs:float(\"NaN\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-21                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: le combined with count().                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT21() {
    final XQuery query = new XQuery(
      "1 le count((1, 2, 3, timezone-from-time(current-time()), 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-22                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: lt combined with count().                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT22() {
    final XQuery query = new XQuery(
      "0 lt count((1, 2, 3, timezone-from-time(current-time()), 4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-3                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:decimal(-1) lt xs:decimal(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT3() {
    final XQuery query = new XQuery(
      "xs:decimal(-1) lt xs:decimal(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-4                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:decimal(-1) le xs:decimal(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT4() {
    final XQuery query = new XQuery(
      "xs:decimal(-1) le xs:decimal(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-5                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `0 lt xs:double("INF")`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT5() {
    final XQuery query = new XQuery(
      "0 lt xs:double(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-6                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:double("-INF") lt 0`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT6() {
    final XQuery query = new XQuery(
      "xs:double(\"-INF\") lt 0",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-7                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:double(-1) lt xs:double(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT7() {
    final XQuery query = new XQuery(
      "xs:double(-1) lt xs:double(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-8                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `xs:double(-1) le xs:double(1)`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT8() {
    final XQuery query = new XQuery(
      "xs:double(-1) le xs:double(1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-NumericLT-9                                   
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: A test whose essence is: `0 lt xs:float("INF")`. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kNumericLT9() {
    final XQuery query = new XQuery(
      "0 lt xs:float(\"INF\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K2-NumericLT-1                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:unsignedLong values.          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2NumericLT1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"100\") lt xs:unsignedLong(\"18446744073709551615\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K2-NumericLT-2                                  
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Compare two xs:unsignedLong values.          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2NumericLT2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"18446744073709551615\") lt xs:unsignedLong(\"100\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:double .
   */
  @org.junit.Test
  public void cbclNumericLessThan001() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:double) as xs:double { $arg * $arg }; not( local:square(1e0) lt local:square(2e0) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:float .
   */
  @org.junit.Test
  public void cbclNumericLessThan002() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:float) as xs:float { $arg * $arg }; not( local:square(xs:float(1e0)) lt local:square(xs:float(2e0)) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:decimal .
   */
  @org.junit.Test
  public void cbclNumericLessThan003() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; not( local:square(1.0) lt local:square(2.0) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:integer .
   */
  @org.junit.Test
  public void cbclNumericLessThan004() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; not( local:square(1) lt local:square(2) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:double .
   */
  @org.junit.Test
  public void cbclNumericLessThan005() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:double) as xs:double { $arg * $arg }; not( local:square(1e0) ge local:square(2e0) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:float .
   */
  @org.junit.Test
  public void cbclNumericLessThan006() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:float) as xs:float { $arg * $arg }; not( local:square(xs:float(1e0)) ge local:square(xs:float(2e0)) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:decimal .
   */
  @org.junit.Test
  public void cbclNumericLessThan007() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:decimal) as xs:decimal { $arg * $arg }; not( local:square(1.0) ge local:square(2.0) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:integer .
   */
  @org.junit.Test
  public void cbclNumericLessThan008() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:integer) as xs:integer { $arg * $arg }; not( local:square(1) ge local:square(2) )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:double .
   */
  @org.junit.Test
  public void cbclNumericLessThan009() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:double) as xs:double { $arg * $arg }; not(not( local:square(1e0) lt local:square(2e0) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:float .
   */
  @org.junit.Test
  public void cbclNumericLessThan010() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:float) as xs:float { $arg * $arg }; not(not( local:square(xs:float(1e0)) lt local:square(xs:float(2e0)) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of xs:double .
   */
  @org.junit.Test
  public void cbclNumericLessThan011() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:double) as xs:double { $arg * $arg }; not(not( local:square(1e0) ge local:square(2e0) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of xs:float .
   */
  @org.junit.Test
  public void cbclNumericLessThan012() {
    final XQuery query = new XQuery(
      "declare function local:square($arg as xs:float) as xs:float { $arg * $arg }; not(not( local:square(xs:float(1e0)) ge local:square(xs:float(2e0)) ))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan013() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return $x + 1 lt 121",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan014() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return $x - 1 lt 121",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan015() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return 121 lt $x + 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan016() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return 121 lt 1 + $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan017() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return $x + 1 ge 121",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan018() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return $x - 1 ge 121",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan019() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return 121 ge $x + 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan020() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return 121 ge 1 + $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan021() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return $x + 2 lt 121",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan022() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return $x - 2 lt 12",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan023() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return 121 lt $x + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan024() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return 121 lt 2 + $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan025() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return $x + 2 ge 121",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan026() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return $x - 2 ge 12",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan027() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return 121 ge $x + 2",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan028() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return 121 ge $x + 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  test comparison of integers .
   */
  @org.junit.Test
  public void cbclNumericLessThan029() {
    final XQuery query = new XQuery(
      "declare function local:factorial($arg as xs:integer) as xs:integer { if ($arg lt 1) then 1 else $arg * local:factorial($arg - 1) }; let $x := local:factorial(5) return 121 ge 2 + $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandbl2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandbl2args1() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") lt xs:double(\"-1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandbl2args-10                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(upper bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandbl2args10() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") ge xs:double(\"1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandbl2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(mid range)                           
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandbl2args2() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") lt xs:double(\"-1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandbl2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(upper bound)                         
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandbl2args3() {
    final XQuery query = new XQuery(
      "xs:double(\"1.7976931348623157E308\") lt xs:double(\"-1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandbl2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(mid range)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandbl2args4() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") lt xs:double(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandbl2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(upper bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandbl2args5() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") lt xs:double(\"1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandbl2args-6                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandbl2args6() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") ge xs:double(\"-1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandbl2args-7                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(mid range)                           
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandbl2args7() {
    final XQuery query = new XQuery(
      "xs:double(\"0\") ge xs:double(\"-1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandbl2args-8                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(upper bound)                         
   * $arg2 = xs:double(lower bound)                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandbl2args8() {
    final XQuery query = new XQuery(
      "xs:double(\"1.7976931348623157E308\") ge xs:double(\"-1.7976931348623157E308\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandbl2args-9                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:double(lower bound)                         
   * $arg2 = xs:double(mid range)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandbl2args9() {
    final XQuery query = new XQuery(
      "xs:double(\"-1.7976931348623157E308\") ge xs:double(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandec2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandec2args1() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") lt xs:decimal(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandec2args-10                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandec2args10() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") ge xs:decimal(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandec2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(mid range)                          
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandec2args2() {
    final XQuery query = new XQuery(
      "xs:decimal(\"617375191608514839\") lt xs:decimal(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandec2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(upper bound)                        
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandec2args3() {
    final XQuery query = new XQuery(
      "xs:decimal(\"999999999999999999\") lt xs:decimal(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandec2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandec2args4() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") lt xs:decimal(\"617375191608514839\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandec2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandec2args5() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") lt xs:decimal(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandec2args-6                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandec2args6() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") ge xs:decimal(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandec2args-7                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(mid range)                          
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandec2args7() {
    final XQuery query = new XQuery(
      "xs:decimal(\"617375191608514839\") ge xs:decimal(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandec2args-8                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(upper bound)                        
   * $arg2 = xs:decimal(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandec2args8() {
    final XQuery query = new XQuery(
      "xs:decimal(\"999999999999999999\") ge xs:decimal(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thandec2args-9                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:decimal(lower bound)                        
   * $arg2 = xs:decimal(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThandec2args9() {
    final XQuery query = new XQuery(
      "xs:decimal(\"-999999999999999999\") ge xs:decimal(\"617375191608514839\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanflt2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanflt2args1() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") lt xs:float(\"-3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanflt2args-10                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(upper bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanflt2args10() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") ge xs:float(\"3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanflt2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(mid range)                            
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanflt2args2() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") lt xs:float(\"-3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanflt2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(upper bound)                          
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanflt2args3() {
    final XQuery query = new XQuery(
      "xs:float(\"3.4028235E38\") lt xs:float(\"-3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanflt2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(mid range)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanflt2args4() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") lt xs:float(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanflt2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(upper bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanflt2args5() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") lt xs:float(\"3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanflt2args-6                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanflt2args6() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") ge xs:float(\"-3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanflt2args-7                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(mid range)                            
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanflt2args7() {
    final XQuery query = new XQuery(
      "xs:float(\"0\") ge xs:float(\"-3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanflt2args-8                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(upper bound)                          
   * $arg2 = xs:float(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanflt2args8() {
    final XQuery query = new XQuery(
      "xs:float(\"3.4028235E38\") ge xs:float(\"-3.4028235E38\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanflt2args-9                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:float(lower bound)                          
   * $arg2 = xs:float(mid range)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanflt2args9() {
    final XQuery query = new XQuery(
      "xs:float(\"-3.4028235E38\") ge xs:float(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanint2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanint2args1() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") lt xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanint2args-10                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(upper bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanint2args10() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") ge xs:int(\"2147483647\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanint2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(mid range)                              
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanint2args2() {
    final XQuery query = new XQuery(
      "xs:int(\"-1873914410\") lt xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanint2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(upper bound)                            
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanint2args3() {
    final XQuery query = new XQuery(
      "xs:int(\"2147483647\") lt xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanint2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(mid range)                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanint2args4() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") lt xs:int(\"-1873914410\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanint2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(upper bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanint2args5() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") lt xs:int(\"2147483647\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanint2args-6                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanint2args6() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") ge xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanint2args-7                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(mid range)                              
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanint2args7() {
    final XQuery query = new XQuery(
      "xs:int(\"-1873914410\") ge xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanint2args-8                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(upper bound)                            
   * $arg2 = xs:int(lower bound)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanint2args8() {
    final XQuery query = new XQuery(
      "xs:int(\"2147483647\") ge xs:int(\"-2147483648\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanint2args-9                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:int(lower bound)                            
   * $arg2 = xs:int(mid range)                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanint2args9() {
    final XQuery query = new XQuery(
      "xs:int(\"-2147483648\") ge xs:int(\"-1873914410\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanintg2args-1                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanintg2args1() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") lt xs:integer(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanintg2args-10                  
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanintg2args10() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") ge xs:integer(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanintg2args-2                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(mid range)                          
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanintg2args2() {
    final XQuery query = new XQuery(
      "xs:integer(\"830993497117024304\") lt xs:integer(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanintg2args-3                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(upper bound)                        
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanintg2args3() {
    final XQuery query = new XQuery(
      "xs:integer(\"999999999999999999\") lt xs:integer(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanintg2args-4                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanintg2args4() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") lt xs:integer(\"830993497117024304\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanintg2args-5                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(upper bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanintg2args5() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") lt xs:integer(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanintg2args-6                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanintg2args6() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") ge xs:integer(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanintg2args-7                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(mid range)                          
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanintg2args7() {
    final XQuery query = new XQuery(
      "xs:integer(\"830993497117024304\") ge xs:integer(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanintg2args-8                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(upper bound)                        
   * $arg2 = xs:integer(lower bound)                        
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanintg2args8() {
    final XQuery query = new XQuery(
      "xs:integer(\"999999999999999999\") ge xs:integer(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanintg2args-9                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:integer(lower bound)                        
   * $arg2 = xs:integer(mid range)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanintg2args9() {
    final XQuery query = new XQuery(
      "xs:integer(\"-999999999999999999\") ge xs:integer(\"830993497117024304\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanlng2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanlng2args1() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") lt xs:long(\"-92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanlng2args-10                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanlng2args10() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") ge xs:long(\"92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanlng2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(mid range)                             
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanlng2args2() {
    final XQuery query = new XQuery(
      "xs:long(\"-47175562203048468\") lt xs:long(\"-92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanlng2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(upper bound)                           
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanlng2args3() {
    final XQuery query = new XQuery(
      "xs:long(\"92233720368547758\") lt xs:long(\"-92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanlng2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanlng2args4() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") lt xs:long(\"-47175562203048468\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanlng2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanlng2args5() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") lt xs:long(\"92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanlng2args-6                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanlng2args6() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") ge xs:long(\"-92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanlng2args-7                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(mid range)                             
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanlng2args7() {
    final XQuery query = new XQuery(
      "xs:long(\"-47175562203048468\") ge xs:long(\"-92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanlng2args-8                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(upper bound)                           
   * $arg2 = xs:long(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanlng2args8() {
    final XQuery query = new XQuery(
      "xs:long(\"92233720368547758\") ge xs:long(\"-92233720368547758\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanlng2args-9                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:long(lower bound)                           
   * $arg2 = xs:long(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanlng2args9() {
    final XQuery query = new XQuery(
      "xs:long(\"-92233720368547758\") ge xs:long(\"-47175562203048468\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannint2args-1                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannint2args1() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") lt xs:negativeInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannint2args-10                  
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(upper bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannint2args10() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") ge xs:negativeInteger(\"-1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannint2args-2                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(mid range)                  
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannint2args2() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-297014075999096793\") lt xs:negativeInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannint2args-3                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(upper bound)                
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannint2args3() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-1\") lt xs:negativeInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannint2args-4                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(mid range)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannint2args4() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") lt xs:negativeInteger(\"-297014075999096793\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannint2args-5                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(upper bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannint2args5() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") lt xs:negativeInteger(\"-1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannint2args-6                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannint2args6() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") ge xs:negativeInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannint2args-7                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(mid range)                  
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannint2args7() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-297014075999096793\") ge xs:negativeInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannint2args-8                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(upper bound)                
   * $arg2 = xs:negativeInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannint2args8() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-1\") ge xs:negativeInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannint2args-9                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:negativeInteger(lower bound)                
   * $arg2 = xs:negativeInteger(mid range)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannint2args9() {
    final XQuery query = new XQuery(
      "xs:negativeInteger(\"-999999999999999999\") ge xs:negativeInteger(\"-297014075999096793\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannni2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannni2args1() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") lt xs:nonNegativeInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannni2args-10                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannni2args10() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") ge xs:nonNegativeInteger(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannni2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(mid range)               
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannni2args2() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"303884545991464527\") lt xs:nonNegativeInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannni2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(upper bound)             
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannni2args3() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"999999999999999999\") lt xs:nonNegativeInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannni2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannni2args4() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") lt xs:nonNegativeInteger(\"303884545991464527\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannni2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannni2args5() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") lt xs:nonNegativeInteger(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannni2args-6                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannni2args6() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") ge xs:nonNegativeInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannni2args-7                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(mid range)               
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannni2args7() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"303884545991464527\") ge xs:nonNegativeInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannni2args-8                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(upper bound)             
   * $arg2 = xs:nonNegativeInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannni2args8() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"999999999999999999\") ge xs:nonNegativeInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannni2args-9                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonNegativeInteger(lower bound)             
   * $arg2 = xs:nonNegativeInteger(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannni2args9() {
    final XQuery query = new XQuery(
      "xs:nonNegativeInteger(\"0\") ge xs:nonNegativeInteger(\"303884545991464527\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannpi2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannpi2args1() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") lt xs:nonPositiveInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannpi2args-10                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannpi2args10() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") ge xs:nonPositiveInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannpi2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(mid range)               
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannpi2args2() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-475688437271870490\") lt xs:nonPositiveInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannpi2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(upper bound)             
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannpi2args3() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"0\") lt xs:nonPositiveInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannpi2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannpi2args4() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") lt xs:nonPositiveInteger(\"-475688437271870490\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannpi2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannpi2args5() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") lt xs:nonPositiveInteger(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannpi2args-6                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannpi2args6() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") ge xs:nonPositiveInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannpi2args-7                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(mid range)               
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannpi2args7() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-475688437271870490\") ge xs:nonPositiveInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannpi2args-8                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(upper bound)             
   * $arg2 = xs:nonPositiveInteger(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannpi2args8() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"0\") ge xs:nonPositiveInteger(\"-999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thannpi2args-9                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:nonPositiveInteger(lower bound)             
   * $arg2 = xs:nonPositiveInteger(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThannpi2args9() {
    final XQuery query = new XQuery(
      "xs:nonPositiveInteger(\"-999999999999999999\") ge xs:nonPositiveInteger(\"-475688437271870490\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanpint2args-1                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanpint2args1() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") lt xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanpint2args-10                  
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(upper bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanpint2args10() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") ge xs:positiveInteger(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanpint2args-2                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(mid range)                  
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanpint2args2() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") lt xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanpint2args-3                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(upper bound)                
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanpint2args3() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999999\") lt xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanpint2args-4                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(mid range)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanpint2args4() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") lt xs:positiveInteger(\"52704602390610033\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanpint2args-5                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(upper bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanpint2args5() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") lt xs:positiveInteger(\"999999999999999999\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanpint2args-6                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanpint2args6() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") ge xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanpint2args-7                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(mid range)                  
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanpint2args7() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"52704602390610033\") ge xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanpint2args-8                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(upper bound)                
   * $arg2 = xs:positiveInteger(lower bound)                
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanpint2args8() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"999999999999999999\") ge xs:positiveInteger(\"1\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanpint2args-9                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:positiveInteger(lower bound)                
   * $arg2 = xs:positiveInteger(mid range)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanpint2args9() {
    final XQuery query = new XQuery(
      "xs:positiveInteger(\"1\") ge xs:positiveInteger(\"52704602390610033\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thansht2args-1                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThansht2args1() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") lt xs:short(\"-32768\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thansht2args-10                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(upper bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThansht2args10() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") ge xs:short(\"32767\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thansht2args-2                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(mid range)                            
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThansht2args2() {
    final XQuery query = new XQuery(
      "xs:short(\"-5324\") lt xs:short(\"-32768\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thansht2args-3                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(upper bound)                          
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThansht2args3() {
    final XQuery query = new XQuery(
      "xs:short(\"32767\") lt xs:short(\"-32768\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thansht2args-4                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(mid range)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThansht2args4() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") lt xs:short(\"-5324\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thansht2args-5                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(upper bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThansht2args5() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") lt xs:short(\"32767\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thansht2args-6                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThansht2args6() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") ge xs:short(\"-32768\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thansht2args-7                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(mid range)                            
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThansht2args7() {
    final XQuery query = new XQuery(
      "xs:short(\"-5324\") ge xs:short(\"-32768\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thansht2args-8                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(upper bound)                          
   * $arg2 = xs:short(lower bound)                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThansht2args8() {
    final XQuery query = new XQuery(
      "xs:short(\"32767\") ge xs:short(\"-32768\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thansht2args-9                    
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:short(lower bound)                          
   * $arg2 = xs:short(mid range)                            
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThansht2args9() {
    final XQuery query = new XQuery(
      "xs:short(\"-32768\") ge xs:short(\"-5324\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanulng2args-1                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanulng2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") lt xs:unsignedLong(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanulng2args-10                  
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(upper bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanulng2args10() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") ge xs:unsignedLong(\"184467440737095516\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanulng2args-2                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(mid range)                     
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanulng2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"130747108607674654\") lt xs:unsignedLong(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanulng2args-3                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(upper bound)                   
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanulng2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"184467440737095516\") lt xs:unsignedLong(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanulng2args-4                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(mid range)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanulng2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") lt xs:unsignedLong(\"130747108607674654\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanulng2args-5                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(upper bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanulng2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") lt xs:unsignedLong(\"184467440737095516\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanulng2args-6                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanulng2args6() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") ge xs:unsignedLong(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanulng2args-7                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(mid range)                     
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanulng2args7() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"130747108607674654\") ge xs:unsignedLong(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanulng2args-8                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(upper bound)                   
   * $arg2 = xs:unsignedLong(lower bound)                   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanulng2args8() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"184467440737095516\") ge xs:unsignedLong(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanulng2args-9                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedLong(lower bound)                   
   * $arg2 = xs:unsignedLong(mid range)                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanulng2args9() {
    final XQuery query = new XQuery(
      "xs:unsignedLong(\"0\") ge xs:unsignedLong(\"130747108607674654\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanusht2args-1                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanusht2args1() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") lt xs:unsignedShort(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanusht2args-10                  
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(upper bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanusht2args10() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") ge xs:unsignedShort(\"65535\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanusht2args-2                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(mid range)                    
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanusht2args2() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"44633\") lt xs:unsignedShort(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanusht2args-3                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(upper bound)                  
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanusht2args3() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"65535\") lt xs:unsignedShort(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanusht2args-4                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(mid range)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanusht2args4() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") lt xs:unsignedShort(\"44633\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanusht2args-5                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(upper bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanusht2args5() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") lt xs:unsignedShort(\"65535\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanusht2args-6                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanusht2args6() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") ge xs:unsignedShort(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanusht2args-7                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(mid range)                    
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanusht2args7() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"44633\") ge xs:unsignedShort(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanusht2args-8                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(upper bound)                  
   * $arg2 = xs:unsignedShort(lower bound)                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanusht2args8() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"65535\") ge xs:unsignedShort(\"0\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-numeric-less-thanusht2args-9                   
   * Written By: Carmelo Montanez                            
   * Date: Thu Dec 16 10:48:16 GMT-05:00 2004                
   * Purpose: Evaluates The "op:numeric-less-than" operator 
   *  with the arguments set as follows:                    
   * $arg1 = xs:unsignedShort(lower bound)                  
   * $arg2 = xs:unsignedShort(mid range)                    
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opNumericLessThanusht2args9() {
    final XQuery query = new XQuery(
      "xs:unsignedShort(\"0\") ge xs:unsignedShort(\"44633\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }
}

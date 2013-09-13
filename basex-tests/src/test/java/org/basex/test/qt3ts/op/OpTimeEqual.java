package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the time-equal() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpTimeEqual extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-1                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:time, returning positive. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ1() {
    final XQuery query = new XQuery(
      "xs:time(\"23:01:04.12\") eq xs:time(\"23:01:04.12\")",
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
   *  Test: K-TimeEQ-10                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'ne' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ10() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") ne xs:date(\"1999-12-04\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-11                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'le' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ11() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") le xs:date(\"1999-12-04\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-12                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'lt' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ12() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") lt xs:date(\"1999-12-04\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-13                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'ge' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ13() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") ge xs:date(\"1999-12-04\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-14                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'gt' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ14() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") gt xs:date(\"1999-12-04\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-15                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'eq' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ15() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") eq xs:time(\"12:12:23\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-16                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'ne' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ16() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") ne xs:time(\"12:12:23\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-17                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'le' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ17() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") le xs:time(\"12:12:23\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-18                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'lt' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ18() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") lt xs:time(\"12:12:23\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-19                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'ge' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ19() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") ge xs:time(\"12:12:23\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-2                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'eq' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ2() {
    final XQuery query = new XQuery(
      "not(xs:time(\"23:01:04.12\") eq xs:time(\"23:01:04.13\"))",
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
   *  Test: K-TimeEQ-20                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'gt' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ20() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-12-04\") gt xs:time(\"12:12:23\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K-TimeEQ-3                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ3() {
    final XQuery query = new XQuery(
      "xs:time(\"23:01:05.12\") ne xs:time(\"23:01:04.12\")",
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
   *  Test: K-TimeEQ-4                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ne' for xs:time.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ4() {
    final XQuery query = new XQuery(
      "not(xs:time(\"23:01:04.12\") ne xs:time(\"23:01:04.12\"))",
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
   *  Test: K-TimeEQ-5                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to Z, in xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ5() {
    final XQuery query = new XQuery(
      "xs:time(\"16:00:12.345-00:00\") eq xs:time(\"16:00:12.345Z\")",
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
   *  Test: K-TimeEQ-6                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset +00:00 is equal to Z, in xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ6() {
    final XQuery query = new XQuery(
      "xs:time(\"16:00:12.345+00:00\") eq xs:time(\"16:00:12.345Z\")",
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
   *  Test: K-TimeEQ-7                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset Z is equal to Z, in xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ7() {
    final XQuery query = new XQuery(
      "xs:time(\"16:00:12.345Z\") eq xs:time(\"16:00:12.345Z\")",
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
   *  Test: K-TimeEQ-8                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Test that zone offset -00:00 is equal to +00:00, in xs:time. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ8() {
    final XQuery query = new XQuery(
      "xs:time(\"16:00:12.345-00:00\") eq xs:time(\"16:00:12.345+00:00\")",
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
   *  Test: K-TimeEQ-9                                      
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The operator 'eq' is not available between xs:dateTime and xs:date . 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kTimeEQ9() {
    final XQuery query = new XQuery(
      "xs:time(\"12:12:23\") eq xs:date(\"1999-12-04\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   * 
   * *******************************************************
   *  Test: K2-TimeEQ-1                                     
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Basic negative equalness test for xs:time.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void k2TimeEQ1() {
    final XQuery query = new XQuery(
      "xs:time(\"01:01:01-03:00\") ne xs:time(\"01:01:01+03:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string };\n" +
      "        declare function local:time($hours as xs:integer, $mins as xs:integer, $seconds as xs:decimal) { let $h := local:two-digit($hours), $m := local:two-digit($mins) return xs:time(concat($h, ':', $m, ':', $seconds)) };\n" +
      "        not(local:time(12, 59, 30) eq xs:time(\"12:32:05\"))",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual002() {
    final XQuery query = new XQuery(
      "xs:time(\"14:00:00-12:00\") eq xs:time(\"02:00:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual003() {
    final XQuery query = new XQuery(
      "xs:time(\"02:00:00\") eq xs:time(\"14:00:00-12:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual004() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:01+01:00\") eq xs:time(\"00:00:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual005() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00\") eq xs:time(\"00:00:01+01:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:two-digit($number as xs:integer) { let $string := string($number) return if (string-length($string) lt 2) then concat('0', $string) else $string };\n" +
      "        declare function local:time($hours as xs:integer, $mins as xs:integer, $seconds as xs:decimal) { let $h := local:two-digit($hours), $m := local:two-digit($mins) return xs:time(concat($h, ':', $m, ':', $seconds)) };\n" +
      "        not(local:time(12, 59, 30) ne xs:time(\"12:32:05\"))",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual007() {
    final XQuery query = new XQuery(
      "xs:time(\"14:00:00-12:00\") ne xs:time(\"02:00:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual008() {
    final XQuery query = new XQuery(
      "xs:time(\"02:00:00\") ne xs:time(\"14:00:00-12:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual009() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:01+01:00\") ne xs:time(\"00:00:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual010() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00\") ne xs:time(\"00:00:01+01:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual011() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:time($time as xs:time, $null as xs:boolean) { if ($null) then () else $time };\n" +
      "        exists(local:time(xs:time(\"23:58:00\"), fn:true()) eq xs:time(\"23:58:00\"))",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual012() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:time($time as xs:time, $null as xs:boolean) { if ($null) then () else $time };\n" +
      "        local:time(xs:time(\"23:58:00\"), fn:false()) eq xs:time(\"23:58:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual013() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:time($time as xs:time, $null as xs:boolean) { if ($null) then () else $time };\n" +
      "        exists(local:time(xs:time(\"23:58:00\"), fn:true()) ne xs:time(\"23:58:00\"))",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual014() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:time($time as xs:time, $null as xs:boolean) { if ($null) then () else $time };\n" +
      "        local:time(xs:time(\"23:58:00\"), fn:false()) ne xs:time(\"23:58:00\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual017() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00+12:00\") eq xs:time(\"00:00:01\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual018() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00+12:00\") ne xs:time(\"00:00:01\")",
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
   *  test comparison of time .
   */
  @org.junit.Test
  public void cbclTimeEqual019() {
    final XQuery query = new XQuery(
      "if (implicit-timezone() eq xs:dayTimeDuration('PT1H'))\n" +
      "            then xs:time(\"12:00:00+02:00\") eq xs:time(\"12:00:00\")\n" +
      "            else xs:time(\"12:00:00+01:00\") eq xs:time(\"12:00:00\")",
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
   * Test: op-time-equal2args-1                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args1() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") eq xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-10                             
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args10() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ne xs:time(\"23:59:59Z\")",
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
   * Test: op-time-equal2args-11                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args11() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") le xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-12                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args12() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") le xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-13                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args13() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") le xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-14                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args14() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") le xs:time(\"08:03:35Z\")",
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
   * Test: op-time-equal2args-15                            
   * Written By: Carmelo Montanez                           
   * Date: June 3 2005                                      
   * Purpose: Evaluates The "op:time-equal" operator (le)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args15() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") le xs:time(\"23:59:59Z\")",
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
   * Test: op-time-equal2args-16                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args16() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ge xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-17                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator  (ge)  
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args17() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") ge xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-q8                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args18() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") ge xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-19                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args19() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ge xs:time(\"08:03:35Z\")",
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
   * Test: op-time-equal2args-2                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args2() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") eq xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-20                            
   * Written By: Carmelo Montanez                           
   * Date: June 3, 2005                                     
   * Purpose: Evaluates The "op:time-equal" operator (ge)   
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args20() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ge xs:time(\"23:59:59Z\")",
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
   * Test: op-time-equal2args-3                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args3() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") eq xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-4                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args4() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") eq xs:time(\"08:03:35Z\")",
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
   * Test: op-time-equal2args-5                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(upper bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args5() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") eq xs:time(\"23:59:59Z\")",
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
   * Test: op-time-equal2args-6                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args6() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ne xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-7                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(mid range)                             
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args7() {
    final XQuery query = new XQuery(
      "xs:time(\"08:03:35Z\") ne xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-8                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(upper bound)                           
   * $arg2 = xs:time(lower bound)                           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args8() {
    final XQuery query = new XQuery(
      "xs:time(\"23:59:59Z\") ne xs:time(\"00:00:00Z\")",
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
   * Test: op-time-equal2args-9                              
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:07 GMT-05:00 2005                
   * Purpose: Evaluates The "op:time-equal" operator        
   *  with the arguments set as follows:                    
   * $arg1 = xs:time(lower bound)                           
   * $arg2 = xs:time(mid range)                             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opTimeEqual2args9() {
    final XQuery query = new XQuery(
      "xs:time(\"00:00:00Z\") ne xs:time(\"08:03:35Z\")",
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
}

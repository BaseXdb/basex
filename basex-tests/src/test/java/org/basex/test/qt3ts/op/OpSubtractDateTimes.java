package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the subtract-dateTimes() function.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpSubtractDateTimes extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-dateTimesSubtract-1                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '-' between xs:date and xs:date that evaluates to zero. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimesSubtract1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-07-19T08:23:12.765\") - xs:dateTime(\"1999-07-19T08:23:12.765\") eq xs:dayTimeDuration(\"PT0S\")",
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
   *  Test: K-dateTimesSubtract-2                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '+' operator is not available between xs:dateTime and xs:dateTime. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimesSubtract2() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-10-12T08:01:23\") + xs:dateTime(\"1999-10-12T08:01:23\")",
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
   *  Test: K-dateTimesSubtract-3                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The 'div' operator is not available between xs:dateTime and xs:dateTime. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimesSubtract3() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-10-12T08:01:23\") div xs:dateTime(\"1999-10-12T08:01:23\")",
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
   *  Test: K-dateTimesSubtract-4                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '*' operator is not available between xs:dateTime and xs:dateTime. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimesSubtract4() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-10-12T08:01:23\") * xs:dateTime(\"1999-10-12T08:01:23\")",
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
   *  Test: K-dateTimesSubtract-5                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The 'mod' operator is not available between xs:dateTime and xs:dateTime. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimesSubtract5() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"1999-10-12T08:01:23\") mod xs:dateTime(\"1999-10-12T08:01:23\")",
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
   *  Test: K-dateTimesSubtract-6                           
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '-' operator is not available between xs:dayTimeDuration and xs:dateTime. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDateTimesSubtract6() {
    final XQuery query = new XQuery(
      "xs:dayTimeDuration(\"P3D\") - xs:dateTime(\"1999-08-12T08:01:23\")",
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
   *  test subtraction of large to dateTime .
   */
  @org.junit.Test
  public void cbclSubtractDateTimes001() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-25252734927766554-12-31T12:00:00\") - xs:dateTime(\"25252734927766554-12-31T12:00:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  test subtraction of large dateTimes .
   */
  @org.junit.Test
  public void cbclSubtractDateTimes002() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"-25252734927766554-12-31T12:00:00\") - xs:dateTime(\"25252734927766554-12-31T12:00:00+01:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FODT0001")
    );
  }

  /**
   *  test subtraction of large dateTimes .
   */
  @org.junit.Test
  public void cbclSubtractDateTimes003() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2008-12-31T12:00:00\") - xs:dateTime(\"2002-12-31T12:00:00+01:00\") + implicit-timezone()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P2192DT1H")
    );
  }

  /**
   *  test subtraction of large to dateTimes .
   */
  @org.junit.Test
  public void cbclSubtractDateTimes004() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2002-12-31T12:00:00+01:00\") - xs:dateTime(\"2008-12-31T12:00:00\") - implicit-timezone()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P2192DT1H")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dateTimes-yielding-DTD-1             
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD1() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2000-10-30T06:12:00-05:00\") - xs:dateTime(\"1999-11-28T09:00:00Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P337DT2H12M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dateTimes-yielding-DTD-10            
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator used  
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD10() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1985-07-05T07:07:07Z\") - xs:dateTime(\"1984-07-05T08:08:08Z\"))) or fn:string((xs:dateTime(\"1985-07-05T09:09:09Z\") - xs:dateTime(\"1984-07-05T10:10:10Z\")))",
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
   * Test: op-subtract-dateTimes-yielding-DTD-11            
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator used  
   * as part of a "div" expression.                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD11() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1985-07-05T07:07:07Z\") - xs:dateTime(\"1985-07-05T07:07:07Z\")) div xs:dayTimeDuration(\"P05DT08H11M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "0")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dateTimes-yielding-DTD-12            
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD12() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1980-03-02T11:11:11Z\") - xs:dateTime(\"1981-12-12T12:12:12Z\"))) and (fn:true())",
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
   * Test: op-subtract-dateTimes-yielding-DTD-13            
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD13() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1980-05-05T13:13:13Z\") - xs:dateTime(\"1979-10-05T14:14:14Z\"))) eq xs:string(xs:dayTimeDuration(\"P17DT10H02M\"))",
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
   * Test: op-subtract-dateTimes-yielding-DTD-14            
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD14() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1979-12-12T16:16:16Z\") - xs:dateTime(\"1978-12-12T17:17:17Z\"))) ne xs:string(xs:dayTimeDuration(\"P17DT10H02M\"))",
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
   * Test: op-subtract-dateTimes-yielding-DTD-15            
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD15() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1978-12-12T10:09:08Z\") - xs:dateTime(\"1977-12-12T09:08:07Z\"))) le xs:string(xs:dayTimeDuration(\"P17DT10H02M\"))",
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
   * Test: op-subtract-dateTimes-yielding-DTD-16             
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD16() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1977-12-12T01:02:02Z\") - xs:dateTime(\"1976-12-12T02:03:04Z\"))) ge xs:string(xs:dayTimeDuration(\"P18DT02H02M\"))",
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
   * Test: op-subtract-dateTimes-yielding-DTD-17            
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" function 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD17() {
    final XQuery query = new XQuery(
      "fn:string(xs:dateTime(\"2000-12-12T12:07:08Z\") - xs:dateTime(\"1999-12-12T13:08:09Z\")) and fn:false()",
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
   * Test: op-subtract-dateTimes-yielding-DTD-18           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator as 
   * part of a boolean expression (or operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD18() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1999-10-23T03:02:01Z\") - xs:dateTime(\"1998-09-09T04:04:05Z\"))) or fn:false()",
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
   * Test: op-subtract-dateTimes-yielding-DTD-19            
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator as 
   * part of a multiplication expression                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD19() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1999-10-23T01:01:01Z\") - xs:dateTime(\"1998-09-09T02:02:02Z\")) * xs:decimal(2.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P817DT21H57M58S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dateTimes-yielding-DTD-2             
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator 
   * for which the value uses a timezone of "+5:00"         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD2() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2000-12-12T09:08:07+05:00\") - xs:dateTime(\"1999-12-12T09:08:07+05:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P366D")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dateTimes-yielding-DTD-20            
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator as 
   * part of a addition expression.                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD20() {
    final XQuery query = new XQuery(
      "(xs:dateTime(\"1999-10-23T09:08:07Z\") - xs:dateTime(\"1998-09-09T04:03:02Z\")) + xs:dayTimeDuration(\"P17DT10H02M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P426DT15H7M5S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dateTimes-yielding-DTD-3             
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator, whose
   * value includes a timezone of "-6.00".                  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD3() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"2000-02-03T02:09:07-06:00\") - xs:dateTime(\"1998-02-03T02:09:07-06:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P730D")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dateTimes-yielding-DTD-4             
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator that  
   * return true and used together with fn:not.             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:dateTime(\"1998-09-12T11:12:12Z\") - xs:dateTime(\"1996-02-02T01:01:01Z\")))",
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
   * Test: op-subtract-dateTimes-yielding-DTD-5             
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:dateTime(\"1962-03-12T10:09:09Z\") - xs:dateTime(\"1961-02-01T20:10:10Z\")))",
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
   * Test: op-subtract-dateTimes-yielding-DTD-6             
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD6() {
    final XQuery query = new XQuery(
      "fn:number(xs:dateTime(\"1988-01-28T10:09:08Z\") - xs:dateTime(\"1987-01-01T01:01:02Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "NaN")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dateTimes-yielding-DTD-7             
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD7() {
    final XQuery query = new XQuery(
      "fn:string(xs:dateTime(\"1989-07-05T02:02:02Z\") - xs:dateTime(\"1988-01-28T03:03:03Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P523DT22H58M59S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dateTimes-yielding-DTD-8             
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD8() {
    final XQuery query = new XQuery(
      "xs:dateTime(\"0001-01-01T01:01:01Z\") - xs:dateTime(\"2005-07-06T12:12:12Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P732132DT11H11M11S")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dateTimes-yielding-DTD-9             
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dateTimes-yielding-DTD" operator used  
   * together with an "and" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDateTimesYieldingDTD9() {
    final XQuery query = new XQuery(
      "fn:string((xs:dateTime(\"1993-12-09T04:04:04Z\") - xs:dateTime(\"1992-12-09T05:05:05Z\"))) and fn:string((xs:dateTime(\"1993-12-09T01:01:01Z\") - xs:dateTime(\"1992-12-09T06:06:06Z\")))",
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

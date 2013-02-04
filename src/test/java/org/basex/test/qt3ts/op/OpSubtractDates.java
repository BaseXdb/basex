package org.basex.test.qt3ts.op;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the subtract-dates() function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpSubtractDates extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-DatesSubtract-1                               
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '-' between xs:date and xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDatesSubtract1() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-07-19\") - xs:date(\"1969-11-30\") eq xs:dayTimeDuration(\"P10823D\")",
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
   *  Test: K-DatesSubtract-2                               
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple testing involving operator '-' between xs:date and xs:date that evaluates to zero. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDatesSubtract2() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-07-19\") - xs:date(\"1999-07-19\") eq xs:dayTimeDuration(\"PT0S\")",
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
   *  Test: K-DatesSubtract-3                               
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '+' operator is not available between xs:date and xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDatesSubtract3() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-10-12\") + xs:date(\"1999-10-12\")",
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
   *  Test: K-DatesSubtract-4                               
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The 'div' operator is not available between xs:date and xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDatesSubtract4() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-10-12\") div xs:date(\"1999-10-12\")",
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
   *  Test: K-DatesSubtract-5                               
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The '*' operator is not available between xs:date and xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDatesSubtract5() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-10-12\") * xs:date(\"1999-10-12\")",
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
   *  Test: K-DatesSubtract-6                               
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: The 'mod' operator is not available between xs:date and xs:date. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kDatesSubtract6() {
    final XQuery query = new XQuery(
      "xs:date(\"1999-10-12\") mod xs:date(\"1999-10-12\")",
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
   *  test subtraction of large dates .
   */
  @org.junit.Test
  public void cbclSubtractDates001() {
    final XQuery query = new XQuery(
      "xs:date(\"-25252734927766554-12-31\") - xs:date(\"25252734927766554-12-31\")",
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
   *  test subtraction of large dates .
   */
  @org.junit.Test
  public void cbclSubtractDates002() {
    final XQuery query = new XQuery(
      "xs:date(\"-25252734927766554-12-31\") - xs:date(\"25252734927766554-12-31+01:00\")",
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
   *  test subtraction of large dates .
   */
  @org.junit.Test
  public void cbclSubtractDates003() {
    final XQuery query = new XQuery(
      "xs:date(\"2008-12-31\") - xs:date(\"2002-12-31+01:00\")",
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
   *  test subtraction of large dates .
   */
  @org.junit.Test
  public void cbclSubtractDates004() {
    final XQuery query = new XQuery(
      "xs:date(\"2002-12-31+01:00\") - xs:date(\"2008-12-31\")",
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
   * Test: op-subtract-dates-yielding-DTD-1                 
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator 
   * As per example 1 (for this function)of the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD1() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-10-30\") - xs:date(\"1999-11-28\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P337D")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dates-yielding-DTD-10                
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator used  
   * together with an "or" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD10() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1985-07-05Z\") - xs:date(\"1977-12-02Z\"))) or fn:string((xs:date(\"1985-07-05Z\") - xs:date(\"1960-11-07Z\")))",
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
   * Test: op-subtract-dates-yielding-DTD-11                
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator used  
   * as part of a "div" expression.                         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD11() {
    final XQuery query = new XQuery(
      "(xs:date(\"1978-12-12Z\") - xs:date(\"1978-12-12Z\")) div xs:dayTimeDuration(\"P17DT10H02M\")",
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
   * Test: op-subtract-dates-yielding-DTD-12                
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator used 
   * with a boolean expression and the "fn:true" function.   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD12() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1980-03-02Z\") - xs:date(\"2001-09-11Z\"))) and (fn:true())",
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
   * Test: op-subtract-dates-yielding-DTD-13                
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator used  
   * together with the numeric-equal-operator "eq".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD13() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1980-05-05Z\") - xs:date(\"1981-12-03Z\"))) eq xs:string(xs:dayTimeDuration(\"P17DT10H02M\"))",
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
   * Test: op-subtract-dates-yielding-DTD-14                
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator used  
   * together with the numeric-equal operator "ne".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD14() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1979-12-12Z\") - xs:date(\"1979-11-11Z\"))) ne xs:string(xs:dayTimeDuration(\"P17DT10H02M\"))",
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
   * Test: op-subtract-dates-yielding-DTD-15                
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator used  
   * together with the numeric-equal operator "le".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD15() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1978-12-12Z\") - xs:date(\"1977-03-12Z\"))) le xs:string(xs:dayTimeDuration(\"P17DT10H02M\"))",
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
   * Test: op-subtract-dates-yielding-DTD-16                
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator used  
   * together with the numeric-equal operator "ge".         
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD16() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1977-12-12Z\") - xs:date(\"1976-12-12Z\"))) ge xs:string(xs:dayTimeDuration(\"P17DT10H02M\"))",
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
   * Test: op-subtract-dates-yielding-DTD-17                
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator 
   * used as part of a boolean expression (and operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD17() {
    final XQuery query = new XQuery(
      "fn:string(xs:date(\"2000-12-12Z\") - xs:date(\"2000-11-11Z\")) and fn:false()",
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
   * Test: op-subtract-dates-yielding-DTD-18                
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator as 
   * part of a boolean expression (or operator) and the "fn:false" function. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD18() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1999-10-23Z\") - xs:date(\"1998-09-09Z\"))) or fn:false()",
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
   * Test: op-subtract-dates-yielding-DTD-19                
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator as 
   * part of a multiplication expression                     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD19() {
    final XQuery query = new XQuery(
      "(xs:date(\"1999-10-23Z\") - xs:date(\"1998-09-09Z\")) * xs:decimal(2.0)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P818D")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dates-yielding-DTD-2                 
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator 
   * as per example 2 (for this operator) from the F&O specs.
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD2() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-10-30+05:00\") - xs:date(\"1999-11-28Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P336DT19H")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dates-yielding-DTD-20                
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator as 
   * part of a addition expression                          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD20() {
    final XQuery query = new XQuery(
      "(xs:date(\"1999-10-23Z\") - xs:date(\"1998-09-09Z\")) + xs:dayTimeDuration(\"P17DT10H02M\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P426DT10H2M")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dates-yielding-DTD-3                 
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator as 
   * per example 3 (for this operator) from the F&O specs.  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD3() {
    final XQuery query = new XQuery(
      "xs:date(\"2000-10-15-05:00\") - xs:date(\"2000-10-10+02:00\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P5DT7H")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dates-yielding-DTD-4                 
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The string value of "subtract-dates-yielding-DTD" 
   *  operator that return true and used together with fn:not. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD4() {
    final XQuery query = new XQuery(
      "fn:not(fn:string(xs:date(\"1998-09-12Z\") - xs:date(\"1998-09-21Z\")))",
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
   * Test: op-subtract-dates-yielding-DTD-5                 
   * Written By: Carmelo Montanez                           
   * Date: July 3, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator that  
   * is used as an argument to the fn:boolean function.     
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD5() {
    final XQuery query = new XQuery(
      "fn:boolean(fn:string(xs:date(\"1962-03-12Z\") - xs:date(\"1962-03-12Z\")))",
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
   * Test: op-subtract-dates-yielding-DTD-6                 
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator that 
   * is used as an argument to the fn:number function.      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD6() {
    final XQuery query = new XQuery(
      "fn:number(xs:date(\"1988-01-28Z\") - xs:date(\"2001-03-02\"))",
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
   * Test: op-subtract-dates-yielding-DTD-7                 
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator used  
   * as an argument to the "fn:string" function).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD7() {
    final XQuery query = new XQuery(
      "fn:string(xs:date(\"1989-07-05Z\") - xs:date(\"1962-09-04Z\"))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "P9801D")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dates-yielding-DTD-8                 
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dayTimeDuration-from-date" operator that  
   * returns a negative value.                              
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD8() {
    final XQuery query = new XQuery(
      "xs:date(\"0001-01-01Z\") - xs:date(\"2005-07-06Z\")",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "-P732132D")
    );
  }

  /**
   * 
   * *******************************************************
   * Test: op-subtract-dates-yielding-DTD-9                 
   * Written By: Carmelo Montanez                           
   * Date: July 6, 2005                                     
   * Purpose: Evaluates The "subtract-dates-yielding-DTD" operator used  
   * together with an "and" expression.                      
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opSubtractDatesYieldingDTD9() {
    final XQuery query = new XQuery(
      "fn:string((xs:date(\"1993-12-09Z\") - xs:date(\"1992-10-02Z\"))) and fn:string((xs:date(\"1993-12-09Z\") - xs:date(\"1980-10-20Z\")))",
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

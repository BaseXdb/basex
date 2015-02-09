package org.basex.qt3ts.op;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the yearMonthDuration-greater-than() function.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class OpYearMonthDurationGreaterThan extends QT3TestSet {

  /**
   * 
   * *******************************************************
   *  Test: K-YearMonthDurationGT-1                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationGT1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") gt xs:yearMonthDuration(\"P1999Y9M\")",
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
   *  Test: K-YearMonthDurationGT-2                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:yearMonthDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationGT2() {
    final XQuery query = new XQuery(
      "not(xs:yearMonthDuration(\"P1999Y10M\") gt xs:yearMonthDuration(\"P1999Y10M\"))",
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
   *  Test: K-YearMonthDurationGT-3                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'gt' for xs:yearMonthDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationGT3() {
    final XQuery query = new XQuery(
      "not(xs:yearMonthDuration(\"P1999Y10M\") gt xs:yearMonthDuration(\"P1999Y11M\"))",
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
   *  Test: K-YearMonthDurationGT-4                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationGT4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y11M\") ge xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  Test: K-YearMonthDurationGT-5                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:yearMonthDuration. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationGT5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1999Y10M\") ge xs:yearMonthDuration(\"P1999Y10M\")",
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
   *  Test: K-YearMonthDurationGT-6                         
   *  Written by: Frans Englich                             
   *  Date: 2007-11-22T11:31:21+01:00                       
   *  Purpose: Simple test of 'ge' for xs:yearMonthDuration, evaluating to false. 
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void kYearMonthDurationGT6() {
    final XQuery query = new XQuery(
      "not(xs:yearMonthDuration(\"P1999Y9M\") ge xs:yearMonthDuration(\"P1999Y10M\"))",
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
   *  test comparison of xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclYearMonthDurationGreaterThan001() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer, $months as xs:integer ) { xs:yearMonthDuration(concat('P', $years, 'Y', $months, 'M')) };\n" +
      "        not(local:yearMonthDuration(1, 1) gt xs:yearMonthDuration(\"P0Y\"))",
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
   *  test comparison of xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclYearMonthDurationGreaterThan002() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($years as xs:integer, $months as xs:integer ) { xs:yearMonthDuration(concat('P', $years, 'Y', $months, 'M')) };\n" +
      "        not(local:yearMonthDuration(1, 1) ge xs:yearMonthDuration(\"P0Y\"))",
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
   *  test comparison of xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclYearMonthDurationGreaterThan003() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($yearMonthDuration as xs:yearMonthDuration, $null as xs:boolean) { if ($null) then () else $yearMonthDuration };\n" +
      "        exists(local:yearMonthDuration(xs:yearMonthDuration(\"P0Y\"), fn:true()) gt xs:yearMonthDuration(\"P0Y\"))",
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
   *  test comparison of xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclYearMonthDurationGreaterThan004() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($yearMonthDuration as xs:yearMonthDuration, $null as xs:boolean) { if ($null) then () else $yearMonthDuration };\n" +
      "        local:yearMonthDuration(xs:yearMonthDuration(\"P0Y\"), fn:false()) gt xs:yearMonthDuration(\"P0Y\")",
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
   *  test comparison of xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclYearMonthDurationGreaterThan005() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($yearMonthDuration as xs:yearMonthDuration, $null as xs:boolean) { if ($null) then () else $yearMonthDuration };\n" +
      "        exists(local:yearMonthDuration(xs:yearMonthDuration(\"P0Y\"), fn:true()) le xs:yearMonthDuration(\"P0Y\"))",
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
   *  test comparison of xs:yearMonthDuration .
   */
  @org.junit.Test
  public void cbclYearMonthDurationGreaterThan006() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:yearMonthDuration($yearMonthDuration as xs:yearMonthDuration, $null as xs:boolean) { if ($null) then () else $yearMonthDuration };\n" +
      "        local:yearMonthDuration(xs:yearMonthDuration(\"P0Y\"), fn:false()) le xs:yearMonthDuration(\"P0Y\")",
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
   * Test: op-yearMonthDuration-greater-than-10             
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function used  
   * together with "or" expression (ge operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan10() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") ge xs:yearMonthDuration(\"P09Y06M\")) or (xs:yearMonthDuration(\"P15Y01M\") ge xs:yearMonthDuration(\"P02Y04M\"))",
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
   * Test: op-yearMonthDuration-greater-than-11             
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function used  
   * together with "fn:true"/or expression (gt operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan11() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y02M\") gt xs:yearMonthDuration(\"P01Y10M\")) or (fn:true())",
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
   * Test: op-yearMonthDuration-greater-than-12             
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function used  
   * together with "fn:true"/or expression (ge operator).   
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan12() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y01M\") ge xs:yearMonthDuration(\"P09Y05M\")) or (fn:true())",
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
   * Test: op-yearMonthDuration-greater-than-13             
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function used  
   * together with "fn:false"/or expression (gt operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan13() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P30Y10M\") gt xs:yearMonthDuration(\"P01Y02M\")) or (fn:false())",
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
   * Test: op-yearMonthDuration-greater-than-14             
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function used 
   * together with "fn:false"/or expression (ge operator).  
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan14() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y05M\") ge xs:yearMonthDuration(\"P20Y10M\")) or (fn:false())",
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
   * Test: op-yearMonthDuration-greater-than-3              
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function that 
   * return true and used together with fn:not (gt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan3() {
    final XQuery query = new XQuery(
      "fn:not((xs:yearMonthDuration(\"P20Y123M\") gt xs:yearMonthDuration(\"P20Y11M\")))",
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
   * Test: op-yearMonthDuration-greater-than-4              
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function that 
   * return true and used together with fn:not (ge operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan4() {
    final XQuery query = new XQuery(
      "fn:not(xs:yearMonthDuration(\"P20Y10M\") ge xs:yearMonthDuration(\"P20Y10M\"))",
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
   * Test: op-yearMonthDuration-greater-than-5              
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function that 
   * return false and used together with fn:not (gt operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan5() {
    final XQuery query = new XQuery(
      "fn:not(xs:yearMonthDuration(\"P20Y09M\") gt xs:yearMonthDuration(\"P20Y10M\"))",
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
   * Test: op-yearMonthDuration-greater-than-6              
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function that 
   * return false and used together with fn:not(ge operator)
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan6() {
    final XQuery query = new XQuery(
      "fn:not(xs:yearMonthDuration(\"P10Y07M\") ge xs:yearMonthDuration(\"P10Y09M\"))",
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
   * Test: op-yearMonthDuration-greater-than-7              
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function used 
   * together with "and" expression (gt operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan7() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y09M\") gt xs:yearMonthDuration(\"P09Y10M\")) and (xs:yearMonthDuration(\"P10Y01M\") gt xs:yearMonthDuration(\"P08Y06M\"))",
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
   * Test: op-yearMonthDuration-greater-than-8              
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function used  
   * together with "and" expression (ge operator).          
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan8() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y09M\") ge xs:yearMonthDuration(\"P10Y01M\")) and (xs:yearMonthDuration(\"P02Y04M\") ge xs:yearMonthDuration(\"P09Y07M\"))",
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
   * Test: op-yearMonthDuration-greater-than-9               
   * Written By: Carmelo Montanez                           
   * Date: June 15, 2005                                    
   * Purpose: Evaluates The "yearMonthDuration-greater-than" function used
   * together with "or" expression (gt operator).           
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan9() {
    final XQuery query = new XQuery(
      "(xs:yearMonthDuration(\"P10Y08M\") gt xs:yearMonthDuration(\"P10Y07M\")) or (xs:yearMonthDuration(\"P10Y09M\") gt xs:yearMonthDuration(\"P10Y09M\"))",
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
   * Test: op-yearMonthDuration-greater-than2args-1          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan2args1() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") gt xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-greater-than2args-10         
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan2args10() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") le xs:yearMonthDuration(\"P2030Y12M\")",
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
   * Test: op-yearMonthDuration-greater-than2args-2          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(mid range)               
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan2args2() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") gt xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-greater-than2args-3          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(upper bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan2args3() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") gt xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-greater-than2args-4          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan2args4() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") gt xs:yearMonthDuration(\"P1000Y6M\")",
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
   * Test: op-yearMonthDuration-greater-than2args-5          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(upper bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan2args5() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") gt xs:yearMonthDuration(\"P2030Y12M\")",
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
   * Test: op-yearMonthDuration-greater-than2args-6          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan2args6() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") le xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-greater-than2args-7          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(mid range)               
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan2args7() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P1000Y6M\") le xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-greater-than2args-8          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(upper bound)             
   * $arg2 = xs:yearMonthDuration(lower bound)             
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan2args8() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P2030Y12M\") le xs:yearMonthDuration(\"P0Y0M\")",
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
   * Test: op-yearMonthDuration-greater-than2args-9          
   * Written By: Carmelo Montanez                            
   * Date: Tue Apr 12 16:29:06 GMT-05:00 2005                
   * Purpose: Evaluates The "op:yearMonthDuration-greater-than" operator
   *  with the arguments set as follows:                    
   * $arg1 = xs:yearMonthDuration(lower bound)             
   * $arg2 = xs:yearMonthDuration(mid range)               
   * *******************************************************
   * .
   */
  @org.junit.Test
  public void opYearMonthDurationGreaterThan2args9() {
    final XQuery query = new XQuery(
      "xs:yearMonthDuration(\"P0Y0M\") le xs:yearMonthDuration(\"P1000Y6M\")",
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

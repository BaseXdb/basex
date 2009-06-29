// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.xqj.testcases;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQSequenceType;

@SuppressWarnings("all")
public class XQSequenceTypeTest extends XQJTestCase {

  public void testGetItemType() throws XQException {
    XQPreparedExpression xqe = xqc.prepareExpression("1,2,3");
    XQSequenceType xqst = xqe.getStaticResultType();
    
    try {
      XQItemType xqit = xqst.getItemType();
      int kind = xqit.getItemKind();
      assertTrue("A-XQST-1.1: Successfully call getItemType()", kind == XQItemType.XQITEMKIND_ATOMIC ||
                                                                                       kind == XQItemType.XQITEMKIND_ITEM);    
    } catch (Exception e) {
      fail("A-XQST-1.1: getItemtype() failed with message: " + e.getMessage());
    }
  }
  
  public void testGetItemOccurrence() throws XQException {
    XQPreparedExpression xqe = xqc.prepareExpression("1,2,3");
    XQSequenceType xqst = xqe.getStaticResultType();
    
    try { 
      int occurence = xqst.getItemOccurrence();
      assertTrue("A-XQST-2.1: Successfully call getItemOccurrence()", occurence == XQSequenceType.OCC_ONE_OR_MORE ||
                                                                                       occurence == XQSequenceType.OCC_ZERO_OR_MORE);      
    } catch (Exception e) {
      fail("A-XQST-2.1: getItemOccurrence() failed with message: " + e.getMessage());
    }
  }  
  
  public void testToString() throws XQException {
    XQPreparedExpression xqe = xqc.prepareExpression("1,2,3");
    XQSequenceType xqst = xqe.getStaticResultType();
    
    try { 
      xqst.toString();    
    } catch (Exception e) {
      fail("A-XQST-3.1: toString() failed with message: " + e.getMessage());
    }
  }  
}

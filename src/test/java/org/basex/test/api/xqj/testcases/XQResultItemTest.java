// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQResultItemTest extends XQJTestCase {

  public void testGetConnection() throws XQException {
    XQExpression xqe;
    XQSequence xqs;
    XQItem xqi;
    XQResultItem xqri;
    XQConnection returned_xqc = null;

    xqe = xqc.createExpression();
    xqs = xqe.executeQuery("1,2,3,4");
    xqs.next();
    xqi = xqs.getItem();
    assertTrue("A-XQS-12.5: Item must be XQResultItem", xqi instanceof XQResultItem);
    xqri = (XQResultItem)xqi;
    xqri.close();
    try {
      xqri.getConnection();
      fail("A-XQRI-1.1: Getting the connection on a closed result item fails.");
    } catch (final XQException e) {
      // Expect an XQException
    }
    xqe.close();

    xqe = xqc.createExpression();
    xqs = xqe.executeQuery("1,2,3,4");
    xqs.next();
    xqi = xqs.getItem();
    assertTrue("A-XQS-12.5: Item must be XQResultItem", xqi instanceof XQResultItem);
    xqri = (XQResultItem)xqi;
    try {
      returned_xqc = xqri.getConnection();
    } catch (final XQException e) {
      fail("A-XQRI-1.2: getConnection() failed with message: " + e.getMessage());
    }
    assertSame("A-XQRI-1.2: Successfully get the connection of a result item.", xqc, returned_xqc);
    xqe.close();
  }
}

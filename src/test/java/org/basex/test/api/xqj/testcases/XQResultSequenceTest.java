// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQResultSequenceTest extends XQJTestCase {

  public void testGetConnection() throws XQException {
    XQExpression xqe;
    XQResultSequence xqs;
    XQConnection returned_xqc = null;

    xqe = xqc.createExpression();
    xqs = xqe.executeQuery("1,2,3,4");
    xqs.close();
    try {
      xqs.getConnection();
      fail("A-XQRS-1.1: Getting the connection on a closed result sequence fails.");
    } catch (final XQException e) {
      // Expect an XQException
    }
    xqe.close();

    xqe = xqc.createExpression();
    xqs = xqe.executeQuery("1,2,3,4");
    try {
      returned_xqc = xqs.getConnection();
    } catch (final XQException e) {
      fail("A-XQRS-1.2: getConnection() failed with message: " + e.getMessage());
    }
    assertSame("A-XQRS-1.2: Successfully get the connection of a result sequence.", xqc, returned_xqc);
    xqe.close();
  }
}

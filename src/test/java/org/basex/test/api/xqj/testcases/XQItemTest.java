// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQItemTest extends XQJTestCase {

  public void testClose() throws XQException {
    XQExpression xqe;
    XQSequence xqs;
    XQItem xqi;

    xqe = xqc.createExpression();
    xqs = xqe.executeQuery("'Hello world!'");
    xqs.next();
    xqi = xqs.getItem();
    try {
      xqi.close();
      xqi.close();
    } catch (final XQException e) {
      fail("A-XQE-2.1: closing item failed with message: " + e.getMessage());
    }
    xqe.close();
  }

  public void testIsClosed() throws XQException {
    XQExpression xqe;
    XQSequence xqs;
    XQItem xqi;

    xqe = xqc.createExpression();
    xqs = xqe.executeQuery("'Hello world!'");
    xqs.next();
    xqi = xqs.getItem();

    assertFalse("A-XQE-1.1: isClosed() on open item", xqi.isClosed());
    xqi.close();
    assertTrue("A-XQE-1.2: isClosed() on closed item", xqi.isClosed());

    xqe.close();
  }
}


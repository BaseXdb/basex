// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQSequenceTypeTest extends XQJTestCase {

  public void testGetItemType() throws XQException {
    final XQPreparedExpression xqe = xqc.prepareExpression("1,2,3");
    final XQSequenceType xqst = xqe.getStaticResultType();

    try {
      final XQItemType xqit = xqst.getItemType();
      final int kind = xqit.getItemKind();
      assertTrue("A-XQST-1.1: Successfully call getItemType()", kind == XQItemType.XQITEMKIND_ATOMIC ||
                                                                                       kind == XQItemType.XQITEMKIND_ITEM);
    } catch (final Exception e) {
      fail("A-XQST-1.1: getItemtype() failed with message: " + e.getMessage());
    }
  }

  public void testGetItemOccurrence() throws XQException {
    final XQPreparedExpression xqe = xqc.prepareExpression("1,2,3");
    final XQSequenceType xqst = xqe.getStaticResultType();

    try {
      final int occurence = xqst.getItemOccurrence();
      assertTrue("A-XQST-2.1: Successfully call getItemOccurrence()", occurence == XQSequenceType.OCC_ONE_OR_MORE ||
                                                                                       occurence == XQSequenceType.OCC_ZERO_OR_MORE);
    } catch (final Exception e) {
      fail("A-XQST-2.1: getItemOccurrence() failed with message: " + e.getMessage());
    }
  }

  public void testToString() throws XQException {
    final XQPreparedExpression xqe = xqc.prepareExpression("1,2,3");
    final XQSequenceType xqst = xqe.getStaticResultType();

    try {
      xqst.toString();
    } catch (final Exception e) {
      fail("A-XQST-3.1: toString() failed with message: " + e.getMessage());
    }
  }
}

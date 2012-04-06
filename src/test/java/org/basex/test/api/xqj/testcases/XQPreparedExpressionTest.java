// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import javax.xml.namespace.*;
import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQPreparedExpressionTest extends XQJTestCase {

  public void testCancel() throws XQException {
    XQPreparedExpression xqpe;

    xqpe = xqc.prepareExpression("'Hello world!'");
    try {
      xqpe.cancel();
    } catch (final XQException e) {
      fail("A-XQPE-1.1: cancellation of prepared expression failed with message: " + e.getMessage());
    }

    xqpe.close();
    try {
      xqpe.cancel();
      fail("A-XQPE-1.2: closed prepared expression supports cancel()");
    } catch (final XQException e) {
      // Expect an XQException
    }
  }

  public void testIsClosed() throws XQException {
    XQPreparedExpression xqpe;

    xqpe = xqc.prepareExpression("'Hello world!'");
    xqpe.executeQuery();

    assertFalse("A-XQPE-2.1: isClosed() on open prepared expression", xqpe.isClosed());
    xqpe.close();
    assertTrue("A-XQPE-2.2: isClosed() on closed prepared expressions", xqpe.isClosed());
  }

  public void testClose() throws XQException {
    XQPreparedExpression xqpe;
    XQSequence xqs;

    xqpe = xqc.prepareExpression("'Hello world!'");
    xqs = xqpe.executeQuery();
    try {
      xqpe.close();
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQPE-3.1: closing prepared expression failed with message: " + e.getMessage());
    }
    assertTrue("A-XQPE-3.2: Closing an expression, closes any result sequences obtained from this expression", xqs.isClosed());
  }

  public void testExecuteQuery() throws XQException {
    XQPreparedExpression xqpe;
    XQSequence xqs = null;

    xqpe = xqc.prepareExpression("'Hello world!'");
    xqpe.close();
    try {
      xqpe.executeQuery();
      fail("A-XQPE-8.1: closed prepared expression supports executeQuery()");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqpe = xqc.prepareExpression("'Hello world!'");
    try {
      xqs = xqpe.executeQuery();
    } catch (final XQException e) {
      fail("A-XQPE-8.2: executeQuery() failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQPE-8.2: executeQuery() failed", xqs);
    xqpe.close();
  }

  public void testGetAllExternalVariables() throws XQException {
    XQPreparedExpression xqpe;
    QName[] extVars = null;

    xqpe = xqc.prepareExpression("'Hello world!'");
    xqpe.close();
    try {
      xqpe.getAllExternalVariables();
      fail("A-XQPE-4.1: closed prepared expression supports getAllExternalVariables()");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqpe = xqc.prepareExpression("'Hello world!'");
    try {
      extVars = xqpe.getAllExternalVariables();
    } catch (final XQException e) {
      fail("A-XQPE-4.2: getAllExternalVariables on prepared expression without external variables failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQPE-4.2: getAllExternalVariables on prepared expression without external variables", extVars);
    assertEquals("A-XQPE-4.2: getAllExternalVariables on prepared expression without external variables", 0, extVars.length);
    xqpe.close();

    xqpe = xqc.prepareExpression("declare variable $v external; $v");
    try {
      extVars = xqpe.getAllExternalVariables();
    } catch (final XQException e) {
      fail("A-XQPE-4.3: getAllExternalVariables on prepared expression with external variables failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQPE-4.3: getAllExternalVariables on prepared expression with external variables", extVars);
    assertEquals("A-XQPE-4.3: getAllExternalVariables on prepared expression with external variables", 1, extVars.length);
    assertEquals("A-XQPE-4.3: getAllExternalVariables on prepared expression with external variables", "v", extVars[0].getLocalPart());
    assertEquals("A-XQPE-4.3: getAllExternalVariables on prepared expression with external variables", "", extVars[0].getNamespaceURI());
    xqpe.close();
  }

  public void testGetAllUnboundExternalVariables() throws XQException {
    XQPreparedExpression xqpe;
    QName[] extVars = null;

    xqpe = xqc.prepareExpression("'Hello world!'");
    xqpe.close();
    try {
      xqpe.getAllUnboundExternalVariables();
      fail("A-XQPE-9.1: closed prepared expression supports getAllUnboundExternalVariables()");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqpe = xqc.prepareExpression("declare variable $v external; $v");

    try {
      extVars = xqpe.getAllUnboundExternalVariables();
    } catch (final XQException e) {
      fail("A-XQPE-9.2: getAllUnboundExternalVariables on prepared expression with unbound variables failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQPE-9.2: getAllUnboundExternalVariables on prepared expression with unbound variables", extVars);
    assertEquals("A-XQPE-9.2: getAllUnboundExternalVariables on prepared expression with unbound variables", 1, extVars.length);
    assertEquals("A-XQPE-9.2: getAllUnboundExternalVariables on prepared expression with unbound variables", "v", extVars[0].getLocalPart());
    assertEquals("A-XQPE-9.2: getAllUnboundExternalVariables on prepared expression with unbound variables", "", extVars[0].getNamespaceURI());
    xqpe.bindString(new QName("v"), "Hello world!", null);
    try {
      extVars = xqpe.getAllUnboundExternalVariables();
    } catch (final XQException e) {
      fail("A-XQPE-9.3: getAllUnboundExternalVariables on prepared expression without unbound variables failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQPE-9.3: getAllUnboundExternalVariables on prepared expression without unbound variables", extVars);
    assertEquals("A-XQPE-9.3: getAllUnboundExternalVariables on prepared expression without unbound variables", 0, extVars.length);

    xqpe.close();
  }

  public void testGetStaticResultType() throws XQException {
    XQPreparedExpression xqpe;
    XQSequenceType xqtype = null;

    xqpe = xqc.prepareExpression("'Hello world!'");
    xqpe.close();
    try {
      xqpe.getStaticResultType();
      fail("A-XQPE-5.1: closed prepared expression supports getStaticResultType()");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqpe = xqc.prepareExpression("declare variable $v external; $v");
    try {
      xqtype = xqpe.getStaticResultType();
    } catch (final XQException e) {
      fail("A-XQPE-5.2: getStaticResultType() failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQPE-5.2: getStaticResultType() failed", xqtype);
    assertEquals("A-XQPE-5.2: getStaticResultType() failed", XQItemType.OCC_ZERO_OR_MORE, xqtype.getItemOccurrence());
    assertEquals("A-XQPE-5.2: getStaticResultType() failed", XQItemType.XQITEMKIND_ITEM, xqtype.getItemType().getItemKind());
    xqpe.close();
  }

  public void testGetStaticVariableType() throws XQException {
    XQPreparedExpression xqpe;
    XQSequenceType xqtype = null;

    xqpe = xqc.prepareExpression("declare variable $v external; $v");
    xqpe.close();
    try {
      xqpe.getStaticVariableType(new QName("v"));
      fail("A-XQPE-6.1: closed prepared expression supports getStaticVariableType()");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqpe = xqc.prepareExpression("declare variable $v external; $v");
    try {
      xqpe.getStaticVariableType(new QName("foo"));
      fail("A-XQPE-6.2: getStaticVariableType() specifying undeclared variable");
    } catch (final XQException e) {
      // Expect an XQException
    }
    xqpe.close();

    xqpe = xqc.prepareExpression("declare variable $v external; $v");
    try {
      xqpe.getStaticVariableType(null);
      fail("A-XQPE-6.3: getStaticVariableType() specifying null");
    } catch (final XQException e) {
      // Expect an XQException
    }
    xqpe.close();

    xqpe = xqc.prepareExpression("declare variable $v external; $v");
    try {
      xqtype = xqpe.getStaticVariableType(new QName("v"));
    } catch (final XQException e) {
      fail("A-XQPE-6.4: getStaticVariableType() failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQPE-6.4: getStaticVariableType() failed", xqtype);
    assertEquals("A-XQPE-6.4: getStaticResultType() failed", XQItemType.OCC_ZERO_OR_MORE, xqtype.getItemOccurrence());
    assertEquals("A-XQPE-6.4: getStaticResultType() failed", XQItemType.XQITEMKIND_ITEM, xqtype.getItemType().getItemKind());
    xqpe.close();
  }

  public void testGetStaticContext() throws XQException {
    XQPreparedExpression xqpe;
    XQStaticContext xqsc = null;

    xqpe = xqc.prepareExpression("'Hello world!'");
    xqpe.close();
    try {
      xqpe.getStaticContext();
      fail("A-XQPE-7.1: closed prepared expression supports getStaticContext()");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqpe = xqc.prepareExpression("'Hello world!'");
    try {
      xqsc = xqpe.getStaticContext();
    } catch (final XQException e) {
      fail("A-XQPE-7.2: getting static context failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQPE-7.2: getting static context failed", xqsc);
    xqpe.close();
  }
}
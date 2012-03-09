// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQExpressionTest extends XQJTestCase {

  public void testCancel() throws XQException {
    XQExpression xqe;

    xqe = xqc.createExpression();
    try {
      xqe.cancel();
    } catch (final XQException e) {
      fail("A-XQE-1.1: cancellation of expression failed with message: " + e.getMessage());
    }

    xqe.close();
    try {
      xqe.cancel();
      fail("A-XQE-1.2: closed expression supports cancel()");
    } catch (final XQException e) {
      // Expect an XQException
    }
  }

  public void testIsClosed() throws XQException {
    XQExpression xqe;

    xqe = xqc.createExpression();
    xqe.executeQuery("'Hello world!'");

    assertFalse("A-XQE-2.1: isClosed() on open expression", xqe.isClosed());
    xqe.close();
    assertTrue("A-XQE-2.2: isClosed() on closed expressions", xqe.isClosed());
  }

  public void testClose() throws XQException {
    XQExpression xqe;
    XQSequence xqs;

    xqe = xqc.createExpression();
    xqs = xqe.executeQuery("'Hello world!'");
    try {
      xqe.close();
      xqe.close();
    } catch (final XQException e) {
      fail("A-XQE-3.1: closing expression failed with message: " + e.getMessage());
    }

    assertTrue("A-XQE-3.2: Closing an expression, closes any result sequences obtained from this expression", xqs.isClosed());
  }

  public void testExecuteCommand_String() throws XQException {
    // optional feature, not tested.
  }

  public void testExecuteCommand_Reader() throws XQException {
    // optional feature, not tested.
  }

  public void testExecuteQuery_String() throws XQException {
    XQExpression xqe;

    xqe = xqc.createExpression();
    try {
      xqe.executeQuery("'Hello world!");
      fail("A-XQE-5.1: executeQuery() should fail");
    } catch (final XQException e) {
      // Expect an XQException
    }
    xqe.close();

    xqe = xqc.createExpression();
    xqe.close();
    try {
      xqe.executeQuery("'Hello world!'");
      fail("A-XQE-5.2: closed expression supports executeQuery()");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqe = xqc.createExpression();
    xqe.close();
    try {
      xqe.executeQuery((String)null);
      fail("A-XQE-5.3: expression supports executeQuery() with null argument");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQE-5.3: executeQuery() with null argument doesn't throw XQException");
    }

    xqe = xqc.createExpression();
    try {
      xqe.executeQuery("'Hello world!'");
    } catch (final XQException e) {
      fail("A-XQE-5.4: executing expression failed with message: " + e.getMessage());
    }
    xqe.close();
  }

  public void testExecuteQuery_Reader() throws XQException {
    XQExpression xqe;

    xqe = xqc.createExpression();
    try {
      xqe.executeQuery(new StringReader("'Hello world!"));
      fail("A-XQE-5.1: executeQuery() should fail");
    } catch (final XQException e) {
      // Expect an XQException
    }
    xqe.close();

    xqe = xqc.createExpression();
    xqe.close();
    try {
      xqe.executeQuery(new StringReader("'Hello world!'"));
      fail("A-XQE-5.2: closed expression supports executeQuery()");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqe = xqc.createExpression();
    xqe.close();
    try {
      xqe.executeQuery((Reader)null);
      fail("A-XQE-5.3: expression supports executeQuery() with null argument");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQE-5.3: expression supports executeQuery() with null argument doesn't throw XQException");
    }

    xqe = xqc.createExpression();
    try {
      xqe.executeQuery(new StringReader("'Hello world!'"));
    } catch (final XQException e) {
      fail("A-XQE-5.4: executing expression failed with message: " + e.getMessage());
    }
    xqe.close();
  }

  public void testExecuteQuery_InputStream() throws XQException, UnsupportedEncodingException {
    XQExpression xqe;

    xqe = xqc.createExpression();
    try {
      xqe.executeQuery(new ByteArrayInputStream("'Hello world!".getBytes("UTF-8")));
      fail("A-XQE-5.1: executeQuery() should fail");
    } catch (final XQException e) {
      // Expect an XQException
    }
    xqe.close();

    xqe = xqc.createExpression();
    xqe.close();
    try {
      xqe.executeQuery(new ByteArrayInputStream("'Hello world!'".getBytes("UTF-8")));
      fail("A-XQE-5.2: closed expression supports executeQuery()");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqe = xqc.createExpression();
    xqe.close();
    try {
      xqe.executeQuery((InputStream)null);
      fail("A-XQE-5.3: expression supports executeQuery() with null argument");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQE-5.3: expression supports executeQuery() with null argument doesn't throw XQException");
    }

    xqe = xqc.createExpression();
    try {
      xqe.executeQuery(new ByteArrayInputStream("'Hello world!'".getBytes("UTF-8")));
    } catch (final XQException e) {
      fail("A-XQE-5.4: executing expression failed with message: " + e.getMessage());
    }
    xqe.close();
  }

  public void testGetStaticContext() throws XQException {
    XQExpression xqe;
    XQStaticContext xqsc = null;

    xqe = xqc.createExpression();
    xqe.close();
    try {
      xqe.getStaticContext();
      fail("A-XQE-6.1: closed expression supports getStaticContext()");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqe = xqc.createExpression();
    try {
      xqsc = xqe.getStaticContext();
    } catch (final XQException e) {
      fail("A-XQE-6.2: getting static context failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQPE-6.2: getting static context failed", xqsc);
    xqe.close();
  }
}
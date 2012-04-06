// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import java.io.*;

import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQConnectionTest extends XQJTestCase {

  public void testClose() throws XQException {
    XQConnection my_xqc;
    XQExpression xqe;
    XQSequence xqs;

    my_xqc = xqds.getConnection();

    xqe = my_xqc.createExpression();
    xqs = xqe.executeQuery("1,2,3");

    try {
      my_xqc.close();
      my_xqc.close();
    } catch (final XQException e) {
      fail("A-XQC-1.1: closing connection failed with message: " + e.getMessage());
    }

    assertTrue("A-XQC-1.2: closing a connection, closes dependent objects", xqe.isClosed());
    assertTrue("A-XQC-1.2: closing a connection, closes dependent objects", xqs.isClosed());
  }

  public void testSetAutoCommit() throws XQException {
    // only test setAutoCommit() if transactions are supported.
    if (xqc.getMetaData().isTransactionSupported()) {
      XQConnection my_xqc;
      my_xqc = xqds.getConnection();
      my_xqc.close();

      try {
        my_xqc.setAutoCommit(true);
        fail("A-XQC-2.1: closed connection supports setAutoCommit()");
      } catch (final XQException e) {
        // Expect an XQException
      }

      try {
        xqc.setAutoCommit(false);
        xqc.setAutoCommit(true);
      } catch (final XQException e) {
        fail("A-XQC-2.2: changing auto commit mode failed with message: " + e.getMessage());
      }
    }
  }

  public void testGetAutoCommit() throws XQException {
    // only test getAutoCommit() if transactions are supported.
    if (xqc.getMetaData().isTransactionSupported()) {
      XQConnection my_xqc;
      my_xqc = xqds.getConnection();
      my_xqc.close();

      try {
        my_xqc.getAutoCommit();
        fail("A-XQC-3.1: closed connection supports getAutoCommit()");
      } catch (final XQException e) {
        // Expect an XQException
      }

      boolean mode = false;
      try {
        mode = xqc.getAutoCommit();
      } catch (final XQException e) {
        fail("A-XQC-3.2: getAutoCommit() failed with message: " + e.getMessage());
      }
      assertTrue("A-XQC-3.2: The default auto commit mode is true.", mode);

      mode = true;
      try {
        xqc.setAutoCommit(false);
        mode = xqc.getAutoCommit();
      } catch (final XQException e) {
        fail("A-XQC-3.3: Retrieve the auto commit mode failed with message: " + e.getMessage());
      }
      assertFalse("A-XQC-3.3: Successfully retrieve the auto commit mode.", mode);
    }
  }

  public void testCommit() throws XQException {
    // only test commit() if transactions are supported.
    if (xqc.getMetaData().isTransactionSupported()) {
      XQConnection my_xqc;
      my_xqc = xqds.getConnection();
      my_xqc.close();

      try {
        my_xqc.commit();
        fail("A-XQC-4.1: closed connection supports commit()");
      } catch (final XQException e) {
        // Expect an XQException
      }

      try {
        xqc.commit();
        fail("A-XQC-4.2: commit() on a connection in auto commit mode must fail");
      } catch (final XQException e) {
        // Expect an XQException
      }

      try {
        xqc.setAutoCommit(false);
        xqc.commit();
      } catch (final XQException e) {
        fail("A-XQC-4.3: commit() failed with message: " + e.getMessage());
      }
    }
  }

  public void testCreateExpression() throws XQException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    my_xqc.close();

    try {
      my_xqc.createExpression();
      fail("A-XQC-5.1: closed connection supports creating expressions");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQExpression xqe;

    try {
      xqe = xqc.createExpression();
      xqe.close();
    } catch (final XQException e) {
      fail("A-XQC-5.2: creating an expression failed with message: " + e.getMessage());
    }

    try {
      final XQStaticContext xqsc = xqc.getStaticContext();
      xqsc.declareNamespace("foo", "http://www.foo.com");
      xqc.setStaticContext(xqsc);
      xqe = xqc.createExpression();
      xqe.executeQuery("<foo:e/>");
      xqe.close();
    } catch (final XQException e) {
      fail("A-XQC-5.3: Failed to copy the default static context with message: " + e.getMessage());
    }
  }

  public void testCreateExpression_XQStaticContext() throws XQException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    final XQStaticContext my_xqsc = my_xqc.getStaticContext();
    my_xqc.close();

    try {
      my_xqc.createExpression(my_xqsc);
      fail("A-XQC-5.1: closed connection supports creating expressions");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createExpression(null);
      fail("A-XQC-5.5: createExpression() with a null XQStaticContext must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    final XQStaticContext xqsc = xqc.getStaticContext();
    XQExpression xqe;

    try {
     xqe = xqc.createExpression(xqsc);
     xqe.close();
    } catch (final XQException e) {
      fail("A-XQC-5.2: creating an expression failed with message: " + e.getMessage());
    }

    try {
      xqsc.declareNamespace("foo", "http://www.foo.com");
      xqe = xqc.createExpression(xqsc);
      xqe.executeQuery("<foo:e/>");
      xqe.close();
    } catch (final XQException e) {
      fail("A-XQC-5.4: Properties of the explicitly specified static context are used- failed with message: " + e.getMessage());
    }
  }

  public void testGetMetaData() throws XQException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    my_xqc.close();

    try {
      my_xqc.getMetaData();
      fail("A-XQC-6.1: closed connection supports getting meta data");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
     xqc.getMetaData();
    } catch (final XQException e) {
      fail("A-XQC-6.2: getting the meta data failed with message: " + e.getMessage());
    }
  }

  public void testIsClosed() throws XQException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();

    assertFalse("A-XQC-7.1: isClosed() on open connection", my_xqc.isClosed());
    my_xqc.close();
    assertTrue("A-XQC-7.2: isClosed() on closed connection", my_xqc.isClosed());
  }

  public void testPrepareExpression() throws XQException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    my_xqc.close();

    try {
      my_xqc.prepareExpression("'Hello world!'");
      fail("A-XQC-8.1: closed connection supports preparing expressions");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQPreparedExpression xqpe;

    try {
      xqpe = xqc.prepareExpression("'Hello world!");
      xqpe.close();
      fail("A-XQC-8.7: prepareExpression() with invalid XQuery expression must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression((String)null);
      xqpe.close();
      fail("A-XQC-8.6: prepareExpression() with a null xquery argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression("'Hello world!'");
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.2: preparing an expression failed with message: " + e.getMessage());
    }

    try {
      final XQStaticContext xqsc = xqc.getStaticContext();
      xqsc.declareNamespace("foo", "http://www.foo.com");
      xqc.setStaticContext(xqsc);
      xqpe = xqc.prepareExpression("<foo:e/>");
      xqpe.executeQuery();
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.3: Failed to copy the default static context with message: " + e.getMessage());
    }
  }

  public void testPrepareExpression_XQStaticContext() throws XQException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    final XQStaticContext my_xqsc = my_xqc.getStaticContext();
    my_xqc.close();

    try {
      my_xqc.prepareExpression("'Hello world!'", my_xqsc);
      fail("A-XQC-8.1: closed connection supports preparing expressions");
    } catch (final XQException e) {
      // Expect an XQException
    }

    final XQStaticContext xqsc = xqc.getStaticContext();
    XQPreparedExpression xqpe;

    try {
      xqpe = xqc.prepareExpression("'Hello world!", xqsc);
      xqpe.close();
      fail("A-XQC-8.7: prepareExpression() with invalid XQuery expression must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression("'Hello world!'", null);
      xqpe.close();
      fail("A-XQC-8.5: prepareExpression() with a null XQStaticContext must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression((String)null, xqsc);
      xqpe.close();
      fail("A-XQC-8.6: prepareExpression() with a null xquery argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression("'Hello world!'", xqsc);
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.2: preparing an expression failed with message: " + e.getMessage());
    }

    try {
      xqsc.declareNamespace("foo", "http://www.foo.com");
      xqpe = xqc.prepareExpression("<foo:e/>", xqsc);
      xqpe.executeQuery();
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.4: Properties of the explicitly specified static context are used - failed with message: " + e.getMessage());
    }
  }

  public void testPrepareExpression_Reader() throws XQException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    my_xqc.close();

    try {
      my_xqc.prepareExpression(new StringReader("'Hello world!'"));
      fail("A-XQC-8.1: closed connection supports preparing expressions");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQPreparedExpression xqpe;

    try {
      xqpe = xqc.prepareExpression(new StringReader("'Hello world!"));
      xqpe.close();
      fail("A-XQC-8.7: prepareExpression() with invalid XQuery expression must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression((Reader)null);
      xqpe.close();
      fail("A-XQC-8.6: prepareExpression() with a null xquery argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression(new StringReader("'Hello world!'"));
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.2: preparing an expression failed with message: " + e.getMessage());
    }

    try {
      final XQStaticContext xqsc = xqc.getStaticContext();
      xqsc.declareNamespace("foo", "http://www.foo.com");
      xqc.setStaticContext(xqsc);
      xqpe = xqc.prepareExpression(new StringReader("<foo:e/>"));
      xqpe.executeQuery();
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.3: Failed to copy the default static context with message: " + e.getMessage());
    }
  }

  public void testPrepareExpression_Reader_XQStaticContext() throws XQException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    final XQStaticContext my_xqsc = my_xqc.getStaticContext();
    my_xqc.close();

    try {
      my_xqc.prepareExpression(new StringReader("'Hello world!'"), my_xqsc);
      fail("A-XQC-8.1: closed connection supports preparing expressions");
    } catch (final XQException e) {
      // Expect an XQException
    }

    final XQStaticContext xqsc = xqc.getStaticContext();
    XQPreparedExpression xqpe;

    try {
      xqpe = xqc.prepareExpression(new StringReader("'Hello world!"), xqsc);
      xqpe.close();
      fail("A-XQC-8.7: prepareExpression() with invalid XQuery expression must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression(new StringReader("'Hello world!'"), null);
      xqpe.close();
      fail("A-XQC-8.5: prepareExpression() with a null XQStaticContext must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression((Reader)null, xqsc);
      xqpe.close();
      fail("A-XQC-8.6: prepareExpression() with a null xquery argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression(new StringReader("'Hello world!'"), xqsc);
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.2: preparing an expression failed with message: " + e.getMessage());
    }

    try {
      xqsc.declareNamespace("foo", "http://www.foo.com");
      xqpe = xqc.prepareExpression(new StringReader("<foo:e/>"), xqsc);
      xqpe.executeQuery();
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.4: Properties of the explicitly specified static context are used - failed with message: " + e.getMessage());
    }
  }

  public void testPrepareExpression_InputStream() throws XQException, UnsupportedEncodingException{

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    my_xqc.close();

    try {
      my_xqc.prepareExpression(new ByteArrayInputStream("'Hello world!'".getBytes("UTF-8")));
      fail("A-XQC-8.1: closed connection supports preparing expressions");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQPreparedExpression xqpe;

    try {
      xqpe = xqc.prepareExpression(new ByteArrayInputStream("'Hello world!".getBytes("UTF-8")));
      xqpe.close();
      fail("A-XQC-8.7: prepareExpression() with invalid XQuery expression must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression((InputStream)null);
      xqpe.close();
      fail("A-XQC-8.6: prepareExpression() with a null xquery argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression(new ByteArrayInputStream("'Hello world!'".getBytes("UTF-8")));
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.2: preparing an expression failed with message: " + e.getMessage());
    }

    try {
      final XQStaticContext xqsc = xqc.getStaticContext();
      xqsc.declareNamespace("foo", "http://www.foo.com");
      xqc.setStaticContext(xqsc);
      xqpe = xqc.prepareExpression(new ByteArrayInputStream("<foo:e/>".getBytes("UTF-8")));
      xqpe.executeQuery();
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.3: Failed to copy the default static context with message: " + e.getMessage());
    }
  }

  public void testPrepareExpression_InputStream_XQStaticContext() throws XQException, UnsupportedEncodingException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    final XQStaticContext my_xqsc = my_xqc.getStaticContext();
    my_xqc.close();

    try {
      my_xqc.prepareExpression(new ByteArrayInputStream("'Hello world!'".getBytes("UTF-8")), my_xqsc);
      fail("A-XQC-8.1: closed connection supports preparing expressions");
    } catch (final XQException e) {
      // Expect an XQException
    }

    final XQStaticContext xqsc = xqc.getStaticContext();
    XQPreparedExpression xqpe;

    try {
      xqpe = xqc.prepareExpression(new ByteArrayInputStream("'Hello world!".getBytes("UTF-8")), xqsc);
      xqpe.close();
      fail("A-XQC-8.7: prepareExpression() with invalid XQuery expression must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression(new ByteArrayInputStream("'Hello world!'".getBytes("UTF-8")), null);
      xqpe.close();
      fail("A-XQC-8.5: prepareExpression() with a null XQStaticContext must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression((InputStream)null, xqsc);
      xqpe.close();
      fail("A-XQC-8.6: prepareExpression() with a null xquery argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqpe = xqc.prepareExpression(new ByteArrayInputStream("'Hello world!'".getBytes("UTF-8")), xqsc);
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.2: preparing an expression failed with message: " + e.getMessage());
    }

    try {
      xqsc.declareNamespace("foo", "http://www.foo.com");
      xqpe = xqc.prepareExpression(new ByteArrayInputStream("<foo:e/>".getBytes("UTF-8")), xqsc);
      xqpe.executeQuery();
      xqpe.close();
    } catch (final XQException e) {
      fail("A-XQC-8.4: Properties of the explicitly specified static context are used - failed with message: " + e.getMessage());
    }
  }

  public void testRollback() throws XQException {
    // only test commit() if transactions are supported.
    if (xqc.getMetaData().isTransactionSupported()) {
      XQConnection my_xqc;
      my_xqc = xqds.getConnection();
      my_xqc.close();

      try {
        my_xqc.rollback();
        fail("A-XQC-9.1: closed connection supports rollback()");
      } catch (final XQException e) {
        // Expect an XQException
      }

      try {
        xqc.rollback();
        fail("A-XQC-9.2: rollback() on a connection in auto commit mode must fail");
      } catch (final XQException e) {
        // Expect an XQException
      }

      try {
        xqc.setAutoCommit(false);
        xqc.rollback();
      } catch (final XQException e) {
        fail("A-XQC-9.3: rollback() failed with message: " + e.getMessage());
      }
    }
  }

  public void testGetStaticContext() throws XQException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    my_xqc.close();

    try {
      my_xqc.getStaticContext();
      fail("A-XQC-10.1: closed connection supports getting the static context");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.getStaticContext();
    } catch (final XQException e) {
      fail("A-XQC-10.2: getting the static context failed with message: " + e.getMessage());
    }
  }

  public void testSetStaticContext() throws XQException {

    XQConnection my_xqc;
    my_xqc = xqds.getConnection();
    final XQStaticContext my_xqsc = my_xqc.getStaticContext();
    my_xqc.close();

    try {
      my_xqc.setStaticContext(my_xqsc);
      fail("A-XQC-11.1: closed connection supports setting the static context");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.setStaticContext(null);
      fail("A-XQC-11.2: setStaticContext() with a null argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      final XQStaticContext xqsc = xqc.getStaticContext();
      xqc.setStaticContext(xqsc);
    } catch (final XQException e) {
      fail("A-XQC-11.3: getting the static context failed with message: " + e.getMessage());
    }
  }

}

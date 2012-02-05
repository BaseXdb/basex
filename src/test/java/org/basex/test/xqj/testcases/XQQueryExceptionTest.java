// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.xqj.testcases;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQQueryException;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQStackTraceElement;

@SuppressWarnings("all")
public class XQQueryExceptionTest extends XQJTestCase {
  public void testConstructor1() {
    try {
      final XQQueryException ex = new XQQueryException("Hello world!");
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", "Hello world!", ex.getMessage());
    } catch (final Exception e) {
      fail("A-XQQEX-1.1: Creating XQQueryException faild with message: " + e.getMessage());
    }
  }

  public void testConstructor2() {
    try {
      final XQQueryException ex = new XQQueryException("Hello world!", new QName("foo"));
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", "Hello world!", ex.getMessage());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", new QName("foo"), ex.getErrorCode());
    } catch (final Exception e) {
      fail("A-XQQEX-1.1: Creating XQQueryException faild with message: " + e.getMessage());
    }
  }

  public void testConstructor5() {
    try {
      final XQQueryException ex = new XQQueryException("Hello world!", new QName("foo"), 7, 8, 56);
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", "Hello world!", ex.getMessage());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", new QName("foo"), ex.getErrorCode());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", 7, ex.getLineNumber());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", 8, ex.getColumnNumber());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", 56, ex.getPosition());
    } catch (final Exception e) {
      fail("A-XQQEX-1.1: Creating XQQueryException faild with message: " + e.getMessage());
    }
  }

  public void testConstructor6() {
    try {
      final XQQueryException ex = new XQQueryException("Hello world!", "VendorCode", new QName("foo"), 7, 8, 56);
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", "Hello world!", ex.getMessage());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", "VendorCode", ex.getVendorCode());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", new QName("foo"), ex.getErrorCode());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", 7, ex.getLineNumber());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", 8, ex.getColumnNumber());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", 56, ex.getPosition());
    } catch (final Exception e) {
      fail("A-XQQEX-1.1: Creating XQQueryException faild with message: " + e.getMessage());
    }
  }

  public void testConstructor9() throws XQException {
    final XQExpression xqe = xqc.createExpression();
    final XQSequence xqs = xqe.executeQuery("1,2");

    try {
      final XQQueryException ex = new XQQueryException("Hello world!", "VendorCode", new QName("foo"), 7, 8, 56,
                                                 "moduleuri", xqs, null);
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", "Hello world!", ex.getMessage());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", "VendorCode", ex.getVendorCode());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", new QName("foo"), ex.getErrorCode());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", 7, ex.getLineNumber());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", 8, ex.getColumnNumber());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", 56, ex.getPosition());
      assertEquals("A-XQQEX-1.1: XQQueryException successfully created", "moduleuri", ex.getModuleURI());
      assertSame("A-XQQEX-1.1: XQQueryException successfully created", xqs, ex.getErrorObject());
      assertNull("A-XQQEX-1.1: XQQueryException successfully created", ex.getQueryStackTrace());
    } catch (final Exception e) {
      fail("A-XQQEX-1.1: Creating XQQueryException faild with message: " + e.getMessage());
    }

    xqs.close();
  }

  public void testGetErrorCode() {
    try {
      final XQQueryException ex = new XQQueryException("Hello world!", new QName("foo"));
      assertEquals("A-XQQEX-2.1: Retrieve error code from an XQQueryException", new QName("foo"), ex.getErrorCode());
    } catch (final Exception e) {
      fail("A-XQQEX-2.1: Retrieve error code from an XQQueryException failed with message: " + e.getMessage());
    }

    try {
      final XQQueryException ex = new XQQueryException("Hello world!");
      assertNull("A-XQQEX-2.2: Error code is null when not available", ex.getErrorCode());
    } catch (final Exception e) {
      fail("A-XQQEX-2.2: Retrieve error code from an XQQueryException failed with message: " + e.getMessage());
    }
  }

  public void testGetErrorObject() throws XQException {
    final XQExpression xqe = xqc.createExpression();
    final XQSequence xqs = xqe.executeQuery("1,2");

    try {
      final XQQueryException ex = new XQQueryException("Hello world!", "VendorCode", new QName("foo"), 7, 8, 56,
                                                 "moduleuri", xqs, null);
      final XQSequence errorObject = ex.getErrorObject();
      errorObject.next();
      assertEquals("A-XQQEX-3.1: Retrieve error object from an XQQueryException", 1, errorObject.getInt());
    } catch (final Exception e) {
      fail("A-XQQEX-3.1: Retrieve error object from an XQQueryException failed with message: " + e.getMessage());
    }

    try {
      final XQQueryException ex = new XQQueryException("Hello world!");
      assertNull("A-XQQEX-3.2: Error object is null when not available", ex.getErrorObject());
    } catch (final Exception e) {
      fail("A-XQQEX-3.2: Retrieve error object from an XQQueryException failed with message: " + e.getMessage());
    }

    xqe.close();
  }

  public void testGetPosition() {
    try {
      final XQQueryException ex = new XQQueryException("Hello world!", "VendorCode", new QName("foo"), 7, 8, 56);
      assertEquals("A-XQQEX-4.1: Retrieve position from an XQQueryException", 56, ex.getPosition());
    } catch (final Exception e) {
      fail("A-XQQEX-4.1: Retrieve position from an XQQueryException failed with message: " + e.getMessage());
    }

    try {
      final XQQueryException ex = new XQQueryException("Hello world!");
      assertEquals("A-XQQEX-4.2: Position is -1 when not available", -1, ex.getPosition());
    } catch (final Exception e) {
      fail("A-XQQEX-4.2: Retrieve position from an XQQueryException failed with message: " + e.getMessage());
    }
  }

  public void testGetQueryStackTrace() {
    try {
      final XQQueryException ex = new XQQueryException("Hello world!", "VendorCode", new QName("foo"), 7, 8, 56,
                                                 "moduleuri", null, new XQStackTraceElement[] {});
      assertEquals("A-XQQEX-5.1: Retrieve query stack trace from an XQQueryException", 0, ex.getQueryStackTrace().length);
    } catch (final Exception e) {
      fail("A-XQQEX-5.1: Retrieve query stack trace from an XQQueryException failed with message: " + e.getMessage());
    }

    try {
      final XQQueryException ex = new XQQueryException("Hello world!");
      assertNull("A-XQQEX-5.2: Query stack trace is null when not available", ex.getQueryStackTrace());
    } catch (final Exception e) {
      fail("A-XQQEX-5.2: Retrieve Query stack trace from an XQQueryException failed with message: " + e.getMessage());
    }
  }

  public void testGetModuleURI() {
    try {
      final XQQueryException ex = new XQQueryException("Hello world!", "VendorCode", new QName("foo"), 7, 8, 56,
                                                 "moduleuri", null, null);
      assertEquals("A-XQQEX-6.1: Retrieve module uri from an XQQueryException", "moduleuri", ex.getModuleURI());
    } catch (final Exception e) {
      fail("A-XQQEX-6.1: Retrieve module uri from an XQQueryException failed with message: " + e.getMessage());
    }

    try {
      final XQQueryException ex = new XQQueryException("Hello world!");
      assertNull("A-XQQEX-6.2: module uri is null when not available", ex.getModuleURI());
    } catch (final Exception e) {
      fail("A-XQQEX-6.2: Retrieve module uri from an XQQueryException failed with message: " + e.getMessage());
    }
  }

  public void testGetLineNumber() {
    try {
      final XQQueryException ex = new XQQueryException("Hello world!", "VendorCode", new QName("foo"), 7, 8, 56);
      assertEquals("A-XQQEX-7.1: Retrieve line number from an XQQueryException", 7, ex.getLineNumber());
    } catch (final Exception e) {
      fail("A-XQQEX-7.1: Retrieve line number from an XQQueryException failed with message: " + e.getMessage());
    }

    try {
      final XQQueryException ex = new XQQueryException("Hello world!");
      assertEquals("A-XQQEX-7.2: Line number is -1 when not available", -1, ex.getLineNumber());
    } catch (final Exception e) {
      fail("A-XQQEX-7.2: Retrieve line number from an XQQueryException failed with message: " + e.getMessage());
    }
  }

  public void testGetColumnNumber() {
    try {
      final XQQueryException ex = new XQQueryException("Hello world!", "VendorCode", new QName("foo"), 7, 8, 56);
      assertEquals("A-XQQEX-8.1: Retrieve column number from an XQQueryException", 8, ex.getColumnNumber());
    } catch (final Exception e) {
      fail("A-XQQEX-8.1: Retrieve column number from an XQQueryException failed with message: " + e.getMessage());
    }

    try {
      final XQQueryException ex = new XQQueryException("Hello world!");
      assertEquals("A-XQQEX-8.2: Column number is -1 when not available", -1, ex.getColumnNumber());
    } catch (final Exception e) {
      fail("A-XQQEX-8.2: Retrieve column number from an XQQueryException failed with message: " + e.getMessage());
    }
  }

}

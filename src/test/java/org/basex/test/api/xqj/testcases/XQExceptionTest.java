// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import javax.xml.xquery.XQException;

@SuppressWarnings("all")
public class XQExceptionTest extends XQJTestCase {

  public void testConstructor1() {
    try {
      final XQException ex = new XQException("Hello world!");
      assertEquals("A-XQEX-1.1: XQException successfully created", "Hello world!", ex.getMessage());
    } catch (final Exception e) {
      fail("A-XQEX-1.1: Creating XQException failed with message: " + e.getMessage());
    }
  }

  public void testConstructor2() {
    try {
      final XQException ex = new XQException("Hello world!", "VendorCode");
      assertEquals("A-XQEX-1.1: XQException successfully created", "Hello world!", ex.getMessage());
      assertEquals("A-XQEX-1.1: XQException successfully created", "VendorCode", ex.getVendorCode());
    } catch (final Exception e) {
      fail("A-XQEX-1.1: Creating XQException failed with message: " + e.getMessage());
    }
  }

  public void testGetVendorCode() {
    try {
      final XQException ex = new XQException("Hello world!", "VendorCode");
      assertEquals("A-XQEX-2.1: Retrieve vendor code from an XQException", "VendorCode", ex.getVendorCode());
    } catch (final Exception e) {
      fail("A-XQEX-2.1: Retrieve vendor code from an XQException failed with message: " + e.getMessage());
    }

    try {
      final XQException ex = new XQException("Hello world!");
      assertNull("A-XQEX-2.2: Vendor code is null when not available", ex.getVendorCode());
    } catch (final Exception e) {
      fail("A-XQEX-2.2: Retrieve vendor code from an XQException failed with message: " + e.getMessage());
    }
  }

  public void testGetNextException() {
    try {
      final XQException ex = new XQException("Hello world!", "VendorCode");
      assertNull("A-XQEX-3.1: getNextException returns null when last in chain", ex.getNextException());
    } catch (final Exception e) {
      fail("A-XQEX-3.1: getNextException failed with message: " + e.getMessage());
    }

    try {
      final XQException ex1 = new XQException("Hello world!", "VendorCode");
      final XQException ex2 = new XQException("Hello world!", "VendorCode");
      ex1.setNextException(ex2);
      assertEquals("A-XQEX-3.2: getNextException returns next exception", ex2, ex1.getNextException());
    } catch (final Exception e) {
      fail("A-XQEX-3.2: getNextException failed with message: " + e.getMessage());
    }
  }

  public void testSetNextException() {
    try {
      final XQException ex1 = new XQException("Hello world!", "VendorCode");
      final XQException ex2 = new XQException("Hello world!", "VendorCode");
      ex1.setNextException(ex2);
      assertEquals("A-XQEX-4.1: setNextException sets the next exception in the chain", ex2, ex1.getNextException());
    } catch (final Exception e) {
      fail("A-XQEX-4.1 : setNextException failed with message: " + e.getMessage());
    }
  }

}

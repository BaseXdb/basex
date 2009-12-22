// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.xqj.testcases;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQCancelledException;
import javax.xml.xquery.XQStackTraceElement;

@SuppressWarnings("all")
public class XQCancelledExceptionTest extends XQJTestCase {

  public void testConstructor() {
    try {
      XQCancelledException ex = new XQCancelledException("Hello world!", "VendorCode", new QName("foo"), 7, 8, 56,
                                                         "moduleuri", null, (XQStackTraceElement[])null);
      assertEquals("A-XQCEX-1.1: XQCancelledException successfully created", "Hello world!", ex.getMessage());
      assertEquals("A-XQCEX-1.1: XQCancelledException successfully created", "VendorCode", ex.getVendorCode());
      assertEquals("A-XQCEX-1.1: XQCancelledException successfully created", new QName("foo"), ex.getErrorCode());
      assertEquals("A-XQCEX-1.1: XQCancelledException successfully created", 7, ex.getLineNumber());
      assertEquals("A-XQCEX-1.1: XQCancelledException successfully created", 8, ex.getColumnNumber());
      assertEquals("A-XQCEX-1.1: XQCancelledException successfully created", 56, ex.getPosition());
      assertEquals("A-XQCEX-1.1: XQCancelledException successfully created", "moduleuri", ex.getModuleURI());
      assertTrue("A-XQCEX-1.1: XQCancelledException successfully created", null == ex.getQueryStackTrace());
    } catch (Exception e) {
      fail("A-XQCEX-1.1: Creating XQCancelledException faild with message: " + e.getMessage());
    }
  }

}

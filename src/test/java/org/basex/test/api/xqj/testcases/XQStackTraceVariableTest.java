// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import javax.xml.namespace.*;
import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQStackTraceVariableTest extends XQJTestCase {

  public void testConstructor() {
    try {
      final XQStackTraceVariable xqstv = new XQStackTraceVariable(new QName("v"), "abc");
      assertEquals("A-XQSTV-1.1: XQStackTraceVariable successfully created", new QName("v"), xqstv.getQName());
      assertEquals("A-XQSTV-1.1: XQStackTraceVariable successfully created", "abc", xqstv.getValue());
    } catch (final Exception e) {
      fail("A-XQSTV-1.1: Creating XQStackTraceVariable faild with message: " + e.getMessage());
    }
  }

  public void testGetQName() {
    try {
      final XQStackTraceVariable xqstv = new XQStackTraceVariable(new QName("v"), "abc");
      assertEquals("A-XQSTV-2.1: Retrieve the QName from an XQStackTraceVariable", new QName("v"), xqstv.getQName());
    } catch (final Exception e) {
      fail("A-XQSTV-2.1: Retrieve QName from an XQStackTraceVariable failed with message: " + e.getMessage());
    }
  }

  public void testGetValue() {
    try {
      final XQStackTraceVariable xqstv = new XQStackTraceVariable(new QName("v"), "abc");
      assertEquals("A-XQSTV-3.1: Retrieve the value from an XQStackTraceVariable", "abc", xqstv.getValue());
    } catch (final Exception e) {
      fail("A-XQSTV-3.1: Retrieve value from an XQStackTraceVariable failed with message: " + e.getMessage());
    }
  }
}

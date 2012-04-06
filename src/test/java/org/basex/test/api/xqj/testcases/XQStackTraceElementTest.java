// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import javax.xml.namespace.*;
import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQStackTraceElementTest extends XQJTestCase {
  public void testConstructor() {
    try {
      final XQStackTraceElement xqste = new XQStackTraceElement("moduleuri",1,2,3, new QName("function"), new XQStackTraceVariable[] {new XQStackTraceVariable(new QName("v"), "abc")});
      assertEquals("A-XQSTE-1.1: XQStackTraceElement successfully created", "moduleuri", xqste.getModuleURI());
      assertEquals("A-XQSTE-1.1: XQStackTraceElement successfully created", 1, xqste.getLineNumber());
      assertEquals("A-XQSTE-1.1: XQStackTraceElement successfully created", 2, xqste.getColumnNumber());
      assertEquals("A-XQSTE-1.1: XQStackTraceElement successfully created", 3, xqste.getPosition());
      assertEquals("A-XQSTE-1.1: XQStackTraceElement successfully created", new QName("function"), xqste.getFunctionQName());
      assertEquals("A-XQSTE-1.1: XQStackTraceElement successfully created", 1, xqste.getVariables().length);
    } catch (final Exception e) {
      fail("A-XQSTE-1.1: Creating XQStackTraceElement faild with message: " + e.getMessage());
    }
  }

  public void testGetModuleURI() {
    try {
      final XQStackTraceElement xqste = new XQStackTraceElement("moduleuri",1,2,3, new QName("function"), null);
      assertEquals("A-XQSTV-2.1: Retrieve the module uri from an XQStackTraceElement", "moduleuri", xqste.getModuleURI());
    } catch (final Exception e) {
      fail("A-XQSTV-2.1: Retrieve module uri from an XQStackTraceElement failed with message: " + e.getMessage());
    }
  }

  public void testGetLineNumber() {
    try {
      final XQStackTraceElement xqste = new XQStackTraceElement("moduleuri",1,2,3, new QName("function"), null);
      assertEquals("A-XQSTV-3.1: Retrieve the line number from an XQStackTraceElement", 1, xqste.getLineNumber());
    } catch (final Exception e) {
      fail("A-XQSTV-3.1: Retrieve line number from an XQStackTraceElement failed with message: " + e.getMessage());
    }
  }

  public void testGetColumnNumber() {
    try {
      final XQStackTraceElement xqste = new XQStackTraceElement("moduleuri",1,2,3, new QName("function"), null);
      assertEquals("A-XQSTV-4.1: Retrieve the column number from an XQStackTraceElement", 2, xqste.getColumnNumber());
    } catch (final Exception e) {
      fail("A-XQSTV-4.1: Retrieve column number from an XQStackTraceElement failed with message: " + e.getMessage());
    }
  }

  public void testGetPosition() {
    try {
      final XQStackTraceElement xqste = new XQStackTraceElement("moduleuri",1,2,3, new QName("function"), null);
      assertEquals("A-XQSTV-5.1: Retrieve the position from an XQStackTraceElement", 3, xqste.getPosition());
    } catch (final Exception e) {
      fail("A-XQSTV-5.1: Retrieve position from an XQStackTraceElement failed with message: " + e.getMessage());
    }
  }

  public void testGetFunction() {
    try {
      final XQStackTraceElement xqste = new XQStackTraceElement("moduleuri",1,2,3, new QName("function"), null);
      assertEquals("A-XQSTV-6.1: Retrieve the function name from an XQStackTraceElement", new QName("function"), xqste.getFunctionQName());
    } catch (final Exception e) {
      fail("A-XQSTV-6.1: Retrieve function name from an XQStackTraceElement failed with message: " + e.getMessage());
    }
  }

  public void testGetVariables() {
    try {
      final XQStackTraceElement xqste = new XQStackTraceElement("moduleuri",1,2,3, new QName("function"), new XQStackTraceVariable[] {new XQStackTraceVariable(new QName("v"), "abc")});
      assertEquals("A-XQSTV-7.1: Retrieve the variables from an XQStackTraceElement", 1, xqste.getVariables().length);
      assertEquals("A-XQSTV-7.1: Retrieve the variables from an XQStackTraceElement", new QName("v"), xqste.getVariables()[0].getQName());
      assertEquals("A-XQSTV-7.1: Retrieve the variables from an XQStackTraceElement", "abc",  xqste.getVariables()[0].getValue());
    } catch (final Exception e) {
      fail("A-XQSTV-7.1: Retrieve variables from an XQStackTraceElement failed with message: " + e.getMessage());
    }
  }
}
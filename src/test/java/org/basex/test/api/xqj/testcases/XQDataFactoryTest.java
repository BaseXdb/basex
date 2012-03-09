// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@SuppressWarnings("all")
public class XQDataFactoryTest extends XQJTestCase {

  public void testCreateItemFromAtomicValue() throws XQException {

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromAtomicValue("Hello world!", null);
      fail("A-XQDF-1.1: createItemFromAtomicValue() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItemFromAtomicValue(null, null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (XQException e) {
      // Expect an XQException
      e = null;
    }

    try {
      xqc.createItemFromAtomicValue("Hello world!", xqc.createCommentType());
      fail("A-XQDF-1.3: An invalid type of the value to be bound must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItemFromAtomicValue("Hello world!", xqc.createAtomicType(XQItemType.XQBASETYPE_DECIMAL));
      fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromAtomicValue("Hello world!", xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromAtomicValue() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", "Hello world!", xqi.getAtomicValue());
  }

  public void testCreateItemFromString() throws XQException {

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromString("Hello world!", null);
      fail("A-XQDF-1.1: createItemFromString() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItemFromString(null, null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromString("Hello world!", xqc.createCommentType());
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    try {
      xqc.createItemFromString("123", xqc.createAtomicType(XQItemType.XQBASETYPE_NCNAME));
      fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromString("Hello world!", xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromString() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", "Hello world!", xqi.getAtomicValue());

    try {
      xqi = xqc.createItemFromString("Hello", xqc.createAtomicType(XQItemType.XQBASETYPE_NCNAME));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromString() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQITEMKIND_ATOMIC, xqi.getItemType().getItemKind());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQBASETYPE_NCNAME, xqi.getItemType().getBaseType());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", "Hello", xqi.getObject());
  }

  public void testCreateItemFromDocument_String() throws XQException {

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromDocument("<e>Hello world!</e>", null, null);
      fail("A-XQDF-1.1: createItemFromDocument() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItemFromDocument((String)null, null, null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromDocument("<e>Hello world!</e>", null, xqc.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN));
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_ATOMIC)
        failed = true;
      if (xqitem.getItemType().getBaseType() != XQItemType.XQBASETYPE_BOOLEAN)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    try {
      xqc.createItemFromDocument("<e>", null, null);
      fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromDocument("<e>Hello world!</e>", null, null);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromDocument() failed with message: " + e.getMessage());
    }
    final String result = xqi.getItemAsString(null);
    assertTrue("A-XQDF-1.5: Expects serialized result contains '<e>Hello world!</e>', but it is '" + result + '\'', result.indexOf("<e>Hello world!</e>") != -1);
  }

  public void testCreateItemFromDocument_Reader() throws XQException {
    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromDocument(new StringReader("<e>Hello world!</e>"), null, null);
      fail("A-XQDF-1.1: createItemFromDocument() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItemFromDocument((Reader)null, null, null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromDocument(new StringReader("<e>Hello world!</e>"), null, xqc.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN));
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_ATOMIC)
        failed = true;
      if (xqitem.getItemType().getBaseType() != XQItemType.XQBASETYPE_BOOLEAN)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    try {
      xqc.createItemFromDocument(new StringReader("<e>"), null, null);
      fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromDocument(new StringReader("<e>Hello world!</e>"), null, null);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromDocument() failed with message: " + e.getMessage());
    }
    final String result = xqi.getItemAsString(null);
    assertTrue("A-XQDF-1.5: Expects serialized result contains '<e>Hello world!</e>', but it is '" + result + '\'', result.indexOf("<e>Hello world!</e>") != -1);
  }

  public void testCreateItemFromDocument_InputStream() throws XQException, UnsupportedEncodingException {
    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromDocument(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?><e>Hello world!</e>".getBytes("UTF-8")), null, null);
      fail("A-XQDF-1.1: createItemFromDocument() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItemFromDocument((InputStream)null, null, null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromDocument(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?><e>Hello world!</e>".getBytes("UTF-8")), null, xqc.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN));
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_ATOMIC)
        failed = true;
      if (xqitem.getItemType().getBaseType() != XQItemType.XQBASETYPE_BOOLEAN)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    try {
      xqc.createItemFromDocument(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?><e>".getBytes("UTF-8")), null, null);
      fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromDocument(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?><e>Hello world!</e>".getBytes("UTF-8")), null, null);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromDocument() failed with message: " + e.getMessage());
    }
    final String result = xqi.getItemAsString(null);
    assertTrue("A-XQDF-1.5: Expects serialized result contains '<e>Hello world!</e>', but it is '" + result + '\'', result.indexOf("<e>Hello world!</e>") != -1);
  }

  public void testCreateItemFromDocument_XMLStreamReader() throws XQException {

    // expression used to create the input XMLStreamReader objects
    final XQExpression xqe = xqc.createExpression();

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromDocument(xqe.executeQuery("<e>Hello world!</e>").getSequenceAsStream(), null);
      fail("A-XQDF-1.1: createItemFromDocument() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItemFromDocument((XMLStreamReader)null, null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromDocument(xqe.executeQuery("<e>Hello world!</e>").getSequenceAsStream(), xqc.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN));
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_ATOMIC)
        failed = true;
      if (xqitem.getItemType().getBaseType() != XQItemType.XQBASETYPE_BOOLEAN)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromDocument(xqe.executeQuery("<e>Hello world!</e>").getSequenceAsStream(), null);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromDocument() failed with message: " + e.getMessage());
    }
    final String result = xqi.getItemAsString(null);
    assertTrue("A-XQDF-1.5: Expects serialized result contains '<e>Hello world!</e>', but it is '" + result + '\'', result.indexOf("<e>Hello world!</e>") != -1);

    xqe.close();
  }

  public void testCreateItemFromDocument_Source() throws XQException {
    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromDocument(new StreamSource(new StringReader("<e>Hello world!</e>")), null);
      fail("A-XQDF-1.1: createItemFromDocument() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItemFromDocument((Source)null, null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromDocument(new StreamSource(new StringReader("<e>Hello world!</e>")), xqc.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN));
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_ATOMIC)
        failed = true;
      if (xqitem.getItemType().getBaseType() != XQItemType.XQBASETYPE_BOOLEAN)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    try {
      xqc.createItemFromDocument(new StreamSource(new StringReader("<e>")), null);
      fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromDocument(new StreamSource(new StringReader("<e>Hello world!</e>")), null);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromDocument() failed with message: " + e.getMessage());
    }
    final String result = xqi.getItemAsString(null);
    assertTrue("A-XQDF-1.5: Expects serialized result contains '<e>Hello world!</e>', but it is '" + result + '\'', result.indexOf("<e>Hello world!</e>") != -1);
  }

  public void testCreateItemFromObject() throws XQException {

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromObject("Hello world!", null);
      fail("A-XQDF-1.1: createItemFromObject() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItemFromObject(null, null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromObject("Hello world!", xqc.createCommentType());
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    try {
      xqc.createItemFromObject("123", xqc.createAtomicType(XQItemType.XQBASETYPE_NCNAME));
      fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqi = null;

    try {
      xqi = xqc.createItemFromObject("Hello world!", xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromObject() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful bindXXX.", "Hello world!", xqi.getAtomicValue());

    try {
      xqi = xqc.createItemFromObject("Hello", xqc.createAtomicType(XQItemType.XQBASETYPE_NCNAME));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromObject() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQITEMKIND_ATOMIC, xqi.getItemType().getItemKind());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQBASETYPE_NCNAME, xqi.getItemType().getBaseType());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", "Hello", xqi.getObject());
  }

  public void testCreateItemFromBoolean() throws XQException {

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromBoolean(true, null);
      fail("A-XQDF-1.1: createItemFromBoolean() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromBoolean(true, xqc.createCommentType());
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromBoolean(true, xqc.createAtomicType(XQItemType.XQBASETYPE_BOOLEAN));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromBoolean() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", "true", xqi.getAtomicValue());
  }

  public void testCreateItemFromByte() throws XQException {

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromByte((byte)1, null);
      fail("A-XQDF-1.1: createItemFromByte() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromByte((byte)1, xqc.createCommentType());
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    // Can't think of a way to verify A-XQDF-1.4 with the createItemFromByte() method

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromByte((byte)1, null);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromByte() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", "1", xqi.getAtomicValue());

    try {
      xqi = xqc.createItemFromByte((byte)1, xqc.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromByte() failed with message: " + e.getMessage());
    }

    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQITEMKIND_ATOMIC, xqi.getItemType().getItemKind());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQBASETYPE_INTEGER, xqi.getItemType().getBaseType());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", "1", xqi.getAtomicValue());
  }

  public void testCreateItemFromDouble() throws XQException {
    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromDouble(1d, null);
      fail("A-XQDF-1.1: createItemFromDouble() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromDouble(1d, xqc.createCommentType());
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    // Can't think of a way to verify A-XQDF-1.4 with the createItemFromDouble() method

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromDouble(1d, xqc.createAtomicType(XQItemType.XQBASETYPE_DOUBLE));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromDouble() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQITEMKIND_ATOMIC, xqi.getItemType().getItemKind());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQBASETYPE_DOUBLE, xqi.getItemType().getBaseType());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", 1d, xqi.getDouble(), 0.0);
  }

  public void testCreateItemFromFloat() throws XQException {

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromFloat(1f, null);
      fail("A-XQDF-1.1: createItemFromFloat() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromFloat(1f, xqc.createCommentType());
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    // Can't think of a way to verify A-XQDF-1.4 with the createItemFromDouble() method

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromFloat(1f, xqc.createAtomicType(XQItemType.XQBASETYPE_FLOAT));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromDouble() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQITEMKIND_ATOMIC, xqi.getItemType().getItemKind());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQBASETYPE_FLOAT, xqi.getItemType().getBaseType());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", 1d, xqi.getFloat(), 0.0);
}

  public void testCreateItemFromInt() throws XQException {

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromInt(1, null);
      fail("A-XQDF-1.1: createItemFromInt() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromInt(1, xqc.createCommentType());
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    try {
      xqc.createItemFromInt(128, xqc.createAtomicType(XQItemType.XQBASETYPE_BYTE));
      fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromInt(1, null);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromInt() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful bindXXX.", "1",xqi.getAtomicValue());

    try {
      xqi = xqc.createItemFromInt(1, xqc.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromInt() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQITEMKIND_ATOMIC, xqi.getItemType().getItemKind());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQBASETYPE_INTEGER, xqi.getItemType().getBaseType());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", "1", xqi.getAtomicValue());
  }

  public void testCreateItemFromLong() throws XQException {
    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromLong(1, null);
      fail("A-XQDF-1.1: createItemFromLong() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromLong(1, xqc.createCommentType());
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    try {
      xqc.createItemFromLong(128, xqc.createAtomicType(XQItemType.XQBASETYPE_BYTE));
      fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromLong(1, null);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromLong() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful bindXXX.", "1",xqi.getAtomicValue());

    try {
      xqi = xqc.createItemFromLong(1, xqc.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromLong() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQITEMKIND_ATOMIC, xqi.getItemType().getItemKind());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQBASETYPE_INTEGER, xqi.getItemType().getBaseType());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", "1", xqi.getAtomicValue());
  }

  public void testCreateItemFromNode() throws XQException, IOException, SAXException, ParserConfigurationException {

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder parser = factory.newDocumentBuilder();
    final Document document = parser.parse(new InputSource(new StringReader("<e>Hello world!</e>")));

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromNode(document, null);
      fail("A-XQDF-1.1: createItemFromNode() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItemFromNode(null, null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromNode(document, xqc.createCommentType());
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    XQItem xqi = null;

    try {
      xqi = xqc.createItemFromNode(document, null);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromNode() failed with message: " + e.getMessage());
    }
    final String result = xqi.getItemAsString(null);
    assertTrue("A-XQDF-1.5: Expects serialized result contains '<e>Hello world!</e>', but it is '" + result + '\'', result.indexOf("<e>Hello world!</e>") != -1);
  }

  public void testCreateItemFromShort() throws XQException {
    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemFromShort((short)1, null);
      fail("A-XQDF-1.1: createItemFromShort() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    boolean failed = false;
    try {
      final XQItem xqitem = xqc.createItemFromShort((short)1, xqc.createCommentType());
      // conversion succeeded, we're having implementation defined behaviour
      // but at least the XDM instance must be of the right type.
      if (xqitem.getItemType().getItemKind() != XQItemType.XQITEMKIND_COMMENT)
        failed = true;
    } catch (final XQException e) {
      // Expect an XQException
    }
    if (failed)
      fail("A-XQDF-1.3: The conversion is subject to the following constraints. Either it fails with an XQException, either it is successful in which case it must result in an instance of XDT.");

    try {
      xqc.createItemFromShort((short)128, xqc.createAtomicType(XQItemType.XQBASETYPE_BYTE));
      fail("A-XQDF-1.4: The conversion of the value to an XDM instance must fail.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqi = null;
    try {
      xqi = xqc.createItemFromShort((short)1, null);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromShort() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful bindXXX.", "1",xqi.getAtomicValue());

    try {
      xqi = xqc.createItemFromShort((short)1, xqc.createAtomicType(XQItemType.XQBASETYPE_INTEGER));
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItemFromLong() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQITEMKIND_ATOMIC, xqi.getItemType().getItemKind());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", XQItemType.XQBASETYPE_INTEGER, xqi.getItemType().getBaseType());
    assertEquals("A-XQDF-1.5: Successful createItemFromXXX.", "1", xqi.getAtomicValue());
  }

  public void testCreateItem() throws XQException {

    // Create an XQItem, which we will use subsequently to test bindItem()
    final XQItem xqi = xqc.createItemFromString("Hello world!", null);

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItem(xqi);
      fail("A-XQDF-1.1: createItem() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createItem(null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQItem xqresult = null;
    try {
      xqresult = xqc.createItem(xqi);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createItem() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-1.5: Successful createItem().", "Hello world!", xqresult.getAtomicValue());

    xqi.close();
    try {
      xqc.createItem(xqi);
      fail("A-XQDF-1.6: Passing a closed XQItem or XQSequence object as argument msut result in an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
  }

  public void testCreateSequence_FromSequence() throws XQException {
    XQSequence xqs;

    // prepared expression we use to create sequences
    final XQPreparedExpression xqpe = xqc.prepareExpression("'Hello world!'");

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    xqs = xqpe.executeQuery();
    try {
      xqdf.createSequence(xqs);
      fail("A-XQDF-1.1: createSequence() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }
    xqs.close();

    try {
      xqc.createSequence((XQSequence)null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQSequence xqsresult = null;
    xqs = xqpe.executeQuery();
    try {
      xqsresult = xqc.createSequence(xqs);
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createSequence() failed with message: " + e.getMessage());
    }
    final String result = xqsresult.getSequenceAsString(null);
    assertTrue("A-XQDF-1.5: Expects serialized result contains 'Hello world!', but it is '" + result + '\'', result.indexOf("Hello world!") != -1);

    xqs.close();
    try {
      xqc.createSequence(xqs);
      fail("A-XQDF-1.6: Passing a closed XQItem or XQSequence object as argument msut result in an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    xqpe.close();
  }

  public void testCreateSequence_FromIterator() throws XQException {

    final List list = new LinkedList();
    list.add("Hello world!");
    list.add(xqc.createItemFromString("Hello world!", null));

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createSequence(list.iterator());
      fail("A-XQDF-1.1: createSequence() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createSequence((Iterator)null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    XQSequence xqsresult = null;
    try {
      xqsresult = xqc.createSequence(list.iterator());
    } catch (final XQException e) {
      fail("A-XQDF-1.5: createSequence() failed with message: " + e.getMessage());
    }
    final String result = xqsresult.getSequenceAsString(null);
    assertTrue("A-XQDF-1.5: Expects serialized result contains 'Hello world!', but it is '" + result + '\'', result.indexOf("Hello world!") != -1);
  }

  public void testCreateAtomicType() throws XQException {
    XQItemType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createAtomicType(XQItemType.XQBASETYPE_UNTYPEDATOMIC);
      fail("A-XQDF-1.1: createAtomicType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createAtomicType(XQItemType.XQBASETYPE_UNTYPED);
      fail("A-XQDF-2.1: createAtomicType() detects invalid xqbasetype arguments.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createAtomicType(XQItemType.XQBASETYPE_UNTYPEDATOMIC);
    } catch (final XQException e) {
      fail("A-XQDF-2.2: createAtomicType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-2.2: Successful createAtomicType.", XQItemType.XQITEMKIND_ATOMIC, xqtype.getItemKind());
    assertEquals("A-XQDF-2.2: Successful createAtomicType.", XQItemType.XQBASETYPE_UNTYPEDATOMIC, xqtype.getBaseType());
    assertEquals("A-XQDF-2.2: Successful createAtomicType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    assertEquals("A-XQDF-2.2: Successful createAtomicType.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertEquals("A-XQDF-2.2: Successful createAtomicType.", "untypedAtomic", xqtype.getTypeName().getLocalPart());

    try {
      xqtype = xqc.createAtomicType(XQItemType.XQBASETYPE_UNTYPEDATOMIC,
                                    new QName("http://www.w3.org/2001/XMLSchema", "untypedAtomic"),
                                    null);
    } catch (final XQException e) {
      fail("A-XQDF-2.2: createAtomicType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-2.2: Successful createAtomicType.", XQItemType.XQITEMKIND_ATOMIC, xqtype.getItemKind());
    assertEquals("A-XQDF-2.2: Successful createAtomicType.", XQItemType.XQBASETYPE_UNTYPEDATOMIC, xqtype.getBaseType());
    assertEquals("A-XQDF-2.2: Successful createAtomicType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    assertEquals("A-XQDF-2.2: Successful createAtomicType.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertEquals("A-XQDF-2.2: Successful createAtomicType.", "untypedAtomic", xqtype.getTypeName().getLocalPart());
  }

  public void testCreateAttributeType() throws XQException {
    XQItemType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createAttributeType(new QName("a"), XQItemType.XQBASETYPE_INTEGER);
      fail("A-XQDF-1.1: createAttributeType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createAttributeType(new QName("a"), XQItemType.XQBASETYPE_UNTYPED);
      fail("A-XQDF-3.1: createAtomicType() detects invalid xqbasetype arguments.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createAttributeType(new QName("a"), XQItemType.XQBASETYPE_INTEGER);
    } catch (final XQException e) {
      fail("A-XQDF-3.2: createAttributeType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", XQItemType.XQITEMKIND_ATTRIBUTE, xqtype.getItemKind());
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", XQItemType.XQBASETYPE_INTEGER, xqtype.getBaseType());
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", new QName("a"), xqtype.getNodeName());
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", "integer", xqtype.getTypeName().getLocalPart());

    try {
      xqtype = xqc.createAttributeType(new QName("a"),
                                       XQItemType.XQBASETYPE_INTEGER,
                                       new QName("http://www.w3.org/2001/XMLSchema", "integer"),
                                       null);
    } catch (final XQException e) {
      fail("A-XQDF-3.2: createAtomicType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", XQItemType.XQITEMKIND_ATTRIBUTE, xqtype.getItemKind());
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", XQItemType.XQBASETYPE_INTEGER, xqtype.getBaseType());
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", new QName("a"), xqtype.getNodeName());
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertEquals("A-XQDF-3.2: Successful createAttributeType.", "integer", xqtype.getTypeName().getLocalPart());
  }

  public void testCreateSchemaAttributeType() throws XQException {
    // Optional feature, not tested
  }

  public void testCreateCommentType() throws XQException {
    XQItemType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createCommentType();
      fail("A-XQDF-1.1: createCommentType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createCommentType();
    } catch (final XQException e) {
      fail("A-XQDF-4.1: createCommentType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-4.1: Successful createCommentType.", XQItemType.XQITEMKIND_COMMENT, xqtype.getItemKind());
    assertEquals("A-XQDF-4.1: Successful createCommentType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
  }

  public void testCreateDocumentElementType() throws XQException {
    XQItemType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createDocumentElementType(xqc.createElementType(new QName("e"), XQItemType.XQBASETYPE_UNTYPED));
      fail("A-XQDF-1.1: createDocumentElementType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createDocumentElementType(xqc.createCommentType());
      fail("A-XQDF-5.1: createDocumentElementType() detects invalid elementType argument.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createDocumentElementType(null);
      fail("A-XQDF-1.2: null argument is invalid and throws an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createDocumentElementType(xqc.createElementType(new QName("e"), XQItemType.XQBASETYPE_INTEGER));
    } catch (final XQException e) {
      fail("A-XQDF-5.2: createDocumentElementType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-5.2: Successful createDocumentElementType.", XQItemType.XQITEMKIND_DOCUMENT_ELEMENT, xqtype.getItemKind());
    assertEquals("A-XQDF-5.2: Successful createDocumentElementType.", XQItemType.XQBASETYPE_INTEGER, xqtype.getBaseType());
    assertEquals("A-XQDF-5.2: Successful createDocumentElementType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    assertEquals("A-XQDF-5.2: Successful createDocumentElementType.", new QName("e"), xqtype.getNodeName());
    assertEquals("A-XQDF-5.2: Successful createDocumentElementType.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertEquals("A-XQDF-5.2: Successful createDocumentElementType.", "integer", xqtype.getTypeName().getLocalPart());
  }

  public void testCreateDocumentSchemaElementType() throws XQException {
    // Optional feature, not tested
  }

  public void testDocumentType() throws XQException {
    XQItemType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createDocumentType();
      fail("A-XQDF-1.1: createDocumentType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createDocumentType();
    } catch (final XQException e) {
      fail("A-XQDF-6.1: createDocumentType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-6.1: Successful createDocumentType.", XQItemType.XQITEMKIND_DOCUMENT, xqtype.getItemKind());
    assertEquals("A-XQDF-6.1: Successful createDocumentType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
  }

  public void testCreateElementType() throws XQException {
    XQItemType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createElementType(new QName("a"), XQItemType.XQBASETYPE_INTEGER);
      fail("A-XQDF-1.1: createElementType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createElementType(new QName("a"), XQItemType.XQBASETYPE_INTEGER);
    } catch (final XQException e) {
      fail("A-XQDF-7.1: createElementType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-7.1: Successful createElementType.", XQItemType.XQITEMKIND_ELEMENT, xqtype.getItemKind());
    assertEquals("A-XQDF-7.1: Successful createElementType.", XQItemType.XQBASETYPE_INTEGER, xqtype.getBaseType());
    assertEquals("A-XQDF-7.1: Successful createElementType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    assertEquals("A-XQDF-7.1: Successful createElementType.", new QName("a"), xqtype.getNodeName());
    assertEquals("A-XQDF-7.1: Successful createElementType.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertEquals("A-XQDF-7.1: Successful createElementType.", "integer", xqtype.getTypeName().getLocalPart());

    try {
      xqtype = xqc.createElementType(new QName("a"),
                                       XQItemType.XQBASETYPE_INTEGER,
                                       new QName("http://www.w3.org/2001/XMLSchema", "integer"),
                                       null,
                                       true);
    } catch (final XQException e) {
      fail("A-XQDF-7.1: createElementType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-7.1: Successful createElementType.", XQItemType.XQITEMKIND_ELEMENT, xqtype.getItemKind());
    assertEquals("A-XQDF-7.1: Successful createElementType.", XQItemType.XQBASETYPE_INTEGER, xqtype.getBaseType());
    assertEquals("A-XQDF-7.1: Successful createElementType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    assertEquals("A-XQDF-7.1: Successful createElementType.", new QName("a"), xqtype.getNodeName());
    assertEquals("A-XQDF-7.1: Successful createElementType.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertEquals("A-XQDF-7.1: Successful createElementType.", "integer", xqtype.getTypeName().getLocalPart());
  }

  public void testCreateSchemaElementType() throws XQException {
    // Optional feature, not tested
  }

  public void testCreateItemType() throws XQException {
    XQItemType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createItemType();
      fail("A-XQDF-1.1: createItemType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createItemType();
    } catch (final XQException e) {
      fail("A-XQDF-8.1: createItemType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-8.1: Successful createItemType.", XQItemType.XQITEMKIND_ITEM, xqtype.getItemKind());
    assertEquals("A-XQDF-8.1: Successful createItemType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
  }

  public void testCreateNodeType() throws XQException {
    XQItemType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createNodeType();
      fail("A-XQDF-1.1: createNodeType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createNodeType();
    } catch (final XQException e) {
      fail("A-XQDF-9.1: createNodeType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-9.1: Successful createNodeType.", XQItemType.XQITEMKIND_NODE, xqtype.getItemKind());
    assertEquals("A-XQDF-9.1: Successful createNodeType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
  }

  public void testCreateProcessingInstructionType() throws XQException {
    XQItemType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createProcessingInstructionType(null);
      fail("A-XQDF-1.1: createProcessingInstructionType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createProcessingInstructionType(null);
    } catch (final XQException e) {
      fail("A-XQDF-9.1: createProcessingInstructionType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-9.1: Successful createProcessingInstructionType.", XQItemType.XQITEMKIND_PI, xqtype.getItemKind());
    assertNull("A-XQDF-9.1: Successful createProcessingInstructionType.", xqtype.getPIName());
    assertEquals("A-XQDF-9.1: Successful createProcessingInstructionType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());

    try {
      xqtype = xqc.createProcessingInstructionType("Hello");
    } catch (final XQException e) {
      fail("A-XQDF-9.1: createProcessingInstructionType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-9.1: Successful createProcessingInstructionType.", XQItemType.XQITEMKIND_PI, xqtype.getItemKind());
    assertEquals("A-XQDF-9.1: Successful createProcessingInstructionType.", "Hello", xqtype.getPIName());
    assertEquals("A-XQDF-9.1: Successful createProcessingInstructionType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
  }

  public void testCreateTextType() throws XQException {
    XQItemType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createTextType();
      fail("A-XQDF-1.1: createTextType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createTextType();
    } catch (final XQException e) {
      fail("A-XQDF-10.1: createTextType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-10.1: Successful createTextType.", XQItemType.XQITEMKIND_TEXT, xqtype.getItemKind());
    assertEquals("A-XQDF-10.1: Successful createTextType.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
  }

  public void testCreateSequenceType() throws XQException {
    XQSequenceType xqtype = null;

    final XQConnection xqdf = xqds.getConnection();
    xqdf.close();
    try {
      xqdf.createSequenceType(xqc.createElementType(new QName("e"), XQItemType.XQBASETYPE_UNTYPED), XQSequenceType.OCC_EXACTLY_ONE);
      fail("A-XQDF-1.1: createSequenceType() throws an XQException when the data factory is in closed state.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createSequenceType(xqc.createElementType(new QName("e"), XQItemType.XQBASETYPE_UNTYPED), XQSequenceType.OCC_EMPTY);
      fail("A-XQDF-11.1: item type parameter of createSequenceType() must be null with occurrence indicator empty.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createSequenceType(null, XQSequenceType.OCC_EXACTLY_ONE);
      fail("A-XQDF-11.2: item type parameter of createSequenceType() must be not null with occurrence indicator different from empty.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqc.createSequenceType(xqc.createElementType(new QName("e"), XQItemType.XQBASETYPE_UNTYPED), 777);
      fail("A-XQDF-11.3: createSequenceType() detects invalid occurrence indicator.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    try {
      xqtype = xqc.createSequenceType(xqc.createElementType(new QName("e"), XQItemType.XQBASETYPE_INTEGER), XQSequenceType.OCC_ONE_OR_MORE);
    } catch (final XQException e) {
      fail("A-XQDF-11.4: createSequenceType() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQDF-11.4: Successful createSequenceType.", XQItemType.XQITEMKIND_ELEMENT, xqtype.getItemType().getItemKind());
    assertEquals("A-XQDF-11.4: Successful createSequenceType.", XQItemType.XQBASETYPE_INTEGER, xqtype.getItemType().getBaseType());
    assertEquals("A-XQDF-11.4: Successful createSequenceType.", XQItemType.OCC_ONE_OR_MORE, xqtype.getItemOccurrence());
    assertEquals("A-XQDF-11.4: Successful createSequenceType.", new QName("e"), xqtype.getItemType().getNodeName());
    assertEquals("A-XQDF-11.4: Successful createSequenceType.", "http://www.w3.org/2001/XMLSchema", xqtype.getItemType().getTypeName().getNamespaceURI());
    assertEquals("A-XQDF-11.4: Successful createSequenceType.", "integer", xqtype.getItemType().getTypeName().getLocalPart());
  }
}

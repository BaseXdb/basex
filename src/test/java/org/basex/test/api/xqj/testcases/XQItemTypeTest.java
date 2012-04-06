// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.api.xqj.testcases;

import javax.xml.namespace.*;
import javax.xml.xquery.*;

@SuppressWarnings("all")
public class XQItemTypeTest extends XQJTestCase {

  private static void testSimpleType(final XQItemType xqtype, final int itemKind) throws XQException {
    try {
      xqtype.getBaseType();
      fail("A-XQIT-1.2: getBaseType() must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
    assertEquals("A-XQIT-2.1: getItemKind() returns the correct item kind.", itemKind, xqtype.getItemKind());
    assertEquals("A-XQIT-3.1: getItemOccurrence() returns OCC_EXACTLY_ONE.", XQSequenceType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    // Not much we can test for toString()
    xqtype.toString();
    try {
      xqtype.getNodeName();
      fail("A-XQIT-5.2: getNodeName() must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
    assertNull("A-XQIT-6.2: getSchemaURI() returns null.", xqtype.getSchemaURI());
    try {
      xqtype.getTypeName();
      fail("A-XQIT-7.2: getTypeName() must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
    assertFalse("A-XQIT-8.1: isAnonymousType() reports if the type is anonymous.", xqtype.isAnonymousType());
    assertFalse("A-XQIT-9.1: isElementNillable() reports if the element is nillable.", xqtype.isElementNillable());
    try {
      xqtype.getPIName();
      fail("A-XQIT-10.2: getPIName() must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
  }

  public void testItemKindAtomic() throws XQException {
    final XQItemType xqtype = xqc.createAtomicType(XQItemType.XQBASETYPE_STRING);

    assertEquals("A-XQIT-1.1: getBaseType() returns the correct base type.", XQItemType.XQBASETYPE_STRING, xqtype.getBaseType());
    assertEquals("A-XQIT-2.1: getItemKind() returns the correct item kind.", XQItemType.XQITEMKIND_ATOMIC, xqtype.getItemKind());
    assertEquals("A-XQIT-3.1: getItemOccurrence() returns OCC_EXACTLY_ONE.", XQSequenceType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    // Not much we can test for toString()
    xqtype.toString();
    try {
      xqtype.getNodeName();
      fail("A-XQIT-5.2: getNodeName() must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
    assertNull("A-XQIT-6.2: getSchemaURI() returns null.", xqtype.getSchemaURI());
    assertEquals("A-XQIT-7.1: getTypeName() returns the correct QName.", "string", xqtype.getTypeName().getLocalPart());
    assertEquals("A-XQIT-7.1: getTypeName() returns the correct QName.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertFalse("A-XQIT-8.1: isAnonymousType() reports if the type is anonymous.", xqtype.isAnonymousType());
    assertFalse("A-XQIT-9.1: isElementNillable() reports if the element is nillable.", xqtype.isElementNillable());
    try {
      xqtype.getPIName();
      fail("A-XQIT-10.2: getPIName() must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
  }

  public void testItemKindAttribute() throws XQException {
    final XQItemType xqtype = xqc.createAttributeType(new QName("http://www.xqj.org", "a"), XQItemType.XQBASETYPE_STRING);

    assertEquals("A-XQIT-1.1: getBaseType() returns the correct base type.", XQItemType.XQBASETYPE_STRING, xqtype.getBaseType());
    assertEquals("A-XQIT-2.1: getItemKind() returns the correct item kind.", XQItemType.XQITEMKIND_ATTRIBUTE, xqtype.getItemKind());
    assertEquals("A-XQIT-3.1: getItemOccurrence() returns OCC_EXACTLY_ONE.", XQSequenceType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    // Not much we can test for toString()
    xqtype.toString();
    assertEquals("A-XQIT-5.1: getNodeName() returns the correct QName.", "a", xqtype.getNodeName().getLocalPart());
    assertEquals("A-XQIT-5.1: getNodeName() returns the correct QName.", "http://www.xqj.org", xqtype.getNodeName().getNamespaceURI());
    assertNull("A-XQIT-6.2: getSchemaURI() returns null.", xqtype.getSchemaURI());
    assertEquals("A-XQIT-7.1: getTypeName() returns the correct QName.", "string", xqtype.getTypeName().getLocalPart());
    assertEquals("A-XQIT-7.1: getTypeName() returns the correct QName.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertFalse("A-XQIT-8.1: isAnonymousType() reports if the type is anonymous.", xqtype.isAnonymousType());
    assertFalse("A-XQIT-9.1: isElementNillable() reports if the element is nillable.", xqtype.isElementNillable());
    try {
      xqtype.getPIName();
      fail("A-XQIT-10.2: getPIName() must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
  }

  public void testItemKindComment() throws XQException {
    final XQItemType xqtype = xqc.createCommentType() ;

    testSimpleType(xqtype, XQItemType.XQITEMKIND_COMMENT);
  }

  public void testItemKindDocument() throws XQException {
    final XQItemType xqtype = xqc.createDocumentType() ;

    testSimpleType(xqtype, XQItemType.XQITEMKIND_DOCUMENT);
  }

  public void testItemKindDocumentElement() throws XQException {
    final XQItemType xqtype_element = xqc.createElementType(new QName("http://www.xqj.org", "e"), XQItemType.XQBASETYPE_STRING);
    final XQItemType xqtype = xqc.createDocumentElementType(xqtype_element);

    assertEquals("A-XQIT-1.1: getBaseType() returns the correct base type.", XQItemType.XQBASETYPE_STRING, xqtype.getBaseType());
    assertEquals("A-XQIT-2.1: getItemKind() returns the correct item kind.", XQItemType.XQITEMKIND_DOCUMENT_ELEMENT, xqtype.getItemKind());
    assertEquals("A-XQIT-3.1: getItemOccurrence() returns OCC_EXACTLY_ONE.", XQSequenceType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    // Not much we can test for toString()
    xqtype.toString();
    assertEquals("A-XQIT-5.1: getNodeName() returns the correct QName.", "e", xqtype.getNodeName().getLocalPart());
    assertEquals("A-XQIT-5.1: getNodeName() returns the correct QName.", "http://www.xqj.org", xqtype.getNodeName().getNamespaceURI());
    assertNull("A-XQIT-6.2: getSchemaURI() returns null", xqtype.getSchemaURI());
    assertEquals("A-XQIT-7.1: getTypeName() returns the correct QName.", "string", xqtype.getTypeName().getLocalPart());
    assertEquals("A-XQIT-7.1: getTypeName() returns the correct QName.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertFalse("A-XQIT-8.1: isAnonymousType() reports if the type is anonymous.", xqtype.isAnonymousType());
    assertFalse("A-XQIT-9.1: isElementNillable() reports if the element is nillable.", xqtype.isElementNillable());
    try {
      xqtype.getPIName();
      fail("A-XQIT-10.2: getPIName() must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
  }

  public void testItemKindDocumentSchemaElement() throws XQException {
    // optional feature, not tested.
  }

  public void testItemKindElement() throws XQException {
    final XQItemType xqtype = xqc.createElementType(new QName("http://www.xqj.org", "e"), XQItemType.XQBASETYPE_STRING);

    assertEquals("A-XQIT-1.1: getBaseType() returns the correct base type.", XQItemType.XQBASETYPE_STRING, xqtype.getBaseType());
    assertEquals("A-XQIT-2.1: getItemKind() returns the correct item kind.", XQItemType.XQITEMKIND_ELEMENT, xqtype.getItemKind());
    assertEquals("A-XQIT-3.1: getItemOccurrence() returns OCC_EXACTLY_ONE.", XQSequenceType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    // Not much we can test for toString()
    xqtype.toString();
    assertEquals("A-XQIT-5.1: getNodeName() returns the correct QName.", "e", xqtype.getNodeName().getLocalPart());
    assertEquals("A-XQIT-5.1: getNodeName() returns the correct QName.", "http://www.xqj.org", xqtype.getNodeName().getNamespaceURI());
    assertNull("A-XQIT-6.2: getSchemaURI() returns null", xqtype.getSchemaURI());
    assertEquals("A-XQIT-7.1: getTypeName() returns the correct QName.", "string", xqtype.getTypeName().getLocalPart());
    assertEquals("A-XQIT-7.1: getTypeName() returns the correct QName.", "http://www.w3.org/2001/XMLSchema", xqtype.getTypeName().getNamespaceURI());
    assertFalse("A-XQIT-8.1: isAnonymousType() reports if the type is anonymous.", xqtype.isAnonymousType());
    assertFalse("A-XQIT-9.1: isElementNillable() reports if the element is nillable.", xqtype.isElementNillable());
    try {
      xqtype.getPIName();
      fail("A-XQIT-10.2: getPIName() must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
  }

  public void testItemKindItem() throws XQException {
    final XQItemType xqtype = xqc.createItemType() ;

    testSimpleType(xqtype, XQItemType.XQITEMKIND_ITEM);
  }

  public void testItemKindNode() throws XQException {
    final XQItemType xqtype = xqc.createNodeType() ;

    testSimpleType(xqtype, XQItemType.XQITEMKIND_NODE);
  }

  public void testItemKindPI() throws XQException {
    final XQItemType xqtype = xqc.createProcessingInstructionType("pi");

    try {
      xqtype.getBaseType();
      fail("A-XQIT-1.2: getBaseType() must throw an XQException");
    } catch (final XQException e) {
      // Expect an XQException
    }
    assertEquals("A-XQIT-2.1: getItemKind() returns the correct item kind.", XQItemType.XQITEMKIND_PI, xqtype.getItemKind());
    assertEquals("A-XQIT-3.1: getItemOccurrence() returns OCC_EXACTLY_ONE.", XQSequenceType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    // Not much we can test for toString()
    xqtype.toString();
    try {
      xqtype.getNodeName();
      fail("A-XQIT-5.2: getNodeName() must throw an XQException");
    } catch (final XQException e) {
      // Expect an XQException
    }
    assertNull("A-XQIT-6.2: getSchemaURI() returns null", xqtype.getSchemaURI());
    try {
      xqtype.getTypeName();
      fail("A-XQIT-7.2: getTypeName() must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }
    assertFalse("A-XQIT-8.1: isAnonymousType() reports if the type is anonymous.", xqtype.isAnonymousType());
    assertFalse("A-XQIT-9.1: isElementNillable() reports if the element is nillable.", xqtype.isElementNillable());
    assertEquals("A-XQIT-10.1: getPIName() returns the correct name.", "pi", xqtype.getPIName());
  }

  public void testItemKindSchemaAttribute() throws XQException {
    // optional feature, not tested.
  }

  public void testItemKindSchemaElement() throws XQException {
    // optional feature, not tested.
  }

  public void testItemKindText() throws XQException {
    final XQItemType xqtype = xqc.createTextType() ;

    testSimpleType(xqtype, XQItemType.XQITEMKIND_TEXT);
  }
}


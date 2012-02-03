// Copyright (c) 2003, 2006, 2007, 2008 Oracle. All rights reserved.
package org.basex.test.xqj.testcases;

import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQStaticContext;

@SuppressWarnings("all")
public class XQStaticContextTest extends XQJTestCase {

  public void testGetNamespacePrefixes() throws XQException {

    String[] prefixes = null;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      prefixes = xqsc.getNamespacePrefixes();
    } catch (final Exception e) {
      fail("A-XQSC-1.1: getNamespacePrefixes() failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQSC-1.1: getNamespacePrefixes() returns a non-null array of prefixes.", prefixes);
  }

  public void testGetNamespaceURI() throws XQException {

    String[] prefixes;
    final XQStaticContext xqsc = xqc.getStaticContext();
    prefixes = xqsc.getNamespacePrefixes();

    try {
      xqsc.getNamespaceURI(null);
      fail("A-XQSC-2.1: getNamespaceURI() with null argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-2.1: getNamespaceURI() with null argument must throw an XQException.");
    }

    try {
      xqsc.getNamespaceURI("a prefix with spaces can never be valid and is thus never returned by getNamespacePrefixes()");
      fail("A-XQSC-2.2: getNamespaceURI() with an unknow prefix argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    }

    for(final String prefixe : prefixes) {
      String uri = null;
      try {
        uri = xqsc.getNamespaceURI(prefixe);
      } catch (final XQException e) {
        fail("A-XQSC-2.3: : getNamespaceURI() failed with message: " + e.getMessage());
      }
      assertNotNull("A-XQSC-2.3: Every prefix returned by getNamespacePrefixes() results in a non-null namesapce URI", uri);
    }
  }

  public void testDeclareNamespace() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.declareNamespace("p",null);
      fail("A-XQSC-3.1: declareNamespace() with null argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-3.1: declareNamespace() with null argument must throw an XQException.");
    }

    try {
      xqsc.declareNamespace("p", "http://www.xqj.org");
    } catch (final XQException e) {
      fail("A-XQSC-3.2: declareNamespace() failed with message: " + e.getMessage());
    }
    String[] prefixes = xqsc.getNamespacePrefixes();
  boolean found = false;
  for (int i = 0; i<prefixes.length && !found; ++i) {
    found = "p".equals(prefixes[i]);
  }
  assertTrue("A-XQSC-3.2: Successfully declare a namespace.", found);
  assertEquals("A-XQSC-3.2: Successfully declare a namespace.", "http://www.xqj.org", xqsc.getNamespaceURI("p"));

    try {
      xqsc.declareNamespace("p", "");
    } catch (final XQException e) {
      fail("A-XQSC-3.3: declareNamespace() failed with message: " + e.getMessage());
    }
    prefixes = xqsc.getNamespacePrefixes();
  found = false;
  for (int i = 0; i<prefixes.length && !found; ++i) {
    found = "p".equals(prefixes[i]);
  }
  assertFalse("A-XQSC-3.2: Successfully undeclare a namespace.", found);
    try {
      xqsc.getNamespaceURI("p");
      fail("A-XQSC-3.2: Successfully undeclare a namespace.");
    } catch (final XQException e) {
      // Expect an XQException
    }
  }

  public void testGetDefaultElementTypeNamespace() throws XQException {

    String uri = null;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      uri = xqsc.getDefaultElementTypeNamespace();
    } catch (final Exception e) {
      fail("A-XQSC-4.1: getDefaultElementTypeNamespace() failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQSC-4.1: getDefaultElementTypeNamespace() returns a non-null namespace URI.", uri);
  }

  public void testSetDefaultElementTypeNamespace() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setDefaultElementTypeNamespace(null);
      fail("A-XQSC-5.1: setDefaultElementTypeNamespace() with null argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-5.1: setDefaultElementTypeNamespace() with null argument must throw an XQException.");
    }

    try {
      xqsc.setDefaultElementTypeNamespace("http://www.xqj.org");
    } catch (final XQException e) {
      fail("A-XQSC-5.2: setDefaultElementTypeNamespace() failed with message: " + e.getMessage());
    }
    String uri = xqsc.getDefaultElementTypeNamespace();
  assertEquals("A-XQSC-5.2: Successfully declare the default element/type namespace.", "http://www.xqj.org", uri);

    try {
      xqsc.setDefaultElementTypeNamespace("");
    } catch (final XQException e) {
      fail("A-XQSC-5.3: setDefaultElementTypeNamespace() failed with message: " + e.getMessage());
    }
    uri = xqsc.getDefaultElementTypeNamespace();
  assertEquals("A-XQSC-5.3: Successfully undeclare the default element/type namespace.", "", uri);
  }

  public void testGetDefaultFunctionNamespace() throws XQException {

    String uri = null;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      uri = xqsc.getDefaultFunctionNamespace();
    } catch (final Exception e) {
      fail("A-XQSC-6.1: getDefaultFunctionNamespace() failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQSC-6.1: getDefaultFunctionNamespace() returns a non-null namespace URI.", uri);
  }

  public void testSetDefaultFunctionNamespace() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setDefaultFunctionNamespace(null);
      fail("A-XQSC-7.1: setDefaultFunctionNamespace() with null argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-7.1: setDefaultFunctionNamespace() with null argument must throw an XQException.");
    }

    try {
      xqsc.setDefaultFunctionNamespace("");
    } catch (final XQException e) {
      fail("A-XQSC-7.3: setDefaultFunctionNamespace() failed with message: " + e.getMessage());
    }
    String uri = xqsc.getDefaultFunctionNamespace();
  assertEquals("A-XQSC-7.3: Successfully undeclare the default element/type namespace.", "", uri);

    try {
      xqsc.setDefaultFunctionNamespace("http://www.xqj.org");
    } catch (final XQException e) {
      fail("A-XQSC-7.2: setDefaultFunctionNamespace() failed with message: " + e.getMessage());
    }
    uri = xqsc.getDefaultFunctionNamespace();
  assertEquals("A-XQSC-7.2: Successfully declare the default function namespace.", "http://www.xqj.org", uri);
  }

  public void testGetContextItemStaticType() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try{
      xqsc.getContextItemStaticType();
    } catch (final Exception e) {
      fail("A-XQSC-8.1: getContextItemStaticType() failed with message: " + e.getMessage());
    }
  }

  public void testSetContextItemStaticType() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    final XQItemType xsstring = xqc.createAtomicType(XQItemType.XQBASETYPE_STRING);

    try {
      xqsc.setContextItemStaticType(xsstring);
    } catch (final Exception e) {
      fail("A-XQSC-9.1: setContextItemStaticType() failed with message: " + e.getMessage());
    }
    XQItemType xqtype = xqsc.getContextItemStaticType();
    assertNotNull("A-XQSC-9.1: Successfully declare the static type of the context item.", xqtype);
    assertEquals("A-XQSC-9.1: Successfully declare the static type of the context item.", XQItemType.OCC_EXACTLY_ONE, xqtype.getItemOccurrence());
    assertEquals("A-XQSC-9.1: Successfully declare the static type of the context item.", XQItemType.XQITEMKIND_ATOMIC, xqtype.getItemType().getItemKind());
    assertEquals("A-XQSC-9.1: Successfully declare the static type of the context item.", XQItemType.XQBASETYPE_STRING, xqtype.getItemType().getBaseType());

    try {
      xqsc.setContextItemStaticType(null);
    } catch (final Exception e) {
      fail("A-XQSC-9.2: setContextItemStaticType() failed with message: " + e.getMessage());
    }
    xqtype = xqsc.getContextItemStaticType();
    assertNull("A-XQSC-9.2: Successfully undeclare the static type of the context item.", xqtype);
  }

  public void testGetDefaultCollation() throws XQException {

    String uri = null;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      uri = xqsc.getDefaultCollation();
    } catch (final Exception e) {
      fail("A-XQSC-10.1: getDefaultCollation() failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQSC-10.1: getDefaultCollation() returns a non-null collation URI.", uri);
  }

  public void testSetDefaultCollation() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setDefaultCollation(null);
      fail("A-XQSC-11.1: setDefaultCollation() with null argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-11.1: setDefaultCollation() with null argument must throw an XQException.");
    }

    try {
      xqsc.setDefaultCollation("http://www.w3.org/2005/xpath-functions/collation/codepoint");
    } catch (final XQException e) {
      fail("A-XQSC-11.2: setDefaultCollation() failed with message: " + e.getMessage());
    }
    final String uri = xqsc.getDefaultCollation();
  assertEquals("A-XQSC-11.2: Successfully set the default collation.", "http://www.w3.org/2005/xpath-functions/collation/codepoint", uri);
  }

  public void testGetConstructionMode() throws XQException {

    int mode = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      mode = xqsc.getConstructionMode();
    } catch (final Exception e) {
      fail("A-XQSC-12.1: getConstructionMode() failed with message: " + e.getMessage());
    }
    assertTrue("A-XQSC-12.1: getConstructionMode() returns CONSTRUCTION_MODE_PRESERVE or CONSTRUCTION_MODE_STRIP.", mode == XQConstants.CONSTRUCTION_MODE_PRESERVE || mode == XQConstants.CONSTRUCTION_MODE_STRIP);
  }

  public void testSetConstructionMode() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setConstructionMode(-1);
      fail("A-XQSC-13.1: setConstructionMode() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-13.1: setConstructionMode() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setConstructionMode(XQConstants.CONSTRUCTION_MODE_PRESERVE);
    } catch (final XQException e) {
      fail("A-XQSC-13.2: setConstructionMode() failed with message: " + e.getMessage());
    }
    final int mode = xqsc.getConstructionMode();
  assertEquals("A-XQSC-13.2: Successfully set the construction mode.",XQConstants.CONSTRUCTION_MODE_PRESERVE, mode);
  }

  public void testGetOrderingMode() throws XQException {

    int mode = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      mode = xqsc.getOrderingMode();
    } catch (final Exception e) {
      fail("A-XQSC-14.1: getOrderingMode() failed with message: " + e.getMessage());
    }
    assertTrue("A-XQSC-14.1: getOrderingMode() returns ORDERING_MODE_ORDERED or ORDERING_MODE_UNORDERED.", mode == XQConstants.ORDERING_MODE_ORDERED || mode == XQConstants.ORDERING_MODE_UNORDERED);
  }

  public void testSetOrderingMode() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setOrderingMode(-1);
      fail("A-XQSC-15.1: setOrderingMode() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-15.1: setOrderingMode() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setOrderingMode(XQConstants.ORDERING_MODE_ORDERED);
    } catch (final XQException e) {
      fail("A-XQSC-15.2: setOrderingMode() failed with message: " + e.getMessage());
    }
    final int mode = xqsc.getOrderingMode();
  assertEquals("A-XQSC-15.2: Successfully set the ordering mode.",XQConstants.ORDERING_MODE_ORDERED, mode);
  }

  public void testGetDefaultOrderForEmptySequences() throws XQException {

    int mode = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      mode = xqsc.getDefaultOrderForEmptySequences();
    } catch (final Exception e) {
      fail("A-XQSC-16.1: getDefaultOrderForEmptySequences() failed with message: " + e.getMessage());
    }
    assertTrue("A-XQSC-16.1: getDefaultOrderForEmptySequences() returns DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST or DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST.", mode == XQConstants.DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST || mode == XQConstants.DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST);
  }

  public void testSetDefaultOrderForEmptySequences() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setDefaultOrderForEmptySequences(-1);
      fail("A-XQSC-17.1: setDefaultOrderForEmptySequences(() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-17.1: setDefaultOrderForEmptySequences(() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setDefaultOrderForEmptySequences(XQConstants.DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST);
    } catch (final XQException e) {
      fail("A-XQSC-17.2: setDefaultOrderForEmptySequences() failed with message: " + e.getMessage());
    }
    final int mode = xqsc.getDefaultOrderForEmptySequences();
  assertEquals("A-XQSC-17.2: Successfully set the default order for empty sequences.",XQConstants.DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST, mode);
  }

  public void testGetBoundarySpacePolicy() throws XQException {

    int policy = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      policy = xqsc.getBoundarySpacePolicy();
    } catch (final Exception e) {
      fail("A-XQSC-18.1: getBoundarySpacePolicy() failed with message: " + e.getMessage());
    }
    assertTrue("A-XQSC-18.1: getBoundarySpacePolicy() returns BOUNDARY_SPACE_PRESERVE or BOUNDARY_SPACE_STRIP.", policy == XQConstants.BOUNDARY_SPACE_PRESERVE || policy == XQConstants.BOUNDARY_SPACE_STRIP);
  }

  public void testSetBoundarySpacePolicy() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setBoundarySpacePolicy(-1);
      fail("A-XQSC-19.1: setBoundarySpacePolicy(() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-19.1: setBoundarySpacePolicy(() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setBoundarySpacePolicy(XQConstants.BOUNDARY_SPACE_PRESERVE);
    } catch (final XQException e) {
      fail("A-XQSC-19.2: setBoundarySpacePolicy() failed with message: " + e.getMessage());
    }
    final int policy = xqsc.getBoundarySpacePolicy();
  assertEquals("A-XQSC-19.2: Successfully set the boundary space policy.",XQConstants.BOUNDARY_SPACE_PRESERVE, policy);
  }

  public void testGetCopyNamespacesModePreserve() throws XQException {

    int mode = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      mode = xqsc.getCopyNamespacesModePreserve();
    } catch (final Exception e) {
      fail("A-XQSC-20.1: getCopyNamespacesModePreserve() failed with message: " + e.getMessage());
    }
    assertTrue("A-XQSC-20.1: getCopyNamespacesModePreserve() returns COPY_NAMESPACES_MODE_PRESERVE or COPY_NAMESPACES_MODE_NO_PRESERVE.", mode == XQConstants.COPY_NAMESPACES_MODE_PRESERVE || mode == XQConstants.COPY_NAMESPACES_MODE_NO_PRESERVE);
  }

  public void testSetCopyNamespacesModePreserve() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setCopyNamespacesModePreserve(-1);
      fail("A-XQSC-21.1: setCopyNamespacesModePreserve(() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-21.1: setCopyNamespacesModePreserve(() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setCopyNamespacesModePreserve(XQConstants.COPY_NAMESPACES_MODE_PRESERVE);
    } catch (final XQException e) {
      fail("A-XQSC-21.2: setCopyNamespacesModePreserve() failed with message: " + e.getMessage());
    }
    final int mode = xqsc.getCopyNamespacesModePreserve();
  assertEquals("A-XQSC-21.2: Successfully set the (no)preserve copy namespace mode.",XQConstants.COPY_NAMESPACES_MODE_PRESERVE, mode);
  }

  public void testGetCopyNamespacesModeInherit() throws XQException {

    int mode = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      mode = xqsc.getCopyNamespacesModeInherit();
    } catch (final Exception e) {
      fail("A-XQSC-22.1: getCopyNamespacesModeInherit() failed with message: " + e.getMessage());
    }
    assertTrue("A-XQSC-22.1: getCopyNamespacesModeInherit() returns COPY_NAMESPACES_MODE_INHERIT or COPY_NAMESPACES_MODE_NO_INHERIT.", mode == XQConstants.COPY_NAMESPACES_MODE_INHERIT || mode == XQConstants.COPY_NAMESPACES_MODE_NO_INHERIT);
  }

  public void testSetCopyNamespacesModeInherit() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setCopyNamespacesModeInherit(-1);
      fail("A-XQSC-23.1: setCopyNamespacesModeInherit(() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-23.1: setCopyNamespacesModeInherit(() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setCopyNamespacesModeInherit(XQConstants.COPY_NAMESPACES_MODE_INHERIT);
    } catch (final XQException e) {
      fail("A-XQSC-23.2: setCopyNamespacesModeInherit() failed with message: " + e.getMessage());
    }
    final int mode = xqsc.getCopyNamespacesModeInherit();
  assertEquals("A-XQSC-23.2: Successfully set the (no)inherit copy namespace mode.",XQConstants.COPY_NAMESPACES_MODE_INHERIT, mode);
  }

  public void testGetBaseURI() throws XQException {

    String uri = null;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      uri = xqsc.getBaseURI();
    } catch (final Exception e) {
      fail("A-XQSC-24.1: getBaseURI() failed with message: " + e.getMessage());
    }
    assertNotNull("A-XQSC-24.1: getBaseURI() returns a non-null URI.", uri);
  }

  public void testSetBaseURI() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setBaseURI(null);
      fail("A-XQSC-25.1: setBaseURI() with null argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-25.1: setBaseURI() with null argument must throw an XQException.");
    }

    try {
      xqsc.setBaseURI("");
    } catch (final XQException e) {
      fail("A-XQSC-25.2: setBaseURI() failed with message: " + e.getMessage());
    }
    final String uri = xqsc.getBaseURI();
    assertEquals("A-XQSC-25.2: Successfully declare the base uri.", "", uri);
  }

  public void testGetBindingMode() throws XQException {

    int mode = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      mode = xqsc.getBindingMode();
    } catch (final Exception e) {
      fail("A-XQSC-26.1: getBindingMode() failed with message: " + e.getMessage());
    }
    assertEquals("A-XQSC-26.1: getBindingMode() returns by default XQConstants.BINDING_MODE_IMMEDIATE", XQConstants.BINDING_MODE_IMMEDIATE, mode);
  }

  public void testSetBindingMode() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setBindingMode(-1);
      fail("A-XQSC-27.1: setBindingMode(() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-27.1: setBindingMode(() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setBindingMode(XQConstants.BINDING_MODE_DEFERRED);
    } catch (final XQException e) {
      fail("A-XQSC-27.2: setBindingMode() failed with message: " + e.getMessage());
    }
    int mode = xqsc.getBindingMode();
  assertEquals("A-XQSC-27.2: Successfully set the binding mode.",XQConstants.BINDING_MODE_DEFERRED, mode);

    try {
      xqsc.setBindingMode(XQConstants.BINDING_MODE_IMMEDIATE);
    } catch (final XQException e) {
      fail("A-XQSC-27.2: setBindingMode() failed with message: " + e.getMessage());
    }
    mode = xqsc.getBindingMode();
  assertEquals("A-XQSC-27.2: Successfully set the binding mode.",XQConstants.BINDING_MODE_IMMEDIATE, mode);
  }

  public void testGetHoldability() throws XQException {

    int mode = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      mode = xqsc.getHoldability();
    } catch (final Exception e) {
      fail("A-XQSC-28.1: getHoldability() failed with message: " + e.getMessage());
    }
    assertTrue("A-XQSC-28.1: getHoldability() returns HOLDTYPE_HOLD_CURSORS_OVER_COMMIT or HOLDTYPE_CLOSE_CURSORS_AT_COMMIT.", mode == XQConstants.HOLDTYPE_HOLD_CURSORS_OVER_COMMIT || mode == XQConstants.HOLDTYPE_CLOSE_CURSORS_AT_COMMIT);
  }

  public void testSetHoldability() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setHoldability(-1);
      fail("A-XQSC-29.1: setHoldability() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-29.1: setHoldability() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setHoldability(XQConstants.HOLDTYPE_HOLD_CURSORS_OVER_COMMIT);
    } catch (final XQException e) {
      fail("A-XQSC-29.2: setHoldability() failed with message: " + e.getMessage());
    }
    final int mode = xqsc.getHoldability();
  assertEquals("A-XQSC-29.2: Successfully set the holdability.",XQConstants.HOLDTYPE_HOLD_CURSORS_OVER_COMMIT, mode);
  }

  public void testGetQueryLanguageTypeAndVersion() throws XQException {

    int lang = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      lang = xqsc.getQueryLanguageTypeAndVersion();
    } catch (final Exception e) {
      fail("A-XQSC-30.1: getQueryLanguageTypeAndVersion() failed with message: " + e.getMessage());
    }
    assertTrue("A-XQSC-30.1: getQueryLanguageTypeAndVersion() returns LANGTYPE_XQUERY, LANGTYPE_XQUERYX or a negative value", lang == XQConstants.LANGTYPE_XQUERY || lang == XQConstants.LANGTYPE_XQUERYX || lang < 0);
  }

  public void testSetQueryLanguageTypeAndVersion() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setQueryLanguageTypeAndVersion(999);
      fail("A-XQSC-31.1: setQueryLanguageTypeAndVersion() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-31.1: setQueryLanguageTypeAndVersion() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setQueryLanguageTypeAndVersion(XQConstants.LANGTYPE_XQUERY);
    } catch (final XQException e) {
      fail("A-XQSC-31.2: setQueryLanguageTypeAndVersion() failed with message: " + e.getMessage());
    }
    int lang = xqsc.getQueryLanguageTypeAndVersion();
  assertEquals("A-XQSC-31.2: Successfully set the query language.",XQConstants.LANGTYPE_XQUERY, lang);

  if (xqc.getMetaData().isXQueryXSupported()) {
      try {
        xqsc.setQueryLanguageTypeAndVersion(XQConstants.LANGTYPE_XQUERYX);
      } catch (final XQException e) {
        fail("A-XQSC-31.2: setQueryLanguageTypeAndVersion() failed with message: " + e.getMessage());
      }
      lang = xqsc.getQueryLanguageTypeAndVersion();
    assertEquals("A-XQSC-31.2: Successfully set the query language.",XQConstants.LANGTYPE_XQUERYX, lang);
  }
  }

  public void testGetScrollability() throws XQException {

    int mode = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      mode = xqsc.getScrollability();
    } catch (final Exception e) {
      fail("A-XQSC-32.1: getScrollability() failed with message: " + e.getMessage());
    }
    assertTrue("A-XQSC-32.1: getScrollability() returns SCROLLTYPE_FORWARD_ONLY or SCROLLTYPE_SCROLLABLE.", mode == XQConstants.SCROLLTYPE_FORWARD_ONLY || mode == XQConstants.SCROLLTYPE_SCROLLABLE);
  }

  public void testSetScrollability() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      xqsc.setScrollability(-1);
      fail("A-XQSC-33.1: setScrollability() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-33.1: setScrollability() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setScrollability(XQConstants.SCROLLTYPE_SCROLLABLE);
    } catch (final XQException e) {
      fail("A-XQSC-33.2: setScrollability() failed with message: " + e.getMessage());
    }
    final int mode = xqsc.getScrollability();
  assertEquals("A-XQSC-33.2: Successfully set the scrollability.",XQConstants.SCROLLTYPE_SCROLLABLE, mode);
  }

  public void testGetQueryTimeout() throws XQException {

    int timeout = -1;
    final XQStaticContext xqsc = xqc.getStaticContext();

    try {
      timeout = xqsc.getQueryTimeout();
    } catch (final Exception e) {
      fail("A-XQSC-34.1: getQueryTimeout() failed with message: " + e.getMessage());
    }
    assertTrue("A-XQSC-34.1: getQueryTimeout() returns a non negative value.", timeout >= 0);
  }

  public void testSetQueryTimeout() throws XQException {

    final XQStaticContext xqsc = xqc.getStaticContext();

    final int originalTimeout = xqsc.getQueryTimeout();

    try {
      xqsc.setQueryTimeout(-1);
      fail("A-XQSC-35.1: setQueryTimeout() with invalid argument must throw an XQException.");
    } catch (final XQException e) {
      // Expect an XQException
    } catch (final Exception other_e) {
      fail("A-XQSC-35.1: setQueryTimeout() with invalid argument must throw an XQException.");
    }

    try {
      xqsc.setQueryTimeout(10);
    } catch (final XQException e) {
      fail("A-XQSC-35.2: setQueryTimeout() failed with message: " + e.getMessage());
    }
    final int timeout = xqsc.getQueryTimeout();
    // expect to get back 10 seconds, or
    // the implementation's default if it doesn't support timeout and decided to ignore the request
    assertTrue("A-XQSC-35.2: Successfully set the timeout.",timeout == 10 ||timeout == originalTimeout);
  }
}

package org.basex.build;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for parsing XML documents.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XMLParserTest extends SandboxTest {
  /** Prepares a test. */
  @BeforeEach public void before() {
    set(MainOptions.MAINMEM, true);
  }

  /** Finishes a test. */
  @AfterEach public void after() {
    set(MainOptions.MAINMEM, false);
    set(MainOptions.STRIPWS, false);
    set(MainOptions.STRIPNS, false);
    set(MainOptions.SERIALIZER, new SerializerOptions());
    set(MainOptions.INTPARSE, true);
    set(MainOptions.DTD, false);
  }

  /** Tests the internal parser (Option {@link MainOptions#INTPARSE}). */
  @Test public void intParse() {
    final StringBuilder sb = new StringBuilder();
    final String[] docs = {
        "<x/>", " <x/> ", "<x></x>", "<x>A</x>", "<x><x>", "<x/><x/>", "<x></x><x/>",
        "<x>", "</x>", "<x></x></x>", "x<x>", "<x>x", "<x><![CDATA[ ]]></x>",
    };
    for(final String doc : docs) {
      // parse document with default parser (expected to yield correct result)
      set(MainOptions.INTPARSE, false);
      boolean def = true;
      try {
        new CreateDB(NAME, doc).execute(context);
      } catch(final BaseXException ex) {
        Util.debug(ex);
        def = false;
      }

      // parse document with internal parser
      set(MainOptions.INTPARSE, true);
      boolean cust = true;
      try {
        new CreateDB(NAME, doc).execute(context);
      } catch(final BaseXException ex) {
        Util.debug(ex);
        cust = false;
      }

      // compare results
      if(def != cust) {
        sb.append('\n').append(def ? "- not accepted: " : "- not rejected: ").append(doc);
      }
    }

    // list all errors
    if(!sb.isEmpty()) fail(sb.toString());
  }

  /** Empty elements with 31 attributes. */
  @Test public void gh1648() {
    set(MainOptions.INTPARSE, true);

    // build document with various number of arguments (30..33)
    for(int a = 30; a <= 33; a++) {
      final StringBuilder doc = new StringBuilder("<_");
      for(int i = 0; i < a; i++) doc.append(" a").append(a).append("=''");
      doc.append("/>");

      execute(new CreateDB(NAME, doc.toString()));
      query("_[_]", "");
      query("count(_/@*)", a);
    }
  }

  /** Internal parser: DTD element content models with nested parenthesized groups. */
  @Test public void dtdContentModel() {
    set(MainOptions.INTPARSE, true);
    set(MainOptions.DTD, true);

    // content models that must be accepted (several contain more than one group)
    final String[] valid = {
        // simple content specs
        "EMPTY", "ANY", "(#PCDATA)", "(#PCDATA)*", "(#PCDATA | b | c)*",
        // sequences and choices
        "(a, b, c)", "(a | b | c)", "(a,b,c)", "( a , b , c )",
        // single group (regression-safe: always worked)
        "(a, (b)?, c)", "((b))", "(((a)))",
        // multiple groups (the actual bug: these failed before the fix)
        "(a, (b)?, (c)*, d, e)", "((b), (c))", "((a | b), (c | d))",
        "((b)?, (c)?, (d)?)", "(a, ((b | c), d)*, e)", "(a | (b, c) | d)",
        // occurrence indicators on groups and on the whole model
        "(a, (b, c)+, d)", "(a | b)?", "((a, b))*", "(a, b)+",
        // whitespace (incl. newlines and tabs) inside the model
        "(a,\n  (b)?,\t(c)*,\n  d)",
    };
    for(final String model : valid) {
      execute(new CreateDB(NAME, "<!DOCTYPE a [ <!ELEMENT a " + model + "> ]><a/>"));
      query(".", "<a/>");
    }

    // malformed content models must be rejected (and not silently accepted or hung)
    final String[] invalid = {
        "(a, (b)", "(a b)", "((b) (c))", "(a, (b) c)", "()", "(a,)", "(a,, b)", "(a | b",
    };
    for(final String model : invalid) {
      final String doc = "<!DOCTYPE a [ <!ELEMENT a " + model + "> ]><a/>";
      assertThrows(BaseXException.class, () -> new CreateDB(NAME, doc).execute(context));
    }
  }

  /** Internal and default parser must agree on DTD content models. */
  @Test public void dtdContentModelParsers() {
    // well-formed documents with an internal subset and a matching element tree
    sameOnBothParsers(
        "<!DOCTYPE a [ <!ELEMENT a (b, c)> ]><a><b/><c/></a>",
        "<!DOCTYPE a [ <!ELEMENT a ((b)?, (c)*)> ]><a><c/><c/></a>",
        "<!DOCTYPE a [ <!ELEMENT a (b | c)> ]><a><b/></a>",
        "<!DOCTYPE a [ <!ELEMENT a (#PCDATA | b)*> ]><a>x<b/>y</a>",
        "<!DOCTYPE a [ <!ELEMENT a (b, (c, d)+, e)> ]><a><b/><c/><d/><e/></a>");
  }

  /**
   * Internal and default parser must agree on attribute defaults (incl. #FIXED and enumeration
   * defaults) and on tokenized-type attribute-value normalization.
   */
  @Test public void dtdAttributes() {
    sameOnBothParsers(
        // default values, incl. empty and multiple defaults; specified values win
        "<!DOCTYPE a [ <!ATTLIST a b CDATA \"x\"> ]><a/>",
        "<!DOCTYPE a [ <!ATTLIST a b CDATA \"x\"> ]><a b=\"y\"/>",
        "<!DOCTYPE a [ <!ATTLIST a b CDATA \"\"> ]><a/>",
        "<!DOCTYPE a [ <!ATTLIST a b CDATA \"1\" c CDATA \"2\"> ]><a c=\"X\"/>",
        // #FIXED and enumeration defaults
        "<!DOCTYPE a [ <!ATTLIST a b CDATA #FIXED \"x\"> ]><a/>",
        "<!DOCTYPE a [ <!ATTLIST a b (l | r) \"l\"> ]><a/>",
        // entity reference in a default value
        "<!DOCTYPE a [ <!ATTLIST a b CDATA \"&#65;\"> ]><a/>",
        // tokenized-type normalization (collapse + trim); CDATA is left untouched
        "<!DOCTYPE a [ <!ATTLIST a b NMTOKEN #IMPLIED> ]><a b=\"  x  \"/>",
        "<!DOCTYPE a [ <!ATTLIST a b NMTOKENS #IMPLIED> ]><a b=\" x   y \"/>",
        "<!DOCTYPE a [ <!ATTLIST a b (l | r) #IMPLIED> ]><a b=\" l \"/>",
        "<!DOCTYPE a [ <!ATTLIST a b CDATA #IMPLIED> ]><a b=\" x   y \"/>",
        // #IMPLIED / #REQUIRED add nothing
        "<!DOCTYPE a [ <!ATTLIST a b CDATA #IMPLIED> ]><a/>",
        "<!DOCTYPE a [ <!ATTLIST a b CDATA #REQUIRED> ]><a b=\"z\"/>");
  }

  /** Internal and default parser must agree on ignorable (element-content) whitespace. */
  @Test public void dtdElementContentWhitespace() {
    sameOnBothParsers(
        // element-only content: whitespace between children is ignorable and dropped
        "<!DOCTYPE a [ <!ELEMENT a (b, b)><!ELEMENT b EMPTY> ]><a>\n  <b/>\n  <b/>\n</a>",
        "<!DOCTYPE a [ <!ELEMENT a (b)><!ELEMENT b (c)><!ELEMENT c EMPTY> ]>" +
            "<a>\n <b>\n  <c/>\n </b>\n</a>",
        // mixed content and ANY: whitespace is significant and kept
        "<!DOCTYPE a [ <!ELEMENT a (#PCDATA | b)*><!ELEMENT b EMPTY> ]><a>\n  <b/>\n</a>",
        "<!DOCTYPE a [ <!ELEMENT a ANY><!ELEMENT b EMPTY> ]><a>\n  <b/>\n</a>",
        // non-whitespace text in element content is kept by both (non-validating)
        "<!DOCTYPE a [ <!ELEMENT a (b)><!ELEMENT b EMPTY> ]><a> x <b/> y </a>");
  }

  /** A malformed DTD must report its real cause, not a masked "empty document" error. */
  @Test public void dtdErrorNotMasked() {
    set(MainOptions.INTPARSE, true);
    set(MainOptions.DTD, true);

    // the real error (a missing ')') must surface instead of being replaced by close()
    final String bad = createError("<!DOCTYPE a [ <!ELEMENT a (b, (c)> ]><a/>");
    assertNotNull(bad, "Malformed DTD was accepted");
    assertFalse(bad.contains("No input found"), "Real error was masked: " + bad);

    // a document without a root element must still report an empty document
    final String empty = createError("<!-- comment, but no root element -->");
    assertNotNull(empty, "Document without root element was accepted");
    assertTrue(empty.contains("No input found"), "Unexpected error: " + empty);
  }

  /**
   * Internal parser: a complex external DTD subset (parameter entities inside declarations,
   * a parameter-entity-driven conditional section, enumerations and empty default values).
   * These constructs are only legal in the external subset.
   */
  @Test public void dtdExternalSubset() {
    final String dtd =
        "<!ENTITY % yesorno \"(0 | 1)\">\n" +
        "<!ENTITY % common \"id ID #IMPLIED\">\n" +
        "<!ENTITY % inclusion \"INCLUDE\">\n" +
        "<!ENTITY copyright \"Copyright &#169; 2026\">\n" +
        "<!NOTATION gif PUBLIC \"-//IETF//NOTATION GIF//EN\">\n" +
        "<!ELEMENT tgroup (colspec*, (thead)?, tbody)>\n" +
        "<!ELEMENT colspec EMPTY>\n" +
        "<!ELEMENT thead (row+)>\n" +
        "<!ELEMENT tbody (row+)>\n" +
        "<!ELEMENT row ((entry)+)>\n" +
        "<!ELEMENT entry (#PCDATA)*>\n" +
        "<!ATTLIST tgroup cols CDATA #REQUIRED\n" +
        "                 colsep %yesorno; #IMPLIED\n" +
        "                 align (left | right | center | char) \"left\"\n" +
        "                 char CDATA \"\"\n" +
        "                 %common; >\n" +
        "<![ %inclusion; [ <!ELEMENT extra (#PCDATA)> ]]>\n" +
        "<![IGNORE[ <!ELEMENT bad ( ]]>\n";
    final IOFile dtdFile = new IOFile(sandbox(), "table.dtd");
    final IOFile xmlFile = new IOFile(sandbox(), "table.xml");
    write(dtdFile, dtd);
    write(xmlFile, "<!DOCTYPE tgroup SYSTEM \"table.dtd\">\n" +
        "<tgroup cols=\"2\"><tbody><row><entry>a &copyright;</entry></row></tbody></tgroup>");

    set(MainOptions.INTPARSE, true);
    set(MainOptions.DTD, true);
    execute(new CreateDB(NAME, xmlFile.path()));
    // the external DTD is scanned without error; the general entity is expanded
    query("//entry/string()", "a Copyright © 2026");
    query("name(*)", "tgroup");
  }

  /**
   * Asserts that the internal and the default parser produce identical documents.
   * @param docs document strings (each with an internal DTD subset)
   */
  private void sameOnBothParsers(final String... docs) {
    set(MainOptions.DTD, true);
    for(final String doc : docs) {
      set(MainOptions.INTPARSE, false);
      execute(new CreateDB(NAME, doc));
      final String def = query(".");
      set(MainOptions.INTPARSE, true);
      execute(new CreateDB(NAME, doc));
      assertEquals(def, query("."), "Parsers disagree on: " + doc);
    }
  }

  /**
   * Creates a database and returns the resulting error message, or {@code null} on success.
   * @param doc document string
   * @return error message or {@code null}
   */
  private static String createError(final String doc) {
    try {
      new CreateDB(NAME, doc).execute(context);
      return null;
    } catch(final BaseXException ex) {
      return ex.getMessage();
    }
  }

  /** Tests the namespace stripping option (Option {@link MainOptions#STRIPNS}). */
  @Test public void parse() {
    set(MainOptions.STRIPNS, true);

    final String doc = "<e xmlns='A'><b:f xmlns:b='B'/></e>";
    for(final boolean b : new boolean[] { false, true }) {
      set(MainOptions.INTPARSE, b);
      execute(new CreateDB(NAME, doc));
      assertEquals("<e><f/></e>", query("."));
      assertEquals("<f/>", query("e/f"));
    }
  }

  /** Tests the xml:space attribute. */
  @Test public void xmlSpace() {
    final String input = "<x><a> </a>"
        + "<a xml:space=\"default\"> </a>"
        + "<a xml:space=\"preserve\"> </a></x>";

    set(MainOptions.INTPARSE, false);
    execute(new CreateDB(NAME, input));
    query(".", input);

    set(MainOptions.INTPARSE, true);
    execute(new CreateDB(NAME, input));
    query(".", input);
  }

  /** Attribute-value normalization of whitespace characters. */
  @Test public void attributeWhitespace() {
    // literal tab is normalized to a space, character reference is preserved
    final String input = "<x a=\"\t\" b=\"&#x9;\"/>";
    for(final boolean b : new boolean[] { false, true }) {
      set(MainOptions.INTPARSE, b);
      execute(new CreateDB(NAME, input));
      query("string-to-codepoints(x/@a)", 32);
      query("string-to-codepoints(x/@b)", 9);
    }
  }

  /** STRIPNS option with identical attribute names. */
  @Test public void gh2027() {
    set(MainOptions.STRIPNS, true);

    execute(new CreateDB(NAME, "<a a:a='x' b:a='y' xmlns:a='a' xmlns:b='b'/>"));
    query("/a/@* ! name()", "a\na_1");

    execute(new CreateDB(NAME, "<a a:a='x' b:a='y' c:a='z' xmlns:a='a' xmlns:b='b' xmlns:c='c'/>"));
    query("/a/@* ! name()", "a\na_1\na_2");
  }
}

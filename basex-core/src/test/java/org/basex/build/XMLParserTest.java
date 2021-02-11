package org.basex.build;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.serial.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for parsing XML documents.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XMLParserTest extends SandboxTest {
  /**
   * Prepares the tests.
   */
  @BeforeEach public void before() {
    set(MainOptions.MAINMEM, true);
  }

  /**
   * Finishes the tests.
   */
  @AfterEach public void after() {
    set(MainOptions.MAINMEM, false);
    set(MainOptions.CHOP, true);
    set(MainOptions.STRIPNS, false);
    set(MainOptions.SERIALIZER, new SerializerOptions());
    set(MainOptions.INTPARSE, true);
  }

  /**
   * Tests the internal parser (Option {@link MainOptions#INTPARSE}).
   */
  @Test public void intParse() {
    set(MainOptions.CHOP, false);

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
        def = false;
      }

      // parse document with internal parser
      set(MainOptions.INTPARSE, true);
      boolean cust = true;
      try {
        new CreateDB(NAME, doc).execute(context);
      } catch(final BaseXException ex) {
        cust = false;
      }

      // compare results
      if(def != cust) {
        sb.append('\n').append(def ? "- not accepted: " : "- not rejected: ").append(doc);
      }
    }

    // list all errors
    if(sb.length() != 0) fail(sb.toString());
  }

  /**
   * Empty elements with 31 attributes.
   * @throws Exception exception
   */
  @Test public void gh1648() throws Exception {
    set(MainOptions.INTPARSE, true);

    // build document with various number of arguments (30..33)
    for(int a = 30; a <= 33; a++) {
      final StringBuilder doc = new StringBuilder("<_");
      for(int i = 0; i < a; i++) doc.append(" a").append(a).append("=''");
      doc.append("/>");

      new CreateDB(NAME, doc.toString()).execute(context);
      new XQuery("_[_]").execute(context);
    }
  }

  /**
   * Tests the namespace stripping option (Option {@link MainOptions#STRIPNS}).
   */
  @Test public void parse() {
    set(MainOptions.STRIPNS, true);
    set(MainOptions.SERIALIZER, SerializerMode.NOINDENT.get());

    final String doc = "<e xmlns='A'><b:f xmlns:b='B'/></e>";
    for(final boolean b : new boolean[] { false, true }) {
      set(MainOptions.INTPARSE, b);
      execute(new CreateDB(NAME, doc));
      assertEquals("<e><f/></e>", query("."));
      assertEquals("<f/>", query("e/f"));
    }
  }

  /**
   * Tests the xml:space attribute.
   */
  @Test public void xmlSpace() {
    set(MainOptions.SERIALIZER, SerializerMode.NOINDENT.get());

    final String in = "<x><a xml:space='default'> </a><a> </a>" +
        "<a xml:space='preserve'> </a></x>";
    final String out = "<x><a xml:space=\"default\"/><a/>" +
        "<a xml:space=\"preserve\"> </a></x>";

    for(final boolean b : new boolean[] { true, false }) {
      set(MainOptions.INTPARSE, b);
      execute(new CreateDB(NAME, in));
      assertEquals(out, query("."), "Internal parser: " + b);
    }
  }
}

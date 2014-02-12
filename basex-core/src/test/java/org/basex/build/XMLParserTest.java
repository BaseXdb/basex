package org.basex.build;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.serial.*;
import org.basex.io.serial.SerializerOptions.YesNo;
import org.basex.*;
import org.junit.*;

/**
 * Tests for parsing XML documents.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class XMLParserTest extends SandboxTest {
  /**
   * Prepares the tests.
   */
  @Before
  public void before() {
    context.options.set(MainOptions.MAINMEM, true);
  }

  /**
   * Finishes the tests.
   */
  @Before
  public void after() {
    context.options.set(MainOptions.MAINMEM, false);
    context.options.set(MainOptions.CHOP, true);
    context.options.set(MainOptions.STRIPNS, false);
    context.options.set(MainOptions.SERIALIZER, new SerializerOptions());
    context.options.set(MainOptions.INTPARSE, true);
  }

  /**
   * Tests the internal parser (Option {@link MainOptions#INTPARSE}).
   */
  @Test
  public void intParse() {
    context.options.set(MainOptions.CHOP, false);

    final StringBuilder sb = new StringBuilder();

    final String[] docs = {
        "<x/>", " <x/> ", "<x></x>", "<x>A</x>", "<x><x>", "<x/><x/>", "<x></x><x/>",
        "<x>", "</x>", "<x></x></x>", "x<x>", "<x>x", "<x><![CDATA[ ]]></x>",
    };
    for(final String doc : docs) {
      // parse document with default parser (expected to yield correct result)
      context.options.set(MainOptions.INTPARSE, false);
      boolean def = true;
      try {
        new CreateDB(NAME, doc).execute(context);
      } catch(final BaseXException ex) {
        def = false;
      }

      // parse document with internal parser
      context.options.set(MainOptions.INTPARSE, true);
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

    context.options.set(MainOptions.MAINMEM, false);
  }

  /**
   * Tests the namespace stripping option (Option {@link MainOptions#STRIPNS}).
   * @throws Exception exceptions
   */
  @Test
  public void parse() throws Exception {
    context.options.set(MainOptions.STRIPNS, true);
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.INDENT, YesNo.NO);
    context.options.set(MainOptions.SERIALIZER, sopts);

    final String doc = "<e xmlns='A'><b:f xmlns:b='B'/></e>";
    for(final boolean b : new boolean[] { false, true }) {
      context.options.set(MainOptions.INTPARSE, b);
      new CreateDB(NAME, doc).execute(context);
      String result = new XQuery(".").execute(context);
      assertEquals("<e><f/></e>", result);
      result = new XQuery("e/f").execute(context);
      assertEquals("<f/>", result);
    }
  }

  /**
   * Tests the xml:space attribute.
   * @throws Exception exceptions
   */
  @Test
  public void xmlSpace() throws Exception {
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.INDENT, YesNo.NO);
    context.options.set(MainOptions.SERIALIZER, sopts);

    final String in = "<x><a xml:space='default'> </a><a> </a>" +
        "<a xml:space='preserve'> </a></x>";
    final String out = "<x><a xml:space=\"default\"/><a/>" +
        "<a xml:space=\"preserve\"> </a></x>";

    for(final boolean b : new boolean[] { true, false }) {
      context.options.set(MainOptions.INTPARSE, b);
      new CreateDB(NAME, in).execute(context);
      final String result = new XQuery(".").execute(context);
      assertEquals("Internal parser: " + b, out, result);
    }
  }
}

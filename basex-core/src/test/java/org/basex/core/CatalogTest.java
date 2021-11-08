package org.basex.core;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.basex.query.func.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the database commands.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dominik Abend
 * @author Christian Gruen
 */
public class CatalogTest extends SandboxTest {
  /** Test folder. */
  private static final String DIR = "src/test/resources/catalog/";
  /** Catalog file. */
  private static final String CAT = DIR + "catalog.xml";

  /** Query string: Option declaration. */
  private static final String OPTION = "declare option db:catfile '" + CAT + "'; ";
  /** Query string: Pragma. */
  private static final String PRAGMA = "(# db:catfile " + CAT + " #) ";

  /** Test method.*/
  @Test public void createDB() {
    try {
      set(MainOptions.DTD, true);
      set(MainOptions.CATFILE, CAT);
      execute(new CreateDB("document", DIR + "doc.xml"));
      query("db:open('document')", "<doc>X</doc>");
    } finally {
      set(MainOptions.CATFILE, "");
      set(MainOptions.DTD, false);
    }
  }

  /** Test method. */
  @Test public void xsltTransformImport() {
    // imports the import.xsl stylesheet
    final Function func = _XSLT_TRANSFORM;
    final String xml = "<dummy/>";
    final String xsl =
      " <xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>" +
      "   <xsl:import href='http://import.xsl'/>" +
      " </xsl:stylesheet>";

    query(OPTION + func.args(' ' + xml, xsl), xml);
    query(PRAGMA + "{ " + func.args(' ' + xml, xsl) + " }", xml);
  }

  /** Test method. */
  @Test public void xsltTransformDocument() {
    // returns an XML document with the string contents of import-xsl.xml
    final Function func = _XSLT_TRANSFORM;
    final String xml = "<dummy/>";
    final String xsl =
      " <xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>" +
      "   <xsl:template match='/'>" +
      "     <document><xsl:value-of select='document(\"http://import-xsl.xml\")'/></document>" +
      "   </xsl:template>" +
      " </xsl:stylesheet>";
    final String output = "<document>text</document>";

    query(OPTION + func.args(xml, xsl), output);
    query(PRAGMA + "{ " + func.args(xml, xsl) + " }", output);
  }

  /** Test method. */
  @Test public void validateXsd() {
    final Function func = _VALIDATE_XSD;
    final String xml =
      " <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>" +
      "   <xs:import namespace='URI' schemaLocation='http://import.xsd'/>" +
      " </xs:schema>";

    // option declaration
    query(OPTION + func.args("<x xmlns='URI'/>", xml), "");
    query(PRAGMA + "{ " + func.args("<x xmlns='URI'/>", xml) + " }", "");
  }

  /** Test method.*/
  @Test public void dbCreate() {
    final Function func = _DB_CREATE;
    query(func.args("document", DIR + "doc.xml", " ()",
      " map { 'catfile': '" + CAT + "', 'dtd': true() }"));
    query("db:open('document')", "<doc>X</doc>");
  }

  /** Test method.*/
  @Test public void fetchXml() {
    final Function func = _FETCH_XML;
    query(func.args(DIR + "doc.xml",
      " map { 'catfile': '" + CAT + "', 'dtd': true() }"), "<doc>X</doc>");
  }
}

package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Validation Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ValidateModuleTest extends SandboxTest {
  /** Test file. */
  private static final String DIR = "src/test/resources/";
  /** Test file. */
  private static final String FILE = DIR + "validate.xml";
  /** Test file. */
  private static final String XSD = DIR + "validate.xsd";
  /** Test file. */
  private static final String DTD = DIR + "validate.dtd";
  /** Test file. */
  private static final String INPUT = DIR + "input.xml";

  /** Test method. */
  @Test public void dtd() {
    final Function func = _VALIDATE_DTD;
    // specify arguments as file paths
    query(func.args(FILE, DTD), "");
    // specify document as document nodes
    query(func.args(" doc(\"" + FILE + "\")", DTD), "");
    // specify arguments as file contents
    query(func.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(DTD)), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT root (#PCDATA)>' " +
      "return validate:dtd($doc, $dtd) ", "");
    // specify embedded schema
    query(func.args(" ``[<!DOCTYPE p [<!ELEMENT p ANY>]><p/>]``"), "");
    query(func.args(" ``[<!DOCTYPE p [<!ELEMENT p ANY>]><p/>]``", " ()"), "");

    // invalid arguments
    error(func.args("unknown"), WHICHRES_X);
    error(func.args(FILE, "unknown.dtd"), WHICHRES_X);
    error(func.args(FILE), VALIDATE_ERROR_X);
    error(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT unknown (#PCDATA)>' " +
      "return " + func.args(" $doc", " $dtd"), VALIDATE_ERROR_X);
  }

  /** Test method. */
  @Test public void dtdInfo() {
    final Function func = _VALIDATE_DTD_INFO;
    // specify arguments as file paths
    query(func.args(FILE, DTD), "");
    // specify document as document nodes
    query(func.args(" doc(\"" + FILE + "\")", DTD), "");
    // specify arguments as file contents
    query(func.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(DTD)), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT root (#PCDATA)>' " +
      "return " + func.args(" $doc", " $dtd"), "");

    // returned error
    query("exists(" + func.args(FILE) + ')', true);
    query("exists(" +
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT unknown (#PCDATA)>' " +
      "return " + func.args(" $doc", " $dtd") + ')', true);

    // invalid arguments
    error(func.args("unknown"), WHICHRES_X);
    error(func.args(FILE, "unknown.dtd"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void dtdReport() {
    final Function func = _VALIDATE_DTD_REPORT;
    // check XML result
    query(func.args(FILE, DTD), "<report>\n<status>valid</status>\n</report>");
    // specify arguments as file paths
    query(func.args(FILE, DTD) + "//status/string()", "valid");
    // specify document as document nodes
    query(func.args(" doc(\"" + FILE + "\")", DTD) + "//status/string()", "valid");
    // specify arguments as file contents
    query(func.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(DTD)) +
        "//status/string()", "valid");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT root (#PCDATA)>' " +
      "return " + func.args(" $doc", " $dtd") + "//status/string()", "valid");

    // returned error
    query(func.args(FILE) + "//status/string()", "invalid");
    query(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT unknown (#PCDATA)>' " +
      "return " + func.args(" $doc", " $dtd") + "//status/string()", "invalid");

    // check XML result
    query(
      "let $doc := <root/> " +
      "let $schema := '<!ELEMENT unknown (#PCDATA)>' " +
      "let $report := " + func.args(" $doc", " $schema") +
      "return $report update (" +
      "  delete node .//message/text()," +
      "  for $a in .//@* return replace value of node $a with ''" +
        ')',
      "<report>\n<status>invalid</status>\n" +
      "<message level=\"\" line=\"\" column=\"\"/>\n" +
      "</report>");
    // check URL attribute
    query("exists(" + func.args(INPUT, DTD) + "//@url)", true);

    // invalid arguments
    error(func.args("unknown"), WHICHRES_X);
    error(func.args(FILE, "unknown.dtd"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void xsd() {
    final Function func = _VALIDATE_XSD;
    // specify arguments as file paths
    query(func.args(FILE, XSD), "");
    // specify arguments as document nodes
    query(func.args(" doc(\"" + FILE + "\")", " doc(\"" + XSD + "\")"), "");
    // specify arguments as file contents
    query(func.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(XSD)), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "<xs:element name='root'/> " +
      "</xs:schema> " +
      "return validate:xsd($doc, $schema)", "");

    // invalid arguments
    error(func.args("unknown"), WHICHRES_X);
    error(func.args(FILE, "unknown.xsd"), WHICHRES_X);
    // specify option
    error(func.args(FILE, XSD, " map { 'unknown-argument': true() }"), VALIDATE_ERROR_X);
    error(func.args(FILE), VALIDATE_ERROR_X);
    error(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "<xs:element name='unknown'/> " +
      "</xs:schema> " +
      "return validate:xsd($doc, $schema)", VALIDATE_ERROR_X);
  }

  /** Test method. */
  @Test public void xsdInfo() {
    final Function func = _VALIDATE_XSD_INFO;
    // specify arguments as file paths
    query(func.args(FILE, XSD), "");
    // specify arguments as document nodes
    query(func.args(" doc(\"" + FILE + "\")", " doc(\"" + XSD + "\")"), "");
    // specify arguments as file contents
    query(func.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(XSD)), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "<xs:element name='root'/> " +
      "</xs:schema> " +
      "return " + func.args(" $doc", " $schema"), "");

    // returned error
    query("exists(" + func.args(FILE) + ')', true);
    query("exists(" +
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "<xs:element name='unknown'/> " +
      "</xs:schema> " +
      "return " + func.args(" $doc", " $schema") + ')', true);

    // invalid arguments
    error(func.args("unknown"), WHICHRES_X);
    error(func.args(FILE, "unknown.xsd"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void xsdProcessor() {
    final Function func = _VALIDATE_XSD_PROCESSOR;
    assertFalse(query(func.args()).isEmpty());
  }

  /** Test method. */
  @Test public void xsdReport() {
    final Function func = _VALIDATE_XSD_REPORT;
    // check XML result
    query(func.args(FILE, XSD), "<report>\n<status>valid</status>\n</report>");
    // specify arguments as file paths
    query(func.args(FILE, XSD) + "//status/string()", "valid");
    // specify arguments as document nodes
    query(func.args(" doc(\"" + FILE + "\")", " doc(\"" + XSD + "\")") +
        "//status/string()", "valid");
    // specify arguments as file contents
    query(func.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(XSD)) +
        "//status/string()", "valid");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "                 <xs:element name='root'/> " +
      "               </xs:schema> " +
      "return " + func.args(" $doc", " $schema") + "//status/string()", "valid");

    // returned error
    query(func.args(FILE) + "//status/string()", "invalid");
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "                 <xs:element name='unknown'/> " +
      "               </xs:schema> " +
      "return " + func.args(" $doc", " $schema") + "//status/string()", "invalid");

    // check XML result
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "                 <xs:element name='unknown'/> " +
      "               </xs:schema> " +
      "let $report := " + func.args(" $doc", " $schema") +
      "return $report update (" +
      "  delete node .//message/text()," +
      "  for $a in .//@* return replace value of node $a with ''" +
        ')',
      "<report>\n<status>invalid</status>\n" +
      "<message level=\"\" line=\"\" column=\"\"/>\n" +
      "</report>");
    // check URL attribute
    query("exists(" + func.args(INPUT, XSD) + "//@url)", true);

    // invalid arguments
    error(func.args("unknown"), WHICHRES_X);
    error(func.args(FILE, "unknown.xsd"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void xsdVersion() {
    final Function func = _VALIDATE_XSD_VERSION;
    assertFalse(query(func.args()).isEmpty());
  }
}

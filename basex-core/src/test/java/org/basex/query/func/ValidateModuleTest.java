package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Validation Module.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ValidateModuleTest extends AdvancedQueryTest {
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
  @Test
  public void xsd() {
    // specify arguments as file paths
    query(_VALIDATE_XSD.args(FILE, XSD), "");
    // specify arguments as document nodes
    query(_VALIDATE_XSD.args(" doc(\"" + FILE + "\")", " doc(\"" + XSD + "\")"), "");
    // specify arguments as file contents
    query(_VALIDATE_XSD.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(XSD)), "");
    // specify version
    query(_VALIDATE_XSD.args(FILE, XSD, "1.0"), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "<xs:element name='root'/> " +
      "</xs:schema> " +
      "return validate:xsd($doc, $schema)", "");

    // invalid arguments
    error(_VALIDATE_XSD.args("unknown"), WHICHRES_X);
    error(_VALIDATE_XSD.args(FILE, "unknown.xsd"), WHICHRES_X);
    error(_VALIDATE_XSD.args(FILE, XSD, "0.99"), BXVA_XSDVERSION_X);
    error(_VALIDATE_XSD.args(FILE), BXVA_FAIL_X);
    error(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "<xs:element name='unknown'/> " +
      "</xs:schema> " +
      "return validate:xsd($doc, $schema)", BXVA_FAIL_X);
  }

  /** Test method. */
  @Test
  public void xsdInfo() {
    // specify arguments as file paths
    query(_VALIDATE_XSD_INFO.args(FILE, XSD), "");
    // specify arguments as document nodes
    query(_VALIDATE_XSD_INFO.args(" doc(\"" + FILE + "\")", " doc(\"" + XSD + "\")"), "");
    // specify arguments as file contents
    query(_VALIDATE_XSD_INFO.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(XSD)), "");
    // specify version
    query(_VALIDATE_XSD_INFO.args(FILE, XSD, "1.0"), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "<xs:element name='root'/> " +
      "</xs:schema> " +
      "return " + _VALIDATE_XSD_INFO.args(" $doc", " $schema"), "");

    // returned error
    query("exists(" + _VALIDATE_XSD_INFO.args(FILE) + ')', true);
    query("exists(" +
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "<xs:element name='unknown'/> " +
      "</xs:schema> " +
      "return " + _VALIDATE_XSD_INFO.args(" $doc", " $schema") + ')', true);

    // invalid arguments
    error(_VALIDATE_XSD_INFO.args("unknown"), WHICHRES_X);
    error(_VALIDATE_XSD_INFO.args(FILE, "unknown.xsd"), WHICHRES_X);
    error(_VALIDATE_XSD_INFO.args(FILE, XSD, "0.99"), BXVA_XSDVERSION_X);
  }

  /** Test method. */
  @Test
  public void xsdReport() {
    // check XML result
    query(_VALIDATE_XSD_REPORT.args(FILE, XSD), "<report>\n<status>valid</status>\n</report>");
    // specify arguments as file paths
    query(_VALIDATE_XSD_REPORT.args(FILE, XSD) + "//status/string()", "valid");
    // specify arguments as document nodes
    query(_VALIDATE_XSD_REPORT.args(" doc(\"" + FILE + "\")", " doc(\"" + XSD + "\")") +
        "//status/string()", "valid");
    // specify arguments as file contents
    query(_VALIDATE_XSD_REPORT.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(XSD)) +
        "//status/string()", "valid");
    // check XML result
    query(_VALIDATE_XSD_REPORT.args(FILE, XSD, "1.0"),
        "<report>\n<status>valid</status>\n</report>");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "                 <xs:element name='root'/> " +
      "               </xs:schema> " +
      "return " + _VALIDATE_XSD_REPORT.args(" $doc", " $schema") + "//status/string()", "valid");

    // returned error
    query(_VALIDATE_XSD_REPORT.args(FILE) + "//status/string()", "invalid");
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "                 <xs:element name='unknown'/> " +
      "               </xs:schema> " +
      "return " + _VALIDATE_XSD_REPORT.args(" $doc", " $schema") + "//status/string()", "invalid");

    // check XML result
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "                 <xs:element name='unknown'/> " +
      "               </xs:schema> " +
      "let $report := " + _VALIDATE_XSD_REPORT.args(" $doc", " $schema") +
      "return $report update (" +
      "  delete node .//message/text()," +
      "  for $a in .//@* return replace value of node $a with ''" +
        ')',
      "<report>\n<status>invalid</status>\n" +
      "<message level=\"\" line=\"\" column=\"\"/>\n" +
      "</report>");
    // check URL attribute
    query("exists(" + _VALIDATE_XSD_REPORT.args(INPUT, XSD) + "//@url)", true);

    // invalid arguments
    error(_VALIDATE_XSD_REPORT.args("unknown"), WHICHRES_X);
    error(_VALIDATE_XSD_REPORT.args(FILE, "unknown.xsd"), WHICHRES_X);
    error(_VALIDATE_XSD_REPORT.args(FILE, XSD, "0.99"), BXVA_XSDVERSION_X);
  }

  /** Test method. */
  @Test
  public void dtd() {
    // specify arguments as file paths
    query(_VALIDATE_DTD.args(FILE, DTD), "");
    // specify document as document nodes
    query(_VALIDATE_DTD.args(" doc(\"" + FILE + "\")", DTD), "");
    // specify arguments as file contents
    query(_VALIDATE_DTD.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(DTD)), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT root (#PCDATA)>' " +
      "return validate:dtd($doc, $dtd) ", "");
    // specify embedded schema
    query(_VALIDATE_DTD.args(" ``[<!DOCTYPE p [<!ELEMENT p ANY>]><p/>]``"), "");
    query(_VALIDATE_DTD.args(" ``[<!DOCTYPE p [<!ELEMENT p ANY>]><p/>]``", " ()"), "");

    // invalid arguments
    error(_VALIDATE_DTD.args("unknown"), WHICHRES_X);
    error(_VALIDATE_DTD.args(FILE, "unknown.dtd"), WHICHRES_X);
    error(_VALIDATE_DTD.args(FILE), BXVA_FAIL_X);
    error(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT unknown (#PCDATA)>' " +
      "return " + _VALIDATE_DTD.args(" $doc", " $dtd"), BXVA_FAIL_X);
  }

  /** Test method. */
  @Test
  public void dtdInfo() {
    // specify arguments as file paths
    query(_VALIDATE_DTD_INFO.args(FILE, DTD), "");
    // specify document as document nodes
    query(_VALIDATE_DTD_INFO.args(" doc(\"" + FILE + "\")", DTD), "");
    // specify arguments as file contents
    query(_VALIDATE_DTD_INFO.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(DTD)), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT root (#PCDATA)>' " +
      "return " + _VALIDATE_DTD_INFO.args(" $doc", " $dtd"), "");

    // returned error
    query("exists(" + _VALIDATE_DTD_INFO.args(FILE) + ')', true);
    query("exists(" +
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT unknown (#PCDATA)>' " +
      "return " + _VALIDATE_DTD_INFO.args(" $doc", " $dtd") + ')', true);

    // invalid arguments
    error(_VALIDATE_DTD_INFO.args("unknown"), WHICHRES_X);
    error(_VALIDATE_DTD_INFO.args(FILE, "unknown.dtd"), WHICHRES_X);
  }

  /** Test method. */
  @Test
  public void dtdReport() {
    // check XML result
    query(_VALIDATE_DTD_REPORT.args(FILE, DTD), "<report>\n<status>valid</status>\n</report>");
    // specify arguments as file paths
    query(_VALIDATE_DTD_REPORT.args(FILE, DTD) + "//status/string()", "valid");
    // specify document as document nodes
    query(_VALIDATE_DTD_REPORT.args(" doc(\"" + FILE + "\")", DTD) + "//status/string()", "valid");
    // specify arguments as file contents
    query(_VALIDATE_DTD_REPORT.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(DTD)) +
        "//status/string()", "valid");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT root (#PCDATA)>' " +
      "return " + _VALIDATE_DTD_REPORT.args(" $doc", " $dtd") + "//status/string()", "valid");

    // returned error
    query(_VALIDATE_DTD_REPORT.args(FILE) + "//status/string()", "invalid");
    query(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT unknown (#PCDATA)>' " +
      "return " + _VALIDATE_DTD_REPORT.args(" $doc", " $dtd") + "//status/string()", "invalid");

    // check XML result
    query(
      "let $doc := <root/> " +
      "let $schema := '<!ELEMENT unknown (#PCDATA)>' " +
      "let $report := " + _VALIDATE_DTD_REPORT.args(" $doc", " $schema") +
      "return $report update (" +
      "  delete node .//message/text()," +
      "  for $a in .//@* return replace value of node $a with ''" +
        ')',
      "<report>\n<status>invalid</status>\n" +
      "<message level=\"\" line=\"\" column=\"\"/>\n" +
      "</report>");
    // check URL attribute
    query("exists(" + _VALIDATE_DTD_REPORT.args(INPUT, DTD) + "//@url)", true);

    // invalid arguments
    error(_VALIDATE_DTD_REPORT.args("unknown"), WHICHRES_X);
    error(_VALIDATE_DTD_REPORT.args(FILE, "unknown.dtd"), WHICHRES_X);
  }
}

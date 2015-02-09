package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Validation Module.
 *
 * @author BaseX Team 2005-15, BSD License
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

  /** Test method. */
  @Test
  public void xsd() {
    // specify arguments as file paths
    query(_VALIDATE_XSD.args(FILE, XSD), "");
    // specify arguments as document nodes
    query(_VALIDATE_XSD.args(DOC.args(FILE), DOC.args(XSD)), "");
    // specify arguments as file contents
    query(_VALIDATE_XSD.args(_FILE_READ_TEXT.args(FILE), _FILE_READ_TEXT.args(XSD)), "");
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
    query(_VALIDATE_XSD_INFO.args(DOC.args(FILE), DOC.args(XSD)), "");
    // specify arguments as file contents
    query(_VALIDATE_XSD_INFO.args(_FILE_READ_TEXT.args(FILE),
        _FILE_READ_TEXT.args(XSD)), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
      "<xs:element name='root'/> " +
      "</xs:schema> " +
      "return validate:xsd-info($doc, $schema)", "");

    // returned error
    query(EXISTS.args(_VALIDATE_XSD_INFO.args(FILE)), "true");
    query(EXISTS.args(
        "let $doc := <root/> " +
        "let $schema := <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'> " +
        "<xs:element name='unknown'/> " +
        "</xs:schema> " +
        "return validate:xsd-info($doc, $schema)"), "true");

    // invalid arguments
    error(_VALIDATE_XSD_INFO.args("unknown"), WHICHRES_X);
    error(_VALIDATE_XSD_INFO.args(FILE, "unknown.xsd"), WHICHRES_X);
  }

  /** Test method. */
  @Test
  public void dtd() {
    // specify arguments as file paths
    query(_VALIDATE_DTD.args(FILE, DTD), "");
    // specify document as document nodes
    query(_VALIDATE_DTD.args(DOC.args(FILE), DTD), "");
    // specify arguments as file contents
    query(_VALIDATE_DTD.args(_FILE_READ_TEXT.args(FILE),
        _FILE_READ_TEXT.args(DTD)), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT root (#PCDATA)>' " +
      "return validate:dtd($doc, $dtd) ", "");

    // invalid arguments
    error(_VALIDATE_DTD.args("unknown"), WHICHRES_X);
    error(_VALIDATE_DTD.args(FILE, "unknown.dtd"), WHICHRES_X);
    error(_VALIDATE_DTD.args(FILE), BXVA_FAIL_X);
    error(
        "let $doc := <root/> " +
        "let $dtd := '<!ELEMENT unknown (#PCDATA)>' " +
        "return validate:dtd($doc, $dtd) ", BXVA_FAIL_X);
  }

  /** Test method. */
  @Test
  public void dtdInfo() {
    // specify arguments as file paths
    query(_VALIDATE_DTD_INFO.args(FILE, DTD), "");
    // specify document as document nodes
    query(_VALIDATE_DTD_INFO.args(DOC.args(FILE), DTD), "");
    // specify arguments as file contents
    query(_VALIDATE_DTD_INFO.args(_FILE_READ_TEXT.args(FILE),
        _FILE_READ_TEXT.args(DTD)), "");
    // specify main-memory fragments as arguments
    query(
      "let $doc := <root/> " +
      "let $dtd := '<!ELEMENT root (#PCDATA)>' " +
      "return validate:dtd($doc, $dtd) ", "");

    // returned error
    query(EXISTS.args(_VALIDATE_DTD_INFO.args(FILE)), "true");
    query(EXISTS.args(
        "let $doc := <root/> " +
        "let $dtd := '<!ELEMENT unknown (#PCDATA)>' " +
        "return validate:dtd($doc, $dtd) "), "true");

    // invalid arguments
    error(_VALIDATE_DTD_INFO.args("unknown"), WHICHRES_X);
    error(_VALIDATE_DTD_INFO.args(FILE, "unknown.dtd"), WHICHRES_X);
  }
}

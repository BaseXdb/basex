package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Fetch Module.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FetchModuleTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String XML = "src/test/resources/input.xml";
  /** Test file. */
  private static final String CSV = "src/test/resources/input.csv";

  /** Test method. */
  @Test
  public void text() {
    query(_FETCH_TEXT.args(XML));
    error(_FETCH_TEXT.args(XML + 'x'), BXFE_IO_X);
    error(_FETCH_TEXT.args(XML, "xxx"), BXFE_ENCODING_X);
  }

  /** Test method. */
  @Test
  public void xml() {
    query(_FETCH_XML.args(XML));
    query("exists(" + _FETCH_XML.args(XML, " map { 'chop':true() }") +
        "//text()[not(normalize-space())])", "false");
    query("exists(" + _FETCH_XML.args(XML, " map { 'chop':false() }") +
        "//text()[not(normalize-space())])", "true");
    query(COUNT.args(_FETCH_XML.args(CSV,
        " map { 'parser':'csv','csvparser': 'header=true' }") + "//City"), "3");
    query(COUNT.args(_FETCH_XML.args(CSV,
        " map { 'parser':'csv','csvparser': map { 'header': true() } }") + "//City"), "3");
    query(COUNT.args(_FETCH_XML.args(CSV,
        " map { 'parser':'csv','csvparser': map { 'header': 'true' } }") + "//City"), "3");
    error(_FETCH_XML.args(XML, " map { 'parser': 'unknown' }"), BASX_VALUE_X_X);
    error(_FETCH_XML.args(XML + 'x'), BXFE_IO_X);
  }

  /** Test method. */
  @Test
  public void xmlBinary() {
    query(_FETCH_XML_BINARY.args(_CONVERT_STRING_TO_BASE64.args(" '<x/>'")), "<x/>");
    final String encoding = "CP1252";
    final String xml = "<x>Ä</x>";
    final String data = "<?xml version=''1.0'' encoding=''" + encoding + "''?>" + xml;
    query(_FETCH_XML_BINARY.args(_CONVERT_STRING_TO_BASE64.args(" '" + data + "'", encoding)), xml);
  }

  /** Test method. */
  @Test
  public void binary() {
    query(_FETCH_BINARY.args(XML));
    error(_FETCH_BINARY.args(XML + 'x'), BXFE_IO_X);
  }

  /** Test method. */
  @Test
  public void contentType() {
    query(_FETCH_CONTENT_TYPE.args(XML));
    error(_FETCH_CONTENT_TYPE.args(XML + 'x'), BXFE_IO_X);
  }
}

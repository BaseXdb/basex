package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the functions of the zip2 module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNZip2Test extends AdvancedQueryTest {
  /** Test ZIP file. */
  private static final String ZIP = "src/test/resources/xml.zip";

  /**
   * Test method for the zip2:create() function.
   */
  @Test
  public void zip2Create() {
    check(_ZIP2_CREATE);
    // simple zip files
    query(COUNT.args(_ZIP2_CREATE.args("<entry>X</entry>", "")), "1");
    query(COUNT.args(_ZIP2_CREATE.args("<entry level='9'>X</entry>", "")), "1");
    query(COUNT.args(_ZIP2_CREATE.args("<entry encoding='US-ASCII'>X</entry>", "")), "1");
    query(COUNT.args(_ZIP2_CREATE.args(
        "<entry last-modified='2000-01-01T12:12:12'>X</entry>", "")), "1");

    // different number of entries and contents
    error(_ZIP2_CREATE.args("<entry>X</entry>", "()"), Err.ZIP2_DIFF);
    // name must not be empty
    error(_ZIP2_CREATE.args("<entry/>", ""), Err.ZIP2_NAME);
    // invalid compression level
    error(_ZIP2_CREATE.args("<entry compression-level='x'>X</entry>", ""),
        Err.ZIP2_LEVEL);
    error(_ZIP2_CREATE.args("<entry compression-level='10'>X</entry>", ""),
        Err.ZIP2_LEVEL);
    // invalid modification date
    error(_ZIP2_CREATE.args("<entry last-modified='2020'>X</entry>", ""),
        Err.ZIP2_MODIFIED);
    // content must be string or base64Binary
    error(_ZIP2_CREATE.args("<entry>X</entry>", " 123"), Err.ZIP2_STRB64);
    // wrong encoding
    error(_ZIP2_CREATE.args("<entry encoding='x'>X</entry>", ""), Err.ZIP2_ENCODING);
    // errors while converting a string
    error(_ZIP2_CREATE.args("<entry encoding='US-ASCII'>X</entry>", "\u00fc"),
        Err.ZIP2_ENCODE);
  }

  /**
   * Test method for the zip2:entries() function.
   */
  @Test
  public void zip2Entries() {
    check(_ZIP2_ENTRIES);
    // read entries
    query(COUNT.args(_ZIP2_ENTRIES.args(_FILE_READ_BINARY.args(ZIP))), "5");
    // simple zip files
    query(COUNT.args(_ZIP2_ENTRIES.args(_FILE_READ_BINARY.args(ZIP)) +
        "[@size][@last-modified][@compressed-size]"), "5");
  }

  /**
   * Test method for the zip2:extract-texts() function.
   */
  @Test
  public void zip2ExtractTexts() {
    check(_ZIP2_EXTRACT_TEXT);
    // extract all entries
    query(COUNT.args(_ZIP2_EXTRACT_TEXT.args(_FILE_READ_BINARY.args(ZIP))), "5");
    // extract all entries
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ZIP2_ENTRIES.args("$a") +
          "return " + COUNT.args(_ZIP2_EXTRACT_TEXT.args("$a", "$b")), 5);
    // extract single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ZIP2_EXTRACT_TEXT.args("$a", "test/input.xml") +
          "let $c := " + PARSE_XML.args("$b") +
          "return $c//title/text()", "XML");
  }

  /**
   * Test method for the zip2:extract-binaries() function.
   */
  @Test
  public void zip2ExtractBinary() {
    check(_ZIP2_EXTRACT_BINARY);
    // extract all entries
    query(COUNT.args(_ZIP2_EXTRACT_BINARY.args(_FILE_READ_BINARY.args(ZIP))), "5");
    // extract all entries
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ZIP2_ENTRIES.args("$a") +
          "return count(" + _ZIP2_EXTRACT_BINARY.args("$a", "$b") + ")", 5);
    // extract single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ZIP2_EXTRACT_BINARY.args("$a", "test/input.xml") +
          "let $c := " + _CONVERT_TO_STRING.args("$b") +
          "let $d := " + PARSE_XML.args("$c") +
          "return $d//title/text()", "XML");
  }

  /**
   * Test method for the zip2:update() function.
   */
  @Test
  public void zip2Update() {
    check(_ZIP2_UPDATE);
    // add a new entry
    query(_FILE_READ_BINARY.args(ZIP) + " ! " +
        _ZIP2_UPDATE.args(" .", "<entry>X</entry>", "X") + " ! " +
        COUNT.args(_ZIP2_ENTRIES.args(" .")), 6);
    query(_ZIP2_CREATE.args("<entry>X</entry>", "X") + " ! " +
        _ZIP2_UPDATE.args(" .", "<entry>Y</entry>", "Y") + " ! " +
        _ZIP2_EXTRACT_TEXT.args(" ."), "X Y");
    // updates an existing entry
    query(_ZIP2_CREATE.args("<entry>X</entry>", "X") + " ! " +
        _ZIP2_UPDATE.args(" .", "<entry>X</entry>", "Y") + " ! " +
        _ZIP2_EXTRACT_TEXT.args(" ."), "Y");
  }

  /**
   * Test method for the zip2:delete() function.
   */
  @Test
  public void zip2Delete() {
    check(_ZIP2_DELETE);
    // delete single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ZIP2_DELETE.args("$a", "infos/stopWords") +
          "let $c := " + _ZIP2_ENTRIES.args("$b") +
          "return count($c)", 4);
    // delete all entries except for the first
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ZIP2_ENTRIES.args("$a") +
          "let $c := " + _ZIP2_DELETE.args("$a", "($b/text())[position() > 1]") +
          "return count(zip2:entries($c))", "1");
  }
}

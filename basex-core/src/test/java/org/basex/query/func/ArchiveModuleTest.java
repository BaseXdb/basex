package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.io.*;
import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Archive Module.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ArchiveModuleTest extends AdvancedQueryTest {
  /** Test ZIP file. */
  private static final String ZIP = "src/test/resources/xml.zip";
  /** Test GZIP file. */
  private static final String GZIP = "src/test/resources/xml.gz";
  /** Test file. */
  private static final String DIR = "src/test/resources/dir";

  /** Test method. */
  @Test
  public void create() {
    // simple zip files
    count(_ARCHIVE_CREATE.args("X", ""), 1);

    // simple zip files
    count(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", ""), 1);
    count(_ARCHIVE_CREATE.args("<archive:entry level='9'>X</archive:entry>", ""), 1);
    count(_ARCHIVE_CREATE.args("<archive:entry encoding='US-ASCII'>X</archive:entry>", ""), 1);
    count(_ARCHIVE_CREATE.args("<archive:entry " +
        "last-modified='2000-01-01T12:12:12'>X</archive:entry>", ""), 1);
    count(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "", " map { }"), 1);
    count(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "",
        " map { 'format':'zip', 'algorithm':'deflate' }"), 1);
    count(_ARCHIVE_CREATE.args("X", "", "<archive:options/>"), 1);
    count(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "",
        "<archive:options><archive:format value='zip'/>" +
        "<archive:algorithm value='deflate'/></archive:options>"), 1);
    count(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "",
        "<archive:options><archive:format value='zip'/></archive:options>"), 1);
    count(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "",
        "<archive:options><archive:format value='gzip'/></archive:options>"), 1);

    // different number of entries and contents
    error(_ARCHIVE_CREATE.args("X", "()"), ARCH_DIFF_X_X);
    // name must not be empty
    error(_ARCHIVE_CREATE.args("<archive:entry/>", ""), ARCH_EMPTY);
    // invalid compression level
    error(_ARCHIVE_CREATE.args("<archive:entry compression-level='x'>X</archive:entry>", ""),
        ARCH_LEVEL_X);
    error(_ARCHIVE_CREATE.args("<archive:entry compression-level='10'>X</archive:entry>", ""),
        ARCH_LEVEL_X);
    // invalid modification date
    error(_ARCHIVE_CREATE.args("<archive:entry last-modified='2020'>X</archive:entry>", ""),
        ARCH_DATETIME_X);
    // content must be string or binary
    error(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", " 123"), STRBIN_X_X);
    // wrong encoding
    error(_ARCHIVE_CREATE.args("<archive:entry encoding='x'>X</archive:entry>", ""),
        ARCH_ENCODING_X);
    // errors while converting a string
    error(_ARCHIVE_CREATE.args("<archive:entry encoding='US-ASCII'>X</archive:entry>",
        "\u00fc"), ARCH_ENCODE_X);
    // format not supported
    error(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "", " map { 'format':'rar' }"),
        ARCH_UNKNOWN);
    // unknown option
    error(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "", " map { 'x':'y' }"),
        INVALIDOPT_X);
    error(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "",
        "<archive:options><archive:format value='rar'/></archive:options>"), ARCH_UNKNOWN);
    // algorithm not supported
    error(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "",
        "<archive:options><archive:algorithm value='unknown'/></archive:options>"), ARCH_SUPP_X_X);
    // algorithm not supported
    error(_ARCHIVE_CREATE.args("('x','y')", "('a','b')",
        "<archive:options><archive:format value='gzip'/></archive:options>"), ARCH_ONE_X);
  }

  /** Test method. */
  @Test
  public void createFrom() {
    count(_ARCHIVE_CREATE_FROM.args(DIR), 4);
    count(_ARCHIVE_CREATE_FROM.args(DIR, " map { }"), 4);
    count(_ARCHIVE_CREATE_FROM.args(DIR, " map { 'algorithm': 'stored' }"), 4);
    query(PARSE_XML.args(_ARCHIVE_EXTRACT_TEXT.args(_ARCHIVE_CREATE_FROM.args(DIR, " map { }"),
        "input.xml")) + " instance of document-node()", "true");

    // errors
    error(_ARCHIVE_CREATE_FROM.args("UNUNUNKNOWN"), FILE_NO_DIR_X);
    error(_ARCHIVE_CREATE_FROM.args(DIR, " map { }", "UNUNUNKNOWN"), FILE_NOT_FOUND_X);
    error(_ARCHIVE_CREATE_FROM.args(DIR, " map { }", "."), FILE_IS_DIR_X);
  }

  /** Test method. */
  @Test
  public void entries() {
    // read entries
    query(COUNT.args(_ARCHIVE_ENTRIES.args(_FILE_READ_BINARY.args(ZIP))), 5);
    // simple zip files
    query(COUNT.args(_ARCHIVE_ENTRIES.args(_FILE_READ_BINARY.args(ZIP)) +
        "[@size][@last-modified][@compressed-size]"), 5);
    // simple gzip files
    query(COUNT.args(_ARCHIVE_ENTRIES.args(_FILE_READ_BINARY.args(GZIP)) +
        "[not(@size)][not(@last-modified)][not(@compressed-size)][not(text())]"), 1);
  }

  /** Test method. */
  @Test
  public void options() {
    // read entries
    query(_ARCHIVE_OPTIONS.args(_FILE_READ_BINARY.args(ZIP)) + "//@value/data()",
        "zip\ndeflate");
    query(_ARCHIVE_OPTIONS.args(_FILE_READ_BINARY.args(GZIP)) + "//@value/data()",
        "gzip\ndeflate");
  }

  /** Test method. */
  @Test
  public void extractText() {
    // extract all entries
    query(COUNT.args(_ARCHIVE_EXTRACT_TEXT.args(_FILE_READ_BINARY.args(ZIP))), 5);
    // extract all entries
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_ENTRIES.args("$a") +
          "return " + COUNT.args(_ARCHIVE_EXTRACT_TEXT.args("$a", "$b")), 5);
    // extract single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_EXTRACT_TEXT.args("$a", "test/input.xml") +
          "let $c := " + PARSE_XML.args("$b") +
          "return $c//title/text()", "XML");
    // extract single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_EXTRACT_TEXT.args("$a",
              "<archive:entry>test/input.xml</archive:entry>") +
          "let $c := " + PARSE_XML.args("$b") +
          "return $c//title/text()", "XML");
  }

  /** Test method. */
  @Test
  public void extractBinary() {
    // extract all entries
    query(COUNT.args(_ARCHIVE_EXTRACT_BINARY.args(_FILE_READ_BINARY.args(ZIP))), 5);
    // extract all entries
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_ENTRIES.args("$a") +
          "return count(" + _ARCHIVE_EXTRACT_BINARY.args("$a", "$b") + ')', 5);
    // extract single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_EXTRACT_BINARY.args("$a", "test/input.xml") +
          "let $c := " + _CONVERT_BINARY_TO_STRING.args("$b") +
          "let $d := " + PARSE_XML.args("$c") +
          "return $d//title/text()", "XML");
    // extract single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_EXTRACT_BINARY.args("$a",
              "<archive:entry>test/input.xml</archive:entry>") +
          "let $c := " + _CONVERT_BINARY_TO_STRING.args("$b") +
          "let $d := " + PARSE_XML.args("$c") +
          "return $d//title/text()", "XML");
  }

  /** Test method. */
  @Test
  public void extractTo() {
    // write archive and count number of entries
    final String tmp = new IOFile(sandbox(), "tmp").path();
    query(_ARCHIVE_EXTRACT_TO.args(tmp, _FILE_READ_BINARY.args(ZIP)));
    count(_FILE_READ_BINARY.args(ZIP), 5);
    // write archive and count number of entries
    query(_ARCHIVE_EXTRACT_TO.args(tmp, _FILE_READ_BINARY.args(ZIP),
        _ARCHIVE_ENTRIES.args(_FILE_READ_BINARY.args(ZIP))));
    count(_FILE_READ_BINARY.args(ZIP), 5);

    query("let $a := " + _ARCHIVE_ENTRIES.args(
      _FILE_READ_BINARY.args(ZIP)) + "/string() " +
      "let $f := " + _FILE_LIST.args(tmp, "true()") + '[' +
      _FILE_IS_FILE.args(" '" + tmp + "/'||.") + "] ! replace(., '\\\\', '/') " +
      "return (every $e in $a satisfies $e = $f) and (every $e in $f satisfies $e =$ a)",
      "true");
  }

  /** Test method. */
  @Test
  public void update() {
    // add a new entry
    query(_FILE_READ_BINARY.args(ZIP) + " ! " +
        _ARCHIVE_UPDATE.args(" .", "X", "X") + " ! " +
        COUNT.args(_ARCHIVE_ENTRIES.args(" .")), 6);
    // add a new entry
    query(_FILE_READ_BINARY.args(ZIP) + " ! " +
        _ARCHIVE_UPDATE.args(" .", "<archive:entry>X</archive:entry>", "X") + " ! " +
        COUNT.args(_ARCHIVE_ENTRIES.args(" .")), 6);
    query(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "X") + " ! " +
        _ARCHIVE_UPDATE.args(" .", "<archive:entry>Y</archive:entry>", "Y") + " ! " +
        _ARCHIVE_EXTRACT_TEXT.args(" ."), "X\nY");
    // updates an existing entry
    query(_ARCHIVE_CREATE.args("<archive:entry>X</archive:entry>", "X") + " ! " +
        _ARCHIVE_UPDATE.args(" .", "<archive:entry>X</archive:entry>", "Y") + " ! " +
        _ARCHIVE_EXTRACT_TEXT.args(" ."), "Y");
    // updates an existing entry
    error(_ARCHIVE_CREATE.args("X", "X",
        "<archive:options><archive:format value='gzip'/></archive:options>") + " ! " +
        _ARCHIVE_UPDATE.args(" .", "X", "Y"), ARCH_MODIFY_X);
  }

  /** Test method. */
  @Test
  public void delete() {
    // delete single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_DELETE.args("$a", "infos/stopWords") +
          "let $c := " + _ARCHIVE_ENTRIES.args("$b") +
          "return count($c)", 4);
    // delete all entries except for the first
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_ENTRIES.args("$a") +
          "let $c := " + _ARCHIVE_DELETE.args("$a", "$b[position() > 1]") +
          "return count(archive:entries($c))", "1");
    // updates an existing entry
    error(_ARCHIVE_CREATE.args("X", "X",
        "<archive:options><archive:format value='gzip'/></archive:options>") + " ! " +
        _ARCHIVE_DELETE.args(" .", "X"), ARCH_MODIFY_X);
  }

  /**
   * Counts the entries of an archive.
   * @param archive archive
   * @param exp expected number of results
   */
  private void count(final String archive, final int exp) {
    query(COUNT.args(_ARCHIVE_ENTRIES.args(archive)), exp);
  }
}

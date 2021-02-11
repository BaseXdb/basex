package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Archive Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArchiveModuleTest extends SandboxTest {
  /** Test ZIP file. */
  private static final String ZIP = "src/test/resources/xml.zip";
  /** Test GZIP file. */
  private static final String GZIP = "src/test/resources/xml.gz";
  /** Test file. */
  private static final String DIR = "src/test/resources/dir";

  /** Test method. */
  @Test public void create() {
    final Function func = _ARCHIVE_CREATE;

    // simple zip files
    count(func.args("X", ""), 1);

    // simple zip files
    count(func.args(" <archive:entry>X</archive:entry>", ""), 1);
    count(func.args(" <archive:entry level='9'>X</archive:entry>", ""), 1);
    count(func.args(" <archive:entry encoding='US-ASCII'>X</archive:entry>", ""), 1);
    count(func.args(" <archive:entry " +
        "last-modified='2000-01-01T12:12:12'>X</archive:entry>", ""), 1);
    count(func.args(" <archive:entry>X</archive:entry>", "", " map { }"), 1);
    count(func.args(" <archive:entry>X</archive:entry>", "",
        " map { 'format':'zip', 'algorithm':'deflate' }"), 1);
    count(func.args("X", "", " map {}"), 1);
    count(func.args(" <archive:entry>X</archive:entry>", "",
        " map { 'format': 'zip', 'algorithm': 'deflate' }"), 1);
    count(func.args(" <archive:entry>X</archive:entry>", "",
        " map { 'format': 'zip' }"), 1);
    count(func.args(" <archive:entry>X</archive:entry>", "",
        " map { 'format': 'gzip' }"), 1);

    // different number of entries and contents
    error(func.args("X", " ()"), ARCHIVE_NUMBER_X_X);
    // name must not be empty
    error(func.args(" <archive:entry/>", ""), ARCHIVE_DESCRIPTOR1);
    // invalid compression level
    error(func.args(" <archive:entry compression-level='x'>X</archive:entry>", ""),
        ARCHIVE_DESCRIPTOR2_X);
    error(func.args(" <archive:entry compression-level='10'>X</archive:entry>", ""),
        ARCHIVE_DESCRIPTOR2_X);
    // invalid modification date
    error(func.args(" <archive:entry last-modified='2020'>X</archive:entry>", ""),
        ARCHIVE_DESCRIPTOR3_X);
    // content must be string or binary
    error(func.args(" <archive:entry>X</archive:entry>", " 123"), STRBIN_X_X);
    // wrong encoding
    error(func.args(" <archive:entry encoding='x'>X</archive:entry>", ""), ARCHIVE_ENCODE1_X);
    // errors while converting a string
    error(func.args(" <archive:entry encoding='US-ASCII'>X</archive:entry>", "\u00fc"),
        ARCHIVE_ENCODE2_X);
    // format not supported
    error(func.args(" <archive:entry>X</archive:entry>", "", " map { 'format':'rar' }"),
        ARCHIVE_FORMAT);
    // unknown option
    error(func.args(" <archive:entry>X</archive:entry>", "", " map { 'x':'y' }"), INVALIDOPT_X);
    error(func.args(" <archive:entry>X</archive:entry>", "", " map { 'format': 'xxx' }"),
        ARCHIVE_FORMAT);
    // algorithm not supported
    error(func.args(" <archive:entry>X</archive:entry>", "", " map { 'algorithm': 'unknown' }"),
        ARCHIVE_FORMAT_X_X);
    // algorithm not supported
    error(func.args(" ('x','y')", " ('a','b')", " map { 'format': 'gzip' }"), ARCHIVE_SINGLE_X);
  }

  /** Test method. */
  @Test public void createFrom() {
    final Function func = _ARCHIVE_CREATE_FROM;
    count(func.args(DIR), 5);
    count(func.args(DIR, " map { }"), 5);
    count(func.args(DIR, " map { 'algorithm': 'stored' }"), 5);

    count(func.args(DIR, " map { 'recursive': false() }"), 4);
    query(_ARCHIVE_ENTRIES.args(func.args(DIR, " map { 'root-dir': true() }")) +
        "[not(starts-with(., 'dir/'))]", "");

    query("parse-xml(" + _ARCHIVE_EXTRACT_TEXT.args(func.args(DIR, " map { }"),
        "input.xml") + ") instance of document-node()", true);

    // errors
    error(func.args("UNUNUNKNOWN"), FILE_NO_DIR_X);
    error(func.args(DIR, " map { }", "UNUNUNKNOWN"), FILE_NOT_FOUND_X);
    error(func.args(DIR, " map { }", "."), FILE_IS_DIR_X);
  }

  /** Test method. */
  @Test public void delete() {
    final Function func = _ARCHIVE_DELETE;
    // delete single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + func.args(" $a", "infos/stopWords") +
          "let $c := " + _ARCHIVE_ENTRIES.args(" $b") +
          "return count($c)", 4);
    // delete all entries except for the first
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_ENTRIES.args(" $a") +
          "let $c := " + func.args(" $a", " $b[position() > 1]") +
          "return count(archive:entries($c))", 1);
    // updates an existing entry
    error(_ARCHIVE_CREATE.args("X", "X", " map { 'format': 'gzip' }") + " ! " +
        func.args(" .", "X"), ARCHIVE_MODIFY_X);
  }

  /** Test method. */
  @Test public void entries() {
    final Function func = _ARCHIVE_ENTRIES;
    // read entries
    query(COUNT.args(func.args(_FILE_READ_BINARY.args(ZIP))), 5);
    // simple zip files
    query(COUNT.args(func.args(_FILE_READ_BINARY.args(ZIP)) +
        "[@size][@last-modified][@compressed-size]"), 5);
    // simple gzip files
    query(COUNT.args(func.args(_FILE_READ_BINARY.args(GZIP)) +
        "[not(@size)][not(@last-modified)][not(@compressed-size)][not(text())]"), 1);
  }

  /** Test method. */
  @Test public void extractBinary() {
    final Function func = _ARCHIVE_EXTRACT_BINARY;
    // extract all entries
    query(COUNT.args(func.args(_FILE_READ_BINARY.args(ZIP))), 5);
    // extract all entries
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_ENTRIES.args(" $a") +
          "return count(" + func.args(" $a", " $b") + ')', 5);
    // extract single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + func.args(" $a", "test/input.xml") +
          "let $c := " + _CONVERT_BINARY_TO_STRING.args(" $b") +
          "let $d := " + PARSE_XML.args(" $c") +
          "return $d//title/text()", "XML");
    // extract single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + func.args(" $a", " <archive:entry>test/input.xml</archive:entry>") +
          "let $c := " + _CONVERT_BINARY_TO_STRING.args(" $b") +
          "let $d := " + PARSE_XML.args(" $c") +
          "return $d//title/text()", "XML");
  }

  /** Test method. */
  @Test public void extractText() {
    final Function func = _ARCHIVE_EXTRACT_TEXT;
    // extract all entries
    query(COUNT.args(func.args(_FILE_READ_BINARY.args(ZIP))), 5);
    // extract all entries
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + _ARCHIVE_ENTRIES.args(" $a") +
          "return " + COUNT.args(func.args(" $a", " $b")), 5);
    // extract single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + func.args(" $a", "test/input.xml") +
          "let $c := " + PARSE_XML.args(" $b") +
          "return $c//title/text()", "XML");
    // extract single entry
    query("let $a := " + _FILE_READ_BINARY.args(ZIP) +
          "let $b := " + func.args(" $a", " <archive:entry>test/input.xml</archive:entry>") +
          "let $c := " + PARSE_XML.args(" $b") +
          "return $c//title/text()", "XML");
  }

  /** Test method. */
  @Test public void extractTo() {
    final Function func = _ARCHIVE_EXTRACT_TO;
    final String tmp = new IOFile(sandbox(), "tmp").path();
    // write archive and count number of entries
    query(func.args(tmp, _FILE_READ_BINARY.args(ZIP)));
    count(_FILE_READ_BINARY.args(ZIP), 5);
    // write archive and count number of entries
    query(func.args(tmp, _FILE_READ_BINARY.args(ZIP),
        _ARCHIVE_ENTRIES.args(_FILE_READ_BINARY.args(ZIP))));
    count(_FILE_READ_BINARY.args(ZIP), 5);

    query("let $a := " + _ARCHIVE_ENTRIES.args(
      _FILE_READ_BINARY.args(ZIP)) + "/string() " +
      "let $f := " + _FILE_LIST.args(tmp, true) + '[' +
      _FILE_IS_FILE.args(" '" + tmp + "/'||.") + "] ! replace(., '\\\\', '/') " +
      "return (every $e in $a satisfies $e = $f) and (every $e in $f satisfies $e =$ a)",
      true);
  }

  /** Test method. */
  @Test public void options() {
    final Function func = _ARCHIVE_OPTIONS;
    // read entries
    query(func.args(_FILE_READ_BINARY.args(ZIP)) + "?format", "zip");
    query(func.args(_FILE_READ_BINARY.args(GZIP)) + "?algorithm", "deflate");
  }

  /** Test method. */
  @Test public void update() {
    final Function func = _ARCHIVE_UPDATE;
    // add a new entry
    query(_FILE_READ_BINARY.args(ZIP) + " ! " +
        func.args(" .", "X", "X") + " ! " +
        COUNT.args(_ARCHIVE_ENTRIES.args(" .")), 6);
    // add a new entry
    query(_FILE_READ_BINARY.args(ZIP) + " ! " +
        func.args(" .", " <archive:entry>X</archive:entry>", "X") + " ! " +
        COUNT.args(_ARCHIVE_ENTRIES.args(" .")), 6);
    query(_ARCHIVE_CREATE.args(" <archive:entry>X</archive:entry>", "X") + " ! " +
        func.args(" .", " <archive:entry>Y</archive:entry>", "Y") + " ! " +
        _ARCHIVE_EXTRACT_TEXT.args(" ."), "X\nY");
    // updates an existing entry
    query(_ARCHIVE_CREATE.args(" <archive:entry>X</archive:entry>", "X") + " ! " +
        func.args(" .", " <archive:entry>X</archive:entry>", "Y") + " ! " +
        _ARCHIVE_EXTRACT_TEXT.args(" ."), "Y");
    // updates an existing entry
    error(_ARCHIVE_CREATE.args("X", "X", " map { 'format': 'gzip' }") + " ! " +
        func.args(" .", "X", "Y"), ARCHIVE_MODIFY_X);
  }

  /**
   * Counts the entries of an archive.
   * @param archive archive
   * @param exp expected number of results
   */
  private static void count(final String archive, final int exp) {
    query(COUNT.args(_ARCHIVE_ENTRIES.args(archive)), exp);
  }
}

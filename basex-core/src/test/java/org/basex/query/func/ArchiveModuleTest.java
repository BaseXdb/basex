package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.*;
import java.util.zip.*;

import org.basex.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Archive Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ArchiveModuleTest extends SandboxTest {
  /** Test ZIP file. */
  private static final String ZIP = "src/test/resources/xml.zip";
  /** Test GZIP file. */
  private static final String GZIP = "src/test/resources/xml.gz";
  /** Test ZIP: legacy CP437-encoded entry name, UTF-8 flag (bit 11) not set. */
  private static final String ZIP_CP437 = "src/test/resources/cp437.zip";
  /** Test ZIP: UTF-8-encoded entry name, UTF-8 flag set (modern, spec-conformant). */
  private static final String ZIP_UTF8 = "src/test/resources/utf8.zip";
  /** Test ZIP: ASCII-only entry name, UTF-8 flag not set. */
  private static final String ZIP_ASCII = "src/test/resources/ascii.zip";
  /** Test ZIP: Shift_JIS-encoded entry name, UTF-8 flag not set (non-CP437 codepage). */
  private static final String ZIP_SJIS = "src/test/resources/shiftjis.zip";
  /** Test ZIP: UTF-8-encoded entry name but UTF-8 flag not set (non-conformant). */
  private static final String ZIP_UTF8_NO_FLAG = "src/test/resources/utf8_no_bit11.zip";
  /** Test file. */
  private static final String DIR = "src/test/resources/dir";

  /** Test method. */
  @Test public void create() {
    final Function func = _ARCHIVE_CREATE;

    // simple zip files
    countEntries(func.args("X", ""), 1);
    countEntries(func.args("X", " []"), 0);
    countEntries(func.args(" ('X', 'Y')", " ('', [])"), 1);
    countEntries(func.args(" ('X', 'Y')", " (['x'], [])"), 1);

    // simple zip files
    countEntries(func.args(" <archive:entry>X</archive:entry>", ""), 1);
    countEntries(func.args(" <archive:entry level='9'>X</archive:entry>", ""), 1);
    countEntries(func.args(" <archive:entry encoding='US-ASCII'>X</archive:entry>", ""), 1);
    countEntries(func.args(" <archive:entry " +
        "last-modified='2000-01-01T12:12:12'>X</archive:entry>", ""), 1);
    countEntries(func.args(" <archive:entry>X</archive:entry>", "", " {}"), 1);
    countEntries(func.args(" <archive:entry>X</archive:entry>", "",
        " { 'format': 'zip', 'algorithm': 'deflate' }"), 1);
    countEntries(func.args("X", "", " {}"), 1);
    countEntries(func.args(" <archive:entry>X</archive:entry>", "",
        " { 'format': 'zip', 'algorithm': 'deflate' }"), 1);
    countEntries(func.args(" <archive:entry>X</archive:entry>", "",
        " { 'format': 'zip' }"), 1);
    countEntries(func.args(" <archive:entry>X</archive:entry>", "",
        " { 'format': 'gzip' }"), 1);

    // different number of entries and contents
    error(func.args("X", " ()"), ARCHIVE_NUMBER_X_X);
    // name must not be empty
    error(func.args(" <archive:entry/>", ""), ARCHIVE_NAME);
    // invalid compression level
    error(func.args(" <archive:entry compression-level='x'>X</archive:entry>", ""),
        ARCHIVE_LEVEL_X);
    error(func.args(" <archive:entry compression-level='10'>X</archive:entry>", ""),
        ARCHIVE_LEVEL_X);
    // invalid modification date
    error(func.args(" <archive:entry last-modified='2020'>X</archive:entry>", ""),
        ARCHIVE_TIMESTAMP_X);
    // content must be string or binary
    error(func.args(" <archive:entry>X</archive:entry>", " 123"), STRBIN_X_X);
    // wrong encoding
    error(func.args(" <archive:entry encoding='x'>X</archive:entry>", ""), ARCHIVE_ENCODE1_X);
    // errors while converting a string
    error(func.args(" <archive:entry encoding='US-ASCII'>X</archive:entry>", "\u00fc"),
        ARCHIVE_ENCODE2_X);
    // format not supported
    error(func.args(" <archive:entry>X</archive:entry>", "", " { 'format': 'rar' }"),
        ARCHIVE_FORMAT);
    // unknown option
    error(func.args(" <archive:entry>X</archive:entry>", "", " { 'x': 'y' }"), INVALIDOPTION_X);
    error(func.args(" <archive:entry>X</archive:entry>", "", " { 'format': 'xxx' }"),
        ARCHIVE_FORMAT);
    // algorithm not supported
    error(func.args(" <archive:entry>X</archive:entry>", "", " { 'algorithm': 'unknown' }"),
        ARCHIVE_FORMAT_X_X);
    // algorithm not supported
    error(func.args(" ('x', 'y')", " ('a', 'b')", " { 'format': 'gzip' }"), ARCHIVE_SINGLE_X);
  }

  /** Test method. */
  @Test public void createFrom() {
    final Function func = _ARCHIVE_CREATE_FROM;
    countEntries(func.args(DIR), 5);
    countEntries(func.args(DIR, " {}"), 5);
    countEntries(func.args(DIR, " { 'algorithm': 'stored' }"), 5);

    countEntries(func.args(DIR, " { 'recursive': false() }"), 4);
    query(_ARCHIVE_ENTRIES.args(func.args(DIR, " { 'root-dir': true() }")) +
        "[not(starts-with(., 'dir/'))]", "");

    query("parse-xml(" + _ARCHIVE_EXTRACT_TEXT.args(func.args(DIR, " {}"),
        "input.xml") + ") instance of document-node()", true);

    // standalone use: verify no temp files remain after query context closes
    final File tmpDir = new File(Prop.TEMPDIR);
    final int tmpsBefore = tmpDir.listFiles(
        f -> f.getName().startsWith(Prop.NAME + '-') && f.getName().endsWith(IO.TMPSUFFIX)).length;
    countEntries(func.args(DIR), 5);
    assertEquals(tmpsBefore, tmpDir.listFiles(
        f -> f.getName().startsWith(Prop.NAME + '-') && f.getName().endsWith(IO.TMPSUFFIX)).length);

    // write directly to file (streaming path, no temp file and no in-memory accumulation)
    final String tmp = Prop.TEMPDIR + NAME + "createFrom";
    query(_FILE_WRITE_BINARY.args(tmp, func.args(DIR)));
    countEntries(tmp, 5);
    query("parse-xml(" + _ARCHIVE_EXTRACT_TEXT.args(tmp, "input.xml") +
        ") instance of document-node()", true);
    query(_FILE_DELETE.args(tmp));

    // errors
    error(func.args("UNUNUNKNOWN"), FILE_NO_DIR_X);
    error(func.args(DIR, " {}", "UNUNUNKNOWN"), FILE_NOT_FOUND_X);
    error(func.args(DIR, " {}", "."), FILE_IS_DIR_X);
  }

  /** Test method. */
  @Test public void delete() {
    final Function func = _ARCHIVE_DELETE;
    // delete single entry
    query("let$archive := " + func.args(ZIP, "infos/stopWords") +
        "let $entries := " + _ARCHIVE_ENTRIES.args(" $archive") +
        "return count($entries)", 4);
    query("let $archive := " + _FILE_READ_BINARY.args(ZIP) +
        "let $updated := " + func.args(" $archive", "infos/stopWords") +
        "let $entries := " + _ARCHIVE_ENTRIES.args(" $updated") +
        "return count($entries)", 4);
    // delete all entries except for the first
    query("let $entries := " + _ARCHIVE_ENTRIES.args(ZIP) +
        "let $updated := " + func.args(ZIP, " tail($entries)") +
        "return count(archive:entries($updated))", 1);
    query("let $archive := " + _FILE_READ_BINARY.args(ZIP) +
        "let $entries := " + _ARCHIVE_ENTRIES.args(" $archive") +
        "let $updated := " + func.args(" $archive", " tail($entries)") +
        "return count(archive:entries($updated))", 1);
    // updates an existing entry
    error(_ARCHIVE_CREATE.args("X", "X", " { 'format': 'gzip' }") + " => " + func.args("X"),
        ARCHIVE_MODIFY_X);
  }

  /** Test method. */
  @Test public void entries() {
    final Function func = _ARCHIVE_ENTRIES;
    // read entries
    query(COUNT.args(func.args(ZIP)), 5);
    query(COUNT.args(func.args(_FILE_READ_BINARY.args(ZIP))), 5);
    // simple zip files
    query(COUNT.args(func.args(ZIP) +
        "[@size][@last-modified][@compressed-size]"), 5);
    query(COUNT.args(func.args(_FILE_READ_BINARY.args(ZIP)) +
        "[@size][@last-modified][@compressed-size]"), 5);
    // simple gzip files
    query(COUNT.args(func.args(GZIP) +
        "[not(@size)][not(@last-modified)][not(@compressed-size)][not(text())]"), 1);
    query(COUNT.args(func.args(_FILE_READ_BINARY.args(GZIP)) +
        "[not(@size)][not(@last-modified)][not(@compressed-size)][not(text())]"), 1);
  }

  /** Test method. */
  @Test public void extractBinary() {
    final Function func = _ARCHIVE_EXTRACT_BINARY;
    // extract all entries
    query(COUNT.args(func.args(ZIP)), 5);
    query(COUNT.args(func.args(_FILE_READ_BINARY.args(ZIP))), 5);
    // extract all entries
    query("count(" + func.args(ZIP, _ARCHIVE_ENTRIES.args(ZIP)) + ')', 5);
    query("let $archive := " + _FILE_READ_BINARY.args(ZIP) +
        "let $entries := " + _ARCHIVE_ENTRIES.args(" $archive") +
        "return count(" + func.args(" $archive", " $entries") + ')', 5);
    // extract single entry
    query("let $extracted := " + func.args(ZIP, "test/input.xml") +
        "let $string := " + _CONVERT_BINARY_TO_STRING.args(" $extracted") +
        "let $doc := " + PARSE_XML.args(" $string") +
        "return $doc//title/text()", "XML");
    query("let $archive := " + _FILE_READ_BINARY.args(ZIP) +
        "let $extracted := " + func.args(" $archive", "test/input.xml") +
        "let $string := " + _CONVERT_BINARY_TO_STRING.args(" $extracted") +
        "let $doc := " + PARSE_XML.args(" $string") +
        "return $doc//title/text()", "XML");
    // extract single entry
    query("let $archive := " + _FILE_READ_BINARY.args(ZIP) +
        "let $extracted := " + func.args(" $archive",
            " <archive:entry>test/input.xml</archive:entry>") +
        "let $string := " + _CONVERT_BINARY_TO_STRING.args(" $extracted") +
        "let $doc := " + PARSE_XML.args(" $string") +
        "return $doc//title/text()", "XML");
    query("let $extracted := " + func.args(ZIP, " <archive:entry>test/input.xml</archive:entry>") +
        "let $string := " + _CONVERT_BINARY_TO_STRING.args(" $extracted") +
        "let $doc := " + PARSE_XML.args(" $string") +
        "return $doc//title/text()", "XML");
    // extract non-existing entry
    query("empty(" + func.args(ZIP, "xyz") + ")", true);
    query("let $archive := " + _FILE_READ_BINARY.args(ZIP) +
        "return empty(" + func.args(" $archive", "xzy") + ")", true);
    query("empty(" + func.args(GZIP, "xyz") + ")", true);
    query("let $archive := " + _FILE_READ_BINARY.args(GZIP) +
        "return empty(" + func.args(" $archive", "xzy") + ")", true);

    // legacy ZIP with CP437-encoded entry name (UTF-8 flag not set)
    query(_ARCHIVE_ENTRIES.args(ZIP_CP437) + "/text() = 'Prüfung.txt'", true);
    query(COUNT.args(func.args(ZIP_CP437)), 2);
    query(_CONVERT_BINARY_TO_STRING.args(" " + func.args(ZIP_CP437, "Prüfung.txt")),
        "hello umlaut");

    // entry-name decoding across all bit-11/encoding combinations:
    // 1. UTF-8 flag set, UTF-8 (spec-conformant): decoded as UTF-8
    query(_ARCHIVE_ENTRIES.args(ZIP_UTF8) + "/text() = 'Prüfung.txt'", true);
    query(_CONVERT_BINARY_TO_STRING.args(" " + func.args(ZIP_UTF8, "Prüfung.txt")),
        "hello utf8");
    // 2. UTF-8 flag not set, ASCII: decoded as CP437 (= ASCII)
    query(_ARCHIVE_ENTRIES.args(ZIP_ASCII) + "/text() = 'plain.txt'", true);
    query(_CONVERT_BINARY_TO_STRING.args(" " + func.args(ZIP_ASCII, "plain.txt")),
        "hello ascii");
    // 3. UTF-8 flag not set, Shift_JIS: CP437 fallback yields mojibake but stays readable;
    //    also a negative control for the mojibake heuristic. Shift_JIS bytes 93 FA 96 7B
    //    are not valid UTF-8, so Strings.fixCp437Mojibake leaves the CP437 decoding alone
    query(_ARCHIVE_ENTRIES.args(ZIP_SJIS) + "/text() = 'ô·û{.txt'", true);
    query(_CONVERT_BINARY_TO_STRING.args(" " + func.args(ZIP_SJIS, "ô·û{.txt")),
        "hello sjis");
    // 4. UTF-8 flag not set but bytes are actually UTF-8 (Linux zip mis-flag): the CP437
    //    fallback would yield mojibake "Pr├╝fung.txt"; Strings.fixCp437Mojibake recovers
    //    the intended UTF-8 name via the CP437→UTF-8 round-trip heuristic
    query(_ARCHIVE_ENTRIES.args(ZIP_UTF8_NO_FLAG) + "/text() = 'Prüfung.txt'", true);
    query(_CONVERT_BINARY_TO_STRING.args(" " + func.args(ZIP_UTF8_NO_FLAG, "Prüfung.txt")),
        "hello mojibake");
  }

  /** Test method. */
  @Test public void extractText() {
    final Function func = _ARCHIVE_EXTRACT_TEXT;
    // extract all entries
    query(COUNT.args(func.args(ZIP)), 5);
    query(COUNT.args(func.args(_FILE_READ_BINARY.args(ZIP))), 5);
    // extract all entries
    query("let $entries := " + _ARCHIVE_ENTRIES.args(ZIP) +
        "return " + COUNT.args(func.args(ZIP, " $entries")), 5);
    query("let $archive := " + _FILE_READ_BINARY.args(ZIP) +
        "let $entries := " + _ARCHIVE_ENTRIES.args(" $archive") +
        "return " + COUNT.args(func.args(" $archive", " $entries")), 5);
    // extract single entry
    query("let $entry := " + func.args(ZIP, "test/input.xml") +
        "let $doc := " + PARSE_XML.args(" $entry") +
        "return $doc//title/text()", "XML");
    query("let $archive := " + _FILE_READ_BINARY.args(ZIP) +
        "let $b := " + func.args(" $archive", "test/input.xml") +
        "let $doc := " + PARSE_XML.args(" $b") +
        "return $doc//title/text()", "XML");
    // extract single entry
    query("let $entry := " + func.args(ZIP,
        " <archive:entry>test/input.xml</archive:entry>") +
        "let $doc := " + PARSE_XML.args(" $entry") +
        "return $doc//title/text()", "XML");
    query("let $archive := " + _FILE_READ_BINARY.args(ZIP) +
        "let $entry := " + func.args(" $archive",
            " <archive:entry>test/input.xml</archive:entry>") +
        "let $doc := " + PARSE_XML.args(" $entry") +
        "return $doc//title/text()", "XML");
    // extract non-existing entry
    query("empty(" + func.args(ZIP, "xyz") + ")", true);
    query("let $archive := " + _FILE_READ_BINARY.args(ZIP) +
        "return empty(" + func.args(" $archive", "xzy") + ")", true);
    query("empty(" + func.args(GZIP, "xyz") + ")", true);
    query("let $archive := " + _FILE_READ_BINARY.args(GZIP) +
        "return empty(" + func.args(" $archive", "xzy") + ")", true);
  }

  /** Test method. */
  @Test public void extractTo() {
    final Function func = _ARCHIVE_EXTRACT_TO;
    final String tmp = Prop.TEMPDIR + NAME + "extractTo";
    // write archive and count number of entries
    query(func.args(tmp, ZIP));
    countEntries(ZIP, 5);
    query(func.args(tmp, _FILE_READ_BINARY.args(ZIP)));
    countEntries(_FILE_READ_BINARY.args(ZIP), 5);
    // write archive and count number of entries
    query(func.args(tmp, ZIP, _ARCHIVE_ENTRIES.args(ZIP)));
    countEntries(_FILE_READ_BINARY.args(ZIP), 5);
    query(func.args(tmp, _FILE_READ_BINARY.args(ZIP),
        _ARCHIVE_ENTRIES.args(_FILE_READ_BINARY.args(ZIP))));
    countEntries(_FILE_READ_BINARY.args(ZIP), 5);

    query("let $entries := " + _ARCHIVE_ENTRIES.args(ZIP) + " ! string() " +
        "let $files := " + _FILE_LIST.args(tmp, true) + '[' +
          _FILE_IS_FILE.args(" '" + tmp + "/'||.") + "] ! replace(., '\\\\', '/') " +
        "return deep-equal($entries, $files, { 'ordered': false() })",
        true);
    query("let $entries := " + _ARCHIVE_ENTRIES.args(_FILE_READ_BINARY.args(ZIP)) + "/string() " +
        "let $files := " + _FILE_LIST.args(tmp, true) + '[' +
          _FILE_IS_FILE.args(" '" + tmp + "/'||.") + "] ! replace(., '\\\\', '/') " +
        "return deep-equal($entries, $files, { 'ordered': false() })",
        true);

    // write non-existing entry
    query(func.args(tmp, ZIP, "xyz"));
    query(func.args(tmp, GZIP, "xyz"));

    // last-modified is preserved on the extracted file
    final String mtime = "2024-06-01T12:00:00Z";
    final String src = Prop.TEMPDIR + NAME + "extractTo_mtime.zip";
    final String dst = Prop.TEMPDIR + NAME + "extractTo_mtime";
    query(_ARCHIVE_WRITE.args(src,
        " <archive:entry last-modified='" + mtime + "'>doc</archive:entry>", "DOC"));
    query(func.args(dst, src));
    query(_FILE_LAST_MODIFIED.args(dst + "/doc"), mtime);
    query(_FILE_DELETE.args(src));
    query(_FILE_DELETE.args(dst, true));
  }

  /** Test method. */
  @Test public void options() {
    final Function func = _ARCHIVE_OPTIONS;
    // format
    query(func.args(ZIP) + "?format", "zip");
    query(func.args(_FILE_READ_BINARY.args(ZIP)) + "?format", "zip");
    query(func.args(GZIP) + "?format", "gzip");
    query(func.args(_FILE_READ_BINARY.args(GZIP)) + "?format", "gzip");
    // algorithm: DEFLATE (default for the test fixtures)
    query(func.args(ZIP) + "?algorithm", "deflate");
    query(func.args(_FILE_READ_BINARY.args(ZIP)) + "?algorithm", "deflate");
    query(func.args(GZIP) + "?algorithm", "deflate");
    query(func.args(_FILE_READ_BINARY.args(GZIP)) + "?algorithm", "deflate");
    // algorithm: STORED via freshly created archive (exercises the non-default branch
    // and verifies round-trip through ZipOutputStream's STORED path)
    query(func.args(_ARCHIVE_CREATE.args("X", "X", " { 'algorithm': 'stored' }")) + "?algorithm",
        "stored");
    // map structure: both keys populated for a regular archive
    query("map:size(" + func.args(ZIP) + ')', 2);
  }

  /** Test method. */
  @Test public void refresh() {
    final Function func = _ARCHIVE_REFRESH;

    // refresh temporary file
    final String tmp = Prop.TEMPDIR + NAME + "refresh";
    query(_FILE_COPY.args(ZIP, tmp));

    query(func.args(tmp, "binary", " xs:hexBinary('414243')"));
    query(_ARCHIVE_EXTRACT_BINARY.args(tmp, "binary"), "ABC");

    query(func.args(tmp, "new", "NEW"));
    query(_ARCHIVE_ENTRIES.args(tmp) + "[. = 'new'] => count()", 1);
    query(_ARCHIVE_EXTRACT_TEXT.args(tmp, "new"), "NEW");

    query(func.args(tmp, "new", "NEWER"));
    query(_ARCHIVE_EXTRACT_TEXT.args(tmp, "new"), "NEWER");

    final String lastModified = "2001-01-01T01:01:01Z";
    query(func.args(tmp, " <archive:entry last-modified='" + lastModified + "'>new</archive:entry>",
        "NEWEST"));
    query(_ARCHIVE_ENTRIES.args(tmp) + "[. = 'new'] ! data(@last-modified)", lastModified);

    error(func.args(GZIP, "x", "x"), ARCHIVE_ZIP_X);
    error(func.args("http://www.abalaba.com/zip.zip", "x", "x"), ARCHIVE_ZIP_X);
    error(func.args(tmp, " <archive:entry encoding='XXX'>new</archive:entry>", "NEWEST"),
        ARCHIVE_ENCODE1_X);
    error(func.args(tmp, " <archive:entry encoding='US-ASCII'>new</archive:entry>", "\u00FC"),
        ARCHIVE_ENCODE2_X);
  }

  /** Test method. */
  @Test public void update() {
    final Function func = _ARCHIVE_UPDATE;
    // add a new entry
    query(func.args(ZIP, "X", "X") + " => " + _ARCHIVE_ENTRIES.args() + " => " + COUNT.args(), 6);
    query(_FILE_READ_BINARY.args(ZIP) + " => " + func.args("X", "X") + " => " +
        _ARCHIVE_ENTRIES.args() + " => " + COUNT.args(), 6);
    // add a new entry
    query(func.args(ZIP, " <archive:entry>X</archive:entry>", "X") + " => " +
        _ARCHIVE_ENTRIES.args() + " => " + COUNT.args(), 6);
    query(_FILE_READ_BINARY.args(ZIP) + " => " +
        func.args(" <archive:entry>X</archive:entry>", "X") + " => " +
        _ARCHIVE_ENTRIES.args() + " => " + COUNT.args(), 6);
    query(_ARCHIVE_CREATE.args(" <archive:entry>X</archive:entry>", "X") + " => " +
        func.args(" <archive:entry>Y</archive:entry>", "Y") + " => " +
        _ARCHIVE_EXTRACT_TEXT.args(), "X\nY");
    // updates an existing entry
    query(_ARCHIVE_CREATE.args(" <archive:entry>X</archive:entry>", "X") + " => " +
        func.args(" <archive:entry>X</archive:entry>", "Y") + " => " +
        _ARCHIVE_EXTRACT_TEXT.args(), "Y");
    // updates an existing entry
    error(_ARCHIVE_CREATE.args("X", "X", " { 'format': 'gzip' }") + " => " +
        func.args("X", "Y"), ARCHIVE_MODIFY_X);

    // updated archive, streamed to a file: must not be routed through the
    // archive:create optimization (different argument layout)
    final String tmp = Prop.TEMPDIR + NAME + "update";
    query(_FILE_WRITE_BINARY.args(tmp, _ARCHIVE_CREATE.args(
        " <archive:entry>X</archive:entry>", "X") + " => " +
        func.args(" <archive:entry>Y</archive:entry>", "Y")));
    query(_ARCHIVE_EXTRACT_TEXT.args(tmp), "X\nY");
    query(_FILE_DELETE.args(tmp));
  }

  /** Test method. */
  @Test public void write() {
    final Function func = _ARCHIVE_WRITE;
    final String tmp = Prop.TEMPDIR + NAME + "write";

    // string content
    query(func.args(tmp, "file", "123"));
    query(_ARCHIVE_EXTRACT_TEXT.args(tmp), 123);
    // empty array == skip entry
    query(func.args(tmp, "file", " []"));
    query(_ARCHIVE_EXTRACT_BINARY.args(tmp), "");
    // binary content round-trip
    query(func.args(tmp, "blob", " xs:hexBinary('414243')"));
    query(_CONVERT_BINARY_TO_STRING.args(" " + _ARCHIVE_EXTRACT_BINARY.args(tmp, "blob")), "ABC");
    // archive:entry header preserves last-modified attribute
    final String lastModified = "2024-06-01T12:00:00Z";
    query(func.args(tmp,
        " <archive:entry last-modified='" + lastModified + "'>doc</archive:entry>", "DOC"));
    query(_ARCHIVE_ENTRIES.args(tmp) + " ! data(@last-modified)", lastModified);
    // options: STORED algorithm round-trips
    query(func.args(tmp, "x", "data", " { 'algorithm': 'stored' }"));
    query(_ARCHIVE_OPTIONS.args(tmp) + "?algorithm", "stored");
    // options: GZIP format round-trips
    query(func.args(tmp, "x", "y", " { 'format': 'gzip' }"));
    query(_ARCHIVE_OPTIONS.args(tmp) + "?format", "gzip");

    // shared validation pipeline with archive:create (sanity checks on routing)
    error(func.args(tmp, " ('a', 'b')", "X"), ARCHIVE_NUMBER_X_X);
    error(func.args(tmp, " <archive:entry/>", ""), ARCHIVE_NAME);

    query(_FILE_DELETE.args(tmp));
  }

  /**
   * Verifies that {@code archive:extract-to} sanitizes path-traversal entries instead of
   * writing outside the target directory. {@code "../foo"} is re-anchored as {@code "foo"}
   * under the target; intermediate {@code "."}/{@code ".."} components are dropped while
   * legitimate subdirectory structure is preserved.
   */
  @Test public void extractToZipSlip() {
    final IOFile safe = new IOFile(Prop.TEMPDIR + NAME + "_safe");
    final IOFile outside = new IOFile(Prop.TEMPDIR + NAME + "_escapee.txt");
    try {
      // leading "..": stripped, file lands inside target — outside path is not touched
      query(_ARCHIVE_EXTRACT_TO.args(safe.path(),
          " " + _ARCHIVE_CREATE.args("../" + NAME + "_escapee.txt", "pwn")));
      assertFalse(outside.exists(), "zip-slip wrote outside target dir: " + outside);
      query(_FILE_READ_TEXT.args(safe.path() + "/" + NAME + "_escapee.txt"), "pwn");

      // "..": stripped, remaining subdir structure preserved
      query(_ARCHIVE_EXTRACT_TO.args(safe.path(),
          " " + _ARCHIVE_CREATE.args("../deep/sub/x.txt", "deep")));
      query(_FILE_READ_TEXT.args(safe.path() + "/deep/sub/x.txt"), "deep");

      // legitimate subdirectory extraction still works
      query(_ARCHIVE_EXTRACT_TO.args(safe.path(),
          " " + _ARCHIVE_CREATE.args("a/b/c.txt", "ok")));
      query(_FILE_READ_TEXT.args(safe.path() + "/a/b/c.txt"), "ok");
    } finally {
      safe.delete();
      outside.delete();
    }
  }

  /**
   * Tests handling of ZIPs whose local headers set the data-descriptor flag (general purpose
   * bit 3) on STORED entries. {@link java.util.zip.ZipInputStream} rejects such layouts
   * with "only DEFLATED entries can have EXT descriptor"; {@link java.util.zip.ZipFile}
   * reads sizes from the central directory and accepts them. Verifies that local-file paths
   * use the tolerant {@code ZipFile}-based code path; eagerly-materialized binary inputs
   * remain subject to the streaming reader's strictness.
   * @throws IOException test setup failure
   */
  @Test public void storedWithDataDescriptor() throws IOException {
    final IOFile zip = new IOFile(Prop.TEMPDIR + NAME + "_stored_dd.zip");
    final IOFile dir = new IOFile(Prop.TEMPDIR + NAME + "_stored_dd_dir");
    final IOFile copy = new IOFile(Prop.TEMPDIR + NAME + "_stored_dd_refresh.zip");
    try {
      zip.write(storedWithDescriptorZip());
      final String path = zip.path();

      // read-side operations: local-file path uses ZipFile (tolerant)
      query(_ARCHIVE_ENTRIES.args(path) + "/text()", "x");
      query(_ARCHIVE_OPTIONS.args(path) + "?format", "zip");
      query(_ARCHIVE_EXTRACT_TEXT.args(path, "x"), "hello");
      query(_CONVERT_BINARY_TO_STRING.args(" " + _ARCHIVE_EXTRACT_BINARY.args(path, "x")),
          "hello");

      // extract-to: writes to the filesystem
      query(_ARCHIVE_EXTRACT_TO.args(dir.path(), path));
      query(_FILE_READ_TEXT.args(dir.path() + "/x"), "hello");

      // write-side operations producing fresh archives
      query(COUNT.args(_ARCHIVE_ENTRIES.args(_ARCHIVE_DELETE.args(path, "x"))), 0);
      query(COUNT.args(_ARCHIVE_ENTRIES.args(_ARCHIVE_UPDATE.args(path, "y", "world"))), 2);

      // refresh: in-place modification via java.nio.file zipfs (also central-directory-based)
      copy.write(storedWithDescriptorZip());
      query(_ARCHIVE_REFRESH.args(copy.path(), "x", "world"));
      query(_ARCHIVE_EXTRACT_TEXT.args(copy.path(), "x"), "world");

      // eager binary input bypasses the localZip detour and takes the streaming
      // path — still rejected by ZipInputStream (file:read-binary's 3-arg form returns
      // an eager B64 instead of a B64Lazy with a local-file backing)
      final String p = "'" + path + "'";
      error(_ARCHIVE_ENTRIES.args(
          " file:read-binary(" + p + ", 0, file:size(" + p + "))"), ARCHIVE_ERROR_X);
    } finally {
      zip.delete();
      dir.delete();
      copy.delete();
    }
  }

  /**
   * Counts the entries of an archive.
   * @param archive archive
   * @param exp expected number of results
   */
  private static void countEntries(final String archive, final int exp) {
    query(COUNT.args(_ARCHIVE_ENTRIES.args(archive)), exp);
  }

  /**
   * Builds a minimal ZIP with one STORED entry whose general-purpose bit 3 (data descriptor)
   * is set in the local header — CRC and sizes are placed in a trailing descriptor record
   * instead of the local header. This is a layout some real-world ZIP creators emit and that
   * {@link java.util.zip.ZipFile} accepts (reading the central directory) but
   * {@link java.util.zip.ZipInputStream} rejects ("only DEFLATED entries can have EXT
   * descriptor").
   * @return ZIP bytes
   */
  private static byte[] storedWithDescriptorZip() {
    final byte[] data = Token.token("hello"), name = Token.token("x");
    final CRC32 crc = new CRC32();
    crc.update(data);
    final int crc32 = (int) crc.getValue(), size = data.length;

    final ByteBuffer buf = ByteBuffer.allocate(256).order(ByteOrder.LITTLE_ENDIAN);

    // local file header
    buf.putInt(0x04034b50);                // signature
    buf.putShort((short) 10);              // version needed
    buf.putShort((short) 0x0008);          // GP flags: bit 3 (data descriptor)
    buf.putShort((short) 0);               // method: STORED
    buf.putShort((short) 0);               // mod time
    buf.putShort((short) 0x0021);          // mod date (1980-01-01)
    buf.putInt(0);                         // CRC (zero in local header)
    buf.putInt(0);                         // compressed size (zero in local header)
    buf.putInt(0);                         // uncompressed size (zero in local header)
    buf.putShort((short) name.length);
    buf.putShort((short) 0);               // extra length
    buf.put(name);

    // entry body
    buf.put(data);

    // data descriptor (with optional signature)
    buf.putInt(0x08074b50);
    buf.putInt(crc32);
    buf.putInt(size);                      // compressed size
    buf.putInt(size);                      // uncompressed size

    final int cdOffset = buf.position();

    // central directory file header
    buf.putInt(0x02014b50);                // signature
    buf.putShort((short) 10);              // version made by
    buf.putShort((short) 10);              // version needed
    buf.putShort((short) 0x0008);          // GP flags
    buf.putShort((short) 0);               // method: STORED
    buf.putShort((short) 0);               // mod time
    buf.putShort((short) 0x0021);          // mod date
    buf.putInt(crc32);
    buf.putInt(size);
    buf.putInt(size);
    buf.putShort((short) name.length);
    buf.putShort((short) 0);               // extra
    buf.putShort((short) 0);               // comment
    buf.putShort((short) 0);               // disk number
    buf.putShort((short) 0);               // internal attrs
    buf.putInt(0);                         // external attrs
    buf.putInt(0);                         // local header offset
    buf.put(name);

    final int cdSize = buf.position() - cdOffset;

    // end of central directory
    buf.putInt(0x06054b50);
    buf.putShort((short) 0);               // disk
    buf.putShort((short) 0);               // cd disk
    buf.putShort((short) 1);               // entries on disk
    buf.putShort((short) 1);               // total entries
    buf.putInt(cdSize);
    buf.putInt(cdOffset);
    buf.putShort((short) 0);               // comment length

    final byte[] out = new byte[buf.position()];
    buf.flip();
    buf.get(out);
    return out;
  }
}

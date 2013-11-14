package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.util.*;
import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the File Module.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Rositsa Shadura
 */
public final class FNFileTest extends AdvancedQueryTest {
  /** Test path. */
  private static final String PATH = Prop.TMP + NAME + '/';
  /** Test path. */
  private static final String PATH1 = PATH + NAME;
  /** Test path. */
  private static final String PATH2 = PATH + NAME + '2';
  /** Test path. */
  private static final String PATH3 = PATH + NAME + "/x";
  /** Test path. */
  private static final String PATH4 = PATH + NAME + "/x/y";

  /** Initializes the test. */
  @After
  public void init() {
    new File(PATH4).delete();
    new File(PATH3).delete();
    new File(PATH2).delete();
    new File(PATH1).delete();
  }

  /** Test method. */
  @Test
  public void dirSeparator() {
    assertFalse(query(_FILE_DIR_SEPARATOR.args()).isEmpty());
  }

  /** Test method. */
  @Test
  public void pathSeparator() {
    assertFalse(query(_FILE_PATH_SEPARATOR.args()).isEmpty());
  }

  /** Test method. */
  @Test
  public void tempDir() {
    assertEquals(query(_FILE_TEMP_DIR.args()), Prop.TMP);
  }

  /** Test method. */
  @Test
  public void createTempDir() {
    final String tmp = query(_FILE_CREATE_TEMP_DIR.args("", ""));
    query(_FILE_EXISTS.args(tmp), "true");
    query(_FILE_IS_DIR.args(tmp), "true");
    query(_FILE_IS_FILE.args(tmp), "false");
    query(_FILE_DELETE.args(tmp));
  }

  /** Test method. */
  @Test
  public void createTempFile() {
    final String tmp = query(_FILE_CREATE_TEMP_FILE.args("", ""));
    query(_FILE_EXISTS.args(tmp), "true");
    query(_FILE_IS_DIR.args(tmp), "false");
    query(_FILE_IS_FILE.args(tmp), "true");
    query(_FILE_DELETE.args(tmp));
  }

  /** Test method. */
  @Test
  public void exists() {
    query(_FILE_WRITE.args(PATH1, "()"));
    query(_FILE_EXISTS.args(PATH1), true);
    query(_FILE_EXISTS.args(IO.FILEPREF + "//" + PATH1), true);
    query(_FILE_DELETE.args(PATH1));
    query(_FILE_EXISTS.args(PATH1), false);
    query(_FILE_EXISTS.args(IO.FILEPREF + "//" + PATH1), false);
  }

  /** Test method. */
  @Test
  public void isDir() {
    query(_FILE_IS_DIR.args(PATH), true);
    query(_FILE_WRITE.args(PATH1, "()"));
    query(_FILE_IS_DIR.args(PATH1), false);
    query(_FILE_DELETE.args(PATH1));
    query(_FILE_CREATE_DIR.args(PATH1));
    query(_FILE_IS_DIR.args(PATH1), true);
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void isFile() {
    query(_FILE_IS_FILE.args(PATH), false);
    query(_FILE_WRITE.args(PATH1, "()"));
    query(_FILE_IS_FILE.args(PATH1), true);
    query(_FILE_DELETE.args(PATH1));
    query(_FILE_CREATE_DIR.args(PATH1));
    query(_FILE_IS_FILE.args(PATH1), false);
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void lastModified() {
    assertFalse(query(_FILE_LAST_MODIFIED.args(PATH)).isEmpty());
  }

  /** Test method. */
  @Test
  public void size() {
    query(_FILE_WRITE.args(PATH1, "abcd"));
    query(_FILE_SIZE.args(PATH1), "4");
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void list() {
    error(_FILE_LIST.args(PATH1), Err.FILE_NODIR);
    query(_FILE_WRITE.args(PATH1, "()"));
    error(_FILE_LIST.args(PATH1), Err.FILE_NODIR);
    contains(_FILE_LIST.args(PATH), NAME);
    contains(_FILE_LIST.args(PATH, "false()"), NAME);
    contains(_FILE_LIST.args(PATH, "false()", NAME), NAME);
    query(_FILE_LIST.args(PATH, "false()", "XXX"), "");
    query(_FILE_DELETE.args(PATH1));
    // check recursive paths
    query(_FILE_CREATE_DIR.args(PATH1));
    query(_FILE_CREATE_DIR.args(PATH3));
    query(_FILE_WRITE.args(PATH4, "()"));
    contains(_FILE_LIST.args(PATH1, "true()"), "y");
    query(_FILE_LIST.args(PATH1, "true()", "x"), 'x' + File.separator);
  }

  /** Test method. */
  @Test
  public void createDir() {
    query(_FILE_CREATE_DIR.args(PATH1));
    query(_FILE_CREATE_DIR.args(PATH1));
    query(_FILE_CREATE_DIR.args(PATH3));
    query(_FILE_DELETE.args(PATH1, "true()"));
    query(_FILE_WRITE.args(PATH1, "()"));
    error(_FILE_CREATE_DIR.args(PATH1), Err.FILE_EXISTS);
    error(_FILE_CREATE_DIR.args(PATH3), Err.FILE_EXISTS);
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void delete() {
    query(_FILE_CREATE_DIR.args(PATH3));
    query(_FILE_DELETE.args(PATH3));
    query(_FILE_CREATE_DIR.args(PATH3));
    query(_FILE_WRITE.args(PATH4, "()"));
    query(_FILE_DELETE.args(PATH1, "true()"));
    error(_FILE_DELETE.args(PATH1), Err.FILE_WHICH);
  }

  /** Test method. */
  @Test
  public void readText() {
    error(_FILE_READ_TEXT.args(PATH1), Err.FILE_WHICH);
    error(_FILE_READ_TEXT.args(PATH), Err.FILE_DIR);
    query(_FILE_WRITE.args(PATH1, "a\u00e4"));
    query(_FILE_READ_TEXT.args(PATH1), "a\u00e4");
    error(_FILE_READ_TEXT.args(PATH1, "UNKNOWN"), Err.FILE_ENCODING);
    assertEquals(3, query(_FILE_READ_TEXT.args(PATH1, "CP1252")).length());
    query(_FILE_WRITE_BINARY.args(PATH1, "xs:hexBinary('00')"));
    error(_FILE_READ_TEXT.args(PATH1), Err.FILE_IO);
    query("declare option db:checkstrings 'off';" + _FILE_READ_TEXT.args(PATH1), "&#x0;");
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void readBinary() {
    // check errors
    error(_FILE_READ_BINARY.args(PATH1), Err.FILE_WHICH);
    error(_FILE_READ_BINARY.args(PATH), Err.FILE_DIR);
    // file with single codepoint
    query(_FILE_WRITE.args(PATH1, "0"));
    query(_FILE_READ_BINARY.args(PATH1), "MA==");
    query(_FILE_READ_BINARY.args(PATH1, 0), "MA==");
    query(_FILE_READ_BINARY.args(PATH1, 0, 1), "MA==");
    query(_FILE_READ_BINARY.args(PATH1, 1), "");
    query(_FILE_READ_BINARY.args(PATH1, 1, 0), "");
    query(_FILE_READ_BINARY.args(PATH1, 0, 0), "");
    error(_FILE_READ_BINARY.args(PATH1, -1), Err.FILE_BOUNDS);
    error(_FILE_READ_BINARY.args(PATH1, 2), Err.FILE_BOUNDS);
    error(_FILE_READ_BINARY.args(PATH1, 0, -1), Err.FILE_BOUNDS);
    error(_FILE_READ_BINARY.args(PATH1, 0, 2), Err.FILE_BOUNDS);
    error(_FILE_READ_BINARY.args(PATH1, 2, 1), Err.FILE_BOUNDS);
    // file with two codepoints
    query(_FILE_WRITE.args(PATH1, "a\u00e4"));
    query(_FILE_READ_BINARY.args(PATH1), "YcOk");
    // file with two codepoints
    query(_FILE_WRITE_BINARY.args(PATH1, _CONVERT_STRING_TO_BASE64.args("a\u00e4")));
    query(_FILE_READ_BINARY.args(PATH1), "YcOk");
    // delete file
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void write() {
    error(_FILE_WRITE.args(PATH, "()"), Err.FILE_DIR);
    error(_FILE_WRITE.args(PATH4, "()"), Err.FILE_NODIR);

    query(_FILE_WRITE.args(PATH1, "0"));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_WRITE.args(PATH1, "0"));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_DELETE.args(PATH1));

    query(_FILE_WRITE.args(PATH1, "a\u00e4", serialParams("<encoding value='CP1252'/>")));
    query(_FILE_READ_TEXT.args(PATH1, "CP1252"), "a\u00e4");

    query(_FILE_WRITE.args(PATH1, "\"<a/>\"", serialParams("<method value='text'/>")));
    query(_FILE_READ_TEXT.args(PATH1), "&lt;a/&gt;");
    query(_FILE_DELETE.args(PATH1));

    // test spaces in filename
    query(_FILE_WRITE.args(PATH1 + "%20X", ""));
    query(_FILE_EXISTS.args(PATH1 + "%20X"), "true");
    query(_FILE_DELETE.args(PATH1 + "%20X"));
    query(_FILE_EXISTS.args(PATH1 + "%20X"), "false");

    query(_FILE_WRITE.args(PATH1 + " X", ""));
    query(_FILE_EXISTS.args(PATH1 + " X"), "true");
    query(_FILE_DELETE.args(PATH1 + " X"));
    query(_FILE_EXISTS.args(PATH1 + " X"), "false");
  }

  /** Test method. */
  @Test
  public void append() {
    error(_FILE_APPEND.args(PATH, "()"), Err.FILE_DIR);
    error(_FILE_APPEND.args(PATH4, "()"), Err.FILE_NODIR);

    query(_FILE_APPEND.args(PATH1, "0"));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_APPEND.args(PATH1, "0", "()"));
    query(_FILE_SIZE.args(PATH1), "2");
    query(_FILE_DELETE.args(PATH1));

    query(_FILE_APPEND.args(PATH1, "a\u00e4",
        serialParams("<encoding value='CP1252'/>")));
    query(_FILE_READ_TEXT.args(PATH1, "CP1252"), "a\u00e4");
    query(_FILE_DELETE.args(PATH1));

    query(_FILE_APPEND.args(PATH1, "\"<a/>\"", serialParams("<method value='text'/>")));
    query(_FILE_READ_TEXT.args(PATH1), "&lt;a/&gt;");
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void writeText() {
    error(_FILE_WRITE_TEXT.args(PATH, "x"), Err.FILE_DIR);
    error(_FILE_WRITE_TEXT.args(PATH1, " 123"), Err.INVCAST);

    query(_FILE_WRITE_TEXT.args(PATH1, "x"));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_WRITE_TEXT.args(PATH1, "\u00fc", "US-ASCII"));
    query(_FILE_READ_TEXT.args(PATH1), "?");
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void writeTextLines() {
    error(_FILE_WRITE_TEXT_LINES.args(PATH, "x"), Err.FILE_DIR);
    error(_FILE_WRITE_TEXT_LINES.args(PATH1, " 123"), Err.INVCAST);

    query(_FILE_WRITE_TEXT_LINES.args(PATH1, "x"));
    query(_FILE_SIZE.args(PATH1), 1 + Prop.NL.length());
    query(_FILE_WRITE_TEXT_LINES.args(PATH1, "\u00fc", "US-ASCII"));
    query(_FILE_READ_TEXT_LINES.args(PATH1), "?");
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void writeBinary() {
    // check errors
    final String bin = "xs:base64Binary('MA==')";
    error(_FILE_WRITE_BINARY.args(PATH, bin), Err.FILE_DIR);
    error(_FILE_WRITE_BINARY.args(PATH1, "NoBinary"), Err.BINARYTYPE);
    // write file and check size
    query(_FILE_WRITE_BINARY.args(PATH1, bin));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_WRITE_BINARY.args(PATH1, bin));
    query(_FILE_SIZE.args(PATH1), "1");
    // write data to specific offset and check size
    error(_FILE_WRITE_BINARY.args(PATH1, bin, 2), Err.FILE_BOUNDS);
    query(_FILE_WRITE_BINARY.args(PATH1, bin, 0));
    query(_FILE_READ_TEXT.args(PATH1), "0");
    query(_FILE_WRITE_BINARY.args(PATH1, bin, 1));
    query(_FILE_READ_TEXT.args(PATH1), "00");
    // delete size
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void appendBinary() {
    final String bin = "xs:base64Binary('MA==')";
    error(_FILE_APPEND_BINARY.args(PATH, bin), Err.FILE_DIR);
    error(_FILE_WRITE_BINARY.args(PATH1, "NoBinary"), Err.BINARYTYPE);

    query(_FILE_APPEND_BINARY.args(PATH1, bin));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_APPEND_BINARY.args(PATH1, bin));
    query(_FILE_READ_TEXT.args(PATH1), "00");
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void appendText() {
    error(_FILE_APPEND_TEXT.args(PATH, "x"), Err.FILE_DIR);
    error(_FILE_APPEND_TEXT.args(PATH1, " 123"), Err.INVCAST);

    query(_FILE_APPEND_TEXT.args(PATH1, "x"));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_APPEND_TEXT.args(PATH1, "\u00fc", "US-ASCII"));
    query(_FILE_READ_TEXT.args(PATH1), "x?");
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void appendTextLines() {
    error(_FILE_APPEND_TEXT_LINES.args(PATH, "x"), Err.FILE_DIR);
    error(_FILE_APPEND_TEXT_LINES.args(PATH1, " 123"), Err.INVCAST);

    query(_FILE_APPEND_TEXT_LINES.args(PATH1, "x"));
    query(_FILE_SIZE.args(PATH1), 1 + Prop.NL.length());
    query(_FILE_APPEND_TEXT_LINES.args(PATH1, "('y','z')"));
    query(_FILE_SIZE.args(PATH1), 3 * (1 + Prop.NL.length()));
    query(_FILE_APPEND_TEXT_LINES.args(PATH1, "\u00fc", "US-ASCII"));
    query(_FILE_READ_TEXT_LINES.args(PATH1), "x y z ?");
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void copy() {
    query(_FILE_WRITE.args(PATH1, "A"));
    query(_FILE_COPY.args(PATH1, PATH2));
    query(_FILE_COPY.args(PATH1, PATH2));
    query(_FILE_COPY.args(PATH2, PATH2));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_SIZE.args(PATH2), "1");
    error(_FILE_COPY.args(PATH1, PATH3), Err.FILE_NODIR);

    query(_FILE_DELETE.args(PATH1));
    query(_FILE_DELETE.args(PATH2));
  }

  /** Test method. */
  @Test
  public void move() {
    error(_FILE_MOVE.args(PATH1, PATH2), Err.FILE_WHICH);
    query(_FILE_WRITE.args(PATH1, "a"));
    query(_FILE_MOVE.args(PATH1, PATH2));
    query(_FILE_MOVE.args(PATH2, PATH1));
    query(_FILE_MOVE.args(PATH1, PATH1));
    query(_FILE_MOVE.args(PATH1 + "/../" + NAME, PATH1));
    error(_FILE_MOVE.args(PATH1, PATH4), Err.FILE_NODIR);
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_EXISTS.args(PATH2), false);
    query(_FILE_DELETE.args(PATH1));
  }

  /** Test method. */
  @Test
  public void resolvePath() {
    final String path = query(_FILE_RESOLVE_PATH.args(PATH1));
    final String can = new File(PATH1).getAbsolutePath();
    assertEquals(path.toLowerCase(Locale.ENGLISH), can.toLowerCase(Locale.ENGLISH));
  }

  /** Test method. */
  @Test
  public void pathToURI() {
    final String path = query(_FILE_PATH_TO_URI.args(PATH1));
    final String uri = new File(PATH1).toURI().toString();
    assertEquals(path.toLowerCase(Locale.ENGLISH), uri.toLowerCase(Locale.ENGLISH));
  }

  /** Test method. */
  @Test
  public void baseName() {
    // check with a simple path
    query(_FILE_BASE_NAME.args(PATH1), NAME);
    // check with a path ending with a directory separator
    query(_FILE_BASE_NAME.args(PATH1 + File.separator), NAME);
    // check with a path consisting only of directory separators
    query(_FILE_BASE_NAME.args("//"), "");
    // check with empty string path
    query(_FILE_BASE_NAME.args(""), ".");
    // check using a suffix
    query(_FILE_BASE_NAME.args(PATH1 + File.separator + "test.xml", ".xml"), "test");
  }

  /** Test method. */
  @Test
  public void dirName() {
    // check with a simple path
    assertEquals(norm(PATH), norm(query(_FILE_DIR_NAME.args(PATH1))).toLowerCase(Locale.ENGLISH));
    // check with an empty path
    query(_FILE_DIR_NAME.args(""), '.' + File.separator);
    // check with a path without directory separators
    query(_FILE_DIR_NAME.args(NAME), '.' + File.separator);
    // check with a path without directory separators
    query(_FILE_DIR_NAME.args("/"), File.separator);
  }

  /**
   * Tests method.
   * @throws IOException I/O exception
   */
  @Test
  public void pathToNative() throws IOException {
    assertEquals(norm(new File(PATH1).getCanonicalPath()),
        norm(query(_FILE_PATH_TO_NATIVE.args(PATH1))));
    query(_FILE_PATH_TO_NATIVE.args(PATH + ".." + "/test.xml"),
        new File(PATH + ".." + "/test.xml").getCanonicalPath());
  }

  /**
   * Normalize slashes of specified path to reduce OS dependent bugs.
   * @param path input path
   * @return normalized path
   */
  private static String norm(final String path) {
    return (path + '/').replaceAll("[\\\\/]+", "/").toLowerCase(Locale.ENGLISH);
  }
}

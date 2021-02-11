package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;

import org.basex.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the File Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public final class FileModuleTest extends SandboxTest {
  /** Test path. */
  private static final String PATH = Prop.TEMPDIR + NAME + '/';
  /** Test path. */
  private static final String PATH1 = PATH + NAME;
  /** Test path. */
  private static final String PATH2 = PATH + NAME + '2';
  /** Test path. */
  private static final String PATH3 = PATH + NAME + "/x";
  /** Test path. */
  private static final String PATH4 = PATH + NAME + "/x/y";

  /** Initializes the test. */
  @AfterEach public void init() {
    new IOFile(PATH4).delete();
    new IOFile(PATH3).delete();
    new IOFile(PATH2).delete();
    new IOFile(PATH1).delete();
  }

  /** Test method. */
  @Test public void append() {
    final Function func = _FILE_APPEND;
    // successful queries
    error(func.args(PATH, " ()"), FILE_IS_DIR_X);
    error(func.args(PATH4, " ()"), FILE_NO_DIR_X);

    query(func.args(PATH1, 0));
    query(_FILE_SIZE.args(PATH1), 1);
    query(func.args(PATH1, 0, " ()"));
    query(_FILE_SIZE.args(PATH1), 2);
    query(_FILE_DELETE.args(PATH1));

    query(func.args(PATH1, "a\u00e4", ' ' + serialParams("<encoding value='CP1252'/>")));
    query(_FILE_READ_TEXT.args(PATH1, "CP1252"), "a\u00e4");
    query(_FILE_DELETE.args(PATH1));

    query(func.args(PATH1, "<a/>", ' ' + serialParams("<method value='text'/>")));
    query(_FILE_READ_TEXT.args(PATH1), "<a/>");
  }

  /** Test method. */
  @Test public void appendBinary() {
    final Function func = _FILE_APPEND_BINARY;
    // queries
    final String bin = " xs:base64Binary('MA==')";
    error(func.args(PATH, bin), FILE_IS_DIR_X);
    error(_FILE_WRITE_BINARY.args(PATH1, "NoBinary"), BINARY_X);

    query(func.args(PATH1, bin));
    query(_FILE_SIZE.args(PATH1), 1);
    query(func.args(PATH1, bin));
    query(_FILE_READ_TEXT.args(PATH1), "00");
  }

  /** Test method. */
  @Test public void appendText() {
    final Function func = _FILE_APPEND_TEXT;
    // queries
    error(func.args(PATH, "x"), FILE_IS_DIR_X);
    error(func.args(PATH1, " 123"), INVTYPE_X_X_X);

    query(func.args(PATH1, "x"));
    query(_FILE_SIZE.args(PATH1), 1);
    query(func.args(PATH1, "\u00fc", "US-ASCII"));
    query(_FILE_READ_TEXT.args(PATH1), "x?");
  }

  /** Test method. */
  @Test public void appendTextLines() {
    final Function func = _FILE_APPEND_TEXT_LINES;
    // queries
    error(func.args(PATH, "x"), FILE_IS_DIR_X);
    error(func.args(PATH1, 123), INVTYPE_X_X_X);

    query(func.args(PATH1, "x"));
    query(_FILE_SIZE.args(PATH1), 1 + Prop.NL.length());
    query(func.args(PATH1, " ('y','z')"));
    query(_FILE_SIZE.args(PATH1), 3 * (1 + Prop.NL.length()));
    query(func.args(PATH1, "\u00fc", "US-ASCII"));
    query(_FILE_READ_TEXT_LINES.args(PATH1), "x\ny\nz\n?");
  }

  /** Test method. */
  @Test public void baseDir() {
    final Function func = _FILE_BASE_DIR;
    // successful queries
    final Path path = Paths.get(".").toAbsolutePath().getParent();
    assertEquals(query(func.args()), path + File.separator);
  }

  /** Test method. */
  @Test public void children() {
    final Function func = _FILE_CHILDREN;
    // successful queries
    query(_FILE_WRITE.args(PATH1, "abcd"));
    error(func.args(PATH1), FILE_NO_DIR_X);
    error(func.args(PATH1 + NAME), FILE_NO_DIR_X);
    query(_FILE_WRITE.args(PATH1, " ()"));
    error(func.args(PATH1), FILE_NO_DIR_X);
    contains(func.args(PATH), NAME);
  }

  /** Test method. */
  @Test public void copy() {
    final Function func = _FILE_COPY;
    // queries
    query(_FILE_WRITE.args(PATH1, "A"));
    query(func.args(PATH1, PATH2));
    query(func.args(PATH1, PATH2));
    query(func.args(PATH2, PATH2));
    query(_FILE_SIZE.args(PATH1), 1);
    query(_FILE_SIZE.args(PATH2), 1);
    error(func.args(PATH1, PATH3), FILE_NO_DIR_X);
  }

  /** Test method. */
  @Test public void createDir() {
    final Function func = _FILE_CREATE_DIR;
    // successful queries
    query(func.args(PATH1));
    query(func.args(PATH1));
    query(func.args(PATH3));
    query(_FILE_DELETE.args(PATH1, true));
    query(_FILE_WRITE.args(PATH1, " ()"));
    error(func.args(PATH1), FILE_EXISTS_X);
    error(func.args(PATH3), FILE_EXISTS_X);
  }

  /** Test method. */
  @Test public void createTempDir() {
    final Function func = _FILE_CREATE_TEMP_DIR;
    // successful queries
    final String tmp = query(func.args("", ""));
    query(_FILE_EXISTS.args(tmp), true);
    query(_FILE_IS_DIR.args(tmp), true);
    query(_FILE_IS_FILE.args(tmp), false);
    query(_FILE_DELETE.args(tmp));
  }

  /** Test method. */
  @Test public void createTempFile() {
    final Function func = _FILE_CREATE_TEMP_FILE;
    // successful queries
    final String tmp = query(func.args("", ""));
    query(_FILE_EXISTS.args(tmp), true);
    query(_FILE_IS_DIR.args(tmp), false);
    query(_FILE_IS_FILE.args(tmp), true);
    query(_FILE_DELETE.args(tmp));
  }

  /** Test method. */
  @Test public void currentDir() {
    final Function func = _FILE_CURRENT_DIR;
    // successful queries
    final Path path = Paths.get(".").toAbsolutePath().getParent();
    assertEquals(query(func.args()), path + File.separator);
  }

  /** Test method. */
  @Test public void delete() {
    final Function func = _FILE_DELETE;
    // successful queries
    query(_FILE_CREATE_DIR.args(PATH3));
    query(func.args(PATH3));
    query(_FILE_CREATE_DIR.args(PATH3));
    query(_FILE_WRITE.args(PATH4, " ()"));
    query(func.args(PATH1, true));
    error(func.args(PATH1), FILE_NOT_FOUND_X);
  }

  /** Test method. */
  @Test public void descendants() {
    final Function func = _FILE_DESCENDANTS;
    query(_FILE_CREATE_DIR.args(PATH1));
    query(_FILE_WRITE.args(PATH3, "abcd"));

    error(func.args(PATH3), FILE_NO_DIR_X);
    error(func.args(PATH3 + NAME), FILE_NO_DIR_X);
    contains(func.args(PATH1), "x");
  }

  /** Test method. */
  @Test public void dirSeparator() {
    final Function func = _FILE_DIR_SEPARATOR;
    // successful queries
    assertFalse(query(func.args()).isEmpty());
  }

  /** Test method. */
  @Test public void exists() {
    final Function func = _FILE_EXISTS;
    // successful queries
    final String url = IO.FILEPREF + '/' + (Prop.WIN ? '/' + PATH1.replace("\\", "/") : PATH1);
    query(_FILE_WRITE.args(PATH1, " ()"));
    query(func.args(PATH1), true);
    query(func.args(url), true);
    query(_FILE_DELETE.args(PATH1));
    query(func.args(PATH1), false);
    query(func.args(url), false);
  }

  /** Test method. */
  @Test public void isAbsolute() {
    final Function func = _FILE_IS_ABSOLUTE;
    // successful queries
    query(func.args(PATH), true);
    query(func.args("a"), false);
  }

  /** Test method. */
  @Test public void isDir() {
    final Function func = _FILE_IS_DIR;
    // successful queries
    query(func.args(PATH), true);
    query(_FILE_WRITE.args(PATH1, " ()"));
    query(func.args(PATH1), false);
    query(_FILE_DELETE.args(PATH1));
    query(_FILE_CREATE_DIR.args(PATH1));
    query(func.args(PATH1), true);
  }

  /** Test method. */
  @Test public void isFile() {
    final Function func = _FILE_IS_FILE;
    // successful queries
    query(func.args(PATH), false);
    query(_FILE_WRITE.args(PATH1, " ()"));
    query(func.args(PATH1), true);
    query(_FILE_DELETE.args(PATH1));
    query(_FILE_CREATE_DIR.args(PATH1));
    query(func.args(PATH1), false);
  }

  /** Test method. */
  @Test public void lastModified() {
    final Function func = _FILE_LAST_MODIFIED;
    // successful queries
    assertFalse(query(func.args(PATH)).isEmpty());
  }

  /** Test method. */
  @Test public void lineSeparator() {
    final Function func = _FILE_LINE_SEPARATOR;
    // successful queries
    assertFalse(query(func.args()).isEmpty());
  }

  /** Test method. */
  @Test public void list() {
    final Function func = _FILE_LIST;
    // successful queries
    query(_FILE_WRITE.args(PATH1, "abcd"));
    error(func.args(PATH1), FILE_NO_DIR_X);
    error(func.args(PATH1 + NAME), FILE_NO_DIR_X);
    query(_FILE_WRITE.args(PATH1, " ()"));
    error(func.args(PATH1), FILE_NO_DIR_X);
    query(func.args(PATH), NAME);
    contains(func.args(PATH, false), NAME);
    contains(func.args(PATH, false, NAME), NAME);
    query(func.args(PATH, false, "XXX"), "");
    query(_FILE_DELETE.args(PATH1));
    // check recursive paths
    query(_FILE_CREATE_DIR.args(PATH1));
    query(_FILE_CREATE_DIR.args(PATH3));
    query(_FILE_WRITE.args(PATH4, " ()"));
    contains(func.args(PATH1, true), "y");
    query(func.args(PATH1, true, "x"), 'x' + File.separator);
  }

  /** Test method. */
  @Test public void move() {
    final Function func = _FILE_MOVE;
    // queries
    error(func.args(PATH1, PATH2), FILE_NOT_FOUND_X);
    query(_FILE_WRITE.args(PATH1, "a"));
    query(func.args(PATH1, PATH2));
    query(func.args(PATH2, PATH1));
    query(func.args(PATH1, PATH1));
    query(func.args(PATH + "../" + NAME + '/' + NAME, PATH1));
    error(func.args(PATH1, PATH4), FILE_NO_DIR_X);
    query(_FILE_SIZE.args(PATH1), 1);
    query(_FILE_EXISTS.args(PATH2), false);
  }

  /** Test method. */
  @Test public void name() {
    final Function func = _FILE_NAME;
    // check with a simple path
    query(func.args(PATH1), NAME);
    // check with a path ending with a directory separator
    query(func.args(PATH1 + File.separator), NAME);
    // check with a path consisting only of directory separators
    query(func.args("/"), "");
    // check with empty string path
    query(func.args(""), "");
    // check using a suffix
    query(func.args(PATH1 + "/test.xml"), "test.xml");
  }

  /** Test method. */
  @Test public void parent() {
    final Function func = _FILE_PARENT;
    // check with a simple path
    assertEquals(Paths.get(PATH1).getParent() + File.separator, query(func.args(PATH1)));
    // check with an empty path
    query("empty(" + func.args("") + ")", false);
    // check with a path without directory separators
    query("empty(" + func.args(NAME) + ")", false);
    // check with a path without directory separators
    query("empty(" + func.args("/") + ")", true);
  }

  /** Test method. */
  @Test public void pathSeparator() {
    final Function func = _FILE_PATH_SEPARATOR;
    // successful queries
    assertFalse(query(func.args()).isEmpty());
  }

  /**
   * Tests method.
   * @throws IOException I/O exception
   */
  @Test public void pathToNative() throws IOException {
    final Function func = _FILE_PATH_TO_NATIVE;
    // queries
    query(_FILE_WRITE.args(PATH1, " ()"));
    assertEquals(Paths.get(PATH1).toRealPath().toString(), query(func.args(PATH1)));
    query(func.args(PATH + "../" + NAME + '/' + NAME),
        Paths.get(PATH + "../" + NAME + '/' + NAME).toRealPath().toString());
    error(func.args(PATH1 + NAME), FILE_NOT_FOUND_X);
  }

  /** Test method. */
  @Test public void pathToURI() {
    final Function func = _FILE_PATH_TO_URI;
    // queries
    final String path = query(func.args(PATH1));
    final String uri = Paths.get(PATH1).toUri().toString();
    assertEquals(path, uri);
  }

  /** Test method. */
  @Test public void readBinary() {
    final Function func = _FILE_READ_BINARY;
    // check errors
    error(func.args(PATH1), FILE_NOT_FOUND_X);
    error(func.args(PATH), FILE_IS_DIR_X);
    // file with single codepoint
    query(_FILE_WRITE.args(PATH1, 0));
    query(func.args(PATH1), 0);
    query(func.args(PATH1, 0), 0);
    query(func.args(PATH1, 0, 1), 0);
    query(func.args(PATH1, 1), "");
    query(func.args(PATH1, 1, 0), "");
    query(func.args(PATH1, 0, 0), "");
    error(func.args(PATH1, -1), FILE_OUT_OF_RANGE_X_X);
    error(func.args(PATH1, 2), FILE_OUT_OF_RANGE_X_X);
    error(func.args(PATH1, 0, -1), FILE_OUT_OF_RANGE_X_X);
    error(func.args(PATH1, 0, 2), FILE_OUT_OF_RANGE_X_X);
    error(func.args(PATH1, 2, 1), FILE_OUT_OF_RANGE_X_X);
    // file with two codepoints
    query(_FILE_WRITE.args(PATH1, "a\u00e4"));
    query(func.args(PATH1), "a\u00e4");
    // file with two codepoints
    query(_FILE_WRITE_BINARY.args(PATH1, " " + _CONVERT_STRING_TO_BASE64.args("a\u00e4")));
    query(func.args(PATH1), "a\u00e4");
  }

  /** Test method. */
  @Test public void readText() {
    final Function func = _FILE_READ_TEXT;
    // successful queries
    error(func.args(PATH1), FILE_NOT_FOUND_X);
    error(func.args(PATH), FILE_IS_DIR_X);
    query(_FILE_WRITE.args(PATH1, "a\u00e4"));
    query(func.args(PATH1), "a\u00e4");
    error(func.args(PATH1, "UNKNOWN"), FILE_UNKNOWN_ENCODING_X);
    assertEquals(3, query(func.args(PATH1, "CP1252")).length());
    query(_FILE_WRITE_BINARY.args(PATH1, " xs:hexBinary('00')"));
    error(func.args(PATH1), FILE_IO_ERROR_X);
  }

  /** Test method. */
  @Test public void resolvePath() {
    final Function func = _FILE_RESOLVE_PATH;
    // queries
    final String path = query(func.args(PATH1));
    final String can1 = Paths.get(PATH1).normalize().toAbsolutePath().toString();
    final String can2 = Paths.get(PATH2).normalize().toAbsolutePath().toString();
    assertEquals(path, can1);
    query("ends-with(" + func.args(".") + ", '" + File.separator + "')", true);

    query("contains(" + func.args(can1, can2) + ", \"" + can1 + "\")", true);
    query("contains(" + func.args("X", can1 + File.separator) + ", \"" +
        can1 + File.separator + "X\")", true);
    error(func.args(can1, "b"), FILE_IS_RELATIVE_X);
    error(func.args("X", "b"), FILE_IS_RELATIVE_X);
  }

  /** Test method. */
  @Test public void size() {
    final Function func = _FILE_SIZE;
    // successful queries
    query(_FILE_WRITE.args(PATH1, "abcd"));
    query(func.args(PATH1), 4);
  }

  /** Test method. */
  @Test public void tempDir() {
    final Function func = _FILE_TEMP_DIR;
    // successful queries
    assertEquals(query(func.args()), Prop.TEMPDIR);
  }

  /** Test method. */
  @Test public void write() {
    final Function func = _FILE_WRITE;
    // successful queries
    error(func.args(PATH, " ()"), FILE_IS_DIR_X);
    error(func.args(PATH4, " ()"), FILE_NO_DIR_X);

    query(func.args(PATH1, 0));
    query(_FILE_SIZE.args(PATH1), 1);
    query(func.args(PATH1, 0));
    query(_FILE_SIZE.args(PATH1), 1);
    query(_FILE_DELETE.args(PATH1));

    query(func.args(PATH1, "a\u00e4", ' ' + serialParams("<encoding value='CP1252'/>")));
    query(_FILE_READ_TEXT.args(PATH1, "CP1252"), "a\u00e4");

    query(func.args(PATH1, "<a/>", ' ' + serialParams("<method value='text'/>")));
    query(_FILE_READ_TEXT.args(PATH1), "<a/>");
    query(_FILE_DELETE.args(PATH1));

    // test spaces in filename
    query(func.args(PATH1 + "%20X", ""));
    query(_FILE_EXISTS.args(PATH1 + "%20X"), true);
    query(_FILE_DELETE.args(PATH1 + "%20X"));
    query(_FILE_EXISTS.args(PATH1 + "%20X"), false);

    query(func.args(PATH1 + " X", ""));
    query(_FILE_EXISTS.args(PATH1 + " X"), true);
    query(_FILE_DELETE.args(PATH1 + " X"));
    query(_FILE_EXISTS.args(PATH1 + " X"), false);
  }

  /** Test method. */
  @Test public void writeBinary() {
    final Function func = _FILE_WRITE_BINARY;
    // check errors
    final String bin = " xs:base64Binary('MA==')";
    error(func.args(PATH, bin), FILE_IS_DIR_X);
    error(func.args(PATH1, "NoBinary"), BINARY_X);
    // write file and check size
    query(func.args(PATH1, bin));
    query(_FILE_SIZE.args(PATH1), 1);
    query(func.args(PATH1, bin));
    query(_FILE_SIZE.args(PATH1), 1);
    // write data to specific offset and check size
    error(func.args(PATH1, bin, 2), FILE_OUT_OF_RANGE_X_X);
    query(func.args(PATH1, bin, 0));
    query(_FILE_READ_TEXT.args(PATH1), "0");
    query(func.args(PATH1, bin, 1));
    query(_FILE_READ_TEXT.args(PATH1), "00");
  }

  /** Test method. */
  @Test public void writeText() {
    final Function func = _FILE_WRITE_TEXT;
    // queries
    error(func.args(PATH, "x"), FILE_IS_DIR_X);
    error(func.args(PATH1, " 123"), INVTYPE_X_X_X);

    query(func.args(PATH1, "x"));
    query(_FILE_SIZE.args(PATH1), 1);
    query(func.args(PATH1, "\u00fc", "US-ASCII"));
    query(_FILE_READ_TEXT.args(PATH1), "?");
  }

  /** Test method. */
  @Test public void writeTextLines() {
    final Function func = _FILE_WRITE_TEXT_LINES;
    // queries
    error(func.args(PATH, "x"), FILE_IS_DIR_X);
    error(func.args(PATH1, " 123"), INVTYPE_X_X_X);

    query(func.args(PATH1, "x"));
    query(_FILE_SIZE.args(PATH1), 1 + Prop.NL.length());
    query(func.args(PATH1, "\u00fc", "US-ASCII"));
    query(_FILE_READ_TEXT_LINES.args(PATH1), "?");
  }
}

package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.basex.core.Prop;
import org.basex.query.util.Err;
import org.basex.test.query.AdvancedQueryTest;
import org.basex.util.Util;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the functions of the file library.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 */
public final class FNFileTest extends AdvancedQueryTest {
  /** Test name. */
  private static final String NAME = Util.name(FNFileTest.class);
  /** Test path. */
  private static final String PATH1 = Prop.TMP + NAME;
  /** Test path. */
  private static final String PATH2 = Prop.TMP + NAME + "2";
  /** Test path. */
  private static final String PATH3 = Prop.TMP + NAME + "/x";
  /** Test path. */
  private static final String PATH4 = Prop.TMP + NAME + "/x/x";

  /** Initializes the test. */
  @BeforeClass
  public static void init() {
    new File(PATH4).delete();
    new File(PATH3).delete();
    new File(PATH2).delete();
    new File(PATH1).delete();
  }

  /**
   * Test method for the file:exists() function.
   */
  @Test
  public void fileExists() {
    check(_FILE_EXISTS);
    query(_FILE_WRITE.args(PATH1, "()"));
    query(_FILE_EXISTS.args(PATH1), true);
    query(_FILE_DELETE.args(PATH1));
    query(_FILE_EXISTS.args(PATH1), false);
  }

  /**
   * Test method for the file:is-directory() function.
   */
  @Test
  public void fileIsDirectory() {
    check(_FILE_IS_DIRECTORY);
    query(_FILE_IS_DIRECTORY.args(Prop.TMP), true);
    query(_FILE_WRITE.args(PATH1, "()"));
    query(_FILE_IS_DIRECTORY.args(PATH1), false);
    query(_FILE_DELETE.args(PATH1));
    query(_FILE_CREATE_DIRECTORY.args(PATH1));
    query(_FILE_IS_DIRECTORY.args(PATH1), true);
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:is-file() function.
   */
  @Test
  public void fileIsFile() {
    check(_FILE_IS_FILE);
    query(_FILE_IS_FILE.args(Prop.TMP), false);
    query(_FILE_WRITE.args(PATH1, "()"));
    query(_FILE_IS_FILE.args(PATH1), true);
    query(_FILE_DELETE.args(PATH1));
    query(_FILE_CREATE_DIRECTORY.args(PATH1));
    query(_FILE_IS_FILE.args(PATH1), false);
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:last-modified() function.
   */
  @Test
  public void fileLastModified() {
    check(_FILE_LAST_MODIFIED);
    assertTrue(!query(_FILE_LAST_MODIFIED.args(Prop.TMP)).isEmpty());
  }

  /**
   * Test method for the file:size() function.
   */
  @Test
  public void fileSize() {
    check(_FILE_SIZE);
    query(_FILE_WRITE.args(PATH1, "abcd"));
    query(_FILE_SIZE.args(PATH1), "4");
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:list() function.
   */
  @Test
  public void fileList() {
    check(_FILE_LIST);
    error(_FILE_LIST.args(PATH1), Err.NOTDIR);
    query(_FILE_WRITE.args(PATH1, "()"));
    error(_FILE_LIST.args(PATH1), Err.NOTDIR);
    contains(_FILE_LIST.args(Prop.TMP), NAME);
    contains(_FILE_LIST.args(Prop.TMP, "false()"), NAME);
    contains(_FILE_LIST.args(Prop.TMP, "false()", "FN"), NAME);
    contains(_FILE_LIST.args(Prop.TMP, "false()", NAME), NAME);
    query(_FILE_LIST.args(Prop.TMP, "false()", "XXX"), "");
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:create-directory() function.
   */
  @Test
  public void fileCreateDirectory() {
    check(_FILE_CREATE_DIRECTORY);
    query(_FILE_CREATE_DIRECTORY.args(PATH1));
    query(_FILE_CREATE_DIRECTORY.args(PATH1));
    query(_FILE_CREATE_DIRECTORY.args(PATH3));
    query(_FILE_DELETE.args(PATH1));
    query(_FILE_WRITE.args(PATH1, "()"));
    error(_FILE_CREATE_DIRECTORY.args(PATH1), Err.FILEEXISTS);
    error(_FILE_CREATE_DIRECTORY.args(PATH3), Err.FILEEXISTS);
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:delete() function.
   */
  @Test
  public void fileDelete() {
    check(_FILE_DELETE);
    query(_FILE_CREATE_DIRECTORY.args(PATH3));
    query(_FILE_DELETE.args(PATH3));
    query(_FILE_CREATE_DIRECTORY.args(PATH3));
    query(_FILE_WRITE.args(PATH4, "()"));
    query(_FILE_DELETE.args(PATH1));
    error(_FILE_DELETE.args(PATH1), Err.PATHNOTEXISTS);
  }

  /**
   * Test method for the file:read-text() function.
   */
  @Test
  public void fileReadText() {
    check(_FILE_READ_TEXT);
    error(_FILE_READ_TEXT.args(PATH1), Err.PATHNOTEXISTS);
    error(_FILE_READ_TEXT.args(Prop.TMP), Err.PATHISDIR);
    query(_FILE_WRITE.args(PATH1, "a\u00e4"));
    query(_FILE_READ_TEXT.args(PATH1), "a\u00e4");
    error(_FILE_READ_TEXT.args(PATH1, "UNKNOWN"), Err.ENCNOTEXISTS);
    assertTrue(query(_FILE_READ_TEXT.args(PATH1, "CP1252")).length() == 3);
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:read-binary() function.
   */
  @Test
  public void fileReadBinary() {
    check(_FILE_READ_BINARY);
    error(_FILE_READ_BINARY.args(PATH1), Err.PATHNOTEXISTS);
    error(_FILE_READ_BINARY.args(Prop.TMP), Err.PATHISDIR);
    query(_FILE_WRITE.args(PATH1, "0"));
    query(_FILE_READ_BINARY.args(PATH1), "MA==");
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:write() function.
   */
  @Test
  public void fileWrite() {
    check(_FILE_WRITE);

    error(_FILE_WRITE.args(Prop.TMP, "()"), Err.PATHISDIR);

    query(_FILE_WRITE.args(PATH1, "0"));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_WRITE.args(PATH1, "0"));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_DELETE.args(PATH1));

    query(_FILE_WRITE.args(PATH1, "a\u00e4",
        serialParams("<encoding value='CP1252'/>")));
    query(_FILE_READ_TEXT.args(PATH1, "CP1252"), "a\u00e4");

    query(_FILE_WRITE.args(PATH1, "\"<a/>\"",
        serialParams("<method value='text'/>")));
    query(_FILE_READ_TEXT.args(PATH1), "&amp;lt;a/&amp;gt;");
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:append() function.
   */
  @Test
  public void fileAppend() {
    check(_FILE_APPEND);

    error(_FILE_APPEND.args(Prop.TMP, "()"), Err.PATHISDIR);

    query(_FILE_APPEND.args(PATH1, "0"));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_APPEND.args(PATH1, "0", "()"));
    query(_FILE_SIZE.args(PATH1), "2");
    query(_FILE_DELETE.args(PATH1));

    query(_FILE_APPEND.args(PATH1, "a\u00e4",
        serialParams("<encoding value='CP1252'/>")));
    query(_FILE_READ_TEXT.args(PATH1, "CP1252"), "a\u00e4");
    query(_FILE_DELETE.args(PATH1));

    query(_FILE_APPEND.args(PATH1, "\"<a/>\"",
        serialParams("<method value='text'/>")));
    query(_FILE_READ_TEXT.args(PATH1), "&amp;lt;a/&amp;gt;");
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:write-binary() function.
   */
  @Test
  public void fileWriteBinary() {
    check(_FILE_WRITE_BINARY);

    final String bin = "xs:base64Binary('MA==')";
    error(_FILE_WRITE_BINARY.args(Prop.TMP, bin), Err.PATHISDIR);
    query(_FILE_WRITE_BINARY.args(PATH1, bin));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_WRITE_BINARY.args(PATH1, bin));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:append-binary() function.
   */
  @Test
  public void fileAppendBinary() {
    check(_FILE_APPEND_BINARY);

    final String bin = "xs:base64Binary('MA==')";
    error(_FILE_APPEND_BINARY.args(Prop.TMP, bin), Err.PATHISDIR);
    query(_FILE_APPEND_BINARY.args(PATH1, bin));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_APPEND_BINARY.args(PATH1, bin));
    query(_FILE_READ_TEXT.args(PATH1), "00");
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:copy() function.
   */
  @Test
  public void fileCopy() {
    check(_FILE_COPY);

    query(_FILE_WRITE.args(PATH1, "A"));
    query(_FILE_COPY.args(PATH1, PATH2));
    query(_FILE_COPY.args(PATH1, PATH2));
    query(_FILE_COPY.args(PATH2, PATH2));
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_SIZE.args(PATH2), "1");
    error(_FILE_COPY.args(PATH1, PATH3), Err.NOTDIR);

    query(_FILE_DELETE.args(PATH1));
    query(_FILE_DELETE.args(PATH2));
  }

  /**
   * Test method for the file:move() function.
   */
  @Test
  public void fileMove() {
    check(_FILE_MOVE);

    error(_FILE_MOVE.args(PATH1, PATH2), Err.PATHNOTEXISTS);
    query(_FILE_WRITE.args(PATH1, "a"));
    query(_FILE_MOVE.args(PATH1, PATH2));
    query(_FILE_MOVE.args(PATH2, PATH1));
    query(_FILE_MOVE.args(PATH1, PATH1));
    error(_FILE_MOVE.args(PATH1, PATH4), Err.NOTDIR);
    query(_FILE_SIZE.args(PATH1), "1");
    query(_FILE_EXISTS.args(PATH2), false);
    query(_FILE_DELETE.args(PATH1));
  }

  /**
   * Test method for the file:resolve-path() function.
   */
  @Test
  public void fileResolvePath() {
    check(_FILE_RESOLVE_PATH);
    final String path = query(_FILE_RESOLVE_PATH.args(PATH1));
    final String can = new File(PATH1).getAbsolutePath();
    assertEquals(path.toLowerCase(Locale.ENGLISH), can.toLowerCase());
  }

  /**
   * Test method for the file:path-to-uri() function.
   */
  @Test
  public void filePathToURI() {
    check(_FILE_PATH_TO_URI);
    final String path = query(_FILE_PATH_TO_URI.args(PATH1));
    final String uri = new File(PATH1).toURI().toString();
    assertEquals(path.toLowerCase(Locale.ENGLISH), uri.toLowerCase());
  }

  /**
   * Tests method for file:base-name() function.
   */
  @Test
  public void fileBaseName() {
    check(_FILE_BASE_NAME);

    // check with a simple path
    query(_FILE_BASE_NAME.args(PATH1), NAME);
    // check with a path ending with a directory separator
    query(_FILE_BASE_NAME.args(PATH1 + File.separator), NAME);
    // check with a path consisting only of directory separators
    query(_FILE_BASE_NAME.args("//"), "");
    // check with empty string path
    query(_FILE_BASE_NAME.args(""), ".");
    // check using a suffix
    query(_FILE_BASE_NAME.args(PATH1 + File.separator + "test.xml", ".xml"),
        "test");
  }

  /**
   * Tests method for file:dir-name() function.
   */
  @Test
  public void fileDirName() {
    check(_FILE_DIR_NAME);
    // check with a simple path
    assertEquals(norm(Prop.TMP),
        norm(query(_FILE_DIR_NAME.args(PATH1))).toLowerCase(Locale.ENGLISH));
    // check with an empty path
    query(_FILE_DIR_NAME.args(""), ".");
    // check with a path without directory separators
    query(_FILE_DIR_NAME.args(NAME), ".");
  }

  /**
   * Tests method for file:path-to-native() function.
   * @throws IOException I/O exception
   */
  @Test
  public void filePathToNative() throws IOException {
    check(_FILE_PATH_TO_NATIVE);
    assertEquals(norm(new File(PATH1).getCanonicalPath()),
        norm(query(_FILE_PATH_TO_NATIVE.args(PATH1))));
    query(_FILE_PATH_TO_NATIVE.args(Prop.TMP + ".." + "/test.xml"),
        new File(Prop.TMP + ".." + "/test.xml").getCanonicalPath());
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

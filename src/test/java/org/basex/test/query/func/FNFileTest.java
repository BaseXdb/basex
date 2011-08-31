package org.basex.test.query.func;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.basex.core.Prop;
import org.basex.query.func.Function;
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
    final String fun = check(Function.FEXISTS);
    query("file:write('" + PATH1 + "', ())");
    query(fun + "('" + PATH1 + "')", "true");
    query("file:delete('" + PATH1 + "')");
    query(fun + "('" + PATH1 + "')", "false");
  }

  /**
   * Test method for the file:is-directory() function.
   */
  @Test
  public void fileIsDirectory() {
    final String fun = check(Function.ISDIR);
    query(fun + "('" + Prop.TMP + "')", "true");
    query(fun + "('" + Prop.TMP + "')", "true");
    query("file:write('" + PATH1 + "', ())");
    query(fun + "('" + PATH1 + "')", "false");
    query("file:delete('" + PATH1 + "')");
    query("file:create-directory('" + PATH1 + "')");
    query(fun + "('" + PATH1 + "')", "true");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:is-file() function.
   */
  @Test
  public void fileIsFile() {
    final String fun = check(Function.ISFILE);
    query(fun + "('" + Prop.TMP + "')", "false");
    query("file:write('" + PATH1 + "', ())");
    query(fun + "('" + PATH1 + "')", "true");
    query("file:delete('" + PATH1 + "')");
    query("file:create-directory('" + PATH1 + "')");
    query(fun + "('" + PATH1 + "')", "false");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:last-modified() function.
   */
  @Test
  public void fileLastModified() {
    final String fun = check(Function.LASTMOD);
    assertTrue(!query(fun + "('" + Prop.TMP + "')").isEmpty());
  }

  /**
   * Test method for the file:size() function.
   */
  @Test
  public void fileSize() {
    final String fun = check(Function.SIZE);
    query("file:write('" + PATH1 + "', 'abcd')");
    query(fun + "('" + PATH1 + "')", "4");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:list() function.
   */
  @Test
  public void fileList() {
    final String fun = check(Function.FLIST);
    error(fun + "('" + PATH1 + "')", Err.NOTDIR);
    query("file:write('" + PATH1 + "', ())");
    error(fun + "('" + PATH1 + "')", Err.NOTDIR);
    contains(fun + "('" + Prop.TMP + "')", NAME);
    contains(fun + "('" + Prop.TMP + "',false())", NAME);
    contains(fun + "('" + Prop.TMP + "',false()," + "'FN')", NAME);
    contains(fun + "('" + Prop.TMP + "',false(),'" + NAME + "')", NAME);
    query(fun + "('" + Prop.TMP + "', false()," + "'XXX')", "");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:create-directory() function.
   */
  @Test
  public void fileCreateDirectory() {
    final String fun = check(Function.CREATEDIR);
    query(fun + "('" + PATH1 + "')");
    query(fun + "('" + PATH1 + "')");
    query(fun + "('" + PATH3 + "')");
    query("file:delete('" + PATH1 + "')");
    query("file:write('" + PATH1 + "', ())");
    error(fun + "('" + PATH1 + "')", Err.FILEEXISTS);
    error(fun + "('" + PATH3 + "')", Err.FILEEXISTS);
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:delete() function.
   */
  @Test
  public void fileDelete() {
    final String fun = check(Function.DELETE);
    query("file:create-directory('" + PATH3 + "')");
    query(fun + "('" + PATH3 + "')");
    query("file:create-directory('" + PATH3 + "')");
    query("file:write('" + PATH4 + "', ())");
    query(fun + "('" + PATH1 + "')");
    error(fun + "('" + PATH1 + "')", Err.PATHNOTEXISTS);
  }

  /**
   * Test method for the file:read-text() function.
   */
  @Test
  public void fileReadText() {
    final String fun = check(Function.READTEXT);
    error(fun + "('" + PATH1 + "')", Err.PATHNOTEXISTS);
    error(fun + "('" + Prop.TMP + "')", Err.PATHISDIR);
    query("file:write('" + PATH1 + "', 'a\u00e4')");
    query(fun + "('" + PATH1 + "')", "a\u00e4");
    error(fun + "('" + PATH1 + "', 'UNKNOWN')", Err.ENCNOTEXISTS);
    assertTrue(query(fun + "('" + PATH1 + "', 'CP1252')").length() == 3);
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:read-binary() function.
   */
  @Test
  public void fileReadBinary() {
    final String fun = check(Function.READBIN);
    error(fun + "('" + PATH1 + "')", Err.PATHNOTEXISTS);
    error(fun + "('" + Prop.TMP + "')", Err.PATHISDIR);
    query("file:write('" + PATH1 + "', '0')");
    query(fun + "('" + PATH1 + "')", "MA==");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:write() function.
   */
  @Test
  public void fileWrite() {
    final String fun = check(Function.WRITE);

    error(fun + "('" + Prop.TMP + "', ())", Err.PATHISDIR);

    query(fun + "('" + PATH1 + "', '0')");
    query("file:size('" + PATH1 + "')", "1");
    query(fun + "('" + PATH1 + "', '0')");
    query("file:size('" + PATH1 + "')", "1");
    query("file:delete('" + PATH1 + "')");

    query(fun + "('" + PATH1 + "', 'a\u00e4',"
        + serialParams("<encoding>CP1252</encoding>") + ")");
    query("file:read-text('" + PATH1 + "', 'CP1252')", "a\u00e4");

    query(fun + "('" + PATH1 + "', '<a/>',"
        + serialParams("<method>text</method>") + ")");
    query("file:read-text('" + PATH1 + "')", "&amp;lt;a/&amp;gt;");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:append() function.
   */
  @Test
  public void fileAppend() {
    final String fun = check(Function.APPEND);

    error(fun + "('" + Prop.TMP + "', ())", Err.PATHISDIR);

    query(fun + "('" + PATH1 + "', '0')");
    query("file:size('" + PATH1 + "')", "1");
    query(fun + "('" + PATH1 + "', '0', ())");
    query("file:size('" + PATH1 + "')", "2");
    query("file:delete('" + PATH1 + "')");

    query(fun + "('" + PATH1 + "', 'a\u00e4',"
        + serialParams("<encoding>CP1252</encoding>") + ")");
    query("file:read-text('" + PATH1 + "', 'CP1252')", "a\u00e4");
    query("file:delete('" + PATH1 + "')");

    query(fun + "('" + PATH1 + "', '<a/>',"
        + serialParams("<method>text</method>") + ")");
    query("file:read-text('" + PATH1 + "')", "&amp;lt;a/&amp;gt;");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:write-binary() function.
   */
  @Test
  public void fileWriteBinary() {
    final String fun = check(Function.WRITEBIN);

    final String a = "xs:base64Binary('MA==')";
    error(fun + "('" + Prop.TMP + "', " + a + ")", Err.PATHISDIR);
    query(fun + "('" + PATH1 + "', " + a + ")");
    query("file:size('" + PATH1 + "')", "1");
    query(fun + "('" + PATH1 + "', " + a + ")");
    query("file:size('" + PATH1 + "')", "1");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:append-binary() function.
   */
  @Test
  public void fileAppendBinary() {
    final String fun = check(Function.APPENDBIN);

    final String a = "xs:base64Binary('MA==')";
    error(fun + "('" + Prop.TMP + "', " + a + ")", Err.PATHISDIR);
    query(fun + "('" + PATH1 + "', " + a + ")");
    query("file:size('" + PATH1 + "')", "1");
    query(fun + "('" + PATH1 + "', " + a + ")");
    query("file:read-text('" + PATH1 + "')", "00");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:copy() function.
   */
  @Test
  public void fileCopy() {
    final String fun = check(Function.COPY);

    query("file:write('" + PATH1 + "', 'a')");
    query(fun + "('" + PATH1 + "', '" + PATH2 + "')");
    query(fun + "('" + PATH1 + "', '" + PATH2 + "')");
    query(fun + "('" + PATH2 + "', '" + PATH2 + "')");
    query("file:size('" + PATH1 + "')", "1");
    query("file:size('" + PATH2 + "')", "1");
    error(fun + "('" + PATH1 + "', '" + PATH3 + "')", Err.NOTDIR);

    query("file:delete('" + PATH1 + "')");
    query("file:delete('" + PATH2 + "')");
  }

  /**
   * Test method for the file:move() function.
   */
  @Test
  public void fileMove() {
    final String fun = check(Function.MOVE);

    error(fun + "('" + PATH1 + "', '" + PATH2 + "')", Err.PATHNOTEXISTS);
    query("file:write('" + PATH1 + "', 'a')");
    query(fun + "('" + PATH1 + "', '" + PATH2 + "')");
    query(fun + "('" + PATH2 + "', '" + PATH1 + "')");
    query(fun + "('" + PATH1 + "', '" + PATH1 + "')");
    error(fun + "('" + PATH1 + "', '" + PATH4 + "')", Err.NOTDIR);
    query("file:size('" + PATH1 + "')", "1");
    query("file:exists('" + PATH2 + "')", "false");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:resolve-path() function.
   */
  @Test
  public void fileResolvePath() {
    final String fun = check(Function.RESOLVEPATH);
    final String path = query(fun + "('" + PATH1 + "')");
    final String can = new File(PATH1).getAbsolutePath();
    assertEquals(path.toLowerCase(), can.toLowerCase());
  }

  /**
   * Test method for the file:path-to-uri() function.
   */
  @Test
  public void filePathToURI() {
    final String fun = check(Function.PATHTOURI);
    final String path = query(fun + "('" + PATH1 + "')");
    final String uri = new File(PATH1).toURI().toString();
    assertEquals(path.toLowerCase(), uri.toLowerCase());
  }

  /**
   * Tests method for file:base-name() function.
   */
  @Test
  public void fileBaseName() {
    final String fun = check(Function.BASENAME);

    // check with a simple path
    final String name1 = query(fun + "('" + PATH1 + "')");
    assertEquals(name1, NAME);
    // check with a path ending with a directory separator
    final String name2 = query(fun + "('" + PATH1 + File.separator + "')");
    assertEquals(name2, NAME);
    // check with a path consisting only of directory separators
    final String name3 = query(fun + "('//')");
    assertEquals(name3, "");
    // check with empty string path
    final String name4 = query(fun + "('" + "" + "')");
    assertEquals(name4, ".");
    // check using a suffix
    final String name5 = query(fun + "('" + PATH1 + File.separator + "test.xml"
        + "', '.xml')");
    assertEquals(name5, "test");
  }

  /**
   * Tests method for file:dir-name() function.
   */
  @Test
  public void fileDirName() {
    final String fun = check(Function.DIRNAME);
    // check with a simple path
    assertEquals(norm(query(fun + "('" + PATH1 + "')")).toLowerCase(),
        norm(Prop.TMP));
    // check with an empty path
    assertEquals(query(fun + "('')"), ".");
    // check with a path without directory separators
    assertEquals(query(fun + "('" + NAME + "')"), ".");
  }

  /**
   * Tests method for file:path-to-native() function.
   * @throws IOException I/O exception
   */
  @Test
  public void filePathToNative() throws IOException {
    final String fun = check(Function.PATHNATIVE);
    final String path1 = query(fun + "('" + PATH1 + "')");
    final String exp1 = new File(PATH1).getCanonicalPath();
    assertEquals(norm(path1), norm(exp1));
    final String path2 = query(fun + "('" + Prop.TMP + ".." + "/test.xml"
        + "')");
    final String exp2 =
      new File(Prop.TMP + ".." + "/test.xml").getCanonicalPath();
    assertEquals(path2, exp2);
  }

  /**
   * Normalize slashes of specified path to reduce OS dependent bugs.
   * @param path input path
   * @return normalized path
   */
  private static String norm(final String path) {
    return (path + '/').replaceAll("[\\\\/]+", "/").toLowerCase();
  }
}

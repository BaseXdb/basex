package org.basex.test.query.advanced;

import static org.junit.Assert.*;

import java.io.File;
import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.query.func.FunDef;
import org.basex.query.util.Err;
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
  /** Directory separator. */
  private static final String DIRSEP = System.getProperty("file.separator");
  /** Path separator. */
  private static final String PATHSEP = System.getProperty("path.separator");
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
   * @throws QueryException database exception
   */
  @Test
  public void testExists() throws QueryException {
    final String fun = check(FunDef.FEXISTS);
    query("file:write('" + PATH1 + "', ())");
    query(fun + "('" + PATH1 + "')", "true");
    query("file:delete('" + PATH1 + "')");
    query(fun + "('" + PATH1 + "')", "false");
  }

  /**
   * Test method for the file:is-directory() function.
   * @throws QueryException database exception
   */
  @Test
  public void testIsDirectory() throws QueryException {
    final String fun = check(FunDef.ISDIR);
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
   * @throws QueryException database exception
   */
  @Test
  public void testIsFile() throws QueryException {
    final String fun = check(FunDef.ISFILE);
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
   * @throws QueryException database exception
   */
  @Test
  public void testLastModified() throws QueryException {
    final String fun = check(FunDef.LASTMOD);
    assertTrue(!query(fun + "('" + Prop.TMP + "')").isEmpty());
  }

  /**
   * Test method for the file:size() function.
   * @throws QueryException database exception
   */
  @Test
  public void testSize() throws QueryException {
    final String fun = check(FunDef.SIZE);
    query("file:write('" + PATH1 + "', 'abcd')");
    query(fun + "('" + PATH1 + "')", "4");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:list() function.
   * @throws QueryException database exception
   */
  @Test
  public void testList() throws QueryException {
    final String fun = check(FunDef.FLIST);
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
   * @throws QueryException database exception
   */
  @Test
  public void testCreateDirectory() throws QueryException {
    final String fun = check(FunDef.CREATEDIR);
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
   * @throws QueryException database exception
   */
  @Test
  public void testDelete() throws QueryException {
    final String fun = check(FunDef.DELETE);
    query("file:create-directory('" + PATH3 + "')");
    query(fun + "('" + PATH3 + "')");
    query("file:create-directory('" + PATH3 + "')");
    query("file:write('" + PATH4 + "', ())");
    query(fun + "('" + PATH1 + "')");
    error(fun + "('" + PATH1 + "')", Err.PATHNOTEXISTS);
  }

  /**
   * Test method for the file:read-text() function.
   * @throws QueryException database exception
   */
  @Test
  public void testRead() throws QueryException {
    final String fun = check(FunDef.READTEXT);
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
   * @throws QueryException database exception
   */
  @Test
  public void testReadBinary() throws QueryException {
    final String fun = check(FunDef.READBIN);
    error(fun + "('" + PATH1 + "')", Err.PATHNOTEXISTS);
    error(fun + "('" + Prop.TMP + "')", Err.PATHISDIR);
    query("file:write('" + PATH1 + "', '0')");
    query(fun + "('" + PATH1 + "')", "MA==");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:write() function.
   * @throws QueryException database exception
   */
  @Test
  public void testWrite() throws QueryException {
    final String fun = check(FunDef.WRITE);

    error(fun + "('" + Prop.TMP + "', ())", Err.PATHISDIR);

    query(fun + "('" + PATH1 + "', '0')");
    query("file:size('" + PATH1 + "')", "1");
    query(fun + "('" + PATH1 + "', '0')");
    query("file:size('" + PATH1 + "')", "1");
    query("file:delete('" + PATH1 + "')");

    query(fun + "('" + PATH1 + "', 'a\u00e4'," +
      serialParams("<encoding>CP1252</encoding>") + ")");
    query("file:read-text('" + PATH1 + "', 'CP1252')", "a\u00e4");

    query(fun + "('" + PATH1 + "', '<a/>'," +
        serialParams("<method>text</method>") + ")");
    query("file:read-text('" + PATH1 + "')", "&amp;lt;a/&amp;gt;");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:append() function.
   * @throws QueryException database exception
   */
  @Test
  public void testAppend() throws QueryException {
    final String fun = check(FunDef.APPEND);

    error(fun + "('" + Prop.TMP + "', ())", Err.PATHISDIR);

    query(fun + "('" + PATH1 + "', '0')");
    query("file:size('" + PATH1 + "')", "1");
    query(fun + "('" + PATH1 + "', '0', ())");
    query("file:size('" + PATH1 + "')", "2");
    query("file:delete('" + PATH1 + "')");

    query(fun + "('" + PATH1 + "', 'a\u00e4'," +
      serialParams("<encoding>CP1252</encoding>") + ")");
    query("file:read-text('" + PATH1 + "', 'CP1252')", "a\u00e4");
    query("file:delete('" + PATH1 + "')");

    query(fun + "('" + PATH1 + "', '<a/>'," +
        serialParams("<method>text</method>") + ")");
    query("file:read-text('" + PATH1 + "')", "&amp;lt;a/&amp;gt;");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:write-binary() function.
   * @throws QueryException database exception
   */
  @Test
  public void testWriteBinary() throws QueryException {
    final String fun = check(FunDef.WRITEBIN);

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
   * @throws QueryException database exception
   */
  @Test
  public void testAppendBinary() throws QueryException {
    final String fun = check(FunDef.APPENDBIN);

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
   * @throws QueryException database exception
   */
  @Test
  public void testCopy() throws QueryException {
    final String fun = check(FunDef.COPY);

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
   * @throws QueryException database exception
   */
  @Test
  public void testMove() throws QueryException {
    final String fun = check(FunDef.MOVE);

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
   * @throws Exception exception
   */
  @Test
  public void testResolvePath() throws Exception {
    final String fun = check(FunDef.RESOLVEPATH);
    final String path = query(fun + "('" + PATH1 + "')");
    final String can = new File(PATH1).getAbsolutePath();
    assertEquals(path.toLowerCase(), can.toLowerCase());
  }

  /**
   * Test method for the file:path-to-full-path() function.
   * @throws Exception exception
   */
  @Test
  public void testPathToURI() throws Exception {
    final String fun = check(FunDef.PATHTOURI);
    final String path = query(fun + "('" + PATH1 + "')");
    final String uri = new File(PATH1).toURI().toString();
    assertEquals(path.toLowerCase(), uri.toLowerCase());
  }

  /**
   * Tests method for file:base-name() function.
   * @throws Exception exception
   */
  @Test
  public void testBaseName() throws Exception {
    final String fun = check(FunDef.BASENAME);

    // Check with a simple path
    final String name1 = query(fun + "('" + PATH1 + "')");
    assertEquals(name1, NAME);
    // Check with a path ending with a directory separator
    final String name2 = query(fun + "('" + PATH1 + DIRSEP + "')");
    assertEquals(name2, NAME);
    // Check with a path consisting only of directory separators
    final String name3 = query(fun + "('" + DIRSEP + DIRSEP + "')");
    assertEquals(name3, "");
    // Check with empty string path
    final String name4 = query(fun + "('" + "" + "')");
    assertEquals(name4, ".");
    // Check using a suffix
    final String name5 = query(fun + "('" + PATH1 + DIRSEP + "test.xml"
        + "', '.xml')");
    assertEquals(name5, "test");
  }

  /**
   * Tests method for file:dir-name() function.
   * @throws Exception exception
   */
  @Test
  public void testDirName() throws Exception {
    final String fun = check(FunDef.DIRNAME);
    // Check with a simple path
    final String dir1 = query(fun + "('" + PATH1 + "')");
    final String exp = Prop.TMP.endsWith(DIRSEP) ? Prop.TMP.substring(0,
        Prop.TMP.length() - 1) : Prop.TMP;
    assertEquals(dir1.toLowerCase(), exp);
    // Check with an empty path
    final String dir2 = query(fun + "('" + "" + "')");
    assertEquals(dir2, ".");
    // Check with a path without directory separators
    final String dir3 = query(fun + "('" + NAME + "')");
    assertEquals(dir3, ".");
  }

  /**
   * Tests method for file:path-to-native() function.
   * @throws Exception exception
   */
  @Test
  public void testPathToNative() throws Exception {
    final String fun = check(FunDef.PATHNATIVE);
    final String path1 = query(fun + "('" + PATH1 + "')");
    assertEquals(path1, PATH1);

    final String path2 = query(fun + "('" + PATH1 + DIRSEP + ".." + DIRSEP
        + "test.xml" + "')");
    assertEquals(path2, Prop.TMP + "test.xml");
  }

  /**
   * Tests method for file:directory-separator() function.
   * @throws Exception exception
   */
  @Test
  public void testDirSep() throws Exception {
    final String fun = check(FunDef.DIRSEP);
    final String sep = query(fun + "()");
    assertEquals(sep, DIRSEP);
  }

  /**
   * Tests method for file:path-separator() function.
   * @throws Exception exception
   */
  @Test
  public void testPathSep() throws Exception {
    final String fun = check(FunDef.PATHSEP);
    final String sep = query(fun + "()");
    assertEquals(sep, PATHSEP);
  }
}

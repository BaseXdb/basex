package org.basex.test.query;

import static org.junit.Assert.*;
import java.io.File;
import org.basex.core.BaseXException;
import org.basex.core.Prop;
import org.basex.query.func.FunDef;
import org.basex.query.util.Err;
import org.basex.util.Util;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the functions of the file library.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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

  /** Constructor. */
  public FNFileTest() {
    super("file");
  }

  /** Initializes the test. */
  @BeforeClass
  public static void init() {
    new File(PATH4).delete();
    new File(PATH3).delete();
    new File(PATH2).delete();
    new File(PATH1).delete();
  }

  /**
   * Test method for the file:exists() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testExists() throws BaseXException {
    final String fun = check(FunDef.FEXISTS, String.class);
    query("file:write('" + PATH1 + "', ())");
    query(fun + "('" + PATH1 + "')", "true");
    query("file:delete('" + PATH1 + "')");
    query(fun + "('" + PATH1 + "')", "false");
  }

  /**
   * Test method for the file:is-directory() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testIsDirectory() throws BaseXException {
    final String fun = check(FunDef.ISDIR, String.class);
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
   * Test method for the file:is-file() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testIsFile() throws BaseXException {
    final String fun = check(FunDef.ISFILE, String.class);
    query(fun + "('" + Prop.TMP + "')", "false");
    query("file:write('" + PATH1 + "', ())");
    query(fun + "('" + PATH1 + "')", "true");
    query("file:delete('" + PATH1 + "')");
    query("file:create-directory('" + PATH1 + "')");
    query(fun + "('" + PATH1 + "')", "false");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:is-readable() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testIsReadable() throws BaseXException {
    final String fun = check(FunDef.ISREAD, String.class);
    query("file:write('" + PATH1 + "', ())");
    query(fun + "('" + PATH1 + "')", "true");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:is-writable() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testIsWritable() throws BaseXException {
    final String fun = check(FunDef.ISWRITE, String.class);
    query("file:write('" + PATH1 + "', ())");
    query(fun + "('" + PATH1 + "')", "true");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:last-modified() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testLastModified() throws BaseXException {
    final String fun = check(FunDef.LASTMOD, String.class);
    assertTrue(query(fun + "('" + Prop.TMP + "')").length() != 0);
  }

  /**
   * Test method for the file:size() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testSize() throws BaseXException {
    final String fun = check(FunDef.SIZE, String.class);
    query("file:write('" + PATH1 + "', 'abcd')");
    query(fun + "('" + PATH1 + "')", "4");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:list() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testList() throws BaseXException {
    final String fun =
      check(FunDef.FLIST, String.class, Boolean.class, String.class);
    error(fun + "('" + PATH1 + "')", Err.PATHNOTEXISTS);
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
   * Test method for the file:create-directory() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testCreateDirectory() throws BaseXException {
    final String fun = check(FunDef.CREATEDIR, String.class);
    query(fun + "('" + PATH1 + "')");
    query(fun + "('" + PATH1 + "')");
    query(fun + "('" + PATH3 + "')");
    query("file:delete('" + PATH1 + "', true())");
    query("file:write('" + PATH1 + "', ())");
    error(fun + "('" + PATH1 + "')", Err.FILEEXISTS);
    error(fun + "('" + PATH3 + "')", Err.FILEEXISTS);
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:delete() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testDelete() throws BaseXException {
    final String fun = check(FunDef.DELETE, String.class, Boolean.class);
    query("file:create-directory('" + PATH3 + "')");
    error(fun + "('" + PATH1 + "')", Err.DIRNOTEMPTY);
    query(fun + "('" + PATH3 + "')");
    query("file:create-directory('" + PATH3 + "')");
    query("file:write('" + PATH4 + "', ())");
    query(fun + "('" + PATH1 + "', true())");
  }

  /**
   * Test method for the file:read() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testRead() throws BaseXException {
    final String fun = check(FunDef.READ, String.class, String.class);
    error(fun + "('" + PATH1 + "')", Err.PATHNOTEXISTS);
    error(fun + "('" + Prop.TMP + "')", Err.PATHISDIR);
    query("file:write('" + PATH1 + "', 'a\u00e4')");
    query(fun + "('" + PATH1 + "')", "a\u00e4");
    error(fun + "('" + PATH1 + "', 'UNKNOWN')", Err.ENCNOTEXISTS);
    assertTrue(query(fun + "('" + PATH1 + "', 'CP1252')").length() == 3);
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:read-binary() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testReadBinary() throws BaseXException {
    final String fun = check(FunDef.READBIN, String.class);
    error(fun + "('" + PATH1 + "')", Err.PATHNOTEXISTS);
    error(fun + "('" + Prop.TMP + "')", Err.PATHISDIR);
    query("file:write('" + PATH1 + "', '0')");
    query(fun + "('" + PATH1 + "')", "MA==");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:write() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testWrite() throws BaseXException {
    final String fun = check(FunDef.WRITE, String.class,
        (Class<?>) null, String.class, Boolean.class);

    error(fun + "('" + Prop.TMP + "', ())", Err.PATHISDIR);

    query(fun + "('" + PATH1 + "', '0')");
    query("file:size('" + PATH1 + "')", "1");
    query(fun + "('" + PATH1 + "', '0')");
    query("file:size('" + PATH1 + "')", "1");
    query(fun + "('" + PATH1 + "', '0', (), true())");
    query("file:size('" + PATH1 + "')", "2");
    query("file:delete('" + PATH1 + "')");
    query(fun + "('" + PATH1 + "', '0', (), true())");
    query("file:size('" + PATH1 + "')", "1");
    query("file:delete('" + PATH1 + "')");

    query(fun + "('" + PATH1 + "', 'a\u00e4'," +
      "<encoding>CP1252</encoding>)");
    query("file:read('" + PATH1 + "', 'CP1252')", "a\u00e4");

    query(fun + "('" + PATH1 + "', '<a/>'," + "<method>text</method>)");
    query("file:read('" + PATH1 + "')", "&amp;lt;a/&amp;gt;");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:write-binary() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testWriteBinary() throws BaseXException {
    final String fun =
      check(FunDef.WRITEBIN, String.class, (Class<?>) null, Boolean.class);

    final String a = "xs:base64Binary('MA==')";
    error(fun + "('" + Prop.TMP + "', " + a + ")", Err.PATHISDIR);
    query(fun + "('" + PATH1 + "', " + a + ")");
    query("file:size('" + PATH1 + "')", "1");
    query(fun + "('" + PATH1 + "', " + a + ")");
    query("file:size('" + PATH1 + "')", "1");
    query(fun + "('" + PATH1 + "', " + a + ", true())");
    query("file:read('" + PATH1 + "')", "00");
    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:copy() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testCopy() throws BaseXException {
    final String fun = check(FunDef.COPY, String.class, String.class);

    query("file:write('" + PATH1 + "', 'a')");
    query(fun + "('" + PATH1 + "', '" + PATH2 + "')");
    query(fun + "('" + PATH1 + "', '" + PATH2 + "')");
    query(fun + "('" + PATH2 + "', '" + PATH2 + "')");
    query("file:size('" + PATH1 + "')", "1");
    query("file:size('" + PATH2 + "')", "1");
    error(fun + "('" + PATH1 + "', '" + PATH3 + "')", Err.PATHINVALID);

    query("file:delete('" + PATH1 + "')");
    query("file:delete('" + PATH2 + "')");
  }

  /**
   * Test method for the file:move() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testMove() throws BaseXException {
    final String fun = check(FunDef.MOVE, String.class, String.class);

    error(fun + "('" + PATH1 + "', '" + PATH2 + "')", Err.PATHNOTEXISTS);
    query("file:write('" + PATH1 + "', 'a')");
    query(fun + "('" + PATH1 + "', '" + PATH2 + "')");
    query(fun + "('" + PATH2 + "', '" + PATH1 + "')");
    query(fun + "('" + PATH1 + "', '" + PATH1 + "')");
    error(fun + "('" + PATH1 + "', '" + PATH4 + "')", Err.PATHINVALID);
    query("file:size('" + PATH1 + "')", "1");
    query("file:exists('" + PATH2 + "')", "false");

    query("file:delete('" + PATH1 + "')");
  }

  /**
   * Test method for the file:path-separator() functions.
   * @throws BaseXException database exception
   */
  @Test
  public void testPathSeparator() throws BaseXException {
    final String fun = check(FunDef.PATHSEP);
    final String sep = query(fun + "()");
    assertTrue(sep.equals("/") || sep.equals("\\"));
  }

  /**
   * Test method for the file:path-to-full-path() functions.
   * @throws Exception exception
   */
  @Test
  public void testPathToFullPath() throws Exception {
    final String fun = check(FunDef.PATHTOFULL, String.class);
    final String path = query(fun + "('" + PATH1 + "')");
    final String can = new File(PATH1).getAbsolutePath();
    assertEquals(path.toLowerCase(), can.toLowerCase());
  }

  /**
   * Test method for the file:path-to-full-path() functions.
   * @throws Exception exception
   */
  @Test
  public void testPathToURI() throws Exception {
    final String fun = check(FunDef.PATHTOURI, String.class);
    final String path = query(fun + "('" + PATH1 + "')");
    final String uri = new File(PATH1).toURI().toString();
    assertEquals(path.toLowerCase(), uri.toLowerCase());
  }
}

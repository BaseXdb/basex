package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

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
    check(FLEXISTS);
    query(FLWRITE.args(PATH1, "()"));
    query(FLEXISTS.args(PATH1), true);
    query(FLDELETE.args(PATH1));
    query(FLEXISTS.args(PATH1), false);
  }

  /**
   * Test method for the file:is-directory() function.
   */
  @Test
  public void fileIsDirectory() {
    check(FLISDIR);
    query(FLISDIR.args(Prop.TMP), true);
    query(FLWRITE.args(PATH1, "()"));
    query(FLISDIR.args(PATH1), false);
    query(FLDELETE.args(PATH1));
    query(FLCREATEDIR.args(PATH1));
    query(FLISDIR.args(PATH1), true);
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:is-file() function.
   */
  @Test
  public void fileIsFile() {
    check(FLISFILE);
    query(FLISFILE.args(Prop.TMP), false);
    query(FLWRITE.args(PATH1, "()"));
    query(FLISFILE.args(PATH1), true);
    query(FLDELETE.args(PATH1));
    query(FLCREATEDIR.args(PATH1));
    query(FLISFILE.args(PATH1), false);
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:last-modified() function.
   */
  @Test
  public void fileLastModified() {
    check(FLLASTMOD);
    assertTrue(!query(FLLASTMOD.args(Prop.TMP)).isEmpty());
  }

  /**
   * Test method for the file:size() function.
   */
  @Test
  public void fileSize() {
    check(FLSIZE);
    query(FLWRITE.args(PATH1, "abcd"));
    query(FLSIZE.args(PATH1), "4");
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:list() function.
   */
  @Test
  public void fileList() {
    check(FLLIST);
    error(FLLIST.args(PATH1), Err.NOTDIR);
    query(FLWRITE.args(PATH1, "()"));
    error(FLLIST.args(PATH1), Err.NOTDIR);
    contains(FLLIST.args(Prop.TMP), NAME);
    contains(FLLIST.args(Prop.TMP, "false()"), NAME);
    contains(FLLIST.args(Prop.TMP, "false()", "FN"), NAME);
    contains(FLLIST.args(Prop.TMP, "false()", NAME), NAME);
    query(FLLIST.args(Prop.TMP, "false()", "XXX"), "");
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:create-directory() function.
   */
  @Test
  public void fileCreateDirectory() {
    check(FLCREATEDIR);
    query(FLCREATEDIR.args(PATH1));
    query(FLCREATEDIR.args(PATH1));
    query(FLCREATEDIR.args(PATH3));
    query(FLDELETE.args(PATH1));
    query(FLWRITE.args(PATH1, "()"));
    error(FLCREATEDIR.args(PATH1), Err.FILEEXISTS);
    error(FLCREATEDIR.args(PATH3), Err.FILEEXISTS);
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:delete() function.
   */
  @Test
  public void fileDelete() {
    check(FLDELETE);
    query(FLCREATEDIR.args(PATH3));
    query(FLDELETE.args(PATH3));
    query(FLCREATEDIR.args(PATH3));
    query(FLWRITE.args(PATH4, "()"));
    query(FLDELETE.args(PATH1));
    error(FLDELETE.args(PATH1), Err.PATHNOTEXISTS);
  }

  /**
   * Test method for the file:read-text() function.
   */
  @Test
  public void fileReadText() {
    check(FLREADTEXT);
    error(FLREADTEXT.args(PATH1), Err.PATHNOTEXISTS);
    error(FLREADTEXT.args(Prop.TMP), Err.PATHISDIR);
    query(FLWRITE.args(PATH1, "a\u00e4"));
    query(FLREADTEXT.args(PATH1), "a\u00e4");
    error(FLREADTEXT.args(PATH1, "UNKNOWN"), Err.ENCNOTEXISTS);
    assertTrue(query(FLREADTEXT.args(PATH1, "CP1252")).length() == 3);
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:read-binary() function.
   */
  @Test
  public void fileReadBinary() {
    check(FLREADBIN);
    error(FLREADBIN.args(PATH1), Err.PATHNOTEXISTS);
    error(FLREADBIN.args(Prop.TMP), Err.PATHISDIR);
    query(FLWRITE.args(PATH1, "0"));
    query(FLREADBIN.args(PATH1), "MA==");
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:write() function.
   */
  @Test
  public void fileWrite() {
    check(FLWRITE);

    error(FLWRITE.args(Prop.TMP, "()"), Err.PATHISDIR);

    query(FLWRITE.args(PATH1, "0"));
    query(FLSIZE.args(PATH1), "1");
    query(FLWRITE.args(PATH1, "0"));
    query(FLSIZE.args(PATH1), "1");
    query(FLDELETE.args(PATH1));

    query(FLWRITE.args(PATH1, "a\u00e4",
        serialParams("<encoding value='CP1252'/>")));
    query(FLREADTEXT.args(PATH1, "CP1252"), "a\u00e4");

    query(FLWRITE.args(PATH1, "\"<a/>\"",
        serialParams("<method value='text'/>")));
    query(FLREADTEXT.args(PATH1), "&amp;lt;a/&amp;gt;");
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:append() function.
   */
  @Test
  public void fileAppend() {
    check(FLAPPEND);

    error(FLAPPEND.args(Prop.TMP, "()"), Err.PATHISDIR);

    query(FLAPPEND.args(PATH1, "0"));
    query(FLSIZE.args(PATH1), "1");
    query(FLAPPEND.args(PATH1, "0", "()"));
    query(FLSIZE.args(PATH1), "2");
    query(FLDELETE.args(PATH1));

    query(FLAPPEND.args(PATH1, "a\u00e4",
        serialParams("<encoding value='CP1252'/>")));
    query(FLREADTEXT.args(PATH1, "CP1252"), "a\u00e4");
    query(FLDELETE.args(PATH1));

    query(FLAPPEND.args(PATH1, "\"<a/>\"",
        serialParams("<method value='text'/>")));
    query(FLREADTEXT.args(PATH1), "&amp;lt;a/&amp;gt;");
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:write-binary() function.
   */
  @Test
  public void fileWriteBinary() {
    check(FLWRITEBIN);

    final String bin = "xs:base64Binary('MA==')";
    error(FLWRITEBIN.args(Prop.TMP, bin), Err.PATHISDIR);
    query(FLWRITEBIN.args(PATH1, bin));
    query(FLSIZE.args(PATH1), "1");
    query(FLWRITEBIN.args(PATH1, bin));
    query(FLSIZE.args(PATH1), "1");
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:append-binary() function.
   */
  @Test
  public void fileAppendBinary() {
    check(FLAPPENDBIN);

    final String bin = "xs:base64Binary('MA==')";
    error(FLAPPENDBIN.args(Prop.TMP, bin), Err.PATHISDIR);
    query(FLAPPENDBIN.args(PATH1, bin));
    query(FLSIZE.args(PATH1), "1");
    query(FLAPPENDBIN.args(PATH1, bin));
    query(FLREADTEXT.args(PATH1), "00");
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:copy() function.
   */
  @Test
  public void fileCopy() {
    check(FLCOPY);

    query(FLWRITE.args(PATH1, "A"));
    query(FLCOPY.args(PATH1, PATH2));
    query(FLCOPY.args(PATH1, PATH2));
    query(FLCOPY.args(PATH2, PATH2));
    query(FLSIZE.args(PATH1), "1");
    query(FLSIZE.args(PATH2), "1");
    error(FLCOPY.args(PATH1, PATH3), Err.NOTDIR);

    query(FLDELETE.args(PATH1));
    query(FLDELETE.args(PATH2));
  }

  /**
   * Test method for the file:move() function.
   */
  @Test
  public void fileMove() {
    check(FLMOVE);

    error(FLMOVE.args(PATH1, PATH2), Err.PATHNOTEXISTS);
    query(FLWRITE.args(PATH1, "a"));
    query(FLMOVE.args(PATH1, PATH2));
    query(FLMOVE.args(PATH2, PATH1));
    query(FLMOVE.args(PATH1, PATH1));
    error(FLMOVE.args(PATH1, PATH4), Err.NOTDIR);
    query(FLSIZE.args(PATH1), "1");
    query(FLEXISTS.args(PATH2), false);
    query(FLDELETE.args(PATH1));
  }

  /**
   * Test method for the file:resolve-path() function.
   */
  @Test
  public void fileResolvePath() {
    check(FLRESOLVEPATH);
    final String path = query(FLRESOLVEPATH.args(PATH1));
    final String can = new File(PATH1).getAbsolutePath();
    assertEquals(path.toLowerCase(), can.toLowerCase());
  }

  /**
   * Test method for the file:path-to-uri() function.
   */
  @Test
  public void filePathToURI() {
    check(FLPATHTOURI);
    final String path = query(FLPATHTOURI.args(PATH1));
    final String uri = new File(PATH1).toURI().toString();
    assertEquals(path.toLowerCase(), uri.toLowerCase());
  }

  /**
   * Tests method for file:base-name() function.
   */
  @Test
  public void fileBaseName() {
    check(FLBASENAME);

    // check with a simple path
    query(FLBASENAME.args(PATH1), NAME);
    // check with a path ending with a directory separator
    query(FLBASENAME.args(PATH1 + File.separator), NAME);
    // check with a path consisting only of directory separators
    query(FLBASENAME.args("//"), "");
    // check with empty string path
    query(FLBASENAME.args(""), ".");
    // check using a suffix
    query(FLBASENAME.args(PATH1 + File.separator + "test.xml", ".xml"), "test");
  }

  /**
   * Tests method for file:dir-name() function.
   */
  @Test
  public void fileDirName() {
    check(FLDIRNAME);
    // check with a simple path
    assertEquals(norm(Prop.TMP),
        norm(query(FLDIRNAME.args(PATH1))).toLowerCase());
    // check with an empty path
    query(FLDIRNAME.args(""), ".");
    // check with a path without directory separators
    query(FLDIRNAME.args(NAME), ".");
  }

  /**
   * Tests method for file:path-to-native() function.
   * @throws IOException I/O exception
   */
  @Test
  public void filePathToNative() throws IOException {
    check(FLPATHNATIVE);
    assertEquals(norm(new File(PATH1).getCanonicalPath()),
        norm(query(FLPATHNATIVE.args(PATH1))));
    query(FLPATHNATIVE.args(Prop.TMP + ".." + "/test.xml"),
        new File(Prop.TMP + ".." + "/test.xml").getCanonicalPath());
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

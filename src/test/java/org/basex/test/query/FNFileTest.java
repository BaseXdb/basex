package org.basex.test.query;

import java.io.File;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.item.B64;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.Uri;
import org.basex.query.iter.ItemIter;
import org.basex.util.Token;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * This class tests the functions of the file library.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Rositsa Shadura
 */
public final class FNFileTest extends QueryTest {
  /** Test directory. */
  private static final String TESTDIR1 = "testDir1";
  /** Test directory. */
  private static final String TESTDIR2 = "testDir2";
  /** Test file for file:copy. */
  private static final String TESTCOPY = "testCopy";
  /** Test file for file:delete. */
  private static final String TESTDEL1 = "testDel1";
  /** Test file for file:delete with $recursive = true. */
  private static final String TESTDEL2 = "testDel2";
  /** Test file for file:delete with $recursive = true. */
  private static final String TESTDEL = "testDel";
  /** Test file for file:move. */
  private static final String TESTMOVE = "testMove";
  /** Test file for file:write. */
  private static final String TESTWRITE = "testWrite";
  /** Test file for file:write-binary. */
  private static final String TESTWRITEBIN = "testWriteBin";
  /** Test directory 1 for file:mkdir. */
  private static final String DIR1 = "test1";
  /** Test directory 2 for file:mkdir with $recursive = true. */
  private static final String DIR2 = "test2";
  /** Test directory 3 for file:mkdir with $recursive = true. */
  private static final String DIR3 = "test3";

  /** /_tmpdir_/testDir1. */
  private static File dir1 = new File(Prop.TMP, TESTDIR1);
  /** /_tmpdir_/testDir2. */
  private static File dir2 = new File(Prop.TMP, TESTDIR2);
  /** /_tmpdir_/testDir1/fileCopy. */
  private static File fileCopy = new File(dir1, TESTCOPY);
  /** /_tmpdir_/testDir1/fileMove. */
  private static File fileMove = new File(dir1, TESTMOVE);
  /** /_tmpdir_/testDir1/testDelDir1/testDelDir2. */
  private static File dirDel = new File(dir1 + Prop.SEP + TESTDEL1, TESTDEL2);
  /** /_tmpdir_/testDir1/testDelFile. */
  private static File fileDel = new File(dir1, TESTDEL);

  static {
    doc = "<?xml version='1.0' encoding='UTF-8'?>\n" +
          "<ACDC>\n" +
            "<Name>\n" +
              "<Vorname>Brian</Vorname>\n" +
              "<Nachname>Johnson</Nachname>\n" +
            "</Name>\n" +
            "<Name>\n" +
              "<Vorname>Angus</Vorname>\n" +
              "<Nachname>Young</Nachname>\n" +
            "</Name>\n" +
          "</ACDC>";

    ItemIter ii = new ItemIter();
    try {
      ii = new ItemIter(new Item[] { new Dtm(
        new File("etc").lastModified(), null)}, 1);
    } catch(final QueryException e) {
      ii = null;
    }

    queries = new Object[][] {
        // /_tmpdir_/testDir1/test1
        { "Test file:mkdir()", empty(),
            "file:mkdir(\"" + dir1 + Prop.SEP + DIR1 + "\")" },

        // /_tmpdir_/testDir1/test2/test3
        { "Test file:mkdir() with $recursive = fn:true()", empty(),
            "file:mkdir(\"" + dir1 + Prop.SEP + DIR2 + Prop.SEP
                + DIR3 + "\", fn:true())"},

        // /_tmpdir_/testDir1
        { "Test file:is-directory()", bool(true),
            "file:is-directory(\"" + dir1 + "\")" },

        // /_tmpdir_/testDir1/fileCopy
        { "Test file:is-file()", bool(true),
            "file:is-file(\"" + fileCopy + "\")" },

         // /_tmpdir_/testDir1/fileCopy
        { "Test file:is-readable()", bool(true),
            "file:is-readable(\"" + fileCopy + "\")" },

         // /_tmpdir_/testDir1/fileCopy
        { "Test file:is-writeable()", bool(true),
            "file:is-writeable(\"" + fileCopy + "\")" },

        { "Test file:path-separator()", str(Prop.SEP), "file:path-separator()"},

         // /_tmpdir_/testDir1/fileCopy
        { "Test file:read()", str("\"" + doc + "\""),
            "file:read(\"" + fileCopy + "\")" },

        // /_tmpdir_/testDir1/fileCopy
        { "Test file:read-binary()",
            new ItemIter(new Item[] { new B64(Token.token(doc))}, 1),
            "file:read-binary(\"" + fileCopy + "\")" },

         // /_tmpdir_/testDir1/fileWrite
        { "Test file:write()", empty(),
            "file:write(\"" + dir1 + Prop.SEP + TESTWRITE + "\"," +
            "//Name/Vorname, "  + "(<indent>yes</indent>))" },

         // /_tmpdir_/testDir1/fileCopy
        { "Test file:write() with $append = true", empty(),
            "file:write(\"" + fileCopy
            + "\", //Name/Vorname, (<indent>yes</indent>), fn:true())" },

        // /_tmpdir_/testDir1/fileWriteBin
        { "Test file:write-binary()", empty(),
            "file:write-binary(\"" + dir1 + Prop.SEP + TESTWRITEBIN +
            "\", \"aGF0\" cast as xs:base64Binary)" },

        // src:  /_tmpdir_/testDir1/fileCopy
        // dest: /_tmpdir_/testDir2/fileCopy
        { "Test file:copy() with $overwrite = true", empty(),
            "file:copy(\"" + fileCopy + "\", \"" +
            dir2 + Prop.SEP + TESTCOPY + "\", fn:true())" },

        // src:  /_tmpdir_/testDir1
        // dest: /_tmpdir_/testDir2
        { "Test file:move()", empty(),
            "file:move(\"" + fileMove + "\", \"" + dir2 + "\")" },

        // /_tmpdir_/testDir1/fileDel
        { "Test file:delete()", empty(),
            "file:delete(\"" + fileDel + "\")" },

        // /_tmpdir_/testDir1/testDelDir1/testDelDir2
        { "Test file:delete() with $recursive = true", empty(),
                "file:delete(\"" + dirDel.getParentFile() + "\", fn:true())" },

        // /_tmpdir_/testDir1/fileCopy
        { "Test file:path-to-full-path()", str(fileCopy.toString()),
            "file:path-to-full-path(\"" + fileCopy + "\")"},

         // file:/_tmpdir_/testDir1/fileCopy
        { "Test file:path-to-uri()", new ItemIter(new Item[]
             { Uri.uri(Token.token(fileCopy.toURI().toString()))}, 1),
             "file:path-to-uri(\"" + fileCopy + "\")"},

        // /_tmpdir_/testDir1/fileCopy
        { "Test file:exists()", bool(true),
            "file:file-exists(\"" + fileCopy + "\")"},

        { "Test file:files()", empty(), "file:files(\"etc\", "
              + "fn:true(),\"[^z]\")"},

        // /_tmpdir_/testDir1/fileCopy
        { "Test file:last-modified()", ii, "file:last-modified(\"etc\")"},

    };

  }

  /**
   * Prepares the tests.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void prepareTest() throws IOException {
    // /_tmpdir_/testDir1
    dir1.mkdir();
    // /_tmpdir_/testDir2
    dir2.mkdir();
    // /_tmpdir_/testDir1/testDelDir1/testDelDir2
    dirDel.mkdirs();

    IO.get(fileCopy.toString()).write(Token.token(doc));
    IO.get(fileMove.toString()).write(Token.token(doc));
    IO.get(fileDel.toString()).write(Token.token(doc));
  }

   /** Finishes the test. */
   @AfterClass
  public static void endTest() {
    // /_tmpdir_/testDir1/testCopy
    fileCopy.delete();
    // /_tmpdir_/testDir1/testMove
    fileMove.delete();
    // /_tmpdir_/testDir1/testWrite
    new File(dir1, TESTWRITE).delete();
    // /_tmpdir_/testDir1/testWriteBin
    new File(dir1, TESTWRITEBIN).delete();
    // /_tmpdir_/testDir1/test1
    new File(dir1, DIR1).delete();
    // /_tmpdir_/testDir1/test2/test3
    new File(dir1 + Prop.SEP + DIR2, DIR3).delete();
    // /_tmpdir_/testDir1/test2
    new File(dir1, DIR2).delete();
    // /_tmpdir_/testDir1
    dir1.delete();
    // /_tmpdir_/testDir2/testCopy
    new File(dir2, TESTCOPY).delete();
    // /_tmpdir_/testDir2/testMove
    new File(dir2, TESTMOVE).delete();
    // /_tmpdir_/testDir2
    dir2.delete();
  }
}

package org.basex.test.query;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.basex.core.Prop;
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
public class FNFileTest extends QueryTest {

  /** Test directory. */
  private static final String TESTDIR1 = "testDir1";
  /** Test directory. */
  private static final String TESTDIR2 = "testDir2";
  /** Test file for file:copy. */
  private static final String TESTCOPY = "testCopy";
  /** Test file for file:delete. */
  private static final String TESTDEL = "testDelete";
  /** Test file for file:move. */
  private static final String TESTMOVE = "testMove";
  /** Test file for file:write. */
  private static final String TESTWRITE = "testWrite";
  /** Test file for file:write-binary. */
  private static final String TESTWRITEBIN = "testWriteBin";
  /** Test directory 1 for file:mkdir. */
  private static final String DIR1 = "test1";
  /** Test directory 2 for file:mkdirs. */
  private static final String DIR2 = "test2";
  /** Test directory 3 for file:mkdirs. */
  private static final String DIR3 = "test3";

  /** /_tmpdir_/testDir1. */
  protected static File dir1 = new File(Prop.TMP + Prop.SEP + TESTDIR1);
  /** /_tmpdir_/testDir2. */
  protected static File dir2 = new File(Prop.TMP + Prop.SEP + TESTDIR2);
  /** /_tmpdir_/testDir1/fileCopy. */
  protected static File fileCopy = new File(dir1.getPath() + Prop.SEP
      + TESTCOPY);
  /** /_tmpdir_/testDir1/fileMove. */
  protected static File fileMove = new File(dir1.getPath() + Prop.SEP
      + TESTMOVE);
  /** /_tmpdir_/testDir1/fileDelete. */
  protected static File fileDel = new File(dir1.getPath() + Prop.SEP + TESTDEL);

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

    queries = new Object[][] {

        // /_tmpdir_/testDir1/test1
        { "Test file:mkdir()", nod(),
            "file:mkdir(\"" + dir1.getPath() + Prop.SEP + DIR1 + "\")" },
            
        // /_tmpdir_/testDir1/test2/test3
        { "Test file:mkdirs()", nod(),
            "file:mkdirs(\"" + dir1.getPath() + Prop.SEP + DIR2 + Prop.SEP
                + DIR3 + "\")"},
                
        // /_tmpdir_/testDir1                
        { "Test file:is-directory()", bool(true),
            "file:is-directory(\"" + dir1.getPath() + "\")" },
            
        // /_tmpdir_/testDir1/fileCopy    
        { "Test file:is-file()", bool(true),
            "file:is-file(\"" + fileCopy.getPath() + "\")" },
            
         // /_tmpdir_/testDir1/fileCopy    
        { "Test file:is-readable()", bool(true),
            "file:is-readable(\"" + fileCopy.getPath() + "\")" },
            
         // /_tmpdir_/testDir1/fileCopy    
        { "Test file:is-writeable()", bool(true),
            "file:is-writeable(\"" + fileCopy.getPath() + "\")" },
            
        { "Test file:path-separator()", str(Prop.SEP), "file:path-separator()"},
        
         // /_tmpdir_/testDir1/fileCopy
        { "Test file:read()", str("\"" + doc + "\""),  
            "file:read(\"" + fileCopy.getPath() + "\")" },
            
        // /_tmpdir_/testDir1/fileCopy    
        { "Test file:read-binary()",
            new ItemIter(new Item[] { new B64(Token.token(doc))}, 1),
            "file:read-binary(\"" + fileCopy.getPath() + "\")" },
            
         // /_tmpdir_/testDir1/fileWrite    
        { "Test file:write()", nod(),
            "file:write(\"" + dir1.getPath() + Prop.SEP + TESTWRITE + "\"," +
            "//Name/Vorname, "  + "(<indent>yes</indent>))" },
            
        // /_tmpdir_/testDir1/fileWriteBin    
        { "Test file:write-binary()", nod(),
            "file:write-binary(\"" + dir1.getPath() + Prop.SEP + TESTWRITEBIN +
            "\", \"aGF0\" cast as xs:base64Binary)" },
                
        // src:  /_tmpdir_/testDir1/fileCopy
        // dest: /_tmpdir_/testDir2/fileCopy        
        { "Test file:copy()", nod(),
            "file:copy(\"" + fileCopy.getPath() + "\", \"" +
            dir2.getPath() + Prop.SEP + TESTCOPY + "\")" },
                
        // src:  /_tmpdir_/testDir1
        // dest: /_tmpdir_/testDir2        
        { "Test file:move()", nod(),
            "file:move(\"" + fileMove.getPath() + "\", \"" + 
            dir2.getPath() + "\")" },
                
        // /_tmpdir_/testDir1/fileDelete        
        { "Test file:delete()", nod(),
            "file:delete(\"" + fileDel.getPath() + "\")" },
            
        // /_tmpdir_/testDir1/fileCopy    
        { "Test file:path-to-full-path()", str(fileCopy.getPath()),
            "file:path-to-full-path(\"" + fileCopy.getPath() + "\")"},
                
         // file:/_tmpdir_/testDir1/fileCopy    
        { "Test file:path-to-uri()", new ItemIter(new Item[]
             { Uri.uri(Token.token(fileCopy.toURI().toString()))}, 1),
             "file:path-to-uri(\"" + fileCopy.getPath() + "\")"},
             
        // /_tmpdir_/testDir1/fileCopy     
        { "Test file:exists()", bool(true),
            "file:file-exists(\"" + fileCopy.getPath() + "\")"},
                
        { "Test file:files()", nod(), "file:files(\"etc\", " + "\"[^z]*e\")"},
        
        // /_tmpdir_/testDir1/fileCopy
        { "Test file:last-modified()", null,
            "file:last-modified(\"" + fileCopy.getPath() + "\")"},

    };

  }

  /** Prepares tests. */
  @BeforeClass
  public static void prepareTest() {

    // /_tmpdir_/testDir1
    dir1.mkdir();

    // /_tmpdir_/testDir2
    dir2.mkdir();

    try {
      BufferedOutputStream outCopy = new BufferedOutputStream(
          new FileOutputStream(fileCopy));
      try {
        outCopy.write(Token.token(doc));
      } finally {
        outCopy.close();
      }
      BufferedOutputStream outMove = new BufferedOutputStream(
          new FileOutputStream(fileMove));
      try {
        outMove.write(Token.token(doc));
      } finally {
        outMove.close();
      }
      BufferedOutputStream outDel = new BufferedOutputStream(
          new FileOutputStream(fileDel));
      try {
        outDel.write(Token.token(doc));
      } finally {
        outDel.close();
      }
    } catch(IOException ex) {
      ex.printStackTrace();
    }

    try {
      queries[18][1] = new ItemIter(new Item[] { new Dtm(
          fileCopy.lastModified(), null)}, 1);
    } catch(QueryException e) { }
  }

   /** Finishes the test. */
   @AfterClass
  public static void endTest() {

    // /_tmpdir_/testDir1/testCopy
    fileCopy.delete();
    // /_tmpdir_/testDir1/testMove
    fileMove.delete();
    // /_tmpdir_/testDir1/testWrite
    new File(dir1.getPath() + Prop.SEP + TESTWRITE).delete();
    // /_tmpdir_/testDir1/testWriteBin
    new File(dir1.getPath() + Prop.SEP + TESTWRITEBIN).delete();
    // /_tmpdir_/testDir1/test1
    new File(dir1.getPath() + Prop.SEP + DIR1).delete();
    // /_tmpdir_/testDir1/test2/test3
    new File(dir1.getPath() + Prop.SEP + DIR2 + Prop.SEP + DIR3).delete();
    // /_tmpdir_/testDir1/test2
    new File(dir1.getPath() + Prop.SEP + DIR2).delete();
    // /_tmpdir_/testDir1
    dir1.delete();
    // /_tmpdir_/testDir2/testCopy
    new File(dir2.getPath() + Prop.SEP + TESTCOPY).delete();
    // /_tmpdir_/testDir2/testMove
    new File(dir2.getPath() + Prop.SEP + TESTMOVE).delete();
    // /_tmpdir_/testDir2
    dir2.delete();

  }
}

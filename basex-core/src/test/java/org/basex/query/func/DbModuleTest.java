package org.basex.query.func;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.core.parse.Commands.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the functions of the Database Module.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DbModuleTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String XML = "src/test/resources/input.xml";
  /** Test file. */
  private static final String CSV = "src/test/resources/input.csv";
  /** Test folder. */
  private static final String FLDR = "src/test/resources/dir/";
  /** Number of XML files for folder. */
  private static final int NFLDR;

  static {
    int fc = 0;
    for(final IOFile c : new IOFile(FLDR).children()) {
      if(c.name().endsWith(IO.XMLSUFFIX)) ++fc;
    }
    NFLDR = fc;
  }

  /**
   * Initializes a test.
   */
  @Before
  public void initTest() {
    execute(new CreateDB(NAME, XML));
  }

  /**
   * Finalizes tests.
   */
  @AfterClass
  public static void finish() {
    execute(new DropDB(NAME));
  }

  /**
   * Test method.
   */
  @Test
  public void open() {
    query(COUNT.args(_DB_OPEN.args(NAME)), "1");
    query(COUNT.args(_DB_OPEN.args(NAME, "")), "1");
    query(COUNT.args(_DB_OPEN.args(NAME, "unknown")), "0");

    // close database instance
    execute(new Close());
    query(COUNT.args(_DB_OPEN.args(NAME, "unknown")), "0");
    query(_DB_OPEN.args(NAME) + "//title/text()", "XML");

    // reference invalid path
    if(Prop.WIN) error(_DB_OPEN.args(NAME, "*"), RESINV_X);

    // run function on non-existing database
    execute(new DropDB(NAME));
    error(_DB_OPEN.args(NAME), BXDB_OPEN_X);
  }

  /** Test method. */
  @Test
  public void openPre() {
    query(_DB_OPEN_PRE.args(NAME, 0) + "//title/text()", "XML");
    error(_DB_OPEN_PRE.args(NAME, -1), BXDB_RANGE_X_X_X);
    error(_DB_OPEN_PRE.args(NAME, Integer.MAX_VALUE), BXDB_RANGE_X_X_X);
  }

  /** Test method. */
  @Test
  public void openId() {
    query(_DB_OPEN_ID.args(NAME, 0) + "//title/text()", "XML");
    error(_DB_OPEN_ID.args(NAME, -1), BXDB_RANGE_X_X_X);
    error(_DB_OPEN_ID.args(NAME, Integer.MAX_VALUE), BXDB_RANGE_X_X_X);
  }

  /** Test method. */
  @Test
  public void text() {
    // run function without and with index
    execute(new DropIndex(CmdIndex.TEXT));
    error(_DB_TEXT.args(NAME, "XML"), BXDB_INDEX_X);
    execute(new CreateIndex(CmdIndex.TEXT));
    query(_DB_TEXT.args(NAME, "XML"), "XML");
    query(_DB_TEXT.args(NAME, "XXX"), "");
  }

  /** Test method. */
  @Test
  public void textRange() {
    // run function without and with index
    execute(new DropIndex(CmdIndex.TEXT));
    error(_DB_TEXT_RANGE.args(NAME, "Exercise", "Fun"), BXDB_INDEX_X);
    execute(new CreateIndex(CmdIndex.TEXT));
    query(_DB_TEXT_RANGE.args(NAME, "Exercise", "Fun"), "Exercise 1\nExercise 2");
    query(_DB_TEXT_RANGE.args(NAME, "XXX", "XXX"), "");
  }

  /** Test method. */
  @Test
  public void attribute() {
    // run function without and with index
    execute(new DropIndex(CmdIndex.ATTRIBUTE));
    error(DATA.args(_DB_ATTRIBUTE.args(NAME, "0")), BXDB_INDEX_X);
    execute(new CreateIndex(CmdIndex.ATTRIBUTE));
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0")), "0");
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0", "id")), "0");
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "0", "XXX")), "");
    query(DATA.args(_DB_ATTRIBUTE.args(NAME, "XXX")), "");
  }

  /** Test method. */
  @Test
  public void attributeRange() {
    // run function without and with index
    execute(new DropIndex(CmdIndex.ATTRIBUTE));
    error(_DB_ATTRIBUTE_RANGE.args(NAME, "0", "9") + "/data()", BXDB_INDEX_X);
    execute(new CreateIndex(CmdIndex.ATTRIBUTE));
    query(_DB_ATTRIBUTE_RANGE.args(NAME, "0", "9") + "/data()", "0\n1");
    query(_DB_ATTRIBUTE_RANGE.args(NAME, "XXX", "XXX"), "");
  }

  /** Test method. */
  @Test
  public void token() {
    // run function without and with index
    execute(new DropIndex(CmdIndex.TOKEN));
    error(DATA.args(_DB_TOKEN.args(NAME, "0")), BXDB_INDEX_X);
    execute(new CreateIndex(CmdIndex.TOKEN));
    query(DATA.args(_DB_TOKEN.args(NAME, "0")), "0");
    query(DATA.args(_DB_TOKEN.args(NAME, "0", "id")), "0");
    query(DATA.args(_DB_TOKEN.args(NAME, "0", "XXX")), "");
    query(DATA.args(_DB_TOKEN.args(NAME, "XXX")), "");
  }

  /** Test method. */
  @Test
  public void list() {
    // add documents
    execute(new Add("test/docs", FLDR));
    contains(_DB_LIST.args(NAME), "test/docs");
    contains(_DB_LIST.args(NAME, "test/"), "test/docs");
    contains(_DB_LIST.args(NAME, "test/docs/input.xml"), "input.xml");
    query(_DB_STORE.args(NAME, "bin/b", "b"));
    query(_DB_LIST.args(NAME, "bin/"), "bin/b");
    query(_DB_LIST.args(NAME, "bin/b"), "bin/b");
    // create two other database and compare substring
    execute(new CreateDB(NAME + 1));
    execute(new CreateDB(NAME + 2));
    contains(_DB_LIST.args(), NAME + 1 + '\n' + NAME + 2);
    execute(new DropDB(NAME + 1));
    execute(new DropDB(NAME + 2));
  }

  /** Test method. */
  @Test
  public void listDetails() {
    query(_DB_LIST_DETAILS.args() + "/@resources/string()", "1");

    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));

    final String xmlCall = _DB_LIST_DETAILS.args(NAME, "xml");
    query(xmlCall + "/@raw/data()", "false");
    query(xmlCall + "/@content-type/data()", MediaType.APPLICATION_XML.toString());
    query(xmlCall + "/@modified-date/xs:dateTime(.)");
    query(xmlCall + "/@size/data()", "2");
    query(xmlCall + "/text()", "xml");

    final String rawCall = _DB_LIST_DETAILS.args(NAME, "raw");
    query(rawCall + "/@raw/data()", "true");
    query(rawCall + "/@content-type/data()", MediaType.APPLICATION_OCTET_STREAM.toString());
    query(rawCall + "/@modified-date/xs:dateTime(.) > " +
        "xs:dateTime('1971-01-01T00:00:01')", "true");
    query(rawCall + "/@size/data()", "3");
    query(rawCall + "/text()", "raw");

    query(_DB_LIST_DETAILS.args(NAME, "test"), "");
    error(_DB_LIST_DETAILS.args("mostProbablyNotAvailable"), BXDB_OPEN_X);
  }

  /** Test method. */
  @Test
  public void backups() {
    query(COUNT.args(_DB_BACKUPS.args(NAME)), "0");
    execute(new CreateBackup(NAME));
    query(COUNT.args(_DB_BACKUPS.args()), "1");
    query(COUNT.args(_DB_BACKUPS.args(NAME)), "1");
    query(COUNT.args(_DB_BACKUPS.args(NAME) + "/(@database, @date, @size)"), "3");
    query(COUNT.args(_DB_BACKUPS.args(NAME + 'X')), "0");
    execute(new DropBackup(NAME));
    query(COUNT.args(_DB_BACKUPS.args(NAME)), "0");
  }

  /** Test method. */
  @Test
  public void system() {
    contains(_DB_SYSTEM.args(), Prop.VERSION);
  }

  /** Test method. */
  @Test
  public void info() {
    query("count(" + _DB_INFO.args(NAME) + "//" +
        SIZE.replaceAll(" |-", "").toLowerCase(Locale.ENGLISH) + ')', 1);
  }

  /** Test method. */
  @Test
  public void nodeID() {
    query(_DB_NODE_ID.args(" /html"), "1");
    query(_DB_NODE_ID.args(" / | /html"), "0\n1");
  }

  /** Test method. */
  @Test
  public void nodePre() {
    query(_DB_NODE_PRE.args(" /html"), "1");
    query(_DB_NODE_PRE.args(" / | /html"), "0\n1");
  }

  /** Test method. */
  @Test
  public void output() {
    query(_DB_OUTPUT.args("x"), "x");
    query(_DB_OUTPUT.args("('x','y')"), "x\ny");
    query(_DB_OUTPUT.args("<a/>"), "<a/>");
    error(_DB_OUTPUT.args("x") + ",1", UPALL);
    error(_DB_OUTPUT.args(" count#1"), BASX_FITEM_X);
    error("copy $c := <a/> modify " + _DB_OUTPUT.args("x") + " return $c", BASX_DBTRANSFORM);
  }

  /** Test method. */
  @Test
  public void outputCache() {
    query(_DB_OUTPUT_CACHE.args(), "");
    query(_DB_OUTPUT.args("x") + ',' + _DB_OUTPUT.args(_DB_OUTPUT_CACHE.args()), "x\nx");
  }

  /** Test method. */
  @Test
  public void add() {
    query(COUNT.args(COLLECTION.args(NAME)), "1");
    query(_DB_ADD.args(NAME, XML));
    query(COUNT.args(COLLECTION.args(NAME)), "2");

    query(_DB_ADD.args(NAME, "\"<root/>\"", "t1.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/t1.xml") + "/root"), "1");

    query(_DB_ADD.args(NAME, " document { <root/> }", "t2.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/t2.xml") + "/root"), "1");

    query(_DB_ADD.args(NAME, " <root/>", "test/t3.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/test/t3.xml") + "/root"), "1");

    query(_DB_ADD.args(NAME, XML, "in/"));
    query(COUNT.args(COLLECTION.args(NAME + "/in/input.xml") + "/html"), "1");

    query(_DB_ADD.args(NAME, XML, "test/t4.xml"));
    query(COUNT.args(COLLECTION.args(NAME + "/test/t4.xml") + "/html"), "1");

    query(_DB_ADD.args(NAME, FLDR, "test/dir"));
    query(COUNT.args(COLLECTION.args(NAME + "/test/dir")), NFLDR);

    query("for $f in " + _FILE_LIST.args(FLDR, true, "*.xml") +
        " return " + _DB_ADD.args(NAME, " '" + FLDR + "' || $f", "dir"));
    query(COUNT.args(COLLECTION.args(NAME + "/dir")), NFLDR);

    query("for $i in 1 to 3 return " +
        _DB_ADD.args(NAME, "\"<root/>\"", "\"doc\" || $i"));
    query(COUNT.args(" for $i in 1 to 3 return " +
        COLLECTION.args('"' + NAME + "/doc\" || $i")), 3);

    // specify parsing options
    query(_DB_ADD.args(NAME, " '<a> </a>'", "chop.xml", " map { 'chop':true() }"));
    query(_DB_OPEN.args(NAME, "chop.xml"), "<a/>");
    query(_DB_ADD.args(NAME, " '<a> </a>'", "nochop.xml", " map { 'chop':false() }"));
    query(_DB_OPEN.args(NAME, "nochop.xml"), "<a> </a>");

    // specify parsing options
    query(_DB_ADD.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': 'header=true' }"));
    query(EXISTS.args(_DB_OPEN.args(NAME, "csv.xml") + "//City"), "true");
    query(_DB_ADD.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': map { 'header': 'true' } }"));
    query(EXISTS.args(_DB_OPEN.args(NAME, "csv.xml") + "//City"), "true");
    query(_DB_ADD.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': map { 'header': true() } }"));
    query(EXISTS.args(_DB_OPEN.args(NAME, "csv.xml") + "//City"), "true");

    error(_DB_ADD.args(NAME, CSV, "csv.xml",
        " map { 'parser':('csv','html') }"), INVALIDOPT_X);
    error(_DB_ADD.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': map { 'header': ('true','false') } }"), INVALIDOPT_X);
    error(_DB_ADD.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': map { 'headr': 'true' } }"), BASX_WHICH_X);
    error(_DB_ADD.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': 'headr=true' }"), BASX_WHICH_X);
  }

  /** Test method. */
  @Test
  public void addWithNS() {
    query(_DB_ADD.args(NAME, " document { <x xmlns:a='a' a:y='' /> }", "x"));
  }

  /** Test method. */
  @Test
  public void delete() {
    execute(new Add("test/docs", FLDR));
    query(_DB_DELETE.args(NAME, "test"));
    query(COUNT.args(COLLECTION.args(NAME + "/test")), 0);
  }

  /** Test method. */
  @Test
  public void create() {
    execute(new Close());

    // create DB without initial content
    query(_DB_CREATE.args(NAME));
    query(_DB_EXISTS.args(NAME), true);

    // create DB w/ initial content
    query(_DB_CREATE.args(NAME, "<dummy/>", "t1.xml"));
    query(_DB_OPEN.args(NAME) + "/root()", "<dummy/>");

    // create DB w/ initial content via document constructor
    query(_DB_CREATE.args(NAME, " document { <dummy/> }", "t2.xml"));
    query(_DB_OPEN.args(NAME) + "/root()", "<dummy/>");

    // create DB w/ initial content given as string
    query(_DB_CREATE.args(NAME, "\"<dummy/>\"", "t1.xml"));
    query(_DB_OPEN.args(NAME) + "/root()", "<dummy/>");

    // create DB w/ initial content multiple times
    query(_DB_CREATE.args(NAME, "<dummy/>", "t1.xml"));
    query(_DB_CREATE.args(NAME, "<dummy/>", "t1.xml"));
    query(_DB_OPEN.args(NAME) + "/root()", "<dummy/>");

    // try to create DB twice during same query
    error(_DB_CREATE.args(NAME) + ',' + _DB_CREATE.args(NAME), BXDB_ONCE_X_X);

    // create DB from file
    query(_DB_CREATE.args(NAME, XML, "in/"));
    query(COUNT.args(COLLECTION.args(NAME + "/in/input.xml") + "/html"), "1");

    // create DB from folder
    query(_DB_CREATE.args(NAME, FLDR, "test/dir"));
    query(COUNT.args(COLLECTION.args(NAME + "/test/dir")), NFLDR);

    // create DB w/ more than one input
    query(_DB_CREATE.args(NAME, "(<a/>,<b/>)", "('1.xml','2.xml')"));
    query(_DB_CREATE.args(NAME, "(<a/>,'" + XML + "')", "('1.xml','2.xml')"));

    error(_DB_CREATE.args(NAME, "()", "1.xml"), BXDB_CREATEARGS_X_X);
    error(_DB_CREATE.args(NAME, "(<a/>,<b/>)", "1.xml"), BXDB_CREATEARGS_X_X);

    // create and drop more than one database
    query("for $i in 1 to 5 return " + _DB_CREATE.args(" '" + NAME + "' || $i"));
    query("for $i in 1 to 5 return " + _DB_DROP.args(" '" + NAME + "' || $i"));

    // create DB with initial EMPTY content
    error(_DB_CREATE.args(""), BXDB_NAME_X);

    // try to access non-existing DB
    query(_DB_DROP.args(NAME));
    error(_DB_CREATE.args(NAME) + ',' + _DB_DROP.args(NAME), BXDB_WHICH_X);

    // run update on existing DB then drop it and create a new one
    query(_DB_CREATE.args(NAME, "<a/>", "a.xml"));
    query("insert node <dummy/> into " + _DB_OPEN.args(NAME));
    query(_DB_CREATE.args(NAME, "<dummy/>", "t1.xml") +
        ", insert node <dummy/> into " + _DB_OPEN.args(NAME) + ',' +
        _DB_DROP.args(NAME));
    query(_DB_OPEN.args(NAME) + "/root()", "<dummy/>");

    // eventually drop database
    query(_DB_DROP.args(NAME));

    // specify index options
    for(final boolean b : new boolean[] { false, true }) {
      query(_DB_CREATE.args(NAME, "()", "()", " map { 'updindex':" + b + "() }"));
      query(_DB_INFO.args(NAME) + "//updindex/text()", b);
    }
    assertEquals(context.options.get(MainOptions.UPDINDEX), false);

    final String[] nopt = { "maxcats", "maxlen", "indexsplitsize", "ftindexsplitsize" };
    for(final String k : nopt) {
      query(_DB_CREATE.args(NAME, "()", "()", " map { '" + k + "':1 }"));
    }
    final String[] bopt = { "textindex", "attrindex", "ftindex", "stemming",
        "casesens", "diacritics" };
    for(final String k : bopt) {
      for(final boolean v : new boolean[] { true, false }) {
        query(_DB_CREATE.args(NAME, "()", "()", " map { '" + k + "':" + v + "() }"));
      }
    }
    final String[] sopt = { "language", "stopwords" };
    for(final String k : sopt) {
      query(_DB_CREATE.args(NAME, "()", "()", " map { '" + k + "':'' }"));
    }

    // specify parsing options
    query(_DB_CREATE.args(NAME, " '<a> </a>'", "a.xml", " map { 'chop':true() }"));
    query(_DB_OPEN.args(NAME), "<a/>");
    query(_DB_CREATE.args(NAME, " '<a> </a>'", "a.xml", " map { 'chop':false() }"));
    query(_DB_OPEN.args(NAME), "<a> </a>");

    // specify unknown or invalid options
    error(_DB_CREATE.args(NAME, "()", "()", " map { 'xyz':'abc' }"), BASX_OPTIONS_X);
    error(_DB_CREATE.args(NAME, "()", "()", " map { 'maxlen':-1 }"), BASX_VALUE_X_X);
    error(_DB_CREATE.args(NAME, "()", "()", " map { 'maxlen':'a' }"), BASX_VALUE_X_X);
    error(_DB_CREATE.args(NAME, "()", "()", " map { 'textindex':'nope' }"), BASX_VALUE_X_X);
  }

  /** Test method. */
  @Test
  public void drop() {
    // non-existing DB name
    final String dbname = NAME + "DBCreate";

    // drop existing DB
    query(_DB_CREATE.args(dbname, "<dummy/>", "doc.xml"));
    query(_DB_DROP.args(dbname));
    query(_DB_EXISTS.args(dbname), "false");

    // invalid name
    error(_DB_DROP.args(" ''"), BXDB_NAME_X);
    // try to drop non-existing DB
    error(_DB_DROP.args(dbname), BXDB_WHICH_X);
  }

  /** Test method. */
  @Test
  public void createCommand() {
    final String dbname = NAME + "DBCreate";
    query(_DB_CREATE.args(dbname));
    execute(new Open(dbname));
    error(_DB_CREATE.args(dbname), BXDB_OPENED_X);
    // close and try again
    execute(new Close());
    query(_DB_CREATE.args(dbname));
    // eventually drop database
    query(_DB_DROP.args(dbname));
  }

  /** Test method. */
  @Test
  public void rename() {
    execute(new Add("test/docs", FLDR));
    query(COUNT.args(COLLECTION.args(NAME + "/test")), NFLDR);

    // rename document
    query(_DB_RENAME.args(NAME, "test", "newtest"));
    query(COUNT.args(COLLECTION.args(NAME + "/test")), 0);
    query(COUNT.args(COLLECTION.args(NAME + "/newtest")), NFLDR);

    // invalid target
    error(_DB_RENAME.args(NAME, "input.xml", " ''"), BXDB_PATH_X);
    error(_DB_RENAME.args(NAME, "input.xml", " '/'"), BXDB_PATH_X);
    error(_DB_RENAME.args(NAME, "input.xml", " '.'"), BXDB_PATH_X);

    // rename paths
    query(_DB_RENAME.args(NAME, "", "x"));
    query(COUNT.args(COLLECTION.args(NAME + "/x/newtest")), NFLDR);

    // rename binary file
    query(_DB_STORE.args(NAME, "file1", ""));
    query(_DB_RENAME.args(NAME, "file1", "file2"));
    query(_DB_RETRIEVE.args(NAME, "file2"));
    error(_DB_RETRIEVE.args(NAME, "file1"), WHICHRES_X);

    query(_DB_RENAME.args(NAME, "file2", "dir1/file3"));
    query(_DB_RETRIEVE.args(NAME, "dir1/file3"));
    query(_DB_RENAME.args(NAME, "dir1", "dir2"));
    query(_DB_RETRIEVE.args(NAME, "dir2/file3"));
    error(_DB_RETRIEVE.args(NAME, "dir1"), WHICHRES_X);

    query(_DB_STORE.args(NAME, "file4", ""));
    query(_DB_STORE.args(NAME, "dir3/file5", ""));

    error(_DB_RENAME.args(NAME, "dir2", "file4"), BXDB_PATH_X);
    error(_DB_RENAME.args(NAME, "file4", "dir2"), BXDB_PATH_X);

    // move files in directories
    query(_DB_RENAME.args(NAME, "dir2", "dir3"));
    query(_DB_RETRIEVE.args(NAME, "dir3/file3"));
    query(_DB_RETRIEVE.args(NAME, "dir3/file5"));
  }

  /** Test method. */
  @Test
  public void replace() {
    execute(new Add("test", XML));

    query(_DB_REPLACE.args(NAME, XML, "\"<R1/>\""));
    query(COUNT.args(COLLECTION.args(NAME + '/' + XML) + "/R1"), 1);
    query(COUNT.args(COLLECTION.args(NAME + '/' + XML) + "/R2"), 0);

    query(_DB_REPLACE.args(NAME, XML, " document { <R2/> }"));
    query(COUNT.args(COLLECTION.args(NAME + '/' + XML) + "/R1"), 0);
    query(COUNT.args(COLLECTION.args(NAME + '/' + XML) + "/R2"), 1);

    query(_DB_REPLACE.args(NAME, XML, XML));
    query(COUNT.args(COLLECTION.args(NAME + '/' + XML) + "/R1"), 0);
    query(COUNT.args(COLLECTION.args(NAME + '/' + XML) + "/R2"), 0);
    query(COUNT.args(COLLECTION.args(NAME + '/' + XML) + "/html"), 1);
  }

  /** Test method. */
  @Test
  public void optimize() {
    query(_DB_OPTIMIZE.args(NAME));
    query(_DB_OPTIMIZE.args(NAME));
    // opened database cannot be fully optimized
    error(_DB_OPTIMIZE.args(NAME, true), UPDBOPTERR_X);
    execute(new Close());
    query(_DB_OPTIMIZE.args(NAME, true));

    // specify additional index options
    final String[] nopt = { "maxcats", "maxlen", "indexsplitsize", "ftindexsplitsize" };
    for(final String k : nopt) {
      query(_DB_OPTIMIZE.args(NAME, false, " map { '" + k + "': 1 }"));
    }
    final String[] bopt = { "textindex", "attrindex", "ftindex", "stemming",
        "casesens", "diacritics" };
    for(final String k : bopt) {
      for(final boolean v : new boolean[] { true, false }) {
        query(_DB_OPTIMIZE.args(NAME, false, " map { '" + k + "':" + v + "() }"));
      }
    }
    final String[] sopt = { "language", "stopwords" };
    for(final String k : sopt) {
      query(_DB_OPTIMIZE.args(NAME, false, " map { '" + k + "':'' }"));
    }
    assertEquals(context.options.get(MainOptions.TEXTINDEX), true);

    error(_DB_OPTIMIZE.args(NAME, false, " map { 'xyz': 'abc' }"), BASX_OPTIONS_X);
    error(_DB_OPTIMIZE.args(NAME, false, " map { 'updindex': 1 }"), BASX_OPTIONS_X);
    error(_DB_OPTIMIZE.args(NAME, false, " map { 'maxlen': -1 }"), BASX_VALUE_X_X);
    error(_DB_OPTIMIZE.args(NAME, false, " map { 'maxlen': 'a' }"), BASX_VALUE_X_X);
    error(_DB_OPTIMIZE.args(NAME, false, " map { 'textindex':'nope' }"), BASX_VALUE_X_X);

    // check if optimize call preserves original options
    query(_DB_OPTIMIZE.args(NAME));
    final String[] indexes = { "textindex", "attrindex", "ftindex" };
    final String[] includes = { "textinclude", "attrinclude", "ftinclude" };
    for(final String i : indexes) query(_DB_INFO.args(NAME) + "//" + i + "/text()", "false");
    for(final String i : includes) query(_DB_INFO.args(NAME) + "//" + i + "/text()", "");

    execute(new Open(NAME));
    final CmdIndex[] cmds = { CmdIndex.TEXT, CmdIndex.ATTRIBUTE, CmdIndex.FULLTEXT };
    for(final String i : includes) execute(new Set(i, "a"));
    for(final CmdIndex cmd : cmds) execute(new CreateIndex(cmd));
    execute(new Close());

    query(_DB_OPTIMIZE.args(NAME));
    for(final String i : indexes) query(_DB_INFO.args(NAME) + "//" + i + "/text()", "true");
    for(final String i : includes) query(_DB_INFO.args(NAME) + "//" + i + "/text()", "a");

    execute(new Open(NAME));
    for(final String i : includes) execute(new Set(i, ""));
    for(final CmdIndex cmd : cmds) execute(new DropIndex(cmd));
    execute(new Close());

    query(_DB_OPTIMIZE.args(NAME));
    for(final String i : indexes) query(_DB_INFO.args(NAME) + "//" + i + "/text()", "false");
    for(final String i : includes) query(_DB_INFO.args(NAME) + "//" + i + "/text()", "");

    query(_DB_OPTIMIZE.args(NAME, true, " map {"
        + "'textindex':true(),'attrindex':true(),'ftindex':true(),'updindex':true(),"
        + "'textinclude':'a','attrinclude':'a','ftinclude':'a'"
        + " }"));
    for(final String i : indexes) query(_DB_INFO.args(NAME) + "//" + i + "/text()", "true");
    for(final String i : includes) query(_DB_INFO.args(NAME) + "//" + i + "/text()", "a");
    query(_DB_INFO.args(NAME) + "//updindex/text()", "true");
  }

  /** Test method. */
  @Test
  public void retrieve() {
    error(_DB_RETRIEVE.args(NAME, "raw"), WHICHRES_X);
    query(_DB_STORE.args(NAME, "raw", "xs:hexBinary('41')"));
    query("xs:hexBinary(" + _DB_RETRIEVE.args(NAME, "raw") + ')', "A");
    query(_DB_DELETE.args(NAME, "raw"));
    error(_DB_RETRIEVE.args(NAME, "raw"), WHICHRES_X);
  }

  /** Test method. */
  @Test
  public void store() {
    query(_DB_STORE.args(NAME, "raw1", "xs:hexBinary('41')"));
    query(_DB_STORE.args(NAME, "raw2", "b"));
    query(_DB_RETRIEVE.args(NAME, "raw2"), "b");
    query(_DB_STORE.args(NAME, "raw3", 123));
    query(_DB_RETRIEVE.args(NAME, "raw3"), "123");
  }

  /** Test method. */
  @Test
  public void flush() {
    query(_DB_FLUSH.args(NAME));
    error(_DB_FLUSH.args(NAME + "unknown"), BXDB_OPEN_X);
  }

  /** Test method. */
  @Test
  public void isRaw() {
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(_DB_IS_RAW.args(NAME, "xml"), "false");
    query(_DB_IS_RAW.args(NAME, "raw"), "true");
    query(_DB_IS_RAW.args(NAME, "xxx"), "false");
  }

  /** Test method. */
  @Test
  public void exists() {
    query(_DB_ADD.args(NAME, "\"<a/>\"", "x/xml"));
    query(_DB_STORE.args(NAME, "x/raw", "bla"));
    // checks if the specified resources exist (false expected for directories)
    query(_DB_EXISTS.args(NAME), "true");
    query(_DB_EXISTS.args(NAME, "x/xml"), "true");
    query(_DB_EXISTS.args(NAME, "x/raw"), "true");
    query(_DB_EXISTS.args(NAME, "xxx"), "false");
    query(_DB_EXISTS.args(NAME, "x"), "false");
    query(_DB_EXISTS.args(NAME, ""), "false");
    // false expected for missing database
    execute(new DropDB(NAME));
    query(_DB_EXISTS.args(NAME), "false");
  }

  /** Test method. */
  @Test
  public void isXML() {
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(_DB_IS_XML.args(NAME, "xml"), "true");
    query(_DB_IS_XML.args(NAME, "raw"), "false");
    query(_DB_IS_XML.args(NAME, "xxx"), "false");
  }

  /** Test method. */
  @Test
  public void contentType() {
    query(_DB_ADD.args(NAME, "\"<a/>\"", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(_DB_CONTENT_TYPE.args(NAME, "xml"), MediaType.APPLICATION_XML.toString());
    query(_DB_CONTENT_TYPE.args(NAME, "raw"), MediaType.APPLICATION_OCTET_STREAM.toString());
    error(_DB_CONTENT_TYPE.args(NAME, "test"), WHICHRES_X);
  }

  /** Test method. */
  @Test
  public void export() {
    // exports the database
    query(_DB_EXPORT.args(NAME, new IOFile(Prop.TMP, NAME)));
    final IOFile f = new IOFile(new IOFile(Prop.TMP, NAME), XML.replaceAll(".*/", ""));
    query(_FILE_EXISTS.args(f));
    // serializes as text; ensures that the output contains no angle bracket
    query(_DB_EXPORT.args(NAME, new IOFile(Prop.TMP, NAME), " map {'method':'text'}"));
    query("0[" + CONTAINS.args(_FILE_READ_TEXT.args(f), "&lt;") + ']', "");
    // deletes the exported file
    query(_FILE_DELETE.args(f));
  }

  /** Test method. */
  @Test
  public void name() {
    query(_DB_NAME.args(_DB_OPEN.args(NAME)), NAME);
    query(_DB_NAME.args(_DB_OPEN.args(NAME) + "/*"), NAME);
  }

  /** Test method. */
  @Test
  public void path() {
    query(_DB_PATH.args(_DB_OPEN.args(NAME)), XML.replaceAll(".*/", ""));
    query(_DB_PATH.args(_DB_OPEN.args(NAME) + "/*"), XML.replaceAll(".*/", ""));
    query(_DB_PATH.args("<x/> update ()"), "");
  }

  /** Test method. */
  @Test
  public void createBackup() {
    query(COUNT.args(_DB_BACKUPS.args(NAME)), "0");
    query(_DB_CREATE_BACKUP.args(NAME));
    query(COUNT.args(_DB_BACKUPS.args(NAME)), "1");

    // invalid name
    error(_DB_CREATE_BACKUP.args(" ''"), BXDB_NAME_X);
    // try to backup non-existing database
    error(_DB_CREATE_BACKUP.args(NAME + "backup"), BXDB_WHICH_X);
  }

  /** Test method. */
  @Test
  public void dropBackup() {
    // create and drop backup
    query(_DB_CREATE_BACKUP.args(NAME));
    query(_DB_DROP_BACKUP.args(NAME));
    query(COUNT.args(_DB_BACKUPS.args(NAME)), "0");

    // create and drop backup file
    query(_DB_CREATE_BACKUP.args(NAME));
    query(_DB_DROP_BACKUP.args(query(_DB_BACKUPS.args(NAME))));

    // invalid name
    error(_DB_DROP_BACKUP.args(" ''"), BXDB_NAME_X);
    // backup file does not exist
    error(_DB_DROP_BACKUP.args(NAME), BXDB_WHICHBACK_X);
    // check if drop is called before create
    error(_DB_CREATE_BACKUP.args(NAME) + ',' + _DB_DROP_BACKUP.args(NAME), BXDB_WHICHBACK_X);
  }

  /** Test method. */
  @Test
  public void copy() {
    // close database in global context
    execute(new Close());

    // copy database to new name and vice versa
    query(_DB_COPY.args(NAME, NAME + 'c'));
    try {
      query(_DB_COPY.args(NAME + 'c', NAME));
    } finally {
      query(_DB_DROP.args(NAME + 'c'));
    }

    // invalid names
    error(_DB_COPY.args("x", " ''"), BXDB_NAME_X);
    error(_DB_COPY.args(" ''", "x"), BXDB_NAME_X);

    // same name is disallowed
    error(_DB_COPY.args(NAME, NAME), BXDB_SAME_X);
    // source database does not exist
    error(_DB_COPY.args(NAME + "copy", NAME), BXDB_WHICH_X);
  }

  /** Test method. */
  @Test
  public void alter() {
    // close database in global context
    execute(new Close());

    // rename database to new name and vice versa
    query(_DB_ALTER.args(NAME, NAME + 'a'));
    query(_DB_ALTER.args(NAME + 'a', NAME));

    // invalid names
    error(_DB_ALTER.args("x", " ''"), BXDB_NAME_X);
    error(_DB_ALTER.args(" ''", "x"), BXDB_NAME_X);

    // same name is disallowed
    error(_DB_ALTER.args(NAME, NAME), BXDB_SAME_X);
    // source database does not exist
    error(_DB_ALTER.args(NAME + "alter", NAME), BXDB_WHICH_X);
  }

  /** Test method. */
  @Test
  public void restore() {
    execute(new Close());

    // backup and restore file
    query(_DB_CREATE_BACKUP.args(NAME));
    query(_DB_RESTORE.args(NAME));
    query(_DB_RESTORE.args(NAME));

    // drop backups
    query(_DB_DROP_BACKUP.args(NAME));
    error(_DB_RESTORE.args(NAME), BXDB_NOBACKUP_X);

    // invalid names
    error(_DB_RESTORE.args(" ''"), BXDB_NAME_X);
  }
}

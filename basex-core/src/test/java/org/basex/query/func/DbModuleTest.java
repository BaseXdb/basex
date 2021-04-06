package org.basex.query.func;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.core.parse.Commands.*;
import org.basex.io.*;
import org.basex.query.ast.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.basex.util.options.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the functions of the Database Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DbModuleTest extends QueryPlanTest {
  /** Test file. */
  private static final String XML = "src/test/resources/input.xml";
  /** Test file. */
  private static final String CSV = "src/test/resources/input.csv";
  /** Test folder. */
  private static final String FLDR = "src/test/resources/dir/";
  /** Number of XML files for folder. */
  private static final int XMLFILES;

  static {
    int files = 0;
    for(final IOFile file : new IOFile(FLDR).children()) {
      if(file.name().endsWith(IO.XMLSUFFIX)) ++files;
    }
    XMLFILES = files;
  }

  /** Initializes a test. */
  @BeforeEach public void initTest() {
    execute(new CreateDB(NAME, XML));
  }

  /** Finalizes a test. */
  @AfterEach public void finish() {
    set(MainOptions.TEXTINCLUDE, "");
    set(MainOptions.ATTRINCLUDE, "");
    set(MainOptions.TOKENINCLUDE, "");
    set(MainOptions.FTINCLUDE, "");
    execute(new DropDB(NAME));
  }

  /** Test method. */
  @Test public void add() {
    final Function func = _DB_ADD;
    query("count(" + COLLECTION.args(NAME) + ")", 1);
    query(func.args(NAME, XML));
    query("count(" + COLLECTION.args(NAME) + ")", 2);

    query(func.args(NAME, " <root/>", "t1.xml"));
    query("count(" + COLLECTION.args(NAME + "/t1.xml") + "/root)", 1);

    query(func.args(NAME, " document { <root/> }", "t2.xml"));
    query("count(" + COLLECTION.args(NAME + "/t2.xml") + "/root)", 1);

    query(func.args(NAME, " <root/>", "test/t3.xml"));
    query("count(" + COLLECTION.args(NAME + "/test/t3.xml") + "/root)", 1);

    query(func.args(NAME, XML, "in/"));
    query("count(" + COLLECTION.args(NAME + "/in/input.xml") + "/html)", 1);

    query(func.args(NAME, XML, "test/t4.xml"));
    query("count(" + COLLECTION.args(NAME + "/test/t4.xml") + "/html)", 1);

    query(func.args(NAME, FLDR, "test/dir"));
    query("count(" + COLLECTION.args(NAME + "/test/dir") + ")", XMLFILES);

    query("for $f in " + _FILE_LIST.args(FLDR, true, "*.xml") +
        " return " + func.args(NAME, " '" + FLDR + "' || $f", "dir"));
    query("count(" + COLLECTION.args(NAME + "/dir") + ")", XMLFILES);

    query("for $i in 1 to 3 return " + func.args(NAME, " <root/>", " 'doc' || $i"));
    query("count(" + " for $i in 1 to 3 return " +
        COLLECTION.args(" '" + NAME + "/doc' || $i") + ")", 3);

    // specify parsing options
    query(func.args(NAME, " '<a> </a>'", "chop.xml",
        " map { '" + lc(MainOptions.CHOP) + "':true() }"));
    query(_DB_OPEN.args(NAME, "chop.xml"), "<a/>");
    query(func.args(NAME, " '<a> </a>'", "nochop.xml",
        " map { '" + lc(MainOptions.CHOP) + "':false() }"));
    query(_DB_OPEN.args(NAME, "nochop.xml"), "<a> </a>");

    // specify parsing options
    query(func.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': 'header=true' }"));
    query("exists(" + _DB_OPEN.args(NAME, "csv.xml") + "//City)", true);
    query(func.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': map { 'header': 'true' } }"));
    query("exists(" + _DB_OPEN.args(NAME, "csv.xml") + "//City)", true);
    query(func.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': map { 'header': true() } }"));
    query("exists(" + _DB_OPEN.args(NAME, "csv.xml") + "//City)", true);

    final String addcache = " map { 'addcache': true() }";
    query(func.args(NAME, " <cache/>", "C1.xml", addcache));
    query("exists(" + _DB_OPEN.args(NAME, "C1.xml") + ")", true);
    query(func.args(NAME, " <cache/>", "C2.xml", addcache));
    query("exists(" + _DB_OPEN.args(NAME, "C2.xml") + ")", true);
    query(func.args(NAME, XML, "C3.xml", addcache));
    query("exists(" + _DB_OPEN.args(NAME, "C3.xml") + ")", true);

    error(func.args(NAME, CSV, "csv.xml",
        " map { 'parser':('csv','html') }"), BASEX_OPTIONS_X_X);
    error(func.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': map { 'header': ('true','false') } }"), INVALIDOPT_X);
    error(func.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': map { 'headr': 'true' } }"), BASEX_OPTIONS2_X);
    error(func.args(NAME, CSV, "csv.xml",
        " map { 'parser':'csv','csvparser': 'headr=true' }"), BASEX_OPTIONS2_X);

    error(func.args(NAME, " <a/>"), RESINV_X);
    error(func.args(NAME, " <a/>", " ()"), RESINV_X);
    error(func.args(NAME, " <a/>", ""), RESINV_X);
    error(func.args(NAME, " <a/>", "/"), RESINV_X);

    // add document with namespaces
    query(_DB_ADD.args(NAME, " document { <x xmlns:a='a' a:y='' /> }", "x"));
  }

  /** Test method. */
  @Test public void alter() {
    // close database in global context
    final Function func = _DB_ALTER;
    execute(new Close());

    // rename database to new name and vice versa
    query(func.args(NAME, NAME + 'a'));
    query(func.args(NAME + 'a', NAME));

    // invalid names
    error(func.args("x", " ''"), DB_NAME_X);
    error(func.args(" ''", "x"), DB_NAME_X);

    // same name is disallowed
    error(func.args(NAME, NAME), DB_CONFLICT4_X);
    // source database does not exist
    error(func.args(NAME + "alter", NAME), DB_OPEN1_X);
  }

  /** Test method. */
  @Test public void alterBackup() {
    // close database in global context
    final Function func = _DB_ALTER_BACKUP;
    execute(new Close());

    query(_DB_CREATE_BACKUP.args(NAME));

    // rename database to new name and vice versa
    query(func.args(NAME, NAME + 'a'));
    query(func.args(NAME + 'a', NAME));

    // invalid names
    error(func.args("x", " ''"), DB_NAME_X);
    error(func.args(" ''", "x"), DB_NAME_X);

    // same name is disallowed
    error(func.args(NAME, NAME), DB_CONFLICT4_X);
    // source database does not exist
    error(func.args(NAME + "alter", NAME), DB_NOBACKUP_X);

    execute(new DropBackup(NAME));
    error(func.args(NAME, NAME + "alter"), DB_NOBACKUP_X);
  }

  /** Test method. */
  @Test public void attribute() {
    // run function without and with index
    final Function func = _DB_ATTRIBUTE;
    execute(new DropIndex(CmdIndex.ATTRIBUTE));
    error(func.args(NAME, "0"), DB_NOINDEX_X_X);

    execute(new CreateIndex(CmdIndex.ATTRIBUTE));
    query(func.args(NAME, "0"), "id=\"0\"");
    query(func.args(NAME, "0", "id"), "id=\"0\"");
    query(func.args(NAME, "0", "XXX"), "");
    query(func.args(NAME, "XXX"), "");
  }

  /** Test method. */
  @Test public void attributeRange() {
    // run function without and with index
    final Function func = _DB_ATTRIBUTE_RANGE;
    execute(new DropIndex(CmdIndex.ATTRIBUTE));
    error(func.args(NAME, "0", "9") + "/data()", DB_NOINDEX_X_X);

    execute(new CreateIndex(CmdIndex.ATTRIBUTE));
    query(func.args(NAME, "0", "9") + "/data()", "0\n1");
    query(func.args(NAME, "XXX", "XXX"), "");
  }

  /** Test method. */
  @Test public void backups() {
    final Function func = _DB_BACKUPS;
    query("count(" + func.args(NAME) + ")", 0);
    execute(new CreateBackup(NAME));
    query("count(" + func.args() + ")", 1);
    query("count(" + func.args(NAME) + ")", 1);
    query("count(" + func.args(NAME) + "/(@database | @date | @size))", 3);
    query("count(" + func.args(NAME) + "/(@database, @date, @size))", 3);
    query("count(" + func.args(NAME + 'X') + ")", 0);
    execute(new DropBackup(NAME));
    query("count(" + func.args(NAME) + ")", 0);
  }

  /** Test method. */
  @Test public void contentType() {
    final Function func = _DB_CONTENT_TYPE;
    query(_DB_ADD.args(NAME, " <a/>", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(func.args(NAME, "xml"), MediaType.APPLICATION_XML.toString());
    query(func.args(NAME, "raw"), MediaType.APPLICATION_OCTET_STREAM.toString());
    error(func.args(NAME, "test"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void copy() {
    // close database in global context
    final Function func = _DB_COPY;
    execute(new Close());

    // copy database to new name and vice versa
    query(func.args(NAME, NAME + 'c'));
    try {
      query(func.args(NAME + 'c', NAME));
    } finally {
      query(_DB_DROP.args(NAME + 'c'));
    }

    // invalid names
    error(func.args("x", " ''"), DB_NAME_X);
    error(func.args(" ''", "x"), DB_NAME_X);

    // same name is disallowed
    error(func.args(NAME, NAME), DB_CONFLICT4_X);
    // source database does not exist
    error(func.args(NAME + "copy", NAME), DB_OPEN1_X);
  }

  /** Test method. */
  @Test public void create() {
    final Function func = _DB_CREATE;
    execute(new Close());

    // create DB without initial content
    query(func.args(NAME));
    query(_DB_EXISTS.args(NAME), true);

    // create DB w/ initial content
    query(func.args(NAME, " <dummy/>", "t1.xml"));
    query(_DB_OPEN.args(NAME) + "/root()", "<dummy/>");

    // create DB w/ initial content via document constructor
    query(func.args(NAME, " document { <dummy/> }", "t2.xml"));
    query(_DB_OPEN.args(NAME) + "/root()", "<dummy/>");

    // create DB w/ initial content given as string
    query(func.args(NAME, " <dummy/>", "t1.xml"));
    query(_DB_OPEN.args(NAME) + "/root()", "<dummy/>");

    // create DB w/ initial content multiple times
    query(func.args(NAME, " <dummy/>", "t1.xml"));
    query(func.args(NAME, " <dummy/>", "t1.xml"));
    query(_DB_OPEN.args(NAME) + "/root()", "<dummy/>");

    // try to create DB twice during same query
    error(func.args(NAME) + ',' + func.args(NAME), DB_CONFLICT1_X_X);

    // create DB from file
    query(func.args(NAME, XML, "in/"));
    query("count(" + COLLECTION.args(NAME + "/in/input.xml") + "/html)", 1);

    // create DB from folder
    query(func.args(NAME, FLDR, "test/dir"));
    query("count(" + COLLECTION.args(NAME + "/test/dir") + ")", XMLFILES);

    // create DB w/ more than one input
    query(func.args(NAME, " (<a/>,<b/>)", " ('1.xml','2.xml')"));
    query(func.args(NAME, " (<a/>,'" + XML + "')", " ('1.xml','2.xml')"));

    error(func.args(NAME, " ()", "1.xml"), DB_ARGS_X_X);
    error(func.args(NAME, " (<a/>,<b/>)", "1.xml"), DB_ARGS_X_X);

    // create and drop more than one database
    query("for $i in 1 to 5 return " + func.args(" '" + NAME + "' || $i"));
    query("for $i in 1 to 5 return " + _DB_DROP.args(" '" + NAME + "' || $i"));

    // create DB with initial EMPTY content
    error(func.args(""), DB_NAME_X);

    // try to access non-existing DB
    query(_DB_DROP.args(NAME));
    error(func.args(NAME) + ',' + _DB_DROP.args(NAME), DB_OPEN1_X);

    // run update on existing DB then drop it and create a new one
    query(func.args(NAME, " <a/>", "a.xml"));
    query("insert node <dummy/> into " + _DB_OPEN.args(NAME));
    query(func.args(NAME, " <dummy/>", "t1.xml") +
        ", insert node <dummy/> into " + _DB_OPEN.args(NAME) + ',' +
        _DB_DROP.args(NAME));
    query(_DB_OPEN.args(NAME) + "/root()", "<dummy/>");

    // eventually drop database
    query(_DB_DROP.args(NAME));

    // specify index options
    for(final boolean b : new boolean[] { false, true }) {
      query(func.args(NAME, " ()", " ()",
          " map { '" + lc(MainOptions.UPDINDEX) + "': " + b + "() }"));
      query(_DB_INFO.args(NAME) + "//" + lc(MainOptions.UPDINDEX) + "/text()", b);
    }
    assertEquals(context.options.get(MainOptions.UPDINDEX), false);

    final String[] numberOptions = lc(MainOptions.MAXCATS, MainOptions.MAXLEN,
        MainOptions.SPLITSIZE);
    final String[] boolOptions = lc(MainOptions.TEXTINDEX, MainOptions.ATTRINDEX,
        MainOptions.TOKENINDEX, MainOptions.FTINDEX, MainOptions.STEMMING,
        MainOptions.CASESENS, MainOptions.DIACRITICS);
    final String[] stringOptions = lc(MainOptions.LANGUAGE, MainOptions.STOPWORDS);

    for(final String option : numberOptions) {
      query(func.args(NAME, " ()", " ()", " map { '" + option + "': 1 }"));
    }
    for(final String option : boolOptions) {
      for(final boolean v : new boolean[] { true, false }) {
        query(func.args(NAME, " ()", " ()", " map { '" + option + "': " + v + "() }"));
      }
    }
    for(final String option : stringOptions) {
      query(func.args(NAME, " ()", " ()", " map { '" + option + "': '' }"));
    }

    // specify parsing options
    query(func.args(NAME, " '<a> </a>'", "a.xml",
        " map { '" + lc(MainOptions.CHOP) + "': true() }"));
    query(_DB_OPEN.args(NAME), "<a/>");
    query(func.args(NAME, " '<a> </a>'", "a.xml",
        " map { '" + lc(MainOptions.CHOP) + "': false() }"));
    query(_DB_OPEN.args(NAME), "<a> </a>");

    // specify unknown or invalid options
    error(func.args(NAME, " ()", " ()", " map { 'xyz': 'abc' }"),
        BASEX_OPTIONS1_X);
    error(func.args(NAME, " ()", " ()", " map { '" + lc(MainOptions.MAXLEN) + "': -1 }"),
        BASEX_OPTIONS_X_X);
    error(func.args(NAME, " ()", " ()", " map { '" + lc(MainOptions.MAXLEN) + "': 'a' }"),
        BASEX_OPTIONS_X_X);
    error(func.args(NAME, " ()", " ()", " map { '" + lc(MainOptions.TEXTINDEX) + "': 'nope' }"),
        BASEX_OPTIONS_X_X);
  }

  /** Test method. */
  @Test public void create2() {
    final Function func = _DB_CREATE;
    final String dbName = NAME + "DBCreate";

    query(func.args(dbName));
    execute(new Open(dbName));
    error(func.args(dbName), DB_LOCK1_X);
    // close and try again
    execute(new Close());
    query(func.args(dbName));
    // eventually drop database
    query(_DB_DROP.args(dbName));
  }

  /** Test method. */
  @Test public void createBackup() {
    final Function func = _DB_CREATE_BACKUP;
    query("count(" + _DB_BACKUPS.args(NAME) + ")", 0);
    query(func.args(NAME));
    query("count(" + _DB_BACKUPS.args(NAME) + ")", 1);

    // invalid name
    error(func.args(""), DB_NAME_X);
    // try to backup non-existing database
    error(func.args(NAME + "backup"), DB_OPEN1_X);
  }

  /** Test method. */
  @Test public void delete() {
    final Function func = _DB_DELETE;
    execute(new Add("test/docs", FLDR));
    query(func.args(NAME, "test"));
    query("count(" + COLLECTION.args(NAME + "/test") + ")", 0);
  }

  /** Test method. */
  @Test public void dir() {
    final Function func = _DB_DIR;
    query(_DB_ADD.args(NAME, " <a/>", "xml/doc.xml"));
    query(_DB_STORE.args(NAME, "raw/raw.data", "bla"));

    final String xmlCall = func.args(NAME, "xml");
    query(xmlCall + "/@raw/data()", false);
    query(xmlCall + "/@content-type/data()", MediaType.APPLICATION_XML.toString());
    query(xmlCall + "/@modified-date/xs:dateTime(.)");
    query(xmlCall + "/@size/data()", "");
    query(xmlCall + "/text()", "doc.xml");

    final String rawCall = func.args(NAME, "raw");
    query(rawCall + "/@raw/data()", true);
    query(rawCall + "/@content-type/data()", MediaType.APPLICATION_OCTET_STREAM.toString());
    query(rawCall + "/@modified-date/xs:dateTime(.) > xs:dateTime('1971-01-01T00:00:01')", true);
    query(rawCall + "/@size/data()", 3);
    query(rawCall + "/text()", "raw.data");

    query(func.args(NAME, "test"), "");
    error(func.args("notAvailable", ""), DB_OPEN2_X);
  }

  /** Test method. */
  @Test public void drop() {
    final Function func = _DB_DROP;
    final String dbName = NAME + "DBCreate";

    // drop existing DB
    query(_DB_CREATE.args(dbName, " <dummy/>", "doc.xml"));
    query(func.args(dbName));
    query(_DB_EXISTS.args(dbName), false);

    // invalid name
    error(func.args(" ''"), DB_NAME_X);
    // try to drop non-existing DB
    error(func.args(dbName), DB_OPEN1_X);
  }

  /** Test method. */
  @Test public void dropBackup() {
    // create and drop backup
    final Function func = _DB_DROP_BACKUP;
    query(_DB_CREATE_BACKUP.args(NAME));
    query(func.args(NAME));
    query("count(" + _DB_BACKUPS.args(NAME) + ")", 0);

    // create and drop backup file
    query(_DB_CREATE_BACKUP.args(NAME));
    query(func.args(' ' + query(_DB_BACKUPS.args(NAME))));

    // invalid name
    error(func.args(""), DB_NAME_X);
    // backup file does not exist
    error(func.args(NAME), DB_NOBACKUP_X);
    // check if drop is called before create
    error(_DB_CREATE_BACKUP.args(NAME) + ',' + func.args(NAME), DB_NOBACKUP_X);
  }

  /** Test method. */
  @Test public void exists() {
    final Function func = _DB_EXISTS;
    query(_DB_ADD.args(NAME, " <a/>", "x/xml"));
    query(_DB_STORE.args(NAME, "x/raw", "bla"));
    // checks if the specified resources exist (false expected for directories)
    query(func.args(NAME), true);
    query(func.args(NAME, "x/xml"), true);
    query(func.args(NAME, "x/raw"), true);
    query(func.args(NAME, "xxx"), false);
    query(func.args(NAME, "x"), false);
    query(func.args(NAME, ""), false);

    // false expected for missing database
    execute(new DropDB(NAME));
    check(func.args(NAME), false, root(Bln.class));
  }

  /** Test method. */
  @Test public void export() {
    final Function func = _DB_EXPORT;
    // exports the database
    query(func.args(NAME, new IOFile(Prop.TEMPDIR, NAME)));
    final IOFile path = new IOFile(new IOFile(Prop.TEMPDIR, NAME), XML.replaceAll(".*/", ""));
    query(_FILE_EXISTS.args(path));
    // serializes as text; ensures that the output contains no angle bracket
    query(func.args(NAME, new IOFile(Prop.TEMPDIR, NAME), " map { 'method': 'text' }"));
    query("0[contains(" + _FILE_READ_TEXT.args(path) + ", '&lt;')]", "");
    // deletes the exported file
    query(_FILE_DELETE.args(path));
  }

  /** Test method. */
  @Test public void flush() {
    final Function func = _DB_FLUSH;
    query(func.args(NAME));
    error(func.args(NAME + "unknown"), DB_OPEN2_X);
  }

  /** Test method. */
  @Test public void info() {
    final Function func = _DB_INFO;
    query("count(" + func.args(NAME) + "//" +
        SIZE.replaceAll("[- ]", "").toLowerCase(Locale.ENGLISH) + ')', 1);
  }

  /** Test method. */
  @Test public void isRaw() {
    final Function func = _DB_IS_RAW;
    query(_DB_ADD.args(NAME, " <a/>", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(func.args(NAME, "xml"), false);
    query(func.args(NAME, "raw"), true);
    query(func.args(NAME, "xxx"), false);
  }

  /** Test method. */
  @Test public void isXML() {
    final Function func = _DB_IS_XML;
    query(_DB_ADD.args(NAME, " <a/>", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));
    query(func.args(NAME, "xml"), true);
    query(func.args(NAME, "raw"), false);
    query(func.args(NAME, "xxx"), false);
  }

  /** Test method. */
  @Test public void list() {
    // add documents
    final Function func = _DB_LIST;
    execute(new Add("test/docs", FLDR));
    contains(func.args(NAME), "test/docs");
    contains(func.args(NAME, "test/"), "test/docs");
    contains(func.args(NAME, "test/docs/input.xml"), "input.xml");

    query(_DB_STORE.args(NAME, "bin/b", "b"));
    query(func.args(NAME, "bin/"), "bin/b");
    query(func.args(NAME, "bin/b"), "bin/b");

    // create two other database and compare substring
    execute(new CreateDB(NAME + 1));
    execute(new CreateDB(NAME + 2));
    contains(func.args(), NAME + 1 + '\n' + NAME + 2);
    execute(new DropDB(NAME + 1));
    execute(new DropDB(NAME + 2));
  }

  /** Test method. */
  @Test public void listDetails() {
    final Function func = _DB_LIST_DETAILS;
    query(func.args() + "/@resources/string()", 1);

    query(_DB_ADD.args(NAME, " <a/>", "xml"));
    query(_DB_STORE.args(NAME, "raw", "bla"));

    final String xmlCall = func.args(NAME, "xml");
    query(xmlCall + "/@raw/data()", false);
    query(xmlCall + "/@content-type/data()", MediaType.APPLICATION_XML.toString());
    query(xmlCall + "/@modified-date/xs:dateTime(.)");
    query(xmlCall + "/@size/data()", 2);
    query(xmlCall + "/text()", "xml");

    final String rawCall = func.args(NAME, "raw");
    query(rawCall + "/@raw/data()", true);
    query(rawCall + "/@content-type/data()", MediaType.APPLICATION_OCTET_STREAM.toString());
    query(rawCall + "/@modified-date/xs:dateTime(.) > xs:dateTime('1971-01-01T00:00:01')", true);
    query(rawCall + "/@size/data()", 3);
    query(rawCall + "/text()", "raw");

    query(func.args(NAME, "test"), "");
    error(func.args("notAvailable"), DB_OPEN2_X);
  }

  /** Test method. */
  @Test public void name() {
    final Function func = _DB_NAME;
    query(func.args(_DB_OPEN.args(NAME)), NAME);
    query(func.args(_DB_OPEN.args(NAME) + "/*"), NAME);
  }

  /** Test method. */
  @Test public void nodeId() {
    final Function func = _DB_NODE_ID;
    query(func.args(" /html"), 1);
    query(func.args(" / | /html"), "0\n1");
  }

  /** Test method. */
  @Test public void nodePre() {
    final Function func = _DB_NODE_PRE;
    query(func.args(" /html"), 1);
    query(func.args(" / | /html"), "0\n1");
  }

  /** Test method. */
  @Test public void open() {
    final Function func = _DB_OPEN;
    query("count(" + func.args(NAME) + ")", 1);
    query("count(" + func.args(NAME, "") + ")", 1);
    query("count(" + func.args(NAME, "unknown") + ")", 0);

    // close database instance
    execute(new Close());
    query("count(" + func.args(NAME, "unknown") + ")", 0);
    query(func.args(NAME) + "//title/text()", "XML");

    // reference invalid path
    if(Prop.WIN) error(func.args(NAME, "*"), RESINV_X);

    // run function on non-existing database
    execute(new DropDB(NAME));
    error(func.args(NAME), DB_OPEN2_X);
  }

  /** Test method. */
  @Test public void openId() {
    final Function func = _DB_OPEN_ID;
    query(func.args(NAME, " ()"), "");
    query(func.args(NAME, 0) + "//title/text()", "XML");
    query(func.args(NAME, " (0,1)") + "//title/text()", "XML");
    error(func.args(NAME, -1), DB_RANGE_X_X);
    error(func.args(NAME, Integer.MAX_VALUE), DB_RANGE_X_X);
  }

  /** Test method. */
  @Test public void openPre() {
    final Function func = _DB_OPEN_PRE;
    query(func.args(NAME, " ()"), "");
    query(func.args(NAME, 0) + "//title/text()", "XML");
    query(func.args(NAME, " (0,1)") + "//title/text()", "XML");
    error(func.args(NAME, -1), DB_RANGE_X_X);
    error(func.args(NAME, Integer.MAX_VALUE), DB_RANGE_X_X);
  }

  /** Test method. */
  @Test public void optimize() {
    final Function func = _DB_OPTIMIZE;

    // simple optimize call
    query(func.args(NAME));
    query(func.args(NAME));
    // opened database cannot be fully optimized
    error(func.args(NAME, true), UPDBERROR_X);
    execute(new Close());
    query(func.args(NAME, true));

    // commands
    final CmdIndex[] cis = { CmdIndex.TEXT, CmdIndex.ATTRIBUTE, CmdIndex.TOKEN, CmdIndex.FULLTEXT };
    // options
    final String[] indexes = lc(MainOptions.TEXTINDEX, MainOptions.ATTRINDEX,
        MainOptions.TOKENINDEX, MainOptions.FTINDEX);
    final String[] includes = lc(MainOptions.TEXTINCLUDE, MainOptions.ATTRINCLUDE,
        MainOptions.TOKENINCLUDE, MainOptions.FTINCLUDE);
    final String[] boolOptions = new StringList(indexes).add(lc(MainOptions.STEMMING,
        MainOptions.CASESENS, MainOptions.DIACRITICS)).finish();
    final String[] stringOptions = lc(MainOptions.LANGUAGE, MainOptions.STOPWORDS);
    final String[] numberOptions = lc(MainOptions.MAXCATS, MainOptions.MAXLEN,
        MainOptions.SPLITSIZE);

    // check single options
    for(final String option : numberOptions)
      query(func.args(NAME, false, " map { '" + option + "': 1 }"));
    for(final String option : boolOptions) {
      for(final boolean bool : new boolean[] { true, false })
        query(func.args(NAME, false, " map { '" + option + "':" + bool + "() }"));
    }
    for(final String option : stringOptions)
      query(func.args(NAME, false, " map { '" + option + "':'' }"));
    // ensure that option in context was not changed
    assertEquals(context.options.get(MainOptions.TEXTINDEX), true);

    // check invalid options
    error(func.args(NAME, false, " map { 'xyz': 'abc' }"), BASEX_OPTIONS1_X);
    error(func.args(NAME, false, " map { '" + lc(MainOptions.UPDINDEX) + "': 1 }"),
        BASEX_OPTIONS1_X);
    error(func.args(NAME, false, " map { '" + lc(MainOptions.MAXLEN) + "': -1 }"),
        BASEX_OPTIONS_X_X);
    error(func.args(NAME, false, " map { '" + lc(MainOptions.MAXLEN) + "': 'a' }"),
        BASEX_OPTIONS_X_X);
    error(func.args(NAME, false, " map { '" + lc(MainOptions.TEXTINDEX) + "':'nope' }"),
        BASEX_OPTIONS_X_X);

    // check if optimize call adopts original options
    query(func.args(NAME));
    for(final String ind : indexes) query(_DB_INFO.args(NAME) + "//" + ind + "/text()", false);
    for(final String inc : includes) query(_DB_INFO.args(NAME) + "//" + inc + "/text()", "");

    // check if options in context are adopted
    execute(new Open(NAME));
    for(final String inc : includes) execute(new Set(inc, "a"));
    for(final CmdIndex ci : cis) execute(new CreateIndex(ci));
    execute(new Close());
    query(func.args(NAME));
    for(final String ind : indexes) query(_DB_INFO.args(NAME) + "//" + ind + "/text()", true);
    for(final String inc : includes) query(_DB_INFO.args(NAME) + "//" + inc + "/text()", "a");

    // check if options in context are adopted, even if database is closed (reset options)
    execute(new Open(NAME));
    for(final String inc : includes) execute(new Set(inc, ""));
    for(final CmdIndex cmd : cis) execute(new DropIndex(cmd));
    for(final String ind : indexes) query(_DB_INFO.args(NAME) + "//" + ind + "/text()", false);
    for(final String inc : includes) query(_DB_INFO.args(NAME) + "//" + inc + "/text()", "");
    execute(new Close());
    query(func.args(NAME));
    for(final String ind : indexes) query(_DB_INFO.args(NAME) + "//" + ind + "/text()", false);
    for(final String inc : includes) query(_DB_INFO.args(NAME) + "//" + inc + "/text()", "");

    // check if options specified in map are adopted
    final StringBuilder options = new StringBuilder();
    for(final String option : indexes) options.append('\'').append(option).append("':true(),");
    for(final String option : includes) options.append('\'').append(option).append("':'a',");
    options.append('\'').append(lc(MainOptions.UPDINDEX)).append("':true()");
    query(func.args(NAME, true, " map { " + options + '}'));

    for(final String ind : indexes) query(_DB_INFO.args(NAME) + "//" + ind + "/text()", true);
    for(final String inc : includes) query(_DB_INFO.args(NAME) + "//" + inc + "/text()", "a");
    query(_DB_INFO.args(NAME) + "//" + lc(MainOptions.UPDINDEX) + "/text()", true);
  }

  /** Test method. */
  @Test public void option() {
    final Function func = _DB_OPTION;
    query(func.args("addraw"), false);
    query(func.args("ADDRAW"), false);
    query(func.args("runs"), 1);
    query(func.args("bindings"), "");
    error(func.args("XYZ"), DB_OPTION_X);
  }

  /** Test method. */
  @Test public void path() {
    final Function func = _DB_PATH;
    query(func.args(_DB_OPEN.args(NAME)), XML.replaceAll(".*/", ""));
    query(func.args(_DB_OPEN.args(NAME) + "/*"), XML.replaceAll(".*/", ""));
    query(func.args(" <x/> update ()"), "");
  }

  /** Test method. */
  @Test public void property() {
    final Function func = _DB_PROPERTY;
    query(func.args(NAME, "name"), NAME);
    error(func.args(NAME, "xyz"), DB_PROPERTY_X);
  }

  /** Test method. */
  @Test public void rename() {
    final Function func = _DB_RENAME;

    execute(new Add("test/docs", FLDR));
    query("count(" + COLLECTION.args(NAME + "/test") + ")", XMLFILES);

    // rename document
    query(func.args(NAME, "test", "newtest"));
    query("count(" + COLLECTION.args(NAME + "/test") + ")", 0);
    query("count(" + COLLECTION.args(NAME + "/newtest") + ")", XMLFILES);

    // invalid target
    error(func.args(NAME, "input.xml", " ''"), DB_PATH_X);
    error(func.args(NAME, "input.xml", " '/'"), DB_PATH_X);
    error(func.args(NAME, "input.xml", " '.'"), DB_PATH_X);

    // rename paths
    query(func.args(NAME, "", "x"));
    query("count(" + COLLECTION.args(NAME + "/x/newtest") + ")", XMLFILES);

    // rename binary file
    query(_DB_STORE.args(NAME, "file1", ""));
    query(func.args(NAME, "file1", "file2"));
    query(_DB_RETRIEVE.args(NAME, "file2"));
    error(_DB_RETRIEVE.args(NAME, "file1"), WHICHRES_X);

    query(func.args(NAME, "file2", "dir1/file3"));
    query(_DB_RETRIEVE.args(NAME, "dir1/file3"));
    query(func.args(NAME, "dir1", "dir2"));
    query(_DB_RETRIEVE.args(NAME, "dir2/file3"));
    error(_DB_RETRIEVE.args(NAME, "dir1"), WHICHRES_X);

    query(_DB_STORE.args(NAME, "file4", ""));
    query(_DB_STORE.args(NAME, "dir3/file5", ""));

    error(func.args(NAME, "dir2", "file4"), DB_PATH_X);
    error(func.args(NAME, "file4", "dir2"), DB_PATH_X);

    // move files in directories
    query(func.args(NAME, "dir2", "dir3"));
    query(_DB_RETRIEVE.args(NAME, "dir3/file3"));
    query(_DB_RETRIEVE.args(NAME, "dir3/file5"));
  }

  /** Test method. */
  @Test public void replace() {
    final Function func = _DB_REPLACE;

    execute(new Add("test", XML));

    query(func.args(NAME, XML, " <R1/>"));
    query("count(" + COLLECTION.args(NAME + '/' + XML) + "/R1)", 1);
    query("count(" + COLLECTION.args(NAME + '/' + XML) + "/R2)", 0);

    query(func.args(NAME, XML, " document { <R2/> }"));
    query("count(" + COLLECTION.args(NAME + '/' + XML) + "/R1)", 0);
    query("count(" + COLLECTION.args(NAME + '/' + XML) + "/R2)", 1);

    query(func.args(NAME, XML, XML));
    query("count(" + COLLECTION.args(NAME + '/' + XML) + "/R1)", 0);
    query("count(" + COLLECTION.args(NAME + '/' + XML) + "/R2)", 0);
    query("count(" + COLLECTION.args(NAME + '/' + XML) + "/html)", 1);

    final String addcache = " map { 'addcache': true() }";
    query(func.args(NAME, "1.xml", " <cache/>", addcache));
    query("exists(" + _DB_OPEN.args(NAME, "1.xml") + ")", true);
    query(func.args(NAME, "2.xml", " <cache/>", addcache));
    query("exists(" + _DB_OPEN.args(NAME, "2.xml") + ")", true);
    query(func.args(NAME, "3.xml", XML, addcache));
    query("exists(" + _DB_OPEN.args(NAME, "3.xml") + ")", true);

    // GH-1302: replace same target more than once
    error("(1 to 2) ! " + func.args(NAME, "3.xml", XML), UPMULTDOC_X_X);
    error("(1 to 2) ! " + func.args(NAME, "X.xml", XML), UPMULTDOC_X_X);
    error(_DB_ADD.args(NAME, XML, "X.xml") + ',' + func.args(NAME, "X.xml", XML),
        UPMULTDOC_X_X);
    error(func.args(NAME, "X.xml", XML) + ',' + _DB_ADD.args(NAME, XML, "X.xml"),
        UPMULTDOC_X_X);

    // GH-1302: check replacements of existing and new paths
    query(func.args(NAME, "3.xml", XML) + ',' + func.args(NAME, "4.xml", XML));
    query(func.args(NAME, "3.xml", " <a/>") + ',' + func.args(NAME, "5.xml", " <a/>"));

    // GH-1302: check replacements of new paths
    query(func.args(NAME, "6.xml", XML) + ',' + func.args(NAME, "7.xml", XML));
    query(func.args(NAME, "8.xml", " <a/>") + ',' + func.args(NAME, "9.xml", " <a/>"));
  }

  /** Test method. */
  @Test public void restore() {
    final Function func = _DB_RESTORE;
    execute(new Close());

    // backup and restore file
    query(_DB_CREATE_BACKUP.args(NAME));
    query(func.args(NAME));
    query(func.args(NAME));

    // drop backups
    query(_DB_DROP_BACKUP.args(NAME));
    error(func.args(NAME), DB_NOBACKUP_X);

    // invalid names
    error(func.args(" ''"), DB_NAME_X);
  }

  /** Test method. */
  @Test public void retrieve() {
    final Function func = _DB_RETRIEVE;
    error(func.args(NAME, "raw"), WHICHRES_X);
    query(_DB_STORE.args(NAME, "raw", " xs:hexBinary('41')"));
    query("xs:hexBinary(" + func.args(NAME, "raw") + ')', "A");
    query(_DB_DELETE.args(NAME, "raw"));
    error(func.args(NAME, "raw"), WHICHRES_X);
  }

  /** Test method. */
  @Test public void store() {
    final Function func = _DB_STORE;
    query(func.args(NAME, "raw1", "xs:hexBinary('41')"));
    query(func.args(NAME, "raw2", "b"));
    query(_DB_RETRIEVE.args(NAME, "raw2"), "b");
    query(func.args(NAME, "raw3", 123));
    query(_DB_RETRIEVE.args(NAME, "raw3"), 123);
  }

  /** Test method. */
  @Test public void system() {
    final Function func = _DB_SYSTEM;
    contains(func.args(), Prop.VERSION);
  }

  /** Test method. */
  @Test public void text() {
    // run function without and with index
    final Function func = _DB_TEXT;
    execute(new DropIndex(CmdIndex.TEXT));
    error(func.args(NAME, "XML"), DB_NOINDEX_X_X);

    execute(new CreateIndex(CmdIndex.TEXT));
    query(func.args(NAME, "XML"), "XML");
    query(func.args(NAME, "XXX"), "");
  }

  /** Test method. */
  @Test public void textRange() {
    // run function without and with index
    final Function func = _DB_TEXT_RANGE;
    execute(new DropIndex(CmdIndex.TEXT));
    error(func.args(NAME, "Exercise", "Fun"), DB_NOINDEX_X_X);

    execute(new CreateIndex(CmdIndex.TEXT));
    query(func.args(NAME, "Exercise", "Fun"), "Exercise 1\nExercise 2");
    query(func.args(NAME, "XXX", "XXX"), "");
  }

  /** Test method. */
  @Test public void token() {
    // run function without and with index
    final Function func = _DB_TOKEN;
    execute(new DropIndex(CmdIndex.TOKEN));
    error("data(" + func.args(NAME, "0") + ")", DB_NOINDEX_X_X);

    execute(new CreateIndex(CmdIndex.TOKEN));
    query("data(" + func.args(NAME, "0") + ")", 0);
    query("data(" + func.args(NAME, "0", "id") + ")", 0);
    query("data(" + func.args(NAME, "0", "XXX") + ")", "");
    query("data(" + func.args(NAME, "XXX") + ")", "");
  }

  /**
   * Returns a lower-case representation of the specified option.
   * @param option option
   * @return string
   */
  private static String lc(final Option<?> option) {
    return option.name().toLowerCase(Locale.ENGLISH);
  }

  /**
   * Returns lower-case representations of the specified options.
   * @param options options
   * @return string
   */
  private static String[] lc(final Option<?>... options) {
    final StringList sl = new StringList();
    for(final Option<?> option : options) sl.add(lc(option));
    return sl.finish();
  }
}

package org.basex.data;

import static org.basex.util.Token.*;

/**
 * This class assembles texts which are used in the data classes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public interface DataText {
  // META DATA ================================================================

  /** Database version; if it's modified, old database instances can't
   * be parsed anymore. */
  String STORAGE = "5.9";
  /** Index version; if it's modified, old indexes can't be parsed anymore. */
  String ISTORAGE = "5.9";

  /** Database version. */
  String DBSTR = "STORAGE";
  /** Database version. */
  String IDBSTR = "ISTORAGE";
  /** Last modification time. */
  String DBTIME = "TIME";
  /** Tree height. */
  String DBHGHT = "HEIGHT";
  /** Number of XML nodes. */
  String DBSIZE = "SIZE";
  /** File name. */
  String DBFNAME = "FNAME";
  /** File size. */
  String DBFSIZE = "FSIZE";
  /** Number of XML documents. */
  String DBNDOCS = "NDOCS";
  /** Encoding. */
  String DBENC = "ENCODING";
  /** Whitespace chopping. */
  String DBCHOP = "CHOPPED";
  /** Entity parsing. */
  String DBENTITY = "ENTITY";
  /** Path indexing. */
  String DBPTHIDX = "PTHINDEX";
  /** Text indexing. */
  String DBTXTIDX = "TXTINDEX";
  /** Attribute indexing. */
  String DBATVIDX = "ATVINDEX";
  /** Full-text indexing. */
  String DBFTXIDX = "FTXINDEX";
  /** Full-text wildcards indexing. */
  String DBWCIDX = "WCINDEX";
  /** Full-text stemming. */
  String DBFTST = "FTSTEM";
  /** Full-text case sensitivity. */
  String DBFTCS = "FTCS";
  /** Full-text diacritics removal. */
  String DBFTDC = "FTDC";
  /** Maximum scoring value. */
  String DBSCMAX = "FTSCMAX";
  /** Minimum scoring value. */
  String DBSCMIN = "FTSCMIN";
  /** Maximal indexed full-text score. */
  String DBSCTYPE = "FTSCTYPE";
  /** Up-to-date flag. */
  String DBUTD = "UPTODATE";
  /** Last (highest) id. */
  String DBLID = "LASTID";
  /** FS Mount point. */
  String DBMNT = "MOUNT";
  /** Permissions. */
  String DBPERM = "PERM";
  /** FS Backing store. */
  String DBBCK = "BACKING";
  /** DeepFS instance. */
  String DBDEEPFS = "DEEPFS";

  /** Tags. */
  String DBTAGS = "TAGS";
  /** Attributes. */
  String DBATTS = "ATTS";
  /** Path Summary. */
  String DBPATH = "PATH";
  /** Tags. */
  String DBNS = "NS";

  // DEEPFS STRINGS ===========================================================

  /** DeepFS tag. */
  String S_DEEPFS = "deepfs";
  /** Directory tag. */
  String S_DIR = "dir";
  /** File tag. */
  String S_FILE = "file";
  /** Time of last modification. */
  String S_NAME = "name";
  /** Time of last modification. */
  String S_SIZE = "size";
  /** Time of last modification. */
  String S_ATIME = "atime";
  /** Time of last modification. */
  String S_CTIME = "ctime";
  /** Time of last modification. */
  String S_MTIME = "mtime";
  /** Time of last modification. */
  String S_SUFFIX = "suffix";

  /** DeepFS token. */
  byte[] DEEPFS = token(S_DEEPFS);
  /** Directory token. */
  byte[] DIR = token(S_DIR);
  /** File token. */
  byte[] FILE = token(S_FILE);
  /** Content tag. */
  byte[] CONTENT = token("content");
  /** Text content tag. */
  byte[] TEXT_CONTENT = token("text");
  /** XML content tag. */
  byte[] XML_CONTENT = token("xml");

  /** Name attribute token. */
  byte[] NAME = token(S_NAME);
  /** Size attribute token. */
  byte[] SIZE = token(S_SIZE);
  /** Time of last modification token. */
  byte[] MTIME = token(S_MTIME);
  /** Suffix attribute. */
  byte[] SUFFIX = token(S_SUFFIX);
  /** Time of last access token. */
  byte[] ATIME = token(S_ATIME);
  /** Time of creation token. */
  byte[] CTIME = token(S_CTIME);
  /** Number of links token. */
  byte[] NLINK = token("nlink");
  /** User ID token. */
  byte[] UID = token("uid");
  /** Group ID token. */
  byte[] GID = token("gid");
  /** Offset attribute. */
  byte[] OFFSET = token("offset");
  /** File mode attribute. */
  byte[] MODE = token("mode");
  /** Mount point attribute. */
  byte[] MOUNTPOINT = token("mountpoint");
  /** Backing store attribute. */
  byte[] BACKINGSTORE = token("backingstore");
  /** Negative mount point attribute. */
  byte[] NOTMOUNTED = token("(not mounted)");

  // XML SERIALIZATION ========================================================

  /** Ampersand Entity. */
  byte[] E_AMP = token("&amp;");
  /** Quote Entity. */
  byte[] E_QU = token("&quot;");
  /** GreaterThan Entity. */
  byte[] E_GT = token("&gt;");
  /** LessThan Entity. */
  byte[] E_LT = token("&lt;");
  /** Tab Entity. */
  byte[] E_TAB = token("&#x9;");
  /** NewLine Entity. */
  byte[] E_NL = token("&#xA;");
  /** CarriageReturn Entity. */
  byte[] E_CR = token("&#xD;");

  /** Results tag. */
  byte[] RESULTS = token("results");
  /** Result tag. */
  byte[] RESULT = token("result");
  /** Path tag. */
  byte[] PATH = token("path");
  /** Node tag. */
  byte[] NODE = token("node");
  /** Kind attribute. */
  byte[] KIND = token("kind");
  /** Count attribute. */
  byte[] COUNT = token("count");

  /** Document declaration. */
  byte[] DOCDECL = token("xml version='1.0' encoding='");
  /** Doctype output. */
  byte[] DOCTYPE = token("<!DOCTYPE");
  /** Doctype system keyword. */
  byte[] SYSTEM = token("SYSTEM");
  /** Comment output. */
  byte[] COM1 = token("<!--");
  /** Comment output. */
  byte[] COM2 = token("-->");
  /** PI output. */
  byte[] PI1 = token("<?");
  /** PI output. */
  byte[] PI2 = token("?>");

  /** Element output. */
  byte[] ELEM1 = { '<' };
  /** Element output. */
  byte[] ELEM2 = { '>' };
  /** Element output. */
  byte[] ELEM3 = token("</");
  /** Element output. */
  byte[] ELEM4 = token("/>");
  /** Attribute output. */
  byte[] ATT1 = token("=\"");
  /** Attribute output. */
  byte[] ATT2 = token("\"");
  /** Document. */
  byte[] DOC = token("doc()");
  /** Text. */
  byte[] TEXT = token("text()");
  /** Comment. */
  byte[] COMM = token("comment()");
  /** Processing instruction. */
  byte[] PI = token("processing-instruction()");
  /** Attribute output. */
  byte[] ATT = { '@' };

  // TABLE SERIALIZATION ======================================================

  /** First table Header. */
  byte[] TABLEID = token("ID");
  /** First table Header. */
  byte[] TABLEPRE = token("PRE");
  /** Second table Header. */
  byte[] TABLEDIST = token("DIS");
  /** Third table Header. */
  byte[] TABLESIZE = token("SIZ");
  /** Fourth table Header. */
  byte[] TABLEATS = token("ATS");
  /** Fifth table Header. */
  byte[] TABLEKND = token("KIND");
  /** Sixth table Header. */
  byte[] TABLECON = token("CONTENT");

  /** Namespace header. */
  byte[] TABLENS = token("NS");
  /** Prefix header. */
  byte[] TABLEPREF = token("PREFIX");
  /** URI header. */
  byte[] TABLEURI = token("NAMESPACE");
  /** Table Kinds. */
  byte[][] TABLEKINDS = {
      { 'D', 'O', 'C', ' ' }, { 'E', 'L', 'E', 'M' },
      { 'T', 'E', 'X', 'T' }, { 'A', 'T', 'T', 'R' },
      { 'C', 'O', 'M', 'M' }, { 'P', 'I', ' ', ' ' }
  };

  // DATABASE FILES ===========================================================

  /** Database - Info. */
  String DATAINFO = "inf";
  /** Database - Tokens. */
  String DATATBL = "tbl";
  /** Database - Temporary Size References. */
  String DATATMP = "tmp";
  /** Database - Text index. */
  String DATATXT = "txt";
  /** Database - Attribute value index. */
  String DATAATV = "atv";
  /** Database - Full-text index. */
  String DATAFTX = "ftx";
  /** Database - Stopword list. */
  String DATASWL = "swl";
}

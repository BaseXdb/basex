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
  String STORAGE = "5.6";
  /** Index version; if it's modified, old indexes can't be parsed anymore. */
  String ISTORAGE = "5.6";

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
  /** Full-text fuzzy indexing. */
  String DBFZIDX = "FZINDEX";
  /** Full-text stemming. */
  String DBFTST = "FTSTEM";
  /** Full-text case sensitivity. */
  String DBFTCS = "FTCS";
  /** Full-text diacritics removal. */
  String DBFTDC = "FTDC";
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
  /** Unknown tag (place holder). */
  byte[] UNKNOWN = token("unknown");

  /** Name attribute token. */
  byte[] NAME = token(S_NAME);
  /** Size attribute token. */
  byte[] SIZE = token(S_SIZE);
  /** Time of last modification token. */
  byte[] MTIME = token(S_MTIME);
  /** Suffix attribute. */
  byte[] SUFFIX = token(S_SUFFIX);
  /** Time of last access token. */
  byte[] ATIME = token("atime");
  /** Time of creation token. */
  byte[] CTIME = token("ctime");
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
  /** Doctype output. */
  byte[] DT = token("<!DOCTYPE");
  /** Doctype system keyword. */
  byte[] SYS = token("SYSTEM");
  /** Comment output. */
  byte[] COM1 = token("<!--");
  /** Comment output. */
  byte[] COM2 = token("-->");
  /** PI output. */
  byte[] PI1 = token("<?");
  /** PI output. */
  byte[] PI2 = token("?>");

  /** Element output. */
  byte[] ELEM1 = { '<'};
  /** Element output. */
  byte[] ELEM2 = { '>'};
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
  byte[] ATT = { '@'};

  // TABLE SERIALIZATION ======================================================

  /** First table Header. */
  String TABLEPRE = "PRE";
  /** Second table Header. */
  String TABLEDIST = "DIS";
  /** Third table Header. */
  String TABLESIZE = "SIZ";
  /** Fourth table Header. */
  String TABLEATS = "ATS";
  /** Namespace header. */
  String TABLENS = "NS";
  /** Prefix header. */
  String TABLEPREF = "PREFIX";
  /** URI header. */
  String TABLEURI = "NAMESPACE";
  /** Table Kinds. */
  String[] TABLEKINDS = { "DOC ", "ELEM", "TEXT", "ATTR", "COMM", "PI  "};

  /** Fifth table Header. */
  String TABLEKND = "KIND";
  /** Fifth table Header. */
  String TABLECON = "CONTENT";
  
  
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
}

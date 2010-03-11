package org.basex.data;

import static org.basex.util.Token.*;

/**
 * This class assembles texts which are used in the data classes.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public interface DataText {
  // META DATA ================================================================

  /** Database version; if it's modified, old database instances can't
   * be parsed anymore. */
  String STORAGE = "5.91";
  /** Index version; if it's modified, old indexes can't be parsed anymore. */
  String ISTORAGE = "5.91";

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
  /** Name tag. */
  byte[] NAME = token("name");
  /** Node tag. */
  byte[] NODE = token("node");
  /** Kind attribute. */
  byte[] KIND = token("kind");
  /** Size tag. */
  byte[] SIZE = token("size");

  /** Document declaration. */
  byte[] DOCDECL1 = token("xml version='");
  /** Document declaration. */
  byte[] DOCDECL2 = token("' encoding='");
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

  /** Version error. */
  String SERVERSION = "XML Version must be '1.0' or '1.1'.";
  /** Version error. */
  String SERENCODING = "Unknown encoding: '%'";

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
  byte[] TABLEPREF = token("PREF");
  /** URI header. */
  byte[] TABLEURI = token("URI");
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

package org.basex.data;

import static org.basex.util.Token.*;

import org.basex.core.Prop;

/**
 * This class assembles texts which are used in the data classes.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public interface DataText {
  // META DATA ================================================================

  /** Database version; if it's modified, old database instances can't
   * be parsed anymore. */
  String STORAGE = "5.02";
  /** Index version; if it's modified, old indexes can't
   * be parsed anymore. */
  String ISTORAGE = "3";

  /** Database version. */
  String DBSTORAGE = "STORAGE";
  /** Database version. */
  String IDBSTORAGE = "ISTORAGE";
  /** Last modification time. */
  String DBTIME = "TIME";
  /** Tree height. */
  String DBHEIGHT = "HEIGHT";
  /** Number of XML nodes. */
  String DBSIZE = "SIZE";
  /** File name. */
  String DBFNAME = "FNAME";
  /** File size. */
  String DBFSIZE = "FSIZE";
  /** Number of XML documents. */
  String DBNDOCS = "NDOCS";
  /** Encoding. */
  String DBENCODING = "ENCODING";
  /** Whitespace chopping. */
  String DBCHOPPED = "CHOPPED";
  /** Entity parsing. */
  String DBENTITY = "ENTITY";
  /** Text indexing. */
  String DBTXTINDEX = "TXTINDEX";
  /** Attribute indexing. */
  String DBATVINDEX = "ATVINDEX";
  /** Word indexing. */
  String DBWRDINDEX = "WRDINDEX";
  /** Fulltext indexing. */
  String DBFTXINDEX = "FTXINDEX";
  /** Fuzzy indexing. */
  String DBFZINDEX = "FZINDEX";
  /** Fulltext stemming. */
  String DBFTSTEM = "FTSTEM";
  /** Fulltext case sensitivity. */
  String DBFTCS = "FTCS";
  /** Fulltext diacritics removal. */
  String DBFTDC = "FTDC";
  /** Up-to-date flag. */
  String DBUPTODATE = "UPTODATE";
  /** Last (highest) id. */
  String DBLASTID = "LASTID";

  // TAGS/ATTRIBUTE NAMES ====================================================

  /** DeepFS tag. */
  String DEEPFS = "deepfs";
  /** Directory tag. */
  byte[] DIR = token("dir");
  /** File tag. */
  byte[] FILE = token("file");
  /** Name attribute. */
  byte[] NAME = token("name");
  /** Size attribute. */
  byte[] SIZE = token("size");
  /** Size attribute. */
  byte[] STAT = token("stat");
  /** Time of last modification. */
  byte[] MTIME = token("mtime");
  /** Suffix attribute. */
  byte[] SUFFIX = token("suffix");
  /** Suffix attribute. */
  byte[] CONTENT = token("content");

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

  /** Opening results tag. */
  byte[] RESULTS = token("results");
  /** Opening result tag. */
  byte[] RESULT = token("result");
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
  /** Text step. */
  byte[] TEXT = token("text()");
  /** Text step. */
  byte[] COMM = token("comment()");
  /** Text step. */
  byte[] PI = token("processing-instruction()");
  /** Attribute output. */
  byte[] ATT = { '@' };

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
  /** Fifth table Header. */
  String TABLEKIND = "  KIND  CONTENT" + Prop.NL;
  /** Table Kinds. */
  String[] TABLEKINDS = { "DOC ", "ELEM", "TEXT", "ATTR", "COMM", "PI  " };

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
  /** Database - Fulltext index. */
  String DATAFTX = "ftx";

  // DEEPFS STRINGS ===========================================================
  /** Name of the current deepfs shared library. */
  String DEEPLIB = "deepstorage";
}

package org.basex.data;

import static org.basex.util.Token.*;
import org.basex.core.Prop;
import org.basex.util.Token;

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
  String STORAGE = "6";
  /** Index version; if it's modified, old indexes can't
   * be parsed anymore. */
  String ISTORAGE = "2";

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
  /** Last (highest) id. */
  String DBLASTID = "LASTID";
  
  // TAGS/ATTRIBUTE NAMES ====================================================

  /** DeepFS tag. */
  byte[] DEEPFS = token("deepfs");
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
  /** XMLNS attribute. */
  byte[] XMLNS = token("xmlns");
  /** XMLNS attribute. */
  byte[] XMLNSC = token("xmlns:");

  // XML SERIALIZATION ========================================================

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
  String TABLEHEAD1 = "PRE";
  /** Second table Header. */
  String TABLEHEAD2 = "DIS";
  /** Third table Header. */
  String TABLEHEAD3 = "SIZ";
  /** Fourth table Header. */
  String TABLEHEAD4 = "ATS";
  /** Fifth table Header. */
  String TABLEHEAD5 = "  KIND  CONTENT" + Prop.NL;
  /** Table Document. */
  String TABLEDOC = "  DOC ";
  /** Table Element. */
  String TABLEELEM = "  ELEM";
  /** Table Text. */
  String TABLETEXT = "  TEXT";
  /** Table Text. */
  String TABLEATTR = "  ATTR";
  /** Table Comment. */
  String TABLECOMM = "  COMM";
  /** Table Text. */
  String TABLEPI = "  PI  ";

  // DATABASE FILES ===========================================================

  /** Database - Info. */
  String DATAINFO = "inf";
  /** Database - Tokens. */
  String DATATBL = "tbl";
  /** Database - Temporary Size References. */
  String DATATMP = "tmp";
  /** Database - Tag index. */
  String DATATAG = "tag";
  /** Database - Attribute name index. */
  String DATAATN = "atn";
  /** Database - Text index. */
  String DATATXT = "txt";
  /** Database - Attribute value index. */
  String DATAATV = "atv";
  /** Database - Word index. */
  String DATAWRD = "wrd";
  /** Database - Fulltext index. */
  String DATAFTX = "ftx";
  /** Database - Documents statistic. */
  String DATASTAT = "sta";
  /** Database - Namespaces. */
  String DATANS = "nsp";

  // Document Statistics ======================================================

  /** Statistics - Integer value. */
  byte[] STATINT = Token.token("INT");
  /** Statistics - Double value. */
  byte[] STATDBL = Token.token("DBL");
  /** Statistics - TEXT/NONE. */
  byte[] STATNONE = Token.token("NONE");
  /** Statistics - TEXT/NONE. */
  byte[] STATTEXT = Token.token("TEXT");
  /** Statistics - Categories. */
  byte[] STATCAT = Token.token("CAT");
}


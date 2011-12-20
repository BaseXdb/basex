package org.basex.data;

import static org.basex.util.Token.*;

/**
 * This class assembles texts which are used in the data classes.
 *
 * @author BaseX Team 2005-11, BSD License
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
  /** Number of nodes. */
  String DBSIZE = "SIZE";
  /** Path to original document. */
  String DBFNAME = "FNAME";
  /** File size. */
  String DBFSIZE = "FSIZE";
  /** Number of documents. */
  String DBNDOCS = "NDOCS";
  /** Encoding. */
  String DBENC = "ENCODING";
  /** Whitespace chopping. */
  String DBCHOP = "CHOPPED";
  /** Path indexing. */
  String DBPTHIDX = "PTHINDEX";
  /** Automatic index update. */
  String DBUPDIDX = "UPDINDEX";
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
  /** Full-text language. */
  String DBFTLN = "FTLANG";
  /** Full-text case sensitivity. */
  String DBFTCS = "FTCS";
  /** Full-text diacritics removal. */
  String DBFTDC = "FTDC";
  /** Maximum scoring value. */
  String DBSCMAX = "FTSCMAX";
  /** Minimum scoring value. */
  String DBSCMIN = "FTSCMIN";
  /** Maximum indexed full-text score. */
  String DBSCTYPE = "FTSCTYPE";
  /** Maximum token length. */
  String DBMAXLEN = "MAXLEN";
  /** Maximum number of categories. */
  String DBMAXCATS = "MAXCATS";
  /** Up-to-date flag. */
  String DBUPTODATE = "UPTODATE";
  /** Last (highest) id. */
  String DBLASTID = "LASTID";
  /** Permissions. */
  String DBPERM = "PERM";
  /** Documents. */
  String DBDOCS = "DOCS";
  /** Path indexing. */
  String DBCRTPTH = "CRTPTH";
  /** Text indexing. */
  String DBCRTTXT = "CRTTXT";
  /** Attribute indexing. */
  String DBCRTATV = "CRTATV";
  /** Full-text indexing. */
  String DBCRTFTX = "CRTFTX";

  /** Tags. */
  String DBTAGS = "TAGS";
  /** Attributes. */
  String DBATTS = "ATTS";
  /** Path summary. */
  String DBPATH = "PATH";
  /** Namespace. */
  String DBNS = "NS";

  // DATABASE FILES ===========================================================

  /** Database - Info. */
  String DATAINF = "inf";
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
  /** Database - Updating flag. */
  String DATAUPD = "upd";

  // XML SERIALIZATION ========================================================

  /** Omit flag. */
  String OMIT = "omit";

  /** NL flag. */
  String S_NL = "\\n";
  /** CR flag. */
  String S_CR = "\\r";
  /** CRNL flag. */
  String S_CRNL = "\\r\\n";

  /** Version. */
  String V10 = "1.0";
  /** Version. */
  String V11 = "1.1";
  /** Version. */
  String V40 = "4.0";
  /** Version. */
  String V401 = "4.01";

  /** Method. */
  String M_CSV = "csv";
  /** Method. */
  String M_MAB2 = "mab2";

  /** Method. */
  String M_XML = "xml";
  /** Method. */
  String M_XHTML = "xhtml";
  /** Method. */
  String M_HTML = "html";
  /** Method. */
  String M_TEXT = "text";
  /** Method. */
  String M_JSON = "json";
  /** Method. */
  String M_JSONML = "jsonml";
  /** Method. */
  String M_RAW = "raw";

  /** Normalization. */
  String NFC = "NFC";
  /** Normalization. */
  String NONE = "none";

  /** Document declaration. */
  String DOCDECL1 = "xml version=\"";
  /** Document declaration. */
  String DOCDECL2 = "\" encoding=\"";
  /** Document declaration. */
  String DOCDECL3 = "\" standalone=\"";

  /** Doctype output. */
  String DOCTYPE = "<!DOCTYPE ";
  /** Doctype system keyword. */
  String SYSTEM = "SYSTEM";
  /** Doctype public keyword. */
  String PUBLIC = "PUBLIC";

  /** Content-Type. */
  String CONTENT_TYPE = "Content-Type";

  /** Ampersand entity. */
  byte[] E_AMP = token("&amp;");
  /** Quote entity. */
  byte[] E_QU = token("&quot;");
  /** GreaterThan entity. */
  byte[] E_GT = token("&gt;");
  /** LessThan entity. */
  byte[] E_LT = token("&lt;");
  /** HTML: Non-breaking space entity. */
  byte[] E_NBSP = token("&nbsp;");

  /** Token: results. */
  byte[] RESULTS = token("results");
  /** Token: result. */
  byte[] RESULT = token("result");
  /** Token: name. */
  byte[] NAME = token("name");
  /** Token: size. */
  byte[] SIZE = token("size");
  /** Token: min. */
  byte[] MIN = token("min");
  /** Token: max. */
  byte[] MAX = token("max");
  /** Token: count. */
  byte[] COUNT = token("count");
  /** Token: index. */
  byte[] INDEX = token("index");
  /** Token: key. */
  byte[] KEY = token("key");

  /** Token: json. */
  byte[] JSON = token("json");
  /** Token: type. */
  byte[] TYPE = token("type");
  /** Token: value. */
  byte[] VALUE = token("value");

  /** Token: string. */
  byte[] STR = token("string");
  /** Token: number. */
  byte[] NUM = token("number");
  /** Token: boolean. */
  byte[] BOOL = token("boolean");
  /** Token: array. */
  byte[] ARR = token("array");
  /** Token: object. */
  byte[] OBJ = token("object");

  /** Comment output. */
  byte[] COMM_O = token("<!--");
  /** Comment output. */
  byte[] COMM_C = token("-->");
  /** XQuery comment. */
  byte[] XQCOMM_O = token("(:");
  /** XQuery comment. */
  byte[] XQCOMM_C = token(":)");

  /** PI output. */
  byte[] PI_O = token("<?");
  /** PI output. */
  byte[] PI_C = token("?>");

  /** Element output. */
  byte[] ELEM_O = { '<' };
  /** Element output. */
  byte[] ELEM_C = { '>' };
  /** Element output. */
  byte[] ELEM_OS = token("</");
  /** Element output. */
  byte[] ELEM_SC = token("/>");

  /** Attribute output. */
  byte[] ATT1 = token("=\"");
  /** Attribute output. */
  byte[] ATT2 = token("\"");

  /** Document output. */
  byte[] DOC = token("doc()");
  /** Text output. */
  byte[] TEXT = token("text()");
  /** Comment output. */
  byte[] COMM = token("comment()");
  /** Processing instruction output. */
  byte[] PI = token("processing-instruction()");
  /** Attribute output. */
  byte[] ATT = { '@' };
  /** CDATA output. */
  byte[] CDATA_O = token("<![CDATA[");
  /** CDATA output. */
  byte[] CDATA_C = token("]]>");

  /** HTML: head element. */
  byte[] HEAD = token("head");
  /** HTML: meta element. */
  byte[] META = token("meta");
  /** HTML: http-equiv attribute. */
  byte[] HTTPEQUIV = token("http-equiv");
  /** HTML: content attribute. */
  byte[] CONTENT = token("content");
  /** HTML: charset attribute value. */
  byte[] CHARSET = token("; charset=");

  // ERRORS ===================================================================

  /** Serialization error. */
  String SERVAL = "Parameter '%' must be [%";
  /** Serialization error. */
  String SERVAL2 = "|%";
  /** Serialization error. */
  String SERVAL3 = "]; '%' found";

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
  /** Table kinds. */
  byte[][] TABLEKINDS = tokens("DOC ", "ELEM", "TEXT", "ATTR", "COMM", "PI  ");
  /** Database - ID->PRE mapping. */
  String DATAIDP = "idp";
}

package org.basex.data;

import static org.basex.util.Token.*;

/**
 * This class assembles texts which are used in the data classes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public interface DataText {
  // META DATA ====================================================================================

  /** Database version; older version cannot open these instances. */
  String STORAGE = "7.8";
  /** Index version; older version cannot open indexes of these instances. */
  String ISTORAGE = "7.8";

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
  /** Automatic optimization. */
  String DBAUTOOPT = "AUTOOPT";
  /** Text indexing. */
  String DBTXTIDX = "TXTINDEX";
  /** Attribute indexing. */
  String DBATVIDX = "ATVINDEX";
  /** Full-text indexing. */
  String DBFTXIDX = "FTXINDEX";
  /** Full-text stemming. */
  String DBFTST = "FTSTEM";
  /** Full-text language. */
  String DBFTLN = "FTLANG";
  /** Full-text stopwords. */
  String DBFTSW = "FTSTOP";
  /** Full-text case sensitivity. */
  String DBFTCS = "FTCS";
  /** Full-text diacritics removal. */
  String DBFTDC = "FTDC";
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
  /** Text indexing. */
  String DBCRTTXT = "CRTTXT";
  /** Attribute indexing. */
  String DBCRTATV = "CRTATV";
  /** Full-text indexing. */
  String DBCRTFTX = "CRTFTX";

  /** Full-text wildcards indexing (legacy, obsolete). */
  String DBWCIDX = "WCINDEX";
  /** Scoring type (legacy, obsolete). */
  String DBSCTYPE = "FTSCTYPE";

  /** Tags. */
  String DBTAGS = "TAGS";
  /** Attributes. */
  String DBATTS = "ATTS";
  /** Path summary. */
  String DBPATH = "PATH";
  /** Namespace. */
  String DBNS = "NS";

  // DATABASE FILES ===============================================================================

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
  /** Database - Document path index. */
  String DATAPTH = "pth";
  /** Database - ID->PRE mapping. */
  String DATAIDP = "idp";

  // XML SERIALIZATION ============================================================================

  /** Version. */
  String V10 = "1.0";
  /** Version. */
  String V11 = "1.1";
  /** Version. */
  String V40 = "4.0";
  /** Version. */
  String V401 = "4.01";
  /** Version. */
  String V50 = "5.0";

  /** Document declaration. */
  String DOCDECL1 = "xml version=\"";
  /** Document declaration. */
  String DOCDECL2 = "\" encoding=\"";
  /** Document declaration. */
  String DOCDECL3 = "\" standalone=\"";

  /** HTML. */
  String HTML = "html";
  /** Doctype output. */
  String DOCTYPE = "<!DOCTYPE ";
  /** Doctype system keyword. */
  String SYSTEM = "SYSTEM";
  /** Doctype public keyword. */
  String PUBLIC = "PUBLIC";

  /** Ampersand entity. */
  byte[] E_AMP = token("&amp;");
  /** Quote entity. */
  byte[] E_QUOT = token("&quot;");
  /** GreaterThan entity. */
  byte[] E_GT = token("&gt;");
  /** LessThan entity. */
  byte[] E_LT = token("&lt;");
  /** Carriage return. */
  byte[] E_0D = token("&#x0D;");
  /** Line feed. */
  byte[] E_0A = token("&#x0A;");
  /** Line separator. */
  byte[] E_2028 = token("&#x2028;");
  /** HTML: Non-breaking space entity. */
  byte[] E_NBSP = token("&nbsp;");

  /** Token: results. */
  byte[] T_RESULTS = token("results");
  /** Token: result. */
  byte[] T_RESULT = token("result");
  /** Token: name. */
  byte[] T_NAME = token("name");
  /** Token: size. */
  byte[] T_SIZE = token("size");

  /** Comment output. */
  byte[] COMM_O = token("<!--");
  /** Comment output. */
  byte[] COMM_C = token("-->");
  /** XQuery comment. */
  byte[] XQCOMM_O = token("(:");
  /** XQuery comment. */
  byte[] XQCOMM_C = token(":)");
  /** Javascript comment. */
  byte[] JSCOMM_O = token("/*");
  /** Javascript comment. */
  byte[] JSCOMM_C = token("*/");

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
  byte[] COMMENT = token("comment()");
  /** Processing instruction output. */
  byte[] PI = token("processing-instruction()");
  /** Attribute output. */
  byte[] ATT = { '@' };
  /** CDATA output. */
  byte[] CDATA_O = token("<![CDATA[");
  /** CDATA output. */
  byte[] CDATA_C = token("]]>");

  /** XML spaces: element name. */
  byte[] XML_SPACE = token("xml:space");
  /** XML spaces: default. */
  byte[] DEFAULT = token("default");
  /** XML spaces: preserve. */
  byte[] PRESERVE = token("preserve");

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

  // TABLE SERIALIZATION ==========================================================================

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
}

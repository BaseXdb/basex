package org.basex.data;

import static org.basex.util.Token.*;

/**
 * This class assembles texts which are used in the data classes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface DataText {
  // META DATA ====================================================================================

  /** Database version; older version cannot open these instances. */
  String STORAGE = "9.0.1";
  /** Index version; older version cannot open indexes of these instances. */
  String ISTORAGE = "8.6";

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
  /** Automatic index update. */
  String DBUPDIDX = "UPDINDEX";
  /** Automatic optimization. */
  String DBAUTOOPT = "AUTOOPT";
  /** Text index. */
  String DBTXTIDX = "TXTINDEX";
  /** Attribute index. */
  String DBATVIDX = "ATVINDEX";
  /** Token index. */
  String DBTOKIDX = "TOKINDEX";
  /** Full-text index. */
  String DBFTXIDX = "FTXINDEX";
  /** Text index: names. */
  String DBTXTINC = "TXTINC";
  /** Attribute index: names. */
  String DBATVINC = "ATVINC";
  /** Token index: names. */
  String DBTOKINC = "TOKINC";
  /** Full-text index: names. */
  String DBFTXINC = "FTXINC";
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
  /** Maximum length of index entries. */
  String DBMAXLEN = "MAXLEN";
  /** Maximum number of categories. */
  String DBMAXCATS = "MAXCATS";
  /** Index split size. */
  String DBSPLITS = "SPLITS";
  /** Up-to-date flag. */
  String DBUPTODATE = "UPTODATE";
  /** Last (highest) id. */
  String DBLASTID = "LASTID";
  /** Documents. */
  String DBDOCS = "DOCS";
  /** Recreate text index. */
  String DBCRTTXT = "CRTTXT";
  /** Recreate attribute index. */
  String DBCRTATV = "CRTATV";
  /** Recreate token index. */
  String DBCRTTOK = "CRTTOK";
  /** Recreate full-text index. */
  String DBCRTFTX = "CRTFTX";

  /** Tags. */
  String DBTAGS = "TAGS";
  /** Attributes. */
  String DBATTS = "ATTS";
  /** Path index. */
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
  /** Database - Token index. */
  String DATATOK = "tok";
  /** Database - Full-text index. */
  String DATAFTX = "ftx";
  /** Database - Stopword list. */
  String DATASWL = "swl";
  /** Database - Updating flag. */
  String DATAUPD = "upd";
  /** Database - Document path index. */
  String DATAPTH = "pth";
  /** Database - ID-PRE mapping. */
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
  byte[] HTML = token("html");
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
  byte[] E_CR = token("&#xD;");
  /** Newline. */
  byte[] E_NL = token("&#xA;");
  /** Line separator. */
  byte[] E_2028 = token("&#x2028;");
  /** HTML: Non-breaking space entity. */
  byte[] E_NBSP = token("&nbsp;");

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

  /** Content-Type. */
  byte[] CONTENT_TYPE = token("Content-Type");
  /** HTML: head element. */
  byte[] HEAD = token("head");
  /** HTML: meta element. */
  byte[] META = token("meta");
  /** HTML: http-equiv attribute. */
  byte[] HTTP_EQUIV = token("http-equiv");
  /** HTML: content attribute. */
  byte[] CONTENT = token("content");
  /** HTML: charset. */
  byte[] CHARSET = token("charset");

  /** XHTML namespace. */
  byte[] XHTML_URI = token("http://www.w3.org/1999/xhtml");
  /** XML namespace. */
  byte[] XML_URI = token("http://www.w3.org/XML/1998/namespace");

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

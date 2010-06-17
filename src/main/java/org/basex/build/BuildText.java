package org.basex.build;

import static org.basex.util.Token.*;

/**
 * This interface organizes textual information for the builder package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public interface BuildText {
  /** Builder error. */
  String BEFOREROOT = "%: No text allowed before root tag.";
  /** Builder error. */
  String AFTERROOT = "%: No text allowed after closed root tag.";
  /** Builder error: Tag Mismatch. */
  String CLOSINGTAG = "%: </%> found, </%> expected.";
  /** Builder error: Tag Mismatch. */
  String MOREROOTS = "%: More than one root node: '<%>'";

  /** Builder error. */
  String LIMITNS = "%: Document has too many different namespaces (limit: %).";
  /** Builder error. */
  String LIMITRANGE = "%: Document is too large for being processed.";
  /** Builder error. */
  String LIMITTAGS = "%: Document has too many different tag names (limit: %).";
  /** Builder error. */
  String LIMITATTS =
    "%: Document has too many different attribute names (limit: %).";;
  /** Builder error. */
  String LIMITATT =
    "%: Element has too many different attributes (limit: %).";

  /** Parser Error. */
  String PARSEINVALID = "%: % expected, % found.";
  /** Parser Error. */
  String DOCOPEN = "%: Closing tag </%> expected.";

  /** Scanner error. */
  String DOCEMPTY = "Document is empty.";
  /** Scanner Error. */
  String UNCLOSED = "Unclosed tokens found.";
  /** Scanner Error. */
  String CONTCDATA = "']]>' not allowed in content.";
  /** Scanner Error. */
  String CDATASEC = "Invalid CDATA section.";
  /** Scanner Error. */
  String XMLCHAR = "Invalid XML character found: #%";
  /** Scanner Error. */
  String CHARACTER = "Invalid character found: '%'";
  /** Scanner Error. */
  String CLOSING = "Tag was not properly closed.";
  /** Scanner Error. */
  String COMMDASH = "Missing '-' in comment declaration.";
  /** Scanner Error. */
  String ATTCHAR = "Invalid character '%' in attribute value.";
  /** Scanner Error. */
  String ATTCLOSE = "Attribute value was not properly closed.";
  /** Scanner Error. */
  String PITEXT = "Invalid processing instruction.";
  /** Scanner Error. */
  String PIRES = "'<?xml' is reserved for document declaration.";
  /** Scanner Error. */
  String DECLVERSION = "XML version must be '1.0' or '1.1'.";
  /** Scanner Error. */
  String DECLSTART = "Document declaration must start with 'version'.";
  /** Scanner Error. */
  String TEXTENC = "'encoding' expected in text declaration.";
  /** Scanner Error. */
  String DECLWRONG = "Invalid document declaration.";
  /** Scanner Error. */
  String DECLENCODE = "Invalid encoding.";
  /** Scanner Error. */
  String DECLSTANDALONE = "Invalid standalone attribute in declaration.";
  /** Scanner Error. */
  String UNKNOWNENTITY = "Unknown entity '&%;'. Try 'set entity off'.";
  /** Scanner Error. */
  String INVALIDENTITY = "Invalid entity '&%...'. Try 'set entity off'.";
  /** Scanner Error. */
  String TYPEAFTER = "Misplaced document type definition.";
  /** Parser Error. */
  String SCANQUOTE = "Quote expected, '%' found.";
  /** Parser Error. */
  String PUBID = "Invalid character '%' in public identifier.";

  /** Scanner Error. */
  String NOWS = "Whitespace expected, '%' found.";
  /** Scanner Error. */
  String WRONGCHAR = "'%' expected, '%' found.";
  /** Scanner Error. */
  String INVNAME = "Invalid name.";
  /** Scanner Error. */
  String INVEND = "Unexpected end.";
  /** Scanner Error. */
  String UNKNOWNPE = "Unknown parameter reference '%'.";
  /** Scanner Error. */
  String INVPE = "Parameter reference not allowed here.";
  /** Scanner Error. */
  String RECENT = "Recursive entity definition.";

  /** DTD Whitespace error. */
  String WSERROR = "Missing Whitespace.";
  /** DTD Error. */
  String ERRDT = "Error in DTD.";

  /** Ampersand. */
  byte[] SEMI = token(";");
  /** CDATA token. */
  byte[] CDATA = token("CDATA[");
  /** XML Document Version. */
  byte[] VERS = token("version");
  /** XML Document Version. */
  byte[] VERS10 = token("1.0");
  /** XML Document Version. */
  byte[] VERS11 = token("1.1");
  /** XML Document Encoding. */
  byte[] ENCOD = token("encoding");
  /** XML Document Standalone flag. */
  byte[] STANDALONE = token("standalone");
  /** XML Document Standalone flag. */
  byte[] YES = token("yes");
  /** XML Document Standalone flag. */
  byte[] NO = token("no");

  /** DTD: XML. */
  byte[] DOCDECL = token("<?xml");
  /** DTD: Doctype. */
  byte[] DOCTYPE = token("DOCTYPE");
  /** DTD: System. */
  byte[] SYSTEM = token("SYSTEM");
  /** DTD: Public. */
  byte[] PUBLIC = token("PUBLIC");
  /** DTD: <!--. */
  byte[] COMS = token("<!--");
  /** DTD: NOTATION. */
  byte[] NOTA = token("<!NOTATION");
  /** DTD: ELEMENT. */
  byte[] ELEM = token("<!ELEMENT");
  /** DTD: ATTLIST. */
  byte[] ATTL = token("<!ATTLIST");
  /** DTD: ENTITY. */
  byte[] ENT = token("<!ENTITY");
  /** DTD: NOTATION. */
  byte[] COND = token("<![");
  /** DTD: NOTATION. */
  byte[] CONE = token("]]>");
  /** DTD: NOTATION. */
  byte[] INCL = token("INCLUDE");
  /** DTD: NOTATION. */
  byte[] IGNO = token("IGNORE");
  /** DTD: XML. */
  byte[] XDECL = token("<?");
  /** DTD: EMPTY ELEMENT. */
  byte[] EMP = token("EMPTY");
  /** DTD: ANY ELEMENT. */
  byte[] ANY = token("ANY");
  /** DTD: #PCDATA ELEMENT. */
  byte[] PC = token("#PCDATA");
  /** DTD: CDATA ELEMENT. */
  byte[] CD = token("CDATA");
  /** DTD: NDATA ELEMENT. */
  byte[] ND = token("NDATA");
  /** DTD: ID ELEMENT. */
  byte[] ID = token("ID");
  /** DTD: IDREF ELEMENT. */
  byte[] IDR = token("IDREF");
  /** DTD: IDREFS ELEMENT. */
  byte[] IDRS = token("IDREFS");
  /** DTD: ENTITIES ELEMENT. */
  byte[] ENTS = token("ENTITIES");
  /** DTD: ENTITY ELEMENT. */
  byte[] ENT1 = token("ENTITY");
  /** DTD: NMTOKEN ELEMENT. */
  byte[] NMT = token("NMTOKEN");
  /** DTD: NMTOKENS ELEMENT. */
  byte[] NMTS = token("NMTOKENS");
  /** DTD: NOTATION ELEMENT. */
  byte[] NOT = token("NOTATION");
  /** DTD: REQUIRED ELEMENT. */
  byte[] REQ = token("#REQUIRED");
  /** DTD: IMPLIED ELEMENT. */
  byte[] IMP = token("#IMPLIED");
  /** DTD: FIXED ELEMENT. */
  byte[] FIX = token("#FIXED");

  /** Token types. */
  enum Type {
    /** Text Node.           */ TEXT("Text"),
    /** Comment.             */ COMMENT("Comment"),
    /** DocType.             */ DTD("Document type"),
    /** PI.                  */ PI("Processing instruction"),
    /** Opening Bracket.     */ L_BR("'<'"),
    /** TagName.             */ TAGNAME("Tag name"),
    /** AttrName.            */ ATTNAME("Attribute name"),
    /** Closing Bracket.     */ R_BR("'>'"),
    /** Whitespace.          */ WS("whitespace"),
    /** AttrValue.           */ ATTVALUE("Attribute value"),
    /** Empty Bracket.       */ L_BR_CLOSE("'</'"),
    /** Closing End Bracket. */ CLOSE_R_BR("'/>'"),
    /** Equal Sign.          */ EQ("'='"),
    /** Quoted Text.         */ EOF("End of File"),
    /** Quoted Text.         */ QUOTE("Quote");

    /** String representation of token type. */
    public final String string;

    /**
     * Enumeration constructor.
     * @param s string representation
     */
    private Type(final String s) {
      string = s;
    }
  }
}

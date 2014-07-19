package org.basex.build;

import static org.basex.util.Token.*;

/**
 * This interface organizes textual information for the builder package.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public interface BuildText {
  /** Builder error. */
  String LIMITNS = "%: Too many different namespaces (limit: %).";
  /** Builder error. */
  String LIMITRANGE = "%: Input is too large for a single database.";
  /** Builder error. */
  String LIMITELEMS = "%: Too many different element names (limit: %).";
  /** Builder error. */
  String LIMITATTS = "%: Too many different attribute names (limit: %).";
  /** Builder error. */
  String WHICHNS = "%: Undeclared namespace prefix '%'.";

  /** Parser error. */
  String PARSEINV = "%: % expected, % found.";
  /** Parser error. */
  String CLOSINGELEM = "%: </%> found, </%> expected.";
  /** Parser error. */
  String OPEN = "%: Opening element <%> expected.";
  /** Parser error. */
  String DOCOPEN = "%: Closing element </%> expected.";
  /** Parser error. */
  String MOREROOTS = "%: No elements allowed after closed root element.";

  /** Scanner error. */
  String BEFOREROOT = "No text allowed before root element.";
  /** Scanner error. */
  String AFTERROOT = "No text allowed after closed root element.";

  /** Scanner error. */
  String DOCEMPTY = "No input found.";
  /** Scanner error. */
  String UNCLOSED = "Unclosed tokens found.";
  /** Scanner error. */
  String CONTCDATA = "']]>' not allowed in content.";
  /** Scanner error. */
  String CDATASEC = "Invalid CDATA section.";
  /** Scanner error. */
  String CHARACTER = "Invalid character found: '%'";
  /** Scanner error. */
  String CLOSING = "Element was not properly closed.";
  /** Scanner error. */
  String COMMDASH = "Comment or doctype declaration expected after '<!'.";
  /** Scanner error. */
  String ATTCHAR = "Invalid character '%' in attribute value.";
  /** Scanner error. */
  String ATTCLOSE = "Attribute value was not properly closed.";
  /** Scanner error. */
  String PITEXT = "Invalid processing instruction.";
  /** Scanner error. */
  String PIRES = "'<?xml' is reserved for document declaration.";
  /** Scanner error. */
  String DECLVERSION = "XML version must be '1.0' or '1.1'.";
  /** Scanner error. */
  String DECLSTART = "Document declaration must start with 'version'.";
  /** Scanner error. */
  String TEXTENC = "Encoding expected in text declaration.";
  /** Scanner error. */
  String DECLENCODE = "Invalid encoding.";
  /** Scanner error. */
  String DECLSTANDALONE = "Invalid standalone attribute in declaration.";
  /** Scanner error. */
  String TYPEAFTER = "Misplaced document type definition.";
  /** Parser error. */
  String SCANQUOTE = "Quote expected, '%' found.";
  /** Parser error. */
  String PUBID = "Invalid character '%' in public identifier.";

  /** Scanner error. */
  String NOWS = "Whitespace expected, '%' found.";
  /** Scanner error. */
  String WRONGCHAR = "'%' expected, '%' found.";
  /** Scanner error. */
  String INVNAME = "Invalid name.";
  /** Scanner error. */
  String INVEND = "Unexpected end.";
  /** Scanner error. */
  String UNKNOWNPE = "Unknown parameter reference '%'.";
  /** Scanner error. */
  String INVPE = "Parameter reference not allowed here.";
  /** Scanner error. */
  String RECENT = "Recursive entity definition.";

  /** DTD whitespace error. */
  String WSERROR = "Missing Whitespace.";
  /** DTD error. */
  String ERRDT = "Error in DTD.";

  /** Semicolon. */
  byte[] SEMI = token(";");
  /** CDATA token. */
  byte[] CDATA = token("CDATA[");
  /** XML document version. */
  byte[] VERS = token("version");
  /** XML document version. */
  byte[] VERS10 = token("1.0");
  /** XML document version. */
  byte[] VERS11 = token("1.1");
  /** XML document encoding. */
  byte[] ENCOD = token("encoding");
  /** XML document standalone flag. */
  byte[] STANDALONE = token("standalone");

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
    /** Text node.              */ TEXT("text"),
    /** Comment.                */ COMMENT("comment"),
    /** Document type.          */ DTD("document type"),
    /** Processing instruction. */ PI("processing instruction"),
    /** Opening bracket.        */ L_BR("'<'"),
    /** Element name.           */ ELEMNAME("element name"),
    /** Attribute name.         */ ATTNAME("attribute name"),
    /** Closing bracket.        */ R_BR("'>'"),
    /** Whitespace.             */ WS("whitespace"),
    /** Attribute value.        */ ATTVALUE("attribute value"),
    /** Empty bracket.          */ L_BR_CLOSE("'</'"),
    /** Closing end bracket.    */ CLOSE_R_BR("'/>'"),
    /** Equal sign.             */ EQ("'='"),
    /** End of input.           */ EOF("end of input"),
    /** Quote.                  */ QUOTE("quote");

    /** String representation of token type. */
    public final String string;

    /**
     * Enumeration constructor.
     * @param string string representation
     */
    Type(final String string) {
      this.string = string;
    }
  }
}

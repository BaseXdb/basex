package org.basex.build;

import org.basex.util.Token;

/**
 * This interface organizes textual information for the builder package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public interface BuildText {
  /** Builder error. */
  String CLOSEMISS = "%: Closing end tag missing.";
  /** Builder error. */
  String BEFOREROOT = "%: No text allowed before root tag: '%'";
  /** Builder error. */
  String AFTERROOT = "%: No text allowed after closed root tag: '%'";
  /** Builder error. */
  String DOCEMPTY = "Document is empty.";
  /** Tag Mismatch. */
  String CLOSINGTAG = "%: </%> found, </%> expected.";
  /** Tag Mismatch. */
  String MOREROOTS = "%: More than one root node: '<%>'";
  /** Attribute Duplicates. */
  String DUPLATT = "%: Tag contains duplicate attribute.";
  /** Empty Tag. */
  String TAGEMPTY = "%: Empty tag found.";
  /** Parser Error. */
  String PARSEINVALID = "%: % expected, % found.";

  /** Scanner Error. */
  String UNCLOSED = "Unclosed tokens found.";
  /** Scanner Error. */
  String CONTCDATA = "']]>' not allowed in content.";
  /** Scanner Error. */
  String CDATASEC = "Invalid CDATA section.";
  /** Scanner Error. */
  String XMLCHAR = "Invalid XML character found: '%' (#%)";
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
  String DECLVERSION = "Declaration must start with 'version=\"1.0\"'.";
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
  String UNEXP = "Unexpected character '%' found.";
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

  /** DTD Scanner Error. */
  String DTDNP = "Could not parse \"%\".";
  /** DTD: WHITESPACEERROR. */
  String WSERROR = "Missing Whitespace.";
  /** DTD: QUOTEERROR. */
  String ERRDT = "Error in DTD.";

  /** Ampersand. */
  byte[] SEMI = Token.token(";");
  /** CDATA token. */
  byte[] CDATA = Token.token("CDATA[");
  /** XML Document Declaration. */
  byte[] XMLDECL = Token.token("xml");
  /** XML Document Version. */
  byte[] VERS = Token.token("version");
  /** XML Document Version. */
  byte[] VERS10 = Token.token("1.0");
  /** XML Document Version. */
  byte[] VERS11 = Token.token("1.1");
  /** XML Document Encoding. */
  byte[] ENCOD = Token.token("encoding");
  /** XML Document Standalone flag. */
  byte[] STANDALONE = Token.token("standalone");
  /** XML Document Standalone flag. */
  byte[] STANDYES = Token.token("yes");
  /** XML Document Standalone flag. */
  byte[] STANDNO = Token.token("no");

  /** DTD: XML. */
  byte[] DOCDECL = Token.token("<?xml");
  /** DTD: Doctype. */
  byte[] DOCTYPE = Token.token("DOCTYPE");
  /** DTD: System. */
  byte[] SYSTEM = Token.token("SYSTEM");
  /** DTD: Public. */
  byte[] PUBLIC = Token.token("PUBLIC");
  /** DTD: <!--. */
  byte[] COMS = Token.token("<!--");
  /** DTD: NOTATION. */
  byte[] NOTA = Token.token("<!NOTATION");
  /** DTD: ELEMENT. */
  byte[] ELEM = Token.token("<!ELEMENT");
  /** DTD: ATTLIST. */
  byte[] ATTL = Token.token("<!ATTLIST");
  /** DTD: ENTITY. */
  byte[] ENT = Token.token("<!ENTITY");
  /** DTD: NOTATION. */
  byte[] COND = Token.token("<![");
  /** DTD: NOTATION. */
  byte[] CONE = Token.token("]]>");
  /** DTD: NOTATION. */
  byte[] INCL = Token.token("INCLUDE");
  /** DTD: NOTATION. */
  byte[] IGNO = Token.token("IGNORE");
  /** DTD: XML. */
  byte[] XML = Token.token("<?");
  /** DTD: EMPTY ELEMENT. */
  byte[] EMP = Token.token("EMPTY");
  /** DTD: ANY ELEMENT. */
  byte[] ANY = Token.token("ANY");
  /** DTD: #PCDATA ELEMENT. */
  byte[] PC = Token.token("#PCDATA");
  /** DTD: CDATA ELEMENT. */
  byte[] CD = Token.token("CDATA");
  /** DTD: NDATA ELEMENT. */
  byte[] ND = Token.token("NDATA");
  /** DTD: ID ELEMENT. */
  byte[] ID = Token.token("ID");
  /** DTD: IDREF ELEMENT. */
  byte[] IDR = Token.token("IDREF");
  /** DTD: IDREFS ELEMENT. */
  byte[] IDRS = Token.token("IDREFS");
  /** DTD: ENTITIES ELEMENT. */
  byte[] ENTS = Token.token("ENTITIES");
  /** DTD: ENTITY ELEMENT. */
  byte[] ENT1 = Token.token("ENTITY");
  /** DTD: NMTOKEN ELEMENT. */
  byte[] NMT = Token.token("NMTOKEN");
  /** DTD: NMTOKENS ELEMENT. */
  byte[] NMTS = Token.token("NMTOKENS");
  /** DTD: NOTATION ELEMENT. */
  byte[] NOT = Token.token("NOTATION");
  /** DTD: REQUIRED ELEMENT. */
  byte[] REQ = Token.token("#REQUIRED");
  /** DTD: IMPLIED ELEMENT. */
  byte[] IMP = Token.token("#IMPLIED");
  /** DTD: FIXED ELEMENT. */
  byte[] FIX = Token.token("#FIXED");
  
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
    public String string;
    
    /**
     * Enumeration constructor.
     * @param s string representation
     */
    private Type(final String s) {
      string = s;
    }
  }
}

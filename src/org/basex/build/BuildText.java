package org.basex.build;

import org.basex.util.Token;

/**
 * This interface organizes textual information for the builder package.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
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
  String INVALID = "Could not parse beginning of document.";
  /** Scanner Error. */
  String UNCLOSED = "Unclosed tokens found.";
  /** Scanner Error. */
  String CONTCDATA = "']]>' not allowed in content.";
  /** Scanner Error. */
  String CDATASEC = "Invalid CDATA section.";
  /** Scanner Error. */
  String XMLCHAR = "Invalid XML character found: '%' (#%)";
  /** Scanner Error. */
  String XMLENT = "Invalid XML character found: '%'";
  /** Scanner Error. */
  String CHARACTER = "Invalid character found: '%'";
  /** Scanner Error. */
  String CLOSING = "Tag was not properly closed.";
  /** Scanner Error. */
  String COMMENTDASH = "'--' not allowed as part of comment.";
  /** Scanner Error. */
  String COMMDASH = "Missing '-' in comment declaration.";
  /** Scanner Error. */
  String ATTCHAR = "Invalid character '%' in attribute value.";
  /** Scanner Error. */
  String ATTCLOSE = "Attribute value was not properly closed.";
  /** Scanner Error. */
  String PINAME = "Invalid name for processing instruction.";
  /** Scanner Error. */
  String PITEXT = "Invalid processing instruction.";
  /** Scanner Error. */
  String PIRES = "'<?xml' is reserved for document declaration.";
  /** Scanner Error. */
  String PILC = "Document declaration must be lower case ('xml').";
  /** Scanner Error. */
  String PIXML = "Document declaration must be placed first in document.";
  /** Scanner Error. */
  String DECLVERSION = "Declaration must start with 'version=\"1.0\"'.";
  /** Scanner Error. */
  String DECLSTART = "Document declaration must start with 'version'.";
  /** Scanner Error. */
  String DECLWRONG = "Invalid document declaration.";
  /** Scanner Error. */
  String DECLENCODE = "Invalid encoding attribute in document declaration.";
  /** Scanner Error. */
  String DECLSTANDALONE = "Invalid standalone attribute in declaration.";
  /** Scanner Error. */
  String INVALIDENTITY = "Unknown entity '%'; " +
    "try the command 'set entity off'.";
  /** Scanner Error. */
  String TYPEAFTER = "Misplaced document type definition.";

  /** CDATA token. */
  byte[] CDATA = Token.token("CDATA[");
  /** XML Document Declaration. */
  byte[] XMLDECL = Token.token("xml");
  /** XML Document Version. */
  byte[] VERS = Token.token("version");
  /** XML Document Version. */
  byte[] VERS1 = Token.token("\"1.0\"");
  /** XML Document Version. */
  byte[] VERS2 = Token.token("'1.0'");
  /** XML Document Encoding. */
  byte[] ENCOD = Token.token("encoding");
  /** XML Document Standalone flag. */
  byte[] STANDALONE = Token.token("standalone");
  /** XML Document Standalone flag. */
  byte[] STANDYES = Token.token("yes");
  /** XML Document Standalone flag. */
  byte[] STANDNO = Token.token("no");

  /** DTD: Doctype. */
  byte[] DOCTYPE = Token.token("DOCTYPE");
  /** DTD: System. */
  byte[] SYSTEM = Token.token("SYSTEM");
  /** DTD: Public. */
  byte[] PUBLIC = Token.token("PUBLIC");
  /** DTD: Square Bracket Open. */
  byte[] SBRACKETO = Token.token("[");
  /** DTD: Square Bracket Close. */
  byte[] SBRACKETC = Token.token("]");
  /** DTD: Bracket Open. */
  byte[] BRACKETO = Token.token("(");
  /** DTD: Bracket Close. */
  byte[] BRACKETC = Token.token(")");
  /** DTD: ELEMENT. */
  byte[] ELEM = Token.token("<!ELEMENT");
  /** DTD: Wrong ELEMENT. */
  byte[] WELEM1 = Token.token("<ELEMENT");
  /** DTD: Wrong ELEMENT. */
  byte[] WELEM2 = Token.token("<!Element");
  /** DTD: Wrong ELEMENT. */
  byte[] WELEM3 = Token.token("<!element");
  /** DTD: ATTLIST. */
  byte[] ATTL = Token.token("<!ATTLIST");
  /** DTD: Wrong ATTLIST. */
  byte[] WATTL1 = Token.token("<!Attlist");
  /** DTD: Wrong ATTLIST. */
  byte[] WATTL2 = Token.token("<ATTLIST");
  /** DTD: ENTITY. */
  byte[] ENT = Token.token("<!ENTITY");
  /** DTD: XML. */
  byte[] XML = Token.token("<?");
  /** DTD: EMPTY ELEMENT. */
  byte[] EMP = Token.token("EMPTY");
  /** DTD: Wrong EMPTY ELEMENT. */
  byte[] WEMP1 = Token.token("empty");
  /** DTD: Wrong EMPTY ELEMENT. */
  byte[] WEMP2 = Token.token("Empty");
  /** DTD: ANY ELEMENT. */
  byte[] ANY = Token.token("ANY");
  /** DTD: Wrong ANY ELEMENT. */
  byte[] WANY1 = Token.token("Any");
  /** DTD: Wrong ANY ELEMENT. */
  byte[] WANY2 = Token.token("any");
  /** DTD: #PCDATA ELEMENT. */
  byte[] PC = Token.token("#PCDATA");
  /** DTD: DASH ELEMENT. */
  byte[] DASH = Token.token("|");
  /** DTD: COLON ELEMENT. */
  byte[] COLON = Token.token(",");
  /** DTD: GREATER ELEMENT. */
  byte[] GREAT = Token.token(">");
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
    /** DocDecl.             */ DECL("Document declaration"),
    /** Opening Bracket.     */ L_BR("'<'"),
    /** TagName.             */ TAGNAME("Tag name"),
    /** AttrName.            */ ATTNAME("Attribute name"),
    /** Closing Bracket.     */ R_BR("'>'"),
    /** Whitespace.          */ WS("whitespace"),
    /** AttrValue.           */ ATTVALUE("Attribute value"),
    /** Empty Bracket.       */ L_BR_CLOSE("'</'"),
    /** Closing End Bracket. */ CLOSE_R_BR("'/>'"),
    /** Equal Sign.          */ EQ("'='"),
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

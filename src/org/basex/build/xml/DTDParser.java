package org.basex.build.xml;

import static org.basex.build.BuildText.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLToken.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.index.Names;
import org.basex.io.IOConstants;
import org.basex.util.Map;
import org.basex.util.XMLToken;

/**
 * Parses the DTD to get the elements, attributes and entities.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Andreas Weiler
 */
public class DTDParser {
  /** Root Element Type. */
  private byte[] root;
  /** Element Type. */
  private byte[] element;
  /** Attlist Type. */
  private byte[] attl;
  /** Tokenizer Type. */
  private byte[] tokenizedType;
  /** Extern id of the DTD file. */
  private byte[] extid;
  /** Root file. */
  private String xmlfile;
  /** Content of internal DTD. */
  private byte[] content;
  /** Current position. */
  private int pos;
  /** Temporary saving of content. */
  private byte[] intSubset;
  /** boolean Value for checking if there is an intern DTD. */
  private boolean isIntern = false;

  /** Index for all tag and attribute names. */
  Names tags;
  /** Index for all tag and attribute names. */
  Names atts;
  /** Index for all entity names. */
  Map ents;

  /**
   * Constructor.
   * @param dtd contents
   * @param xml input xml file
   * @param tag tag index
   * @param att attribute index
   * @param ent entity index
   * @throws IOException I/O Exception
   */
  public DTDParser(final byte[] dtd, final String xml, final Names tag,
      final Names att, final Map ent) throws IOException {

    xmlfile = xml;
    tags = tag;
    atts = att;
    ents = ent;

    // cache content
    content = dtd;
    // check DOCTYPE S
    if(!consume(DOCTYPE) || !consumeWS()) error("Error in DOCTYPE");
    // check NAME
    root = consumeName();
    // check Whitespace
    consumeWS();
    // check for ExternDTD
    if(consume(SYSTEM)) {
      if(!consumeWS()) error(WSERROR);
      extid = consumeQuoted();
      externalID();
    } else if(consume(PUBLIC)) {
      if(!consumeWS()) error(WSERROR);
      consumeQuoted();
      if(!consumeWS()) error(WSERROR);
      extid = consumeQuoted();
      externalID();
    } else if(consume(SBRACKETO)) {
      content = dtd;
      BaseX.debug("- Root Element Type: %", root);
      BaseX.debug("- InternContent:\n %", content);
      markupdecl();
      consumeWS();
      if(!consume(SBRACKETC)) error(SIGNERROR);
      BaseX.debug("----------------------");
      BaseX.debug("THE END");
    } else {
      error();
    }
  }

  /**
   * Starts the parsing of external DTD.
   * @throws IOException I/O Exception
   */
  private void externalID() throws IOException {
    consumeWS();
    if(consume(SBRACKETO)) {
      consumeWS();
      isIntern = true;
      consumeWS();
      intSubset = substring(content, pos, content.length);
    }
    // read external file
    final String dtd = string(extid);
    try {
      content = IOConstants.read(xmlfile, dtd);
    } catch(final FileNotFoundException ex) {
      error(DTDNOTFOUND, dtd);
    }
    pos = 0;
    BaseX.debug("- Root Element Type: %", root);
    BaseX.debug("- ExternContent of %:\n %", dtd, content);
    BaseX.debug("----------------");
    if(content.length != 0) {
    markupdecl();
    }
    BaseX.debug("----------------");
    BaseX.debug("THE END");
    if(isIntern) {
      content = intSubset;
      pos = 0;
      BaseX.debug("- InternContent:\n %", content);
      markupdecl();
      consumeWS();
      if(!consume(SBRACKETC)) error(SIGNERROR);
      BaseX.debug("----------------");
      BaseX.debug("THE END");
    }
  }

  /**
   * Method to consume the Content of Internal or/and External DTD.
   * @throws BuildException Build Exception
   */
  private void markupdecl() throws BuildException {
    // checks for element, attlist and entity tags
    consumeWS();
    if(consume(ELEM)) {
      elementDecl();
      markupdecl();
    } else if(consume(ATTL)) {
      attlistDecl();
      consumeWS();
      if(!consume(GREAT)) {
        error(SIGNERROR);
      }
      markupdecl();
    } else if(consume(ENT)) {
      entityDecl();
      consumeWS();
      if(!consume(GREAT)) error(SIGNERROR);
      markupdecl();
    } else if(consume(NOTA)) {
      notationDecl();
      markupdecl();
    } else if(consume(GQ)) {
      piDecl();
      markupdecl();
    } else if(consume(COMS)) {
      commentDecl();
      markupdecl();
    } else {
      consumeWS();
      byte next = next();
      if(percentage(next)) {
        BaseX.debug(consumeSpecName());
        markupdecl();
      } else {
        if(XMLToken.isLetter(next)) {
          error(SIGNERROR);
        }
      }
    }
  }

  /**
   * Checks the ElementDeclaration.
   * @throws BuildException Build Exception
   */
  private void elementDecl() throws BuildException {
    if(!consumeWS()) error(WSERROR);
    element = consumeSpecName();
    tags.add(element);
    BaseX.debug("----------------------");
    BaseX.debug("- Element: '%'", element);
    contentSpec();
    consumeWS();
    if(!consume(GREAT)) error(SIGNERROR);
  }

  /**
   * Checks the contentSpec for Element Objects.
   * @throws BuildException Build Exception
   */
  private void contentSpec() throws BuildException {
    // sign after name has to be a whitespace
    if(!consumeWS()) error(WSERROR);
    // checks for empty, any or mixed elements
    if(consume(EMP)) {
      BaseX.debug(EMP);
    } else if(consume(ANY)) {
      BaseX.debug(ANY);
    } else if(consume(BRACKETO)) {
      consumeWS();
      if(consume(PC)) {
        BaseX.debug(PC);
        consumeWS();
        if (!consume(BRACKETC)) {
        mixedElement();
        } else {
          if (consume(STAR)) {
            BaseX.debug("*");
          }
        }
      } else {
        childrenElement();
      }
    } else {
      consumeWS();
      BaseX.debug(consumeSpecName());
    }
  }

  /**
   * Consumes mixed content of an Element.
   * @throws BuildException Build Exception
   */
  private void mixedElement() throws BuildException {
    consumeWS();
    if(consume(BRACKETC) || consume(DASH)) {
      consumeWS();
      if(consume(STAR)) {
        BaseX.debug("*");
      } else if(consume(GREAT)) {
        prev();
      } else {
        mixedElement();  
      }
    } else {
      BaseX.debug(consumeSpecName());
      mixedElement();
    }
  }

  /**
   * Consumes children content of an Element.
   * @throws BuildException Build Exception
   */
  private void childrenElement() throws BuildException {
    consumeWS();
    if(consume(BRACKETC)) {
      if(consume(STAR) || consume(QUESTION) || consume(PLUS)) {
        BaseX.debug(substring(content, pos - 1, pos));
        consumeWS();
        if(!consume(GREAT)) {
          childrenElement();
        } else {
          prev();
        }
      } else {
        consumeWS();
        if(!consume(GREAT)) {
          childrenElement();
        } else {
          prev();
        }
      }
    } else if(consume(DASH) || consume(COLON) || consume(BRACKETO)) {
      childrenElement();
    } else {
      BaseX.debug(consumeSpecName());
      childrenElement();
    }
  }

  /**
   * Checks the AttlistDeclaration.
   * @throws BuildException Build Exception
   */
  private void attlistDecl() throws BuildException {
    if(!consumeWS()) error(WSERROR);
    attl = consumeSpecName();
    atts.add(attl);
    BaseX.debug("----------------------");
    BaseX.debug("- ATTLIST: '%'", attl);
    consumeWS();
    if(!consume(GREAT)) {
      attDef();
    } else {
      prev();
    }
  }

  /**
   * Checks the attDef for Attlist Objects.
   * @throws BuildException Build Exception
   */
  private void attDef() throws BuildException {
    consumeWS();
    BaseX.debug(consumeSpecName());
    if(!consumeWS()) error(WSERROR);
    attType();
  }

  /**
   * Checks the attType for Attlist Objects.
   * @throws BuildException Build Exception
   */
  private void attType() throws BuildException {
    if(consume(CD)) {
      BaseX.debug(CD);
      dDecl();
    } else if(consume(BRACKETO)) {
      enumeratedAttlist();
      dDecl();
    } else if(consume(NOT)) {
      if(!consumeWS()) error(WSERROR);
      if(!consume(BRACKETO)) error(SIGNERROR);
      consumeWS();
      BaseX.debug(consumeSpecName());
      enumeratedAttlist();
      dDecl();
    } else if(tokenizedType()) {
      BaseX.debug(tokenizedType);
      dDecl();
    } else error(SIGNERROR);
  }
  
  /** Value if there is an open Bracket or not. */
  private boolean bopen = false;
  
  /**
   * Consumes bracketed content.
   * @throws BuildException Build Exception
   */
  private void enumeratedAttlist() throws BuildException {
    consumeWS();
    if (consume(BRACKETC)) {
      if(bopen) {
        enumeratedAttlist();
        bopen = false;
      }
    } else if(consume(DASH)) {
      enumeratedAttlist();
    } else if(consume(BRACKETO)) {
      bopen = true;
      enumeratedAttlist();
    } else {
      BaseX.debug(consumeSpecName());
      enumeratedAttlist();
    }
  }

  /**
   * Checks the dDecl for Attlist Objects.
   * @throws BuildException Build Exception
   */
  private void dDecl() throws BuildException {
    if(!consumeWS()) error(WSERROR);
    // checks for REQUIRED, IMPLIED or FIXED elements
    if(consume(REQ)) {
      BaseX.debug(REQ);
      consumeWS();
      if(!consume(GREAT)) {
        attDef();
      } else {
        prev();
      }
    } else if(consume(IMP)) {
      BaseX.debug(IMP);
      consumeWS();
      if(!consume(GREAT)) {
        attDef();
      } else {
        prev();
      }
    } else if(consume(FIX)) {
      BaseX.debug(FIX);
      consumeWS();
      if(consume(GREAT)) {
        markupdecl();
      } else {
        BaseX.debug(consumeQuoted());
        consumeWS();
        if(!consume(GREAT)) {
          attDef();
        } else {
          prev();
        }
      }
    } else {
      BaseX.debug(consumeQuoted());
      consumeWS();
      if(!consume(GREAT)) {
        attDef();
      } else {
        prev();
      }
    }
  }

  /**
   * Checks the EntityDeclaration.
   * @throws BuildException Build Exception
   */
  private void entityDecl() throws BuildException {
    if(!consumeWS()) error(WSERROR);
    if(consume(PERCENT)) {
      if(!consumeWS()) error(WSERROR);
      byte[] name = consumeSpecName();
      BaseX.debug("----------------------");
      BaseX.debug("- Entity: '%'", name);
      final byte[] val = entDef();
      ents.add(name, val);
    } else {
      byte[] name = consumeSpecName();
      BaseX.debug("----------------------");
      BaseX.debug("- Entity: '%'", name);
      final byte[] val = entDef();
      ents.add(name, val);
    }
  }

  /**
   * Checks the EntityDef and PEDef for Entity Objects.
   * @return entity definition
   * @throws BuildException Build Exception
   */
  private byte[] entDef() throws BuildException {
    if(!consumeWS()) error(WSERROR);
    if(consume(SYSTEM)) {
      if(!consumeWS()) error(WSERROR);
      byte[] val = consumeQuoted();
      BaseX.debug(val);
      consumeWS();
      if(consume(ND)) {
        BaseX.debug(ND);
        if(!consumeWS()) error(WSERROR);
        BaseX.debug(consumeSpecName());
      }
      return val;
    } else if(consume(PUBLIC)) {
      if(!consumeWS()) error(WSERROR);
      consumeQuoted();
      if(!consumeWS()) error(WSERROR);
      byte[] val = consumeQuoted();
      BaseX.debug(val);
      if(consume(ND)) {
        BaseX.debug(ND);
        if(!consumeWS()) error(WSERROR);
        BaseX.debug(consumeSpecName());
      }
      return val;
    } else {
      consumeWS();
      byte[] val = consumeQuoted();
      BaseX.debug(val);
      return val;
    }
  }

  /**
   * Checks the NotationDeclaration.
   * @throws BuildException Build Exception
   */
  private void notationDecl() throws BuildException {
    if(!consumeWS()) error(WSERROR);
    BaseX.debug("----------------------");
    BaseX.debug("- Notation: %", consumeSpecName());
    if(!consumeWS()) error(WSERROR);
    notationID();
  }

  /**
   * checks for External or Public ID in a Notation Object.
   * @throws BuildException Build Exception
   */
  private void notationID() throws BuildException {
    if(consume(SYSTEM)) {
      if(!consumeWS()) error(WSERROR);
      BaseX.debug(" - ExternID: '%'", consumeQuoted());
      consumeWS();
      if(!consume(GREAT)) error(SIGNERROR);
    } else if(consume(PUBLIC)) {
      if(!consumeWS()) error(WSERROR);
      BaseX.debug(" - PUBID: '%'", consumeQuoted());
      consumeWS();
      if(!consume(GREAT)) {
        BaseX.debug(" - ExternID: '%'", consumeQuoted());
      }
    } else {
      error(SIGNERROR);
    }
  }

  /**
   * Checks the PIDeclaration.
   * @throws BuildException Build Exception
   */
  private void piDecl() throws BuildException {
    byte ch = next();
    if(!isFirstLetter(ch)) error(PINAME);
    int p = pos;
    while(!consume(GREAT)) {
      next();
    }
    BaseX.debug("- PI: '%'", substring(content, p, pos));
  }

  /**
   * Checks the CommentDeclaration.
   */
  private void commentDecl() {
    int tmp = pos;
    while(!consume(COME)) {
      next();
    }
    BaseX.debug("- Comment: '%'", substring(content, tmp, pos - 3));
  }

  /**
   * Scans whitespace.
   * @return true if whitespace was found
   */
  private boolean consumeWS() {
    byte c = next();
    if(!ws(c)) {
      prev();
      return false;
    }
    do {
      c = next();
    } while(ws(c) && c != 0);
    prev();
    return true;
  }

  /**
   * Consume the specified token.
   * @param tok token to be consumed
   * @return true if token was consumed
   */
  private boolean consume(final byte[] tok) {
    boolean found = indexOf(content, tok, pos) == pos;
    if(found) pos += tok.length;
    return found;
  }

  /**
   * Consumes a name.
   * @return consumed name
   * @throws BuildException Build Exception
   */
  private byte[] consumeName() throws BuildException {
    int p = pos;
    byte c = next();
    if(!XMLToken.isFirstLetter(c)) error(NAMEERROR);
    do {
      c = next();
      if(c == ')') {
        prev();
        return substring(content, p, pos);
      }
    } while(XMLToken.isLetter(c));
    return substring(content, p, pos);
  }

  /**
   * Consumes a special name with special characters.
   * @return consumed name
   * @throws BuildException Build Exception
   */
  private byte[] consumeSpecName() throws BuildException {
    int p = pos;
    byte c = next();
    if(!XMLToken.isFirstLetter(c) && !percentage(c)) {
      error();
    }
    do {
      c = next();
    } while(XMLToken.isLetter(c) || percentage(c) || semicolon(c)
        || quantity(c));
    prev();
    return substring(content, p, pos);
  }

  /**
   * Consumes a quoted token.
   * @return quoted token
   * @throws BuildException Build Exception
   */
  private byte[] consumeQuoted() throws BuildException {
    byte quote = next();
    if(quote != '\'' && quote != '"') error(QUOTEERROR);
    int p = pos;
    byte c;
    while((c = next()) != quote) {
      if(c == 0) error();
    }
    return substring(content, p, pos - 1);
  }

  /**
   * Checks for all kind of defined tokens.
   * @return boolean if token is found.
   * @throws BuildException Build Exception
   */
  private boolean tokenizedType() throws BuildException {
    int p = pos;
    tokenizedType = consumeSpecName();
    byte[] help = tokenizedType;
    if(eq(help, ID) || eq(help, IDR) || eq(help, IDRS) || eq(help, ENT1)
        || eq(help, ENTS) || eq(help, NMT) || eq(help, NMTS)) {
      return true;
    }
    pos = p;
    return false;
  }

  /**
   * Returns the next character or 0 if no more are found.
   * @return next character
   */
  private byte next() {
    return pos < content.length ? content[pos++] : 0;
  }

  /**
   * Jumps one character back.
   */
  private void prev() {
    --pos;
  }

  /**
   * Compares characters for percentage sign.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  private boolean percentage(final byte ch) {
    return ch == '%';
  }

  /**
   * Compares characters for semicolon sign.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  private boolean semicolon(final byte ch) {
    return ch == ';';
  }

  /**
   * Compares characters for quantity signs.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  private boolean quantity(final byte ch) {
    return ch == '?' || ch == '*' || ch == '+';
  }

  /**
   * Throws an error.
   * @param err error message
   * @param arg error arguments
   * @throws BuildException Build Exception
   */
  private void error(final String err, final Object... arg)
      throws BuildException {
    throw new BuildException(DTDERR, BaseX.inf(err, arg));
  }

  /**
   * Throws an error.
   * @throws BuildException Build Exception
   */
  private void error() throws BuildException {
    throw new BuildException("Error while DTD parsing.");
  }
}

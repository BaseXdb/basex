package org.basex.build.xml;

import static org.basex.build.BuildText.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.index.Names;
import org.basex.io.IOConstants;
import org.basex.util.Map;

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
  private byte[] checkT;
  /** Root file. */
  private String xmlfile;
  /** int Value for help. */
  private boolean check = true;

  /** Content of internal DTD. */
  private byte[] content;
  /** Current position. */
  private int pos;

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
    if(!consume(DOCTYPE) || !consumeWS()) error();
    // check NAME
    root = consumeName();
    if (string(root).equals(string(SYSTEM))) error();
    // check Whitespace
    if(!consumeWS()) error();
    // check for ExternDTD
    if(consume(SYSTEM) && consumeWS()) {
      // check ExternalID in quotes
      byte[] extid = consumeQuoted();
      consumeWS();
      if(consume(SBRACKETO)) {
        content = dtd;
        BaseX.debug("- Root Element Type: %", root);
        BaseX.debug("- Content:\n %", content);
        consumeContent();
      }
      // read external file
      String file = new File(xmlfile).getParent() + "\\" + string(extid);
      content = IOConstants.read(file);
      pos = 0;
      BaseX.debug("- Root Element Type: %", root);
      BaseX.debug("- Content:\n %", content);
      BaseX.debug("----------------");
      consumeContent();
    } else if (consume(SBRACKETO)) {
      content = dtd;
      BaseX.debug("- Root Element Type: %", root);
      BaseX.debug("- Content:\n %", content);
      consumeContent();
    } else {
      error();
    }
  }

  /**
   * Method to consume the Content of Internal or/and External DTD.
   * @throws BuildException Build Exception
   */
  private void consumeContent() throws BuildException {
    // runs till the last character
    while(next() != 0) {
      check = true;
      // checks for element, attlist and entity tags
      if(consume(ELEM)) {
        if (!consumeWS()) error();
        element = consumeName();
        tags.add(element);
        BaseX.debug("----------------------");
        BaseX.debug("- Element: %", element);
        contentSpec();
      } else if(consume(ATTL)) {
        if (!consumeWS()) error();
        attl = consumeName();
        atts.add(attl);
        BaseX.debug("----------------------");
        BaseX.debug("- ATTLIST: %", attl);
        if (!consume(GREAT)) {
          attType();
          dDecl();
        }
      } else if(consume(ENT)) {
        if (!consumeWS()) error();
        if(percentage(next())) {
          if(!consumeWS()) error();
          byte[] name = consumeName();
          BaseX.debug("----------------------");
          BaseX.debug("- Entity: %", name);
          if(!consumeWS()) error();
          final byte[] val = entDef();
          ents.add(name, val);
        } else {
          prev();
          byte[] name = consumeName();
          BaseX.debug("----------------------");
          BaseX.debug("- Entity: %", name);
          final byte[] val = entDef();
          ents.add(name, val);
        }
      } else if(consume(NOT)) {
        if (!consumeWS()) error();
        BaseX.debug("----------------------");
        BaseX.debug(NOT);
      } else if(consume(WELEM1) || consume(WELEM2) || consume(WELEM3)
          || consume(WATTL1) || consume(WATTL2) || consume(XML)) {
        error();
      }
    }
    consumeWS();
    if(!consume(SBRACKETC)) error();
    BaseX.debug("----------------------");
    BaseX.debug("THE END");
  }

  /**
   * Checks the contentSpec for Element Objects.
   * @throws BuildException Build Exception
   */
  private void contentSpec() throws BuildException {
    // sign after name has to be a whitespace
    if(!consumeWS()) error();
    // checks for empty, any or mixed elements
    if(consume(EMP)) {
      BaseX.debug(EMP);
    } else if(consume(ANY)) {
      BaseX.debug(ANY);
    } else if(consume(BRACKETO)) {
      consumeWS();
      if(consume(PC)) {
        BaseX.debug(PC);
        check = false;
        consumeWS();
        consumeBracketed();
      } else {
        consumeBracketed();
      }
    } else if(consume(WEMP1) || consume(WEMP2) || consume(WANY1)
        || consume(WANY2)) {
      error();
    } else {
      consumeWS();
      BaseX.debug(consumeName());
      consumeWS();
    }
  }
  
    /**
     * Consumes bracketed content.
     * @throws BuildException Build Exception
     */  
  private void consumeBracketed() throws BuildException  {
    while(!consume(BRACKETC)) {
      consumeWS();
      if(consume(DASH) || consume(COLON)) {
        consumeWS();
        if (consume(BRACKETO)) {
          consumeBracketed();
        } else {
        BaseX.debug(consumeName());
        consumeWS();
        }
      } else if (consume(BRACKETO)) { 
        consumeBracketed();
      } else {
        if (check) {
        consumeWS();
        BaseX.debug(consumeName());
        consumeWS();
        check = false;
        } else {
          error();
        }
      }
    }
    BaseX.debug(consumeQuantity());
    if(consumeWS()) {
      if (consume(DASH) || consume(COLON)) {
        consumeWS();
        if(consume(BRACKETO)) {
          consumeBracketed();
        }
      }
    } 
  }
  
  

  /**
   * Checks the attType for Attlist Objects.
   * @throws BuildException Build Exception
   */
  private void attType() throws BuildException {
    consumeWS();
    BaseX.debug(consumeName());
    consumeWS();
    if(consume(CD)) {
      BaseX.debug(CD);
    } else if(consume(BRACKETO)) {
        consumeBracketed2();
    } else if(consume(NOT)) {
      if(!consumeWS() && !consume(BRACKETO)) error();
      consumeWS();
      if(consume(BRACKETO)) {
        consumeBracketed2();
      } else {
      BaseX.debug(consumeName());
      }
    } else if(checkTokenize()) {
      BaseX.debug(checkT);
    } else error();
  }
  
  /**
   * Consumes bracketed content.
   * @throws BuildException Build Exception
   */  
private void consumeBracketed2() throws BuildException  {
  while(!consume(BRACKETC)) {
    consumeWS();
    if(consume(DASH)) {
      consumeWS();
      if (consume(BRACKETO)) {
        consumeBracketed();
      } else {
      BaseX.debug(consumeName());
      consumeWS();
      }
    } else if (consume(BRACKETO)) { 
      consumeBracketed();
    } else if (consume(COLON)) {
      error();
    } else {
      if (check) {
      consumeWS();
      BaseX.debug(consumeName());
      consumeWS();
      check = false;
      } else {
        error();
      }
    }
  }
  BaseX.debug(consumeQuantity());
  if(consumeWS()) {
    if (consume(DASH) || consume(COLON)) {
      consumeWS();
      if(consume(BRACKETO)) {
        consumeBracketed();
      }
    }
  } 
}

  /**
   * Checks the dDecl for Attlist Objects.
   * @throws BuildException Build Exception
   */
  private void dDecl() throws BuildException {
    if (!consumeWS()) error();
    // checks for REQUIRED, IMPLIED or FIXED elements
    if(consume(REQ)) {
      BaseX.debug(REQ);
      if (consumeWS()) {
        if (consume(REQ) || consume(IMP) || consume(FIX)) error();
      }
    } else if(consume(IMP)) {
      BaseX.debug(IMP);
      if (consumeWS()) {
        if (consume(REQ) || consume(IMP) || consume(FIX)) error();
      }
    } else if(consume(FIX)) {
      BaseX.debug(FIX);
      if (!consumeWS()) error();
      BaseX.debug(consumeQuoted());
    } else {
      BaseX.debug(consumeQuoted());
      if (consumeWS()) {
        if (consume(REQ) || consume(IMP) || consume(FIX)) error();
      }
    }
  }

  /**
   * Checks the EntityDef and PEDef for Entity Objects.
   * @return entity definition
   * @throws BuildException Build Exception
   */
  private byte[] entDef() throws BuildException {
    consumeWS();
    if(consume(SYSTEM) || consume(PUBLIC)) {
      consumeWS();
      byte[] val = consumeQuoted();
      BaseX.debug(val);
      if (!consumeWS()) error();
      if(consume(ND)) {
        BaseX.debug(ND);
        if (!consumeWS()) error();
        BaseX.debug(consumeName());
      } else if(consume(GREAT)) {
      } else {
        BaseX.debug(consumeQuoted());
      }
      return val;
    } else {
      consumeWS();
      byte[] val = consumeQuoted();
      BaseX.debug(val);
      if (consumeWS()) {
        if (consume(SYSTEM)) error();
      }
      return val;
    }
  }

  /**
   * Scans whitespace.
   * @return true if whitespace was found
   */
  private boolean consumeWS() {
    byte c = next();
    if(!XMLScanner.whitespace(c)) {
      prev();
      return false;
    }
    do {
      c = next();
    } while(XMLScanner.whitespace(c) && c != 0);
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
    if(!XMLScanner.isFirstLetter(c) && !percentage(c)) {
      error();
    }
    do {
      c = next();
    } while(XMLScanner.isLetter(c) || percentage(c) || semicolon(c)
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
    if(quote != '\'' && quote != '"') error();
    int p = pos;
    byte c;
    while((c = next()) != quote) {
      if(c == 0) error();
    }
    return substring(content, p, pos - 1);
  }

  /**
   * Checks for quantity sign.
   * @return boolean if sign is there
   */
  private byte[] consumeQuantity() {
    byte c = next();
    if(!quantity(c)) prev();
    return substring(content, pos, pos);
  }

  /**
   * Checks for all kind of defined tokens.
   * @return boolean if token is found.
   * @throws BuildException Build Exception
   */
  private boolean checkTokenize() throws BuildException {
    int p = pos;
    checkT = consumeName();
    String help = string(checkT);
    if(help.equals(string(ID)) || help.equals(string(IDR))
        || help.equals(string(IDRS)) || help.equals(string(ENT))
        || help.equals(string(ENTS)) || help.equals(string(NMT))
        || help.equals(string(NMTS))) {
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
   * @throws BuildException Build Exception
   */
  private void error() throws BuildException {
    //BaseX.debug(substring(content, pos, pos + 4));
    throw new BuildException("Error while DTD parsing.");
  }
}

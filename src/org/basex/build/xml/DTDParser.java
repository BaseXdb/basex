package org.basex.build.xml;

import static org.basex.build.BuildText.*;
import static org.basex.util.Token.*;
import java.io.File;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.index.Names;
import org.basex.io.IOConstants;

/**
 * Parses the DTD to get the elements, attributes and entities.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class DTDParser {
  /** Root Element Type. */
  private byte[] root;
  /** Element Type. */
  private byte[] element;
  /** Attlist Type. */
  private byte[] attl;
  /** Entity Type. */
  private byte[] enti;
  /** Tokenizer Type. */
  private byte[] checkT;
  /** Root file. */
  private String xmlfile;

  /** Content of internal DTD. */
  private byte[] content;
  /** Current position. */
  private int pos;

  /** Index for all tag and attribute names. */
  Names tags;
  /** Index for all tag and attribute names. */
  Names atts;
  /** Index for all entity names. */
  Names ents;

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
      final Names att, final Names ent) throws IOException {

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
    // check Whitespace
    if(!consumeWS()) error();
    // check for ExternDTD
    if(consume(SYSTEM) && consumeWS()) {
      // check ExternalID in quotes
      byte[] extid = consumeQuoted();
      // read external file
      String file = new File(xmlfile).getParent() + "\\" + string(extid);
      content = IOConstants.read(file);
      pos = 0;
      BaseX.debug("- Root Element Type: %", root);
      BaseX.debug("- Content:\n %", content);
      BaseX.debug("----------------");
      consumeContent();
    }
    // check for InternDTD
    if(consume(SBRACKETO)) {
      content = dtd;
      BaseX.debug("- Root Element Type: %", root);
      BaseX.debug("- Content:\n %", content);
      consumeContent();
    }
  }

  /**
   * Method to consume the Content of Internal or/and External DTD.
   * @throws BuildException Build Exception
   */
  private void consumeContent() throws BuildException {
    // runs till the last character
    while(next() != 0) {
      // checks for element, attlist and entity tags
      if(consume(ELEM) && consumeWS()) {
        element = consumeName();
        tags.add(element);
        BaseX.debug("----------------------");
        BaseX.debug("- Element: %", element);
        contentSpec();
      } else if(consume(ATTL) && consumeWS()) {
        attl = consumeName();
        atts.add(attl);
        BaseX.debug("----------------------");
        BaseX.debug("- ATTLIST: %", attl);
        attType();
        dDecl();
      } else if(consume(ENT) && consumeWS()) {
        if(percentage(next())) {
          if(!consumeWS()) error();
          enti = consumeName();
          ents.add(enti);
          BaseX.debug("----------------------");
          BaseX.debug("- Entity: %", enti);
          if(!consumeWS()) error();
          peDef();
        } else {
          prev();
          enti = consumeName();
          ents.add(enti);
          BaseX.debug("----------------------");
          BaseX.debug("- Entity: %", enti);
          entityDef();
        }
      }
    }
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
        consumeMixed();
      } else {
        consumeChildren();
      }
    } else error();
  }

  /**
   * Consumes mixedContent of Element.
   * @throws BuildException Build Exception
   */
  private void consumeMixed() throws BuildException {
    consumeWS();
    while(!consume(BRACKETC)) {
      consumeWS();
      if(consume(DASH)) {
        consumeWS();
        BaseX.debug(consumeName());
        consumeWS();
      } else if(consume(BRACKETO)) {
        while(!consume(BRACKETC)) {
          consumeWS();
          if(consume(DASH)) {
            consumeWS();
            BaseX.debug(consumeName());
            consumeWS();
          }
        }
        if(checkQuantity()) BaseX.debug(consumeName());
      }
    }
    if(checkQuantity()) BaseX.debug(consumeName());
  }

  /**
   * Consumes childrenContent of Element.
   * @throws BuildException Build Exception
   */
  private void consumeChildren() throws BuildException {
    while(!consume(BRACKETC)) {
      if(consume(BRACKETO)) {
        consumeBracketed();
        } else if(consume(DASH) || consume(COLON)) {
        consumeWS();
        BaseX.debug(consumeName());
        consumeWS();
      } else {
        consumeWS();
        BaseX.debug(consumeName());
        consumeWS();
      }
    }
    if(checkQuantity()) BaseX.debug(consumeName());
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
        BaseX.debug(consumeName());
        consumeWS();
      } else {
        consumeWS();
        BaseX.debug(consumeName());
        consumeWS();
      }
    }
    if(checkQuantity()) BaseX.debug(consumeName());
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
    // sign after name has to be a whitespace
    if(!consumeWS()) error();
    BaseX.debug(consumeName());
    if(!consumeWS()) error();
    if(consume(CD)) {
      BaseX.debug(CD);
    } else if(checkTokenize()) {
      BaseX.debug(checkT);
    } else if(consume(NOT)) {
      if(!consumeWS() && !consume(BRACKETO)) error();
      consumeWS();
      BaseX.debug(consumeName());
      if(consume(BRACKETO)) {
        while(!consume(BRACKETC)) {
          consumeWS();
          if(consume(DASH)) {
            consumeWS();
            BaseX.debug(consumeName());
            consumeWS();
          } else if(consume(BRACKETO)) {
            while(!consume(BRACKETC)) {
              consumeWS();
              if(consume(DASH)) {
                consumeWS();
                BaseX.debug(consumeName());
                consumeWS();
              }
            }
          }
        }
      }
    } else if(consume(BRACKETO)) {
      consumeWS();
      //TODO: consume(NMTOKEN)
      if(consume(BRACKETO)) {
        while(!consume(BRACKETC)) {
          consumeWS();
          if(consume(DASH)) {
            consumeWS();
            //TODO: consume(NMTOKEN)
            consumeWS();
          } else if(consume(BRACKETO)) {
            while(!consume(BRACKETC)) {
              consumeWS();
              if(consume(DASH)) {
                consumeWS();
                //TODO: consume(NMTOKEN)
                consumeWS();
              }
            }
          }
        }
      }
    } else error();
  }

  /**
   * Checks the dDecl for Attlist Objects.
   * @throws BuildException Build Exception
   */
  private void dDecl() throws BuildException {
    // sign after attType has to be a whitespace
    if(!consumeWS()) error();
    // checks for REQUIRED, IMPLIED or FIXED elements
    if(consume(REQ)) {
      BaseX.debug(REQ);
    } else if(consume(IMP)) {
      BaseX.debug(IMP);
    } else if(consume(FIX)) {
      //TODO: (('#FIXED' S)? AttValue);
      BaseX.debug(FIX);
      consumeWS();
      BaseX.debug(consumeQuoted());
    }
  }

  /**
   * Checks the PEDef for Entity Objects.
   */
  private void peDef() {
  //TODO: Implement EntityValue | ExternalID
  }

  /**
   * Checks the EntityDef for Entity Objects.
   */
  private void entityDef() {
  //TODO: Implement EntityValue  | (ExternalID  NDataDecl?)
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
    if(!XMLScanner.isFirstLetter(c) && !percentage(c) && !quantity(c)) {
      System.out.println("Pos: " + string(substring(content, pos, pos + 5)));
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
  private boolean checkQuantity() {
    byte c = next();
    prev();
    if(quantity(c)) return true;
    return false;
  }

  /**
   * Checks for all kind of defined tokens.
   * @return boolean if token is found.
   * @throws BuildException Build Exception
   */
  private boolean checkTokenize() throws BuildException {
    checkT = consumeName();
    if(checkT.equals(ID) || checkT.equals(IDR) || checkT.equals(IDRS)
        || checkT.equals(ENT) || checkT.equals(ENTS) || checkT.equals(NMT)
        || checkT.equals(NMTS)) {
      return true;
    }
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
    throw new BuildException("Error while DTD parsing.");
  }
}

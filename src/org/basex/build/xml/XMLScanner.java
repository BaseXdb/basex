package org.basex.build.xml;

import static org.basex.util.Token.*;
import static org.basex.build.BuildText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.core.Prop;
import org.basex.io.BufferInput;
import org.basex.util.Array;
import org.basex.util.Map;
import org.basex.util.TokenBuilder;

/**
 * This class scans an XML document and creates atomic tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XMLScanner {
  /** Verbose mode. */
  private static final boolean VERBOSE = false;
  /** Scanning states. */
  private static enum State {
    /** Content state.   */ CONTENT,
    /** Tag state.       */ TAG,
    /** Attribute state. */ ATT,
    /** Quoted state.    */ QUOTE,
  }

  /** Document encoding. */
  String encoding = UTF8;
  /** Input stream reference. */
  BufferInput input;
  /** Character buffer for the current token. */
  TokenBuilder token = new TokenBuilder();
  /** Current token type. */
  Type type;
  /** Current line. */
  int line = 1;
  /** Current column. */
  int col = 1;
  /** Flag for character data in text nodes. */
  boolean chars;
  /** Standalone flag. */
  private String standalone;

  /** Buffer with previously read bytes. */
  private final byte[] last = new byte[16];
  /** Buffer with previously read bytes. */
  private int lp;
  /** Pointer to previously read byte. */
  private int pp;

  /** Index for all entity names. */
  public Map ents;

  /** Current scanner state. */
  private State state = State.CONTENT;
  /** Finished flag. */
  private boolean more = true;
  /** Opening tag found. */
  private boolean prolog = true;
  /** Current quote character. */
  private byte quote;

  /**
   * Constructor.
   * @param file input file
   * @throws IOException I/O Exception
   */
  public XMLScanner(final String file) throws IOException {
    this(new BufferInput(file));
  }

  /**
   * Initializes the scanner.
   * @param in input stream
   */
  public XMLScanner(final BufferInput in) {
    input = in;
    ents = new Map();
    ents.add(token("amp"), token("&"));
    ents.add(token("apos"), token("'"));
    ents.add(token("quot"), token("\""));
    ents.add(token("gt"), token("<"));
    ents.add(token("lt"), token(">"));
  }

  /**
   * Read and interpret all tokens from the input stream.
   * @throws BuildException Build Exception
   */
  public void scan() throws BuildException {
    while(more()) next();
  }

  /**
   * Returns true if the document scanning has been completed.
   * @return true if the document scanning has been completed.
   */
  public boolean more() {
    return more;
  }

  /**
   * Reads and interprets the next token from the input stream.
   * @throws BuildException Build Exception
   */
  public void next() throws BuildException {

    // gets next character from the input stream
    token.reset();
    byte ch = nextValid();
    
    // UTF8 header?
    if(prolog && ch == -0x11) {
      if(nextValid() != -0x45 || nextValid() != -0x41) error(INVALID);
      ch = nextValid();
    }
    if(ch == 0) {
      more = false;
      return;
    }

    // checks the scanner state
    switch(state) {
      case CONTENT: scanCONTENT(ch); break;
      case TAG:
      case ATT: scanTAG(ch); break;
      case QUOTE: scanATTVAL(ch);
    }

    if(VERBOSE) {
      // get string representations
      String typ = type.toString();
      String sta = state.toString();
      // formatted output
      while(sta.length() < 12) sta += " ";
      while(typ.length() < 13) typ += " ";
      String out = sta + typ;
      if(token != null) out += "'" + token + "'";
      BaseX.outln(out);
    }
  }

  /**
   * Finishes file scanning.
   * @throws IOException I/O Exception
   */
  public void finish() throws IOException {
    input.close();
    if(prolog) throw new BuildException(BaseX.info(DOCEMPTY));
  }
  
  /**
   * Scans an XML content.
   * @param ch current character
   * @throws BuildException Build Exception
   */
  private void scanCONTENT(final byte ch) throws BuildException {
    int c = ch;
    // parse TEXT
    chars = false;
    if(c != '<' || isCDATA()) {
      if(c == '<') {
        scanCDATA();
        c = nextValid();
      }
      type = Type.TEXT;

      while(c != '<' || isCDATA()) {
        if(c == 0) return;

        if(c == '<') {
          scanCDATA();
        } else {
          if(c == ']') {
            if((nextValid()) == ']') {
              if((nextValid()) == '>') error(CONTCDATA);
              prevChar(1);
            }
            prevChar(1);
          }
          chars |= !whitespace(c);
          if(c == '&') token.add(getEntity());
          else if(c != 0x0d) token.addUTF(c);
        }
        c = nextValid();
      }
      prevChar(1);
      return;
    }

    // parse a TAG
    c = nextChar();
    
    // parse comments etc...
    if(c == '!') {
      scanCOMMENT();
      return;
    }
    // checking a PI
    if(c == '?') {
      scanPI();
      return;
    }

    prolog = false;
    state = State.TAG;
    
    // closing tag...
    if(c == '/') {
      type = Type.L_BR_CLOSE;
      return;
    }
    // opening tag...
    type = Type.L_BR;
    prevChar(1);
  }

  /**
   * Checks input for CDATA section... &lt;![DATA[...]]&gt;.
   * @return true for CDATA
   * @throws BuildException Build Exception
   */
  private boolean isCDATA() throws BuildException {
    int p = 0;
    if(nextChar() != '!') p = 1;
    else if(nextChar() != '[') p = 2;
    else {
      if(!nextToken(CDATA)) error(CDATASEC);
      return true;
    }
    prevChar(p);
    return false;
  }
  
  /**
   * Scans CDATA.
   * @throws BuildException Build Exception
   */
  private void scanCDATA() throws BuildException {
    byte ch = 0;
    chars = true;
    while(true) {
      while((ch = nextChar()) != ']') token.add(ch);
      if((nextChar()) == ']') {
        if((nextChar()) == '>') return;
        prevChar(1);
      }
      prevChar(1);
      token.add(ch);
    } 
  }
  
  /**
   * Scans a comment and doc types.
   * @throws BuildException Build Exception
   */
  private void scanCOMMENT() throws BuildException {
    byte ch = nextChar();
    if(ch != '-') {
      if(!prolog) error(TYPEAFTER);
      
      type = Type.DTD;
      int tag = 1;
      do {
        if(ch == '<') tag++;
        if(ch == '>') tag--;
        if(tag == 0) break;
        token.add(ch);
        ch = nextChar();
      } while(true);
      return;
    }

    type = Type.COMMENT;
    if(nextChar() != '-') error(COMMDASH, token);

    do {
      while((ch = nextChar()) != '-') token.add(ch);
      if((nextChar()) == '-') {
        if((nextChar()) == '>') return;
        error(COMMENTDASH);
      }
      prevChar(1);
      token.add(ch);
    } while(true);
  }

  /**
   * Scans a processing instruction.
   * @throws BuildException Build Exception
   */
  private void scanPI() throws BuildException {
    final boolean start = input.size() < 6;

    byte ch = nextChar();
    if(!isFirstLetter(ch)) error(PINAME);
    do token.add(ch); while(isLetter(ch = nextChar()));

    final byte[] tok = token.finish();
    if(eq(lc(tok), XMLDECL)) {
      if(!eq(tok, XMLDECL)) error(start ? PILC : PIRES);
      if(!start) error(PIXML);

      // process document declaration...
      type = Type.DECL;
      scanWS(ch);
      if(!nextToken(VERS)) error(DECLSTART);
      scanWS();
      if(nextChar() != '=') error(DECLWRONG);
      scanWS();
      if(!nextToken(VERS1) && !nextToken(VERS2)) error(DECLVERSION);
      
      ch = nextChar();
      final TokenBuilder enc = new TokenBuilder();
      if(scanWS(ch)) {
        if(nextToken(ENCOD)) {
          scanWS();
          if(nextChar() != '=') error(DECLWRONG);
          scanWS();
          final byte d = nextChar();
          if(d == '\'' || d == '"') {
            ch = nextChar();
            if(letter(ch)) {
              while(letterOrDigit(ch) || ch == '.' || ch == '-') {
                enc.add(ch);
                ch = nextChar();
              }
              if(ch != d) enc.reset();
            }
          }
          if((ch = nextChar()) == '?') prevChar(1);
          else if(!scanWS(ch)) enc.reset();
          encoding = string(enc.finish());
          if(encoding.length() == 0) error(DECLENCODE);
        }
        if(nextToken(STANDALONE)) {
          scanWS();
          if(nextChar() != '=') error(DECLWRONG);
          scanWS();
          final byte d = nextChar();
          if(d == '\'' || d == '"') {
            if(nextToken(STANDYES)) standalone = string(STANDYES);
            else if(nextToken(STANDNO)) standalone = string(STANDNO);
            if(nextChar() != d) standalone = null;
          }
          if(standalone == null) error(DECLSTANDALONE);
        }
        ch = nextChar();
        if(scanWS(ch)) ch = nextChar();
      }
      if(ch != '?' || nextChar() != '>') error(DECLWRONG);
    } else {
      type = Type.PI;
      if(ch != '?' && !whitespace(ch)) error(PITEXT);
      do {
        while(ch != '?') {
          token.add(ch);
          ch = nextChar();
        }
        if((ch = nextChar()) == '>') return;
        token.add('?');
      } while(true);
    }
  }

  /**
   * Scans an XML tag.
   * @param ch current character
   * @throws BuildException Build Exception
   */
  private void scanTAG(final byte ch) throws BuildException {
    byte c = ch;
    // scan tag end...
    if(c == '>') {
      type = Type.R_BR;
      state = State.CONTENT;
    } else if(c == '=') {
      // scan equal sign...
      type = Type.EQ;
    } else if(c == '\'' || c == '"') {
      // scan quote...
      type = Type.QUOTE;
      state = State.QUOTE;
      quote = c;
    } else if(c == '/') {
      // scan empty tag end...
      type = Type.CLOSE_R_BR;
      if((c = nextChar()) == '>') {
        state = State.CONTENT;
      } else {
        token.add(c);
        error(CLOSING);
      }
    } else if(scanWS(c)) {
      // scan whitespace...
      type = Type.WS;
      return;
    } else if(isFirstLetter(c)) {
      // scan tag name...
      type = state == State.ATT ? Type.ATTNAME : Type.TAGNAME;
      do token.add(c); while(isLetter(c = nextChar()));
      prevChar(1);
      state = State.ATT;
    } else {
      // undefined character...
      error(CHARACTER, (char) c);
    }
  }

  /**
   * Scans a quoted token.
   * @param ch current character
   * @throws BuildException Build Exception
   */
  private void scanATTVAL(final byte ch) throws BuildException {
    int c = ch;
    if(c == quote) {
      type = Type.QUOTE;
      state = State.ATT;
    } else {
      boolean wrong = false;
      type = Type.ATTVALUE;
      do {
        if(c == 0) error(ATTCLOSE , (char) c);
        wrong |= c == '\'' || c == '"';
        if(c == 0x0D) continue;
        if(c == '<') error(wrong ? ATTCLOSE : ATTCHAR, (char) c);
        if(c == 0x0A) c = ' ';
        if(c == '&') token.add(getEntity());
        else token.addUTF(c);
      } while((c = nextValid()) != quote);
      prevChar(1);
    }
  }

  /**
   * Scans whitespace.
   * @throws BuildException Build Exception
   */
  private void scanWS() throws BuildException {
    final byte ch = nextChar();
    if(!scanWS(ch)) prevChar(1);
  }

  /**
   * Scans whitespace.
   * @param ch current character
   * @return true for whitespace
   * @throws BuildException Build Exception
   */
  private boolean scanWS(final byte ch) throws BuildException {
    byte c = ch;
    if(whitespace(c)) {
      do c = nextChar(); while(whitespace(c));
      prevChar(1);
      return true;
    }
    return false;
  }

  /** Character buffer for the current entity. */
  private final TokenBuilder entity = new TokenBuilder();

  /**
   * Scans an entity.
   * @return entity
   * @throws BuildException Build Exception
   */
  private byte[] getEntity() throws BuildException {
    entity.reset();
    byte ch = nextChar();

    // scans numeric entities
    if(ch == '#') {
      int b = 10;
      entity.add(ch = nextChar());
      if(ch == 'x') {
        b = 16;
        entity.add(ch = nextChar());
      }
      int n = 0;
      do {
        final boolean m = ch >= '0' && ch <= '9';
        final boolean h = b == 16 && (ch >= 'a' && ch <= 'f' ||
            ch >= 'A' && ch <= 'F');
        if(!m && !h) {
          completeEntity();
          if(Prop.entity) error(INVALIDENTITY, entity);
          return EMPTY;
        }
        n *= b;
        n += ch & 15;
        if(!m) n += 9;
        entity.add(ch = nextChar());
      } while(ch != ';');
      if(!valid(n)) {
        if(Prop.entity) error(XMLENT, entity);
        return EMPTY;
      }
      entity.reset();
      entity.addUTF(n);
      return entity.finish();
    }
    
    // scans predefined entities
    while(ch != ';') {
      entity.add(ch);
      ch = nextChar();
    };

    final byte[] en = ents.get(entity.finish());
    if(en != null) return en;
    if(Prop.entity) error(XMLENT, entity);
    entity.reset();
    return entity.finish();

    //completeEntity();
    //if(Prop.entity) error(INVALIDENTITY, entity);
    //return 0;
  }

  /**
   * Adds some characters to the entity.
   * @throws BuildException Build Exception
   */
  private void completeEntity() throws BuildException {
    byte ch = nextValid();
    while(entity.size < 10 && ch >= ' ') {
      entity.add(ch);
      if(ch == ';') return;
      ch = nextValid();
    }
  }

  /**
   * Reads next character or throws an exception if all bytes have been read.
   * @return next character
   * @throws BuildException Build Exception
   */
  private byte nextChar() throws BuildException {
    final byte ch = nextValid();
    if(ch == 0) error(UNCLOSED, token);
    return ch;
  }

  /**
   * Reads next character.
   * @return next character
   * @throws BuildException Build Exception
   */
  private byte nextValid() throws BuildException {
    byte ch;
    if(pp != 0) {
      ch = last[(lp + pp++) & 0x0F];
    } else {
      ch = input.readByte();
      last[lp++] = ch;
      lp &= 0x0F;
      if(ch == '\n') {
        col = 1;
        line++;
      } else {
        col++;
      }
    }
    if(ch < ' ' && ch > 0 && !whitespace(ch)) error(XMLCHAR, (char) ch, ch);
    return ch;
  }

  /**
   * Scans the specified token.
   * @param tok token to be found
   * @return true if token was found
   * @throws BuildException Build Exception
   */
  private boolean nextToken(final byte[] tok) throws BuildException {
    for(int t = 0; t < tok.length; t++) {
      if(nextChar() != tok[t]) {
        prevChar(t + 1);
        return false;
      }
    }
    return true;
  }

  /**
   * Jumps the specified number of characters back.
   * @param p number of characters
   */
  public void prevChar(final int p) {
    pp -= p;
  }

  /**
   * Checks if the specified character is an XML whitespace.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean whitespace(final int ch) {
    return ch == 0x09 || ch == 0x0A || ch == 0x0D || ch == 0x20;
  }

  /**
   * Checks if the specified character is an XML whitespace.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean valid(final int ch) {
    return ch >= 0x20 && ch <= 0xD7FF || ch == 0xA || ch == 0x9 || ch == 0xD ||
      ch >= 0xE000 && ch <= 0xFFFD || ch >= 0x10000 && ch <= 0x10ffff;
  }

  /**
   * Checks if the specified character is an XML first-letter.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean isFirstLetter(final byte ch) {
    // <CG> XML/Scanning: check if characters < 0 are really valid
    return letter(ch) || ch == ':' || ch < 0;
  }
  
  /**
   * Checks if the specified character is an XML letter.
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean isLetter(final byte ch) {
    return isFirstLetter(ch) || digit(ch) || ch == '-' || ch == '.';
  }

  /**
   * Throws an error.
   * @param err error message
   * @param arg error arguments
   * @throws BuildException Build Exception
   */
  private void error(final String err, final Object... arg)
      throws BuildException {

    final Object[] tmp = new Object[arg.length + 2];
    Array.copy(arg, tmp, 2);
    tmp[0] = line;
    tmp[1] = col;
    throw new BuildException(SCANPOS + ": " + err, tmp);
  }
  
  /**
   * Finds the name of the DTD (intern or extern).
   * @param dtdHelp the String to be checked.
   */
  public void findDTD(final String dtdHelp) {
    
    String dtdName = dtdHelp.substring(8, dtdHelp.length());
    int save = 0;
    String filename = dtdName;
    
    // search for quotation mark --> extern DTD
    if(dtdName.codePointAt(dtdName.length() - 1) == 34) {
      for (int i = 0; i < dtdName.length() - 1; i++) {
        if (dtdName.codePointAt(i) == 34) {
          save = i;
          filename = dtdName.substring(save + 1, dtdName.length() - 1);
        }
      }
    }
    System.out.println(filename);
  }

  /**
   * Main method; used for testing purposes.
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    // get filename(s) or use default
    final String[] fn = args.length > 0 ? args : new String[] { "input.xml" };
    final StringBuilder sb = new StringBuilder("Accepted:\n");
    
    final long time = System.nanoTime();
    for(final String f : fn) {
      try {
        new XMLScanner(f).scan();
        sb.append(f + "\n");
      } catch(final IOException e) {
        BaseX.errln("%: %", f, e.getMessage());
      }
    }
    BaseX.outln("% ms.", (System.nanoTime() - time) / 1000000);
    BaseX.outln(sb.toString());
  }
}

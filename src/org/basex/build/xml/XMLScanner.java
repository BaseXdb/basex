package org.basex.build.xml;

import static org.basex.Text.*;
import static org.basex.build.BuildText.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLToken.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.build.BuildText.Type;
import org.basex.core.Prop;
import org.basex.io.BufferInput;
import org.basex.io.CachedInput;
import org.basex.io.IOConstants;
import org.basex.util.Map;
import org.basex.util.TokenBuilder;

/**
 * This class scans an XML document and creates atomic tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Andreas Weiler
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
  /** Character buffer for the current token. */
  TokenBuilder token = new TokenBuilder();
  /** Current token type. */
  Type type;
  /** Index for all entity names. */
  Map ents;
  /** Index for all PEReferences. */
  Map pents;
  /** Whitespace flag. */
  boolean ws;
  /** Parameter entity parsing. */
  boolean pe;

  /** Current scanner state. */
  private State state = State.CONTENT;
  /** Opening tag found. */
  private boolean prolog = true;
  /** Tag flag. */
  private boolean tag = false;
  /** Current quote character. */
  private int quote;

  /** XML Input. */
  private XMLInput input;

  /**
   * Constructor.
   * @param f input file
   * @throws IOException I/O Exception
   */
  public XMLScanner(final String f) throws IOException {
    this(new BufferInput(f), f);
  }

  /**
   * Initializes the scanner.
   * @param in input stream
   * @param f input file
   * @throws BuildException Build Exception
   */
  public XMLScanner(final BufferInput in, final String f)
      throws BuildException {
    input = new XMLInput(in, f);
    ents = new Map();
    ents.add(E_AMP, AMP);
    ents.add(E_APOS, APOS);
    ents.add(E_QU, QU);
    ents.add(E_LT, LT);
    ents.add(E_GT, GT);
    pents = new Map();

    if(consume(DOCDECL)) {
      // process document declaration...
      checkS();
      if(!version()) error(DECLSTART);
      boolean s = s();
      final String enc = encoding();
      if(enc != null) {
        if(!s) error(WSERROR);
        encoding = enc;
        s = s();
      }
      if(sddecl() != null && !s) error(WSERROR);
      s();
      final int ch = nextChar();
      if(ch != '?' || nextChar() != '>') error(DECLWRONG);
    }
  }

  /**
   * Read and interpret all tokens from the input stream.
   * @throws BuildException Build Exception
   */
  public void scan() throws BuildException {
    while(more());
  }

  /**
   * Reads and interprets the next token from the input stream.
   * @throws BuildException Build Exception
   * @return true if the document scanning has been completed.
   */
  public boolean more() throws BuildException {
    // gets next character from the input stream
    token.reset();
    final int ch = consume();
    if(ch == 0) {
      type = Type.EOF;
      return false;
    }

    // checks the scanner state
    switch(state) {
      case CONTENT: scanCONTENT(ch); break;
      case TAG:
      case ATT: scanTAG(ch); break;
      case QUOTE: scanATTVALUE(ch);
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
    return true;
  }

  /**
   * Finishes file scanning.
   * @throws IOException I/O Exception
   */
  public void finish() throws IOException {
    input.finish();
    if(prolog) throw new BuildException(DOCEMPTY);
  }

  /**
   * Scans XML content.
   * @param ch current character
   * @throws BuildException Build Exception
   */
  private void scanCONTENT(final int ch) throws BuildException {
    // parse TEXT
    if(!tag && (ch != '<' || isCDATA())) {
      content(ch);
      return;
    }

    // parse a TAG
    tag = false;
    final int c = nextChar();

    // parse comments etc...
    if(c == '!') {
      if(consume(DOCTYPE)) {
        type = Type.DTD;
        dtd();
      } else {
        type = Type.COMMENT;
        if(!consume('-') || !consume('-')) error(COMMDASH);
        comment();
      }
      return;
    }
    // checking a PI
    if(c == '?') {
      type = Type.PI;
      pi();
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
    prev(1);
  }

  /**
   * Scans an XML tag.
   * @param ch current character
   * @throws BuildException Build Exception
   */
  private void scanTAG(final int ch) throws BuildException {
    int c = ch;
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
        token.addUTF(c);
        error(CLOSING);
      }
    } else if(s(c)) {
      // scan whitespace...
      type = Type.WS;
      return;
    } else if(isFirstLetter(c)) {
      // scan tag name...
      type = state == State.ATT ? Type.ATTNAME : Type.TAGNAME;
      do token.addUTF(c); while(isLetter(c = nextChar()));
      prev(1);
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
  private void scanATTVALUE(final int ch) throws BuildException {
    final int c = ch;
    if(c == quote) {
      type = Type.QUOTE;
      state = State.ATT;
    } else {
      type = Type.ATTVALUE;
      attValue(c);
      prev(1);
    }
  }

  /**
   * Scans an attribute value. [10]
   * @param ch current character
   * @throws BuildException Build Exception
   */
  private void attValue(final int ch) throws BuildException {
    boolean wrong = false;
    int c = ch;
    do {
      if(c == 0) error(ATTCLOSE , (char) c);
      wrong |= c == '\'' || c == '"';
      if(c == '<') error(wrong ? ATTCLOSE : ATTCHAR, (char) c);
      if(c == 0x0A) c = ' ';
      if(c == '&') {
        // verify...
        final byte[] r = ref(true);
        if(r.length == 1) token.add(r);
        else if(!input.add(r, false)) error(RECENT);
      } else {
        token.addUTF(c);
      }
    } while((c = consume()) != quote);
  }

  /**
   * Scans XML text.
   * @param ch current character
   * @throws BuildException Build Exception
   */
  private void content(final int ch) throws BuildException {
    type = Type.TEXT;
    ws = true;
    boolean f = true;
    int c = ch;
    while(c != 0) {
      if(c != '<') {
        ws &= ws(c);
        if(c == '&') {
          // verify...
          final byte[] r = ref(true);
          if(r.length == 1) token.add(r);
          else if(!input.add(r, false)) error(RECENT);
        } else {
          if(c == ']') {
            if((consume()) == ']') {
              if((consume()) == '>') error(CONTCDATA);
              prev(1);
            }
            prev(1);
          }
          token.addUTF(c);
        }
      } else {
        if(!f && !isCDATA()) {
          tag = true;
          prev(1);
          return;
        }
        ws = false;
        cDATA();
      }
      c = consume();
      f = false;
    }
  }

  /**
   * Checks input for CDATA section... &lt;![DATA[...]]&gt;.
   * @return true for CDATA
   * @throws BuildException Build Exception
   */
  private boolean isCDATA() throws BuildException {
    if(!consume('!')) return false;
    if(!consume('[')) {
      prev(1);
      return false;
    }
    if(!consume(CDATA)) error(CDATASEC);
    return true;
  }

  /**
   * Scans CDATA.
   * @throws BuildException Build Exception
   */
  private void cDATA() throws BuildException {
    int ch;
    while(true) {
      while((ch = nextChar()) != ']') token.addUTF(ch);
      if(consume(']')) {
        if(consume('>')) return;
        prev(1);
      }
      token.addUTF(ch);
    }
  }

  /**
   * Scans a comment.
   * @throws BuildException Build Exception
   */
  private void comment() throws BuildException {
    do {
      final int ch = nextChar();
      if(ch == '-') {
        if(consume('-')) {
          check('>');
          return;
        }
      }
      token.addUTF(ch);
    } while(true);
  }

  /**
   * Scans a processing instruction.
   * @throws BuildException Build Exception
   */
  private void pi() throws BuildException {
    final byte[] tok = name(true);
    if(eq(lc(tok), XMLDECL)) error(PIRES);

    int ch = nextChar();
    if(ch != '?' && !ws(ch)) error(PITEXT);
    do {
      while(ch != '?') {
        token.addUTF(ch);
        ch = nextChar();
      }
      if((ch = consume()) == '>') return;
      token.add('?');
    } while(true);
  }

  /**
   * Scans whitespaces.
   * @return true for whitespaces
   * @throws BuildException Build Exception
   */
  private boolean s() throws BuildException {
    final int ch = consume();
    if(s(ch)) return true;
    prev(1);
    return false;
  }

  /**
   * Checks input for whitespaces; if none are found, throws an error.
   * @throws BuildException Build Exception
   */
  private void checkS() throws BuildException {
    if(!s()) error(NOWS, (char) consume());
  }

  /**
   * Checks input for the specified character.
   * @param ch character to be found
   * @throws BuildException Build Exception
   */
  private void check(final char ch) throws BuildException {
    final int c = consume();
    if(c != ch) error(WRONGCHAR, ch, (char) c);
  }

  /**
   * Checks input for the specified token.
   * @param tok token to be found
   * @throws BuildException Build Exception
   */
  private void check(final byte[] tok) throws BuildException {
    if(!consume(tok)) error(WRONGCHAR, tok, (char) consume());
  }

  /**
   * Scans whitespaces.
   * @param ch current character
   * @return true for whitespaces
   * @throws BuildException Build Exception
   */
  private boolean s(final int ch) throws BuildException {
    int c = ch;
    if(ws(c)) {
      do c = consume(); while(ws(c));
      prev(1);
      return true;
    }
    return false;
  }

  /**
   * Consumes a quote.
   * @return found quote
   * @throws BuildException Build Exception
   */
  public int qu() throws BuildException {
    final int qu = consume();
    if(qu != '\'' && qu != '"') error(SCANQUOTE, (char) qu);
    return qu;
  }

  /**
   * Scans a reference. [67]
   * @param f dissolve entities
   * @return entity
   * @throws BuildException Build Exception
   */
  private byte[] ref(final boolean f) throws BuildException {
    // scans numeric entities
    if(consume('#')) { // [66]
      final TokenBuilder entity = new TokenBuilder();
      int b = 10;
      int ch = nextChar();
      entity.addUTF(ch);
      if(ch == 'x') {
        b = 16;
        entity.addUTF(ch = nextChar());
      }
      int n = 0;
      do {
        final boolean m = ch >= '0' && ch <= '9';
        final boolean h = b == 16 && (ch >= 'a' && ch <= 'f' ||
            ch >= 'A' && ch <= 'F');
        if(!m && !h) {
          completeRef(entity);
          if(Prop.entity) error(INVALIDENTITY, entity);
          return EMPTY;
        }
        n *= b;
        n += ch & 15;
        if(!m) n += 9;
        entity.addUTF(ch = nextChar());
      } while(ch != ';');

      if(!valid(n)) {
        if(Prop.entity) error(INVALIDENTITY, entity);
        return EMPTY;
      }
      entity.reset();
      entity.addUTF(n);
      return entity.finish();
    }

    // scans predefined entities // [68]
    final byte[] name = name(Prop.entity);
    if(!consume(';')) {
      if(Prop.entity) error(INVALIDENTITY, name);
      return EMPTY;
    }

    if(!f) return concat(AMP, name, SEMI);

    final byte[] en = ents.get(name);
    if(en != null) return en;
    if(Prop.entity) error(UNKNOWNENTITY, name);
    return EMPTY;
  }

  /**
   * Scans a PEReference. [69]
   * @return entity
   * @throws BuildException Build Exception
   */
  private byte[] peRef() throws BuildException {
    // scans predefined entities
    final byte[] name = name(true);
    if(!consume(';')) error(INVALIDENTITY, name);

    final byte[] en = pents.get(name);
    if(en != null) return en;
    if(Prop.entity) error(UNKNOWNENTITY, name);
    return name;
  }

  /**
   * Adds some characters to the entity.
   * @param entity token builder
   * @throws BuildException Build Exception
   */
  private void completeRef(final TokenBuilder entity) throws BuildException {
    int ch = consume();
    while(entity.size < 10 && ch >= ' ' && ch != ';') {
      entity.addUTF(ch);
      ch = consume();
    }
  }

  /**
   * Reads next character or throws an exception if all bytes have been read.
   * @return next character
   * @throws BuildException Build Exception
   */
  private int nextChar() throws BuildException {
    final int ch = consume();
    if(ch == 0) error(UNCLOSED, token);
    return ch;
  }

  /**
   * Jumps the specified number of characters back.
   * @param p number of characters
   */
  private void prev(final int p) {
    input.prev(p);
  }

  /**
   * Reads next character or throws an exception if all bytes have been read.
   * @return next character
   * @throws BuildException Build Exception
   */
  private int consume() throws BuildException {
    while(true) {
      final int ch = input.next();
      if(ch < ' ' && ch > 0 && !ws(ch)) error(XMLCHAR, (char) ch, ch);

      if(ch == '%' && pe) { // [69]
        final byte[] key = name(true);
        final byte[] val = pents.get(key);
        if(val == null) error(UNKNOWNPE, key);
        check(';');
        input.add(val, true);
      } else if(ch != 0x0D) {
        return ch;
      }
    }
  }

  /**
   * Returns the current character.
   * @return current character
   * @throws BuildException Build Exception
   */
  char curr() throws BuildException {
    final int ch = consume();
    prev(1);
    return (char) ch;
  }

  /**
   * Consumes the specified character.
   * @param ch character to be found
   * @return true if token was found
   * @throws BuildException Build Exception
   */
  private boolean consume(final char ch) throws BuildException {
    if(consume() == ch) return true;
    prev(1);
    return false;
  }

  /**
   * Consumes the specified token.
   * @param tok token to be found
   * @return true if token was found
   * @throws BuildException Build Exception
   */
  private boolean consume(final byte[] tok) throws BuildException {
    for(int t = 0; t < tok.length; t++) {
      final int ch = consume();
      if(ch != tok[t]) {
        prev(t + 1);
        return false;
      }
    }
    return true;
  }

  /**
   * Consumes an XML name. [5]
   * @param f force parsing
   * @return name
   * @throws BuildException Build Exception
   */
  private byte[] name(final boolean f) throws BuildException {
    final TokenBuilder name = new TokenBuilder();
    int c = consume();
    if(!isFirstLetter(c)) {
      if(f) error(INVNAME);
      prev(1);
      return null;
    }
    do name.addUTF(c); while(isLetter(c = nextChar()));
    prev(1);
    return name.finish();
  }

  /**
   * Consumes an Nmtoken. [7]
   * @return name
   * @throws BuildException Build Exception
   */
  private byte[] nmtoken() throws BuildException {
    final TokenBuilder name = new TokenBuilder();
    int c;
    while(isLetter(c = nextChar())) name.addUTF(c);
    prev(1);
    if(name.size == 0) error(INVNAME);
    return name.finish();
  }

  /**
   * Scans doc type definitions. [28]
   * @throws BuildException Build Exception
   */
  private void dtd() throws BuildException {
    if(!prolog) error(TYPEAFTER);
    if(!s()) error(ERRDT);

    name(true); // parse root tag
    s(); externalID(true, true); s();

    while(consume('[')) {
      s();
      while(markupDecl());
      s(); check(']'); s();
    }
    check('>');
  }

  /**
   * Scans an external ID.
   * @param f full flag
   * @param r root flag
   * @return id
   * @throws BuildException Build Exception
   */
  private byte[] externalID(final boolean f, final boolean r)
      throws BuildException {
    byte[] cont = null;

    final boolean pub = consume(PUBLIC);
    if(pub || consume(SYSTEM)) {
      checkS();
      if(pub) {
        pubidLit();
        if(f) checkS();
      }
      final int qu = consume(); // [11]
      if(qu == '\'' || qu == '"') {
        int ch = 0;
        final TokenBuilder tok = new TokenBuilder();
        while((ch = nextChar()) != qu) tok.addUTF(ch);
        if(!f) return null;
        final byte[] name = tok.finish();

        final XMLInput tin = input;
        try {
          final String fn = IOConstants.merge(input.file, string(name));
          cont = IOConstants.read(fn);
          input = new XMLInput(new CachedInput(cont), fn);
        } catch(final IOException ex) {
          error(DTDNP, string(name));
        }

        byte[] c = cont;
        if(consume(XML)) {
          check(XMLDECL); s();
          if(version()) checkS();
          s(); if(encoding() == null) error(TEXTENC);
          ch = nextChar();
          if(s(ch)) ch = nextChar();
          if(ch != '?' || nextChar() != '>') error(DECLWRONG);

          c = new byte[cont.length - input.pos()];
          System.arraycopy(cont, input.pos(), c, 0, c.length);
          cont = c;
        }

        s();
        if(r) {
          extSubsetDecl();
          if(!consume((char) 0)) error(INVEND);
        }
        input = tin;
      } else {
        if(f) error(SCANQUOTE, (char) qu);
        prev(1);
      }
    }
    return cont;
  }


  /** PublicID characters. */
  private static final byte[] PUBIDTOK = token(" \n'()+,/=?;!*#@$%");

  /**
   * Scans an public ID literal. [12]
   * @throws BuildException Build Exception
   */
  private void pubidLit() throws BuildException {
    final int qu = qu();
    int ch;
    while((ch = nextChar()) != qu) {
      if(!isLetter(ch) && !contains(PUBIDTOK, ch)) error(PUBID, (char) ch);
    }
  }

  /**
   * Scans an external subset declaration. [31]
   * @return true if a declaration was found
   * @throws BuildException Build Exception
   */
  private boolean extSubsetDecl() throws BuildException {
    boolean found = false;
    while(true) {
      s();
      if(markupDecl()) {
        found = true;
        continue;
      }
      if(!consume(COND)) return found;
      found = true;
      //pe = true;

      s(); // [61
      boolean incl = consume(INCL);
      if(!incl) check(IGNO);
      s();
      check('[');

      if(incl) {
        extSubsetDecl();
        check(CONE);
      } else {
        int c = 1;
        while(c != 0) {
          if(consume(COND)) c++;
          else if(consume(CONE)) c--;
          else if(consume() == 0) error(INVEND);
        }
      }
    }
  }

  /**
   * Scans a markup declaration. [29]
   * @return true if a declaration was found
   * @throws BuildException Build Exception
   */
  private boolean markupDecl() throws BuildException {
    if(consume(ENT)) { // [70]
      checkS();
      if(consume('%')) { // [72] PEDecl
        checkS();
        final byte[] key = name(true);
        checkS();
        byte[] val = entityValue(true); //[74]
        if(val == null) {
          val = externalID(true, false);
          if(val == null) error(INVEND);
        }
        s();
        pents.add(key, val);
      } else { // [71] GEDecl
        final byte[] key = name(true);
        checkS();
        byte[] val = entityValue(false); // [73] EntityDef
        if(val == null) {
          val = externalID(true, false);
          if(val == null) error(INVEND);
          if(s()) {
            check(ND);
            checkS();
            name(true);
          }
        }
        s();
        ents.add(key, val);
      }
      check('>');
      pe = true;
    } else if(consume(ELEM)) { // [45]
      checkS();
      name(true);
      checkS();
      pe = true;
      if(!consume(EMP) && !consume(ANY)) { // [46]
        if(consume('(')) {
          s();
          if(consume(PC)) { // [51]
            s();
            boolean alt = false;
            while(consume('|')) { s(); name(true); s(); alt = true; }
            check(')');
            if(!consume('*') && alt) error(INVEND);
          } else {
            cp();
            check(')');
            occ();
          }
        } else {
          error(INVEND);
        }
      }
      s();
      check('>');
    } else if(consume(ATTL)) { // [52]
      pe = true;
      checkS();
      name(true);
      s();
      while(name(false) != null) { // [53]
        checkS();
        if(!consume(CD) && !consume(IDRS) && !consume(IDR) && !consume(ID) &&
            !consume(ENTS) && !consume(ENT1) && !consume(NMTS) &&
            !consume(NMT)) { // [56]
          if(consume(NOT)) { // [57,58]
            checkS(); check('('); s(); name(true); s();
            while(consume('|')) { s(); name(true); s(); }
            check(')');
          } else { // [59]
            check('('); s(); nmtoken(); s();
            while(consume('|')) { s(); nmtoken(); s(); }
            check(')');
          }
        }

        // [54]
        pe = true;
        checkS();
        if(!consume(REQ) && !consume(IMP)) { // [60]
          if(consume(FIX)) checkS();
          quote = qu();
          attValue(consume());
        }
        s();
      }
      check('>');
    } else if(consume(NOTA)) { // [82]
      checkS();
      name(true);
      s(); externalID(false, false); s();
      check('>');
    } else if(consume(COMS)) {
      comment();
    } else if(consume(XML)) {
      pi();
    } else {
      return false;
    }
    s();
    pe = false;
    return true;
  }

  /**
   * Scans a mixed value and children. [47-50]
   * @throws BuildException Build Exception
   */
  private void cp() throws BuildException {
    s();
    final byte[] name = name(false);
    if(name == null) { check('('); s(); cp(); } else { occ(); }

    s();
    if(consume('|') || consume(',')) {
      cp();
      s();
    }
    if(name == null) {
      check(')');
      occ();
    }
  }

  /**
   * Scans occurrences.
   * @throws BuildException Build Exception
   */
  private void occ() throws BuildException {
    if(consume('+') || consume('?') || consume('*'));
  }

  /**
   * Scans an entity value. [9]
   * @param p pe reference flag
   * @return value
   * @throws BuildException Build Exception
   */
  private byte[] entityValue(final boolean p) throws BuildException {
    final int qu = consume();
    if(qu != '\'' && qu != '"') { prev(1); return null; }
    TokenBuilder tok = new TokenBuilder();
    int ch;
    while((ch = nextChar()) != qu) {
      if(ch == '&') tok.add(ref(false));
      else if(ch == '%') {
        if(!p) error(INVPE);
        tok.add(peRef());
      } else {
        tok.addUTF(ch);
      }
    }

    final XMLInput tmp = input;
    input = new XMLInput(new CachedInput(tok.finish()), input.file);
    tok = new TokenBuilder();
    while((ch = consume()) != 0) {
      if(ch == '&') tok.add(ref(false));
      else tok.addUTF(ch);
    }
    input = tmp;
    return tok.finish();
  }

  /**
   * Scans a document version.
   * @return true if version was found
   * @throws BuildException Build Exception
   */
  private boolean version() throws BuildException {
    if(!consume(VERS)) return false;
    s(); check('='); s();
    final int d = qu();
    if(!consume(VERS10) && !consume(VERS11)) error(DECLVERSION);
    check((char) d);
    return true;
  }

  /**
   * Scans a document encoding.
   * @return encoding
   * @throws BuildException Build Exception
   */
  private String encoding() throws BuildException {
    if(!consume(ENCOD)) return null;
    s(); check('='); s();
    final TokenBuilder enc = new TokenBuilder();
    final int d = qu();
    int ch = nextChar();
    if(letter(ch) && ch != '_') {
      while(letterOrDigit(ch) || ch == '.' || ch == '-') {
        enc.addUTF(ch);
        ch = nextChar();
      }
      prev(1);
    }
    check((char) d);
    if(enc.size == 0) error(DECLENCODE, enc);
    return string(enc.finish());
  }

  /**
   * Scans a standalone flag.
   * @return flag
   * @throws BuildException Build Exception
   */
  private byte[] sddecl() throws BuildException {
    if(!consume(STANDALONE)) return null;
    s(); check('='); s();
    final int d = qu();
    boolean yes = consume(STANDYES);
    boolean no = !yes && consume(STANDNO);
    check((char) d);
    if(!yes && !no) error(DECLSTANDALONE);
    return yes ? STANDYES : STANDNO;
  }

  /**
   * Throws an error.
   * @param e error message
   * @param a error arguments
   * @throws BuildException Build Exception
   */
  private void error(final String e, final Object... a) throws BuildException {
    throw new BuildException(det() + ": " + e, a);
  }

  /**
   * Returns detail info on the scanning process.
   * @return info string
   */
  public String det() {
    final String f = input.file.replaceAll(".*(\\\\|/)", "");
    int c = input.col; if(c != 1) c--;
    return BaseX.info(SCANPOS, f, input.line, c);
  }

  /**
   * Returns per cent info on the scanning process.
   * @return per cent
   */
  public double percent() {
    return (double) input.pos() / input.length();
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

/* TODOS:
 *
 * - Treat tags within entities:
 * <!DOCTYPE doc [
 *    <!ENTITY e "</doc><doc>"> ]>
 * <doc>&e;</doc>
 *
 *
 */

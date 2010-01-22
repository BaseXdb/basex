package org.basex.build.xml;

import static org.basex.build.BuildText.*;
import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLToken.*;
import java.io.IOException;
import java.util.Arrays;
import org.basex.build.BuildException;
import org.basex.build.BuildText.Type;
import org.basex.core.Main;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenMap;

/**
 * This class scans an XML document and creates atomic tokens.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class XMLScanner extends Progress {
  /** PublicID characters. */
  private static final byte[] PUBIDTOK = token(" \n'()+,/=?;!*#@$%");
  /** Quote Entity. */
  private static final byte[] E_QU = token("quot");
  /** Ampersand Entity. */
  private static final byte[] E_AMP = token("amp");
  /** Apostrophe Entity. */
  private static final byte[] E_APOS = token("apos");
  /** GreaterThan Entity. */
  private static final byte[] E_GT = token("gt");
  /** LessThan Entity. */
  private static final byte[] E_LT = token("lt");

  /** Scanning states. */
  private static enum State {
    /** Content state.   */ CONTENT,
    /** Tag state.       */ TAG,
    /** Attribute state. */ ATT,
    /** Quoted state.    */ QUOTE,
  }

  /** Character buffer for the current token. */
  final TokenBuilder token = new TokenBuilder();
  /** Document encoding. */
  String encoding = UTF8;
  /** Current token type. */
  Type type;

  /** Index for all entity names. */
  private final TokenMap ents;
  /** Index for all PEReferences. */
  private final TokenMap pents;
  /** Entity flag. */
  private final boolean entity;
  /** DTD flag. */
  private final boolean dtd;
  /** Parameter entity parsing. */
  private boolean pe;

  /** Current scanner state. */
  private State state = State.CONTENT;
  /** Opening tag found. */
  private boolean prolog = true;
  /** Tag flag. */
  private boolean tag;
  /** Current quote character. */
  private int quote;

  /** XML Input. */
  private XMLInput input;

  /**
   * Initializes the scanner.
   * @param f input file
   * @param pr database properties
   * @throws IOException I/O exception
   */
  public XMLScanner(final IO f, final Prop pr) throws IOException {
    input = new XMLInput(f);
    ents = new TokenMap();
    ents.add(E_AMP, AMP);
    ents.add(E_APOS, APOS);
    ents.add(E_QU, QU);
    ents.add(E_LT, LT);
    ents.add(E_GT, GT);
    pents = new TokenMap();
    entity = pr.is(Prop.ENTITY);
    dtd = pr.is(Prop.DTD);

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
    if(!s(consume())) prev(1);
  }

  /**
   * Reads and interprets the next token from the input stream.
   * @return true if the document scanning has been completed
   * @throws IOException I/O exception
   */
  boolean more() throws IOException {
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
    return true;
  }

  /**
   * Finishes file scanning.
   * @throws IOException I/O exception
   */
  void finish() throws IOException {
    input.finish();
    if(prolog) error(DOCEMPTY);
  }

  /**
   * Scans XML content.
   * @param ch current character
   * @throws IOException I/O exception
   */
  private void scanCONTENT(final int ch) throws IOException {
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
   * @throws IOException I/O exception
   */
  private void scanTAG(final int ch) throws IOException {
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
      do token.addUTF(c); while(isLetterOrDigit(c = nextChar()));
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
   * @throws IOException I/O exception
   */
  private void scanATTVALUE(final int ch) throws IOException {
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
   * @throws IOException I/O exception
   */
  private void attValue(final int ch) throws IOException {
    boolean wrong = false;
    int c = ch;
    do {
      if(c == 0) error(ATTCLOSE, (char) c);
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
   * @throws IOException I/O exception
   */
  private void content(final int ch) throws IOException {
    type = Type.TEXT;
    boolean ws = true;
    boolean f = true;
    int c = ch;
    while(c != 0) {
      if(c != '<') {
        if(ws) ws = ws(c);
        if(c == '&') {
          // verify...
          final byte[] r = ref(true);
          if(r.length == 1) token.add(r);
          else if(!input.add(r, false)) error(RECENT);
        } else {
          if(c == ']') {
            if(consume() == ']') {
              if(consume() == '>') error(CONTCDATA);
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
    if(ws) type = Type.EOF;
  }

  /**
   * Checks input for CDATA section... &lt;![DATA[...]]&gt;.
   * @return true for CDATA
   * @throws IOException I/O exception
   */
  private boolean isCDATA() throws IOException {
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
   * @throws IOException I/O exception
   */
  private void cDATA() throws IOException {
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
   * @throws IOException I/O exception
   */
  private void comment() throws IOException {
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
   * @throws IOException I/O exception
   */
  private void pi() throws IOException {
    final byte[] tok = name(true);
    if(eq(lc(tok), XML)) error(PIRES);
    token.add(tok);

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
   * @throws IOException I/O exception
   */
  private boolean s() throws IOException {
    final int ch = consume();
    if(s(ch)) return true;
    prev(1);
    return false;
  }

  /**
   * Checks input for whitespaces; if none are found, throws an error.
   * @throws IOException I/O exception
   */
  private void checkS() throws IOException {
    if(!s()) error(NOWS, (char) consume());
  }

  /**
   * Checks input for the specified character.
   * @param ch character to be found
   * @throws IOException I/O exception
   */
  private void check(final char ch) throws IOException {
    final int c = consume();
    if(c != ch) error(WRONGCHAR, ch, (char) c);
  }

  /**
   * Checks input for the specified token.
   * @param tok token to be found
   * @throws IOException I/O exception
   */
  private void check(final byte[] tok) throws IOException {
    if(!consume(tok)) error(WRONGCHAR, tok, (char) consume());
  }

  /**
   * Scans whitespaces.
   * @param ch current character
   * @return true for whitespaces
   * @throws IOException I/O exception
   */
  private boolean s(final int ch) throws IOException {
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
   * @throws IOException I/O exception
   */
  private int qu() throws IOException {
    final int qu = consume();
    if(qu != '\'' && qu != '"') error(SCANQUOTE, (char) qu);
    return qu;
  }

  /**
   * Scans a reference. [67]
   * @param f dissolve entities
   * @return entity
   * @throws IOException I/O exception
   */
  private byte[] ref(final boolean f) throws IOException {
    // scans numeric entities
    if(consume('#')) { // [66]
      final TokenBuilder ent = new TokenBuilder();
      int b = 10;
      int ch = nextChar();
      ent.addUTF(ch);
      if(ch == 'x') {
        b = 16;
        ent.addUTF(ch = nextChar());
      }
      int n = 0;
      do {
        final boolean m = ch >= '0' && ch <= '9';
        final boolean h = b == 16 && (ch >= 'a' && ch <= 'f' ||
            ch >= 'A' && ch <= 'F');
        if(!m && !h) {
          completeRef(ent);
          if(entity) error(INVALIDENTITY, ent);
          return EMPTY;
        }
        n *= b;
        n += ch & 15;
        if(!m) n += 9;
        ent.addUTF(ch = nextChar());
      } while(ch != ';');

      if(!valid(n)) {
        if(entity) error(INVALIDENTITY, ent);
        return EMPTY;
      }
      ent.reset();
      ent.addUTF(n);
      return ent.finish();
    }

    // scans predefined entities // [68]
    final byte[] name = name(entity);
    if(!consume(';')) {
      if(entity) error(INVALIDENTITY, name);
      return EMPTY;
    }

    if(!f) return concat(AMP, name, SEMI);

    final byte[] en = ents.get(name);
    if(en != null) return en;
    if(entity) error(UNKNOWNENTITY, name);
    return EMPTY;
  }

  /**
   * Scans a PEReference. [69]
   * @return entity
   * @throws IOException I/O exception
   */
  private byte[] peRef() throws IOException {
    // scans predefined entities
    final byte[] name = name(true);
    if(!consume(';')) error(INVALIDENTITY, name);

    final byte[] en = pents.get(name);
    if(en != null) return en;
    if(entity) error(UNKNOWNENTITY, name);
    return name;
  }

  /**
   * Adds some characters to the entity.
   * @param ent token builder
   * @throws IOException I/O exception
   */
  private void completeRef(final TokenBuilder ent) throws IOException {
    int ch = consume();
    while(ent.size() < 10 && ch >= ' ' && ch != ';') {
      ent.addUTF(ch);
      ch = consume();
    }
  }

  /**
   * Reads next character or throws an exception if all bytes have been read.
   * @return next character
   * @throws IOException I/O exception
   */
  private int nextChar() throws IOException {
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
   * @throws IOException I/O exception
   */
  private int consume() throws IOException {
    while(true) {
      final int ch = input.next();
      if(ch < 0) error(XMLCHAR, -ch);
      if(ch > 0 && ch < ' ' && !ws(ch)) error(XMLCHAR, ch);

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
   * Consumes the specified character.
   * @param ch character to be found
   * @return true if token was found
   * @throws IOException I/O exception
   */
  private boolean consume(final char ch) throws IOException {
    if(consume() == ch) return true;
    prev(1);
    return false;
  }

  /**
   * Consumes the specified token.
   * @param tok token to be found
   * @return true if token was found
   * @throws IOException I/O exception
   */
  private boolean consume(final byte[] tok) throws IOException {
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
   * @throws IOException I/O exception
   */
  private byte[] name(final boolean f) throws IOException {
    final TokenBuilder name = new TokenBuilder();
    int c = consume();
    if(!isFirstLetter(c)) {
      if(f) error(INVNAME);
      prev(1);
      return null;
    }
    do name.addUTF(c); while(isLetterOrDigit(c = nextChar()));
    prev(1);
    return name.finish();
  }

  /**
   * Consumes an Nmtoken. [7]
   * @return name
   * @throws IOException I/O exception
   */
  private byte[] nmtoken() throws IOException {
    final TokenBuilder name = new TokenBuilder();
    int c;
    while(isLetterOrDigit(c = nextChar())) name.addUTF(c);
    prev(1);
    if(name.size() == 0) error(INVNAME);
    return name.finish();
  }

  /**
   * Scans doc type definitions. [28]
   * @throws IOException I/O exception
   */
  private void dtd() throws IOException {
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
   * @throws IOException I/O exception
   */
  private byte[] externalID(final boolean f, final boolean r)
      throws IOException {
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
        final String name = string(tok.finish());
        if(!dtd && r) return cont;

        final XMLInput tin = input;
        try {
          final IO file = input.file.merge(name);
          cont = file.content();
          input = new XMLInput(new IOContent(cont, name));
        } catch(final IOException ex) {
          Main.debug(ex);
          error(PARSEERR, name);
        }

        if(consume(XDECL)) {
          check(XML); s();
          if(version()) checkS();
          s(); if(encoding() == null) error(TEXTENC);
          ch = nextChar();
          if(s(ch)) ch = nextChar();
          if(ch != '?' || nextChar() != '>') error(DECLWRONG);
          cont = Arrays.copyOfRange(cont, input.pos(), cont.length);
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

  /**
   * Scans an public ID literal. [12]
   * @throws IOException I/O exception
   */
  private void pubidLit() throws IOException {
    final int qu = qu();
    int ch;
    while((ch = nextChar()) != qu) {
      if(!isLetterOrDigit(ch) && !contains(PUBIDTOK, ch))
        error(PUBID, (char) ch);
    }
  }

  /**
   * Scans an external subset declaration. [31]
   * @return true if a declaration was found
   * @throws IOException I/O exception
   */
  private boolean extSubsetDecl() throws IOException {
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
      final boolean incl = consume(INCL);
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
   * @throws IOException I/O exception
   */
  private boolean markupDecl() throws IOException {
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
            s();
            //check(')'); // to be fixed...
            while(!consume(')')) consume();
            //input.prev(1);
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
   * @throws IOException I/O exception
   */
  private void cp() throws IOException {
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
   * @throws IOException I/O exception
   */
  private void occ() throws IOException {
    if(!consume('+') && !consume('?')) consume('*');
  }

  /**
   * Scans an entity value. [9]
   * @param p pe reference flag
   * @return value
   * @throws IOException I/O exception
   */
  private byte[] entityValue(final boolean p) throws IOException {
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
    input = new XMLInput(new IOContent(tok.finish()));
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
   * @throws IOException I/O exception
   */
  private boolean version() throws IOException {
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
   * @throws IOException I/O exception
   */
  private String encoding() throws IOException {
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
    if(enc.size() == 0) error(DECLENCODE, enc);
    final String e = string(enc.finish());
    input.encoding(e);
    return e;
  }

  /**
   * Scans a standalone flag.
   * @return flag
   * @throws IOException I/O exception
   */
  private byte[] sddecl() throws IOException {
    if(!consume(STANDALONE)) return null;
    s(); check('='); s();
    final int d = qu();
    final boolean yes = consume(STANDYES);
    final boolean no = !yes && consume(STANDNO);
    check((char) d);
    if(!yes && !no) error(DECLSTANDALONE);
    return yes ? STANDYES : STANDNO;
  }

  /**
   * Throws an error.
   * @param e error message
   * @param a error arguments
   * @throws IOException I/O exception
   */
  private void error(final String e, final Object... a) throws IOException {
    throw new BuildException(det() + ": " + e, a);
  }

  @Override
  public String det() {
    return Main.info(SCANPOS, input.file.name(), input.line);
  }

  @Override
  public double prog() {
    return (double) input.pos() / input.length();
  }
}

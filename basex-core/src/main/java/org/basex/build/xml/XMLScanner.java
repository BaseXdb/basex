package org.basex.build.xml;

import static org.basex.build.BuildText.*;
import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLToken.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.build.BuildText.Type;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class scans an XML document and creates atomic tokens.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class XMLScanner extends Proc {
  /** Entities. */
  private static final String[] ENTITIES =
    { "amp", "&", "apos", "'", "quot", "\"", "lt", "<", "gt", ">" };
  /** PublicID characters. */
  private static final byte[] PUBIDTOK = token(" \n'()+,/=?;!*#@$%");
  /** Question mark. */
  private static final byte[] QUESTION = { '?' };
  /** Ampersand entity. */
  private static final byte[] AMPER = { '&' };

  /** Scanning states. */
  private enum State {
    /** Content state.   */ CONTENT,
    /** Element state.   */ ELEMENT,
    /** Attribute state. */ ATT,
    /** Quoted state.    */ QUOTE,
  }

  /** Character buffer for the current token. */
  final TokenBuilder token = new TokenBuilder();
  /** Document encoding. */
  final String encoding;
  /** Current token type. */
  Type type;

  /** Index for all entity names. */
  private final TokenMap ents = new TokenMap();
  /** Index for all PEReferences. */
  private final TokenMap pents = new TokenMap();
  /** DTD flag. */
  private final boolean dtd;
  /** Allow document fragment as input. */
  private final boolean fragment;

  /** Current scanner state. */
  private State state = State.CONTENT;
  /** Scanning prolog (will be invalidated when root element is parsed). */
  private boolean prolog = true;
  /** Parameter entity parsing. */
  private boolean pe;
  /** Text mode. */
  private boolean text = true;
  /** Current quote character. */
  private int quote;
  /** XML input. */
  private XMLInput input;

  /**
   * Initializes the scanner.
   * @param file input file
   * @param opts database options
   * @param fragment allow parsing of document fragment
   * @throws IOException I/O exception
   */
  XMLScanner(final IO file, final MainOptions opts, final boolean fragment) throws IOException {
    this.fragment = fragment;
    input = new XMLInput(file);

    try {
      final int el = ENTITIES.length;
      for(int e = 0; e < el; e += 2) ents.put(ENTITIES[e], ENTITIES[e + 1]);
      dtd = opts.get(MainOptions.DTD);

      String enc = null;
      // process document declaration...
      if(consume(DOCDECL)) {
        if(s()) {
          if(!version()) throw error(DECLSTART);
          boolean s = s();
          enc = encoding();
          if(enc != null) {
            if(!s) throw error(WSERROR);
            s = s();
          }
          if(sddecl() != null && !s) throw error(WSERROR);
          s();
          int ch = nextChar();
          if(ch != '?') throw error(WRONGCHAR, '?', (char) ch);
          ch = nextChar();
          if(ch != '>') throw error(WRONGCHAR, '>', (char) ch);
        } else {
          prev(5);
        }
      }
      encoding = enc == null ? Strings.UTF8 : enc;

      if(!fragment) {
        final int n = consume();
        if(!s(n)) {
          if(n != '<') throw error(n == 0 ? DOCEMPTY : BEFOREROOT);
          prev(1);
        }
      }
    } catch(final IOException ex) {
      input.close();
      throw ex;
    }
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
      case ELEMENT:
      case ATT: scanELEMENT(ch); break;
      case QUOTE: scanATTVALUE(ch);
    }
    return true;
  }

  /**
   * Finishes file scanning.
   * @throws IOException I/O exception
   */
  void close() throws IOException {
    input.close();
    if(!fragment && prolog) throw error(DOCEMPTY);
  }

  /**
   * Scans XML content.
   * @param ch current character
   * @throws IOException I/O exception
   */
  private void scanCONTENT(final int ch) throws IOException {
    // parse TEXT
    if(text && (ch != '<' || isCDATA())) {
      content(ch);
      return;
    }

    // parse ELEMENT
    text = true;
    final int c = nextChar();

    // parse comments etc...
    if(c == '!') {
      if(consume('-') && consume('-')) {
        type = Type.COMMENT;
        comment();
      } else if(!fragment && consume(DOCTYPE)) {
        type = Type.DTD;
        dtd();
      } else {
        throw error(COMMDASH);
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
    state = State.ELEMENT;

    // closing element...
    if(c == '/') {
      type = Type.L_BR_CLOSE;
      return;
    }
    // opening element...
    type = Type.L_BR;
    prev(1);
  }

  /**
   * Scans an XML element.
   * @param ch current character
   * @throws IOException I/O exception
   */
  private void scanELEMENT(final int ch) throws IOException {
    int c = ch;
    // scan element end...
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
      // scan empty element end...
      type = Type.CLOSE_R_BR;
      if((c = nextChar()) == '>') {
        state = State.CONTENT;
      } else {
        token.add(c);
        throw error(CLOSING);
      }
    } else if(s(c)) {
      // scan whitespace...
      type = Type.WS;
    } else if(isStartChar(c)) {
      // scan name of attribute or element...
      type = state == State.ATT ? Type.ATTNAME : Type.ELEMNAME;
      do token.add(c); while(isChar(c = nextChar()));
      prev(1);
      state = State.ATT;
    } else {
      // undefined character...
      throw error(CHARACTER, (char) c);
    }
  }

  /**
   * Scans a quoted token.
   * @param ch current character
   * @throws IOException I/O exception
   */
  private void scanATTVALUE(final int ch) throws IOException {
    if(ch == quote) {
      type = Type.QUOTE;
      state = State.ATT;
    } else {
      type = Type.ATTVALUE;
      attValue(ch);
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
      if(c == 0) throw error(ATTCLOSE, (char) 0);
      wrong |= c == '\'' || c == '"';
      if(c == '<') throw error(wrong ? ATTCLOSE : ATTCHAR, '<');
      if(c == 0x0A) c = ' ';
      if(c == '&') {
        // verify...
        final byte[] r = ref(true);
        if(r.length == 1) token.add(r);
        else if(!input.add(r, false)) throw error(RECENT);
      } else {
        token.add(c);
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
    boolean f = true;
    int c = ch;
    while(c != 0) {
      if(c == '<') {
        if(!f && !isCDATA()) {
          text = false;
          prev(1);
          return;
        }
        cDATA();
      } else {
        if(c == '&') {
          // scan entity
          final byte[] r = ref(true);
          if(r.length == 1) token.add(r);
          else if(!input.add(r, false)) throw error(RECENT);
        } else {
          if(c == ']') {
            // ']]>' not allowed in content
            if(consume() == ']') {
              if(consume() == '>') throw error(CONTCDATA);
              prev(1);
            }
            prev(1);
          }
          // add character to cached content
          token.add(c);
        }
      }
      c = consume();
      f = false;
    }
    // end of file
    if(!fragment) {
      if(!ws(token.toArray())) throw error(AFTERROOT);
      type = Type.EOF;
    }
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
    if(!consume(CDATA)) throw error(CDATASEC);
    return true;
  }

  /**
   * Scans CDATA.
   * @throws IOException I/O exception
   */
  private void cDATA() throws IOException {
    while(true) {
      int ch;
      while((ch = nextChar()) != ']') token.add(ch);
      if(consume(']')) {
        if(consume('>')) return;
        prev(1);
      }
      token.add(ch);
    }
  }

  /**
   * Scans a comment.
   * @throws IOException I/O exception
   */
  private void comment() throws IOException {
    do {
      final int ch = nextChar();
      if(ch == '-' && consume('-')) {
        check('>');
        return;
      }
      token.add(ch);
    } while(true);
  }

  /**
   * Scans a processing instruction.
   * @throws IOException I/O exception
   */
  private void pi() throws IOException {
    final byte[] tok = name(true);
    if(eq(lc(tok), XML)) throw error(PIRES);
    token.add(tok);

    int ch = nextChar();
    if(ch != '?' && !ws(ch)) throw error(PITEXT);
    do {
      while(ch != '?') {
        token.add(ch);
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
   * Checks input for whitespaces; if none are found, throws an exception.
   * @throws IOException I/O exception
   */
  private void checkS() throws IOException {
    if(!s()) throw error(NOWS, (char) consume());
  }

  /**
   * Checks input for the specified character.
   * @param ch character to be found
   * @throws IOException I/O exception
   */
  private void check(final char ch) throws IOException {
    final int c = consume();
    if(c != ch) throw error(WRONGCHAR, ch, (char) c);
  }

  /**
   * Checks input for the specified token.
   * @param tok token to be found
   * @throws IOException I/O exception
   */
  private void check(final byte[] tok) throws IOException {
    if(!consume(tok)) throw error(WRONGCHAR, tok, (char) consume());
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
    if(qu != '\'' && qu != '"') throw error(SCANQUOTE, (char) qu);
    return qu;
  }

  /**
   * Scans a reference. [67]
   * @param e dissolve entities
   * @return entity
   * @throws IOException I/O exception
   */
  private byte[] ref(final boolean e) throws IOException {
    // scans numeric entities
    if(consume('#')) { // [66]
      final TokenBuilder ent = new TokenBuilder();
      int ch = nextChar();
      ent.add(ch);
      int b = 10;
      if(ch == 'x') {
        b = 0x10;
        ent.add(ch = nextChar());
      }
      int n = 0;
      do {
        final boolean m = ch >= '0' && ch <= '9';
        final boolean h = b == 0x10 && (ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F');
        if(!m && !h) {
          completeRef(ent);
          return QUESTION;
        }
        n *= b;
        n += ch & 0x0F;
        if(!m) n += 9;
        ent.add(ch = nextChar());
      } while(ch != ';');

      if(!valid(n)) return QUESTION;
      ent.reset();
      ent.add(n);
      return ent.finish();
    }

    // scans predefined entities [68]
    final byte[] name = name(false);
    if(!consume(';')) return QUESTION;

    if(!e) return concat(AMPER, name, SEMI);

    byte[] en = ents.get(name);
    if(en == null) en = getEntity(name);
    return en == null ? QUESTION : en;
  }

  /**
   * Scans a PEReference. [69]
   * @return entity, or {@code null}
   * @throws IOException I/O exception
   */
  private byte[] peRef() throws IOException {
    // scans predefined entities
    final byte[] name = name(true);
    consume(';');

    final byte[] en = pents.get(name);
    return en == null ? name : en;
  }

  /**
   * Adds some characters to the entity.
   * @param ent token builder
   * @throws IOException I/O exception
   */
  private void completeRef(final TokenBuilder ent) throws IOException {
    int ch = consume();
    while(ent.size() < 10 && ch >= ' ' && ch != ';') {
      ent.add(ch);
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
    if(ch == 0) throw error(UNCLOSED, token);
    return ch;
  }

  /**
   * Jumps the specified number of characters back.
   * @param num number of characters
   */
  private void prev(final int num) {
    input.prev(num);
  }

  /**
   * Reads next character or throws an exception if all bytes have been read.
   * @return next character
   * @throws IOException I/O exception
   */
  private int consume() throws IOException {
    while(true) {
      final int ch = input.read();
      if(ch == -1) return 0;
      if(ch == '%' && pe) { // [69]
        final byte[] key = name(true);
        final byte[] val = pents.get(key);
        if(val == null) throw error(UNKNOWNPE, key);
        check(';');
        input.add(val, true);
      } else {
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
    final int tl = tok.length;
    for(int t = 0; t < tl; t++) {
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
   * @param force force parsing
   * @return name, or {@code null}
   * @throws IOException I/O exception
   */
  private byte[] name(final boolean force) throws IOException {
    final TokenBuilder name = new TokenBuilder();
    int c = consume();
    if(!isStartChar(c)) {
      if(force) throw error(INVNAME);
      prev(1);
      return null;
    }
    do name.add(c); while(isChar(c = nextChar()));
    prev(1);
    return name.finish();
  }

  /**
   * Consumes an Nmtoken. [7]
   * @throws IOException I/O exception
   */
  private void nmtoken() throws IOException {
    final TokenBuilder name = new TokenBuilder();
    int c;
    while(isChar(c = nextChar())) name.add(c);
    prev(1);
    if(name.isEmpty()) throw error(INVNAME);
  }

  /**
   * Scans doc type definitions. [28]
   * @throws IOException I/O exception
   */
  private void dtd() throws IOException {
    if(!prolog) throw error(TYPEAFTER);
    if(!s()) throw error(ERRDT);

    name(true); // parse root element
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
   * @param full full flag
   * @param root root flag
   * @return id, or {@code null}
   * @throws IOException I/O exception
   */
  private byte[] externalID(final boolean full, final boolean root) throws IOException {
    byte[] cont = null;
    final boolean pub = consume(PUBLIC);
    if(pub || consume(SYSTEM)) {
      checkS();
      if(pub) {
        pubidLit();
        if(full) checkS();
      }
      final int qu = consume(); // [11]
      if(qu == '\'' || qu == '"') {
        int ch;
        final TokenBuilder tok = new TokenBuilder();
        while((ch = nextChar()) != qu) tok.add(ch);
        if(!full) return null;
        final String name = string(tok.finish());
        if(!dtd && root) return null;

        final XMLInput tin = input;
        if(dtd) {
          try {
            cont = input.io().merge(name).read();
          } catch(final IOException ex) {
            throw error(Util.message(ex));
          }
        } else {
          cont = new byte[0];
        }
        input = new XMLInput(new IOContent(cont, name));

        if(consume(XDECL)) {
          check(XML); s();
          if(version()) checkS();
          s(); if(encoding() == null) throw error(TEXTENC);
          ch = nextChar();
          if(s(ch)) ch = nextChar();
          if(ch != '?') throw error(WRONGCHAR, '?', ch);
          ch = nextChar();
          if(ch != '>') throw error(WRONGCHAR, '>', ch);
          cont = Arrays.copyOfRange(cont, input.pos(), cont.length);
        }

        s();
        if(root) {
          extSubsetDecl();
          if(!consume((char) 0)) throw error(INVEND);
        }
        input = tin;
      } else {
        if(full) throw error(SCANQUOTE, (char) qu);
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
      if(!isChar(ch) && !contains(PUBIDTOK, ch)) throw error(PUBID, (char) ch);
    }
  }

  /**
   * Scans an external subset declaration. [31]
   * @throws IOException I/O exception
   */
  private void extSubsetDecl() throws IOException {
    while(true) {
      s();
      if(markupDecl()) continue;
      if(!consume(COND)) return;

      s(); // [61]
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
          if(consume(COND)) ++c;
          else if(consume(CONE)) --c;
          else if(consume() == 0) throw error(INVEND);
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
          if(val == null) throw error(INVEND);
        }
        s();
        pents.put(key, val);
      } else { // [71] GEDecl
        final byte[] key = name(true);
        checkS();
        byte[] val = entityValue(false); // [73] EntityDef
        if(val == null) {
          val = externalID(true, false);
          if(val == null) throw error(INVEND);
          if(s()) {
            check(ND);
            checkS();
            name(true);
          }
        }
        s();
        ents.put(key, val);
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
            if(!consume('*') && alt) throw error(INVEND);
          } else {
            cp();
            s();
            //check(')'); // to be fixed...
            while(!consume(')')) consume();
            //input.prev(1);
            occ();
          }
        } else {
          throw error(INVEND);
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
   * @return value, or {@code null}
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
        if(!p) throw error(INVPE);
        tok.add(peRef());
      } else {
        tok.add(ch);
      }
    }

    final XMLInput tmp = input;
    input = new XMLInput(new IOContent(tok.finish()));
    tok = new TokenBuilder();
    while((ch = consume()) != 0) {
      if(ch == '&') tok.add(ref(false));
      else tok.add(ch);
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
    if(!consume(VERS10) && !consume(VERS11)) throw error(DECLVERSION);
    check((char) d);
    return true;
  }

  /**
   * Scans a document encoding.
   * @return encoding, or {@code null}
   * @throws IOException I/O exception
   */
  private String encoding() throws IOException {
    if(!consume(ENCOD)) {
      if(fragment) throw error(TEXTENC);
      return null;
    }
    s(); check('='); s();
    final TokenBuilder enc = new TokenBuilder();
    final int d = qu();
    int ch = nextChar();
    if(letter(ch) && ch != '_') {
      while(letterOrDigit(ch) || ch == '.' || ch == '-') {
        enc.add(ch);
        ch = nextChar();
      }
      prev(1);
    }
    check((char) d);
    if(enc.isEmpty()) throw error(DECLENCODE, enc);
    final String e = string(enc.finish());
    input.encoding(e);
    return e;
  }

  /**
   * Scans a standalone flag.
   * @return flag, or {@code null}
   * @throws IOException I/O exception
   */
  private byte[] sddecl() throws IOException {
    if(!consume(STANDALONE)) return null;
    s(); check('='); s();
    final int d = qu();
    byte[] sd = token(NO);
    if(!consume(sd)) {
      sd = token(YES);
      if(!consume(sd) || fragment) throw error(DECLSTANDALONE);
    }
    check((char) d);
    return sd;
  }

  /**
   * Throws an exception.
   * @param message error message
   * @param ext error arguments
   * @return build exception (indicates that an error is raised)
   */
  private BuildException error(final String message, final Object... ext) {
    return new BuildException(det() + COLS + message, ext);
  }

  @Override
  public String det() {
    final String path = input.io().path();
    return path.isEmpty() ? Util.info(LINE_X, input.line()) :
        Util.info(SCANPOS_X_X, input.io().path(), input.line());
  }

  @Override
  public double prog() {
    final double l = input.length();
    return l <= 0 ? 0 : input.pos() / l;
  }
}

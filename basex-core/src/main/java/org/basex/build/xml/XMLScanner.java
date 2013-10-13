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
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Andreas Weiler
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
    /** Tag state.       */ TAG,
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
   * @param f input file
   * @param opts database options
   * @param frag allow parsing of document fragment
   * @throws IOException I/O exception
   */
  XMLScanner(final IO f, final MainOptions opts, final boolean frag) throws IOException {
    input = new XMLInput(f);
    fragment = frag;

    try {
      for(int e = 0; e < ENTITIES.length; e += 2) ents.put(ENTITIES[e], ENTITIES[e + 1]);
      dtd = opts.get(MainOptions.DTD);

      String enc = null;
      // process document declaration...
      if(consume(DOCDECL)) {
        if(s()) {
          if(!version()) error(DECLSTART);
          boolean s = s();
          enc = encoding();
          if(enc != null) {
            if(!s) error(WSERROR);
            s = s();
          }
          if(sddecl() != null && !s) error(WSERROR);
          s();
          int ch = nextChar();
          if(ch != '?') error(WRONGCHAR, '?', (char) ch);
          ch = nextChar();
          if(ch != '>') error(WRONGCHAR, '>', (char) ch);
        } else {
          prev(5);
        }
      }
      encoding = enc == null ? UTF8 : enc;

      if(!fragment) {
        final int n = consume();
        if(!s(n)) {
          if(n != '<') error(n == 0 ? DOCEMPTY : BEFOREROOT);
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
  void close() throws IOException {
    input.close();
    if(!fragment && prolog) error(DOCEMPTY);
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

    // parse a TAG
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
        error(COMMDASH);
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
        token.add(c);
        error(CLOSING);
      }
    } else if(s(c)) {
      // scan whitespace...
      type = Type.WS;
    } else if(isStartChar(c)) {
      // scan tag name...
      type = state == State.ATT ? Type.ATTNAME : Type.ELEMNAME;
      do token.add(c); while(isChar(c = nextChar()));
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
      if(c != '<') {
        if(c == '&') {
          // scan entity
          final byte[] r = ref(true);
          if(r.length == 1) token.add(r);
          else if(!input.add(r, false)) error(RECENT);
        } else {
          if(c == ']') {
            // ']]>' not allowed in content
            if(consume() == ']') {
              if(consume() == '>') error(CONTCDATA);
              prev(1);
            }
            prev(1);
          }
          // add character to cached content
          token.add(c);
        }
      } else {
        if(!f && !isCDATA()) {
          text = false;
          prev(1);
          return;
        }
        cDATA();
      }
      c = consume();
      f = false;
    }
    // end of file
    if(!fragment) {
      if(!ws(token.finish())) error(AFTERROOT);
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
    if(eq(lc(tok), XML)) error(PIRES);
    token.add(tok);

    int ch = nextChar();
    if(ch != '?' && !ws(ch)) error(PITEXT);
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
      ent.add(ch);
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

    if(!f) return concat(AMPER, name, SEMI);

    byte[] en = ents.get(name);
    if(en == null) {
      // unknown entity: try HTML entities (lazy initialization)
      if(HTMLENTS.isEmpty()) {
        for(int s = 0; s < HTMLENTITIES.length; s += 2) {
          HTMLENTS.put(HTMLENTITIES[s], HTMLENTITIES[s + 1]);
        }
      }
      en = HTMLENTS.get(name);
    }
    return en == null ? QUESTION : en;
  }

  /**
   * Scans a PEReference. [69]
   * @return entity
   * @throws IOException I/O exception
   */
  private byte[] peRef() throws IOException {
    // scans predefined entities
    final byte[] name = name(true);
    consume(';');

    final byte[] en = pents.get(name);
    if(en != null) return en;
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
      final int ch = input.read();
      if(ch == -1) return 0;
      if(ch == '%' && pe) { // [69]
        final byte[] key = name(true);
        final byte[] val = pents.get(key);
        if(val == null) error(UNKNOWNPE, key);
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
    for(int t = 0; t < tok.length; ++t) {
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
    if(!isStartChar(c)) {
      if(f) error(INVNAME);
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
    if(name.isEmpty()) error(INVNAME);
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
  private byte[] externalID(final boolean f, final boolean r) throws IOException {
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
        int ch;
        final TokenBuilder tok = new TokenBuilder();
        while((ch = nextChar()) != qu) tok.add(ch);
        if(!f) return null;
        final String name = string(tok.finish());
        if(!dtd && r) return cont;

        final XMLInput tin = input;
        try {
          final IO file = input.io().merge(name);
          cont = file.read();
        } catch(final IOException ex) {
          Util.debug(ex);
          // skip unknown DTDs/entities
          cont = new byte[] { '?' };
        }
        input = new XMLInput(new IOContent(cont, name));

        if(consume(XDECL)) {
          check(XML); s();
          if(version()) checkS();
          s(); if(encoding() == null) error(TEXTENC);
          ch = nextChar();
          if(s(ch)) ch = nextChar();
          if(ch != '?') error(WRONGCHAR, '?', ch);
          ch = nextChar();
          if(ch != '>') error(WRONGCHAR, '>', ch);
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
      if(!isChar(ch) && !contains(PUBIDTOK, ch)) error(PUBID, (char) ch);
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
        pents.put(key, val);
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
    if(!consume(ENCOD)) {
      if(fragment) error(TEXTENC);
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
    if(enc.isEmpty()) error(DECLENCODE, enc);
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
    byte[] sd = token(NO);
    if(!consume(sd)) {
      sd = token(YES);
      if(!consume(sd) || fragment) error(DECLSTANDALONE);
    }
    check((char) d);
    return sd;
  }

  /**
   * Throws an error.
   * @param e error message
   * @param a error arguments
   * @return build exception (indicates that an error is raised)
   * @throws BuildException build exception
   */
  private BuildException error(final String e, final Object... a) throws BuildException {
    throw new BuildException(det() + COLS + e, a);
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

  /** Index for all HTML entities. */
  private static final TokenMap HTMLENTS = new TokenMap();
  /** HTML entities. */
  private static final String[] HTMLENTITIES = { "Aacute", "\u00c1", "aacute",
    "\u00e1", "Acirc", "\u00c2", "acirc", "\u00e2", "acute", "\u00b4",
    "AElig", "\u00c6", "aelig", "\u00e6", "Agrave", "\u00c0", "agrave",
    "\u00e0", "alefsym", "\u2135", "Alpha", "\u0391", "alpha", "\u03b1",
    "and", "\u2227", "ang", "\u2220", "Aring", "\u00c5", "aring", "\u00e5",
    "asymp", "\u2248", "Atilde", "\u00c3", "atilde", "\u00e3", "Auml",
    "\u00c4", "auml", "\u00e4", "bdquo", "\u201e", "Beta", "\u0392", "beta",
    "\u03b2", "brvbar", "\u00a6", "bull", "\u2022", "cap", "\u2229",
    "Ccedil", "\u00c7", "ccedil", "\u00e7", "cedil", "\u00b8", "cent",
    "\u00a2", "Chi", "\u03a7", "chi", "\u03c7", "circ", "\u02c6", "clubs",
    "\u2663", "cong", "\u2245", "copy", "\u00a9", "crarr", "\u21b5", "cup",
    "\u222a", "curren", "\u00a4", "dagger", "\u2020", "Dagger", "\u2021",
    "darr", "\u2193", "dArr", "\u21d3", "deg", "\u00b0", "Delta", "\u0394",
    "delta", "\u03b4", "diams", "\u2666", "divide", "\u00f7", "Eacute",
    "\u00c9", "eacute", "\u00e9", "Ecirc", "\u00ca", "ecirc", "\u00ea",
    "Egrave", "\u00c8", "egrave", "\u00e8", "empty", "\u2205", "emsp",
    "\u2003", "ensp", "\u2002", "Epsilon", "\u0395", "epsilon", "\u03b5",
    "equiv", "\u2261", "Eta", "\u0397", "eta", "\u03b7", "ETH", "\u00d0",
    "eth", "\u00f0", "Euml", "\u00cb", "euml", "\u00eb", "euro", "\u20ac",
    "exist", "\u2203", "fnof", "\u0192", "forall", "\u2200", "frac12",
    "\u00bd", "frac14", "\u00bc", "frac34", "\u00be", "frasl", "\u2044",
    "Gamma", "\u0393", "gamma", "\u03b3", "ge", "\u2265", "harr", "\u2194",
    "hArr", "\u21d4", "hearts", "\u2665", "hellip", "\u2026", "Iacute",
    "\u00cd", "iacute", "\u00ed", "Icirc", "\u00ce", "icirc", "\u00ee",
    "iexcl", "\u00a1", "Igrave", "\u00cc", "igrave", "\u00ec", "image",
    "\u2111", "infin", "\u221e", "int", "\u222b", "Iota", "\u0399", "iota",
    "\u03b9", "iquest", "\u00bf", "isin", "\u2208", "Iuml", "\u00cf", "iuml",
    "\u00ef", "Kappa", "\u039a", "kappa", "\u03ba", "Lambda", "\u039b",
    "lambda", "\u03bb", "lang", "\u2329", "laquo", "\u00ab", "larr",
    "\u2190", "lArr", "\u21d0", "lceil", "\u2308", "ldquo", "\u201c", "le",
    "\u2264", "lfloor", "\u230a", "lowast", "\u2217", "loz", "\u25ca", "lrm",
    "\u200e", "lsaquo", "\u2039", "lsquo", "\u2018", "macr", "\u00af",
    "mdash", "\u2014", "micro", "\u00b5", "middot", "\u00b7", "minus",
    "\u2212", "Mu", "\u039c", "mu", "\u03bc", "nabla", "\u2207", "nbsp",
    "\u00a0", "ndash", "\u2013", "ne", "\u2260", "ni", "\u220b", "not",
    "\u00ac", "notin", "\u2209", "nsub", "\u2284", "Ntilde", "\u00d1",
    "ntilde", "\u00f1", "Nu", "\u039d", "nu", "\u03bd", "Oacute", "\u00d3",
    "oacute", "\u00f3", "Ocirc", "\u00d4", "ocirc", "\u00f4", "OElig",
    "\u0152", "oelig", "\u0153", "Ograve", "\u00d2", "ograve", "\u00f2",
    "oline", "\u203e", "Omega", "\u03a9", "omega", "\u03c9", "Omicron",
    "\u039f", "omicron", "\u03bf", "oplus", "\u2295", "or", "\u2228", "ordf",
    "\u00aa", "ordm", "\u00ba", "Oslash", "\u00d8", "oslash", "\u00f8",
    "Otilde", "\u00d5", "otilde", "\u00f5", "otimes", "\u2297", "Ouml",
    "\u00d6", "ouml", "\u00f6", "para", "\u00b6", "part", "\u2202", "permil",
    "\u2030", "perp", "\u22a5", "Phi", "\u03a6", "phi", "\u03c6", "Pi",
    "\u03a0", "pi", "\u03c0", "piv", "\u03d6", "plusmn", "\u00b1", "pound",
    "\u00a3", "prime", "\u2032", "Prime", "\u2033", "prod", "\u220f", "prop",
    "\u221d", "Psi", "\u03a8", "psi", "\u03c8", "radic", "\u221a", "rang",
    "\u232a", "raquo", "\u00bb", "rarr", "\u2192", "rArr", "\u21d2", "rceil",
    "\u2309", "rdquo", "\u201d", "real", "\u211c", "reg", "\u00ae", "rfloor",
    "\u230b", "Rho", "\u03a1", "rho", "\u03c1", "rlm", "\u200f", "rsaquo",
    "\u203a", "rsquo", "\u2019", "sbquo", "\u201a", "Scaron", "\u0160",
    "scaron", "\u0161", "sdot", "\u22c5", "sect", "\u00a7", "shy", "\u00ad",
    "Sigma", "\u03a3", "sigma", "\u03c3", "sigmaf", "\u03c2", "sim",
    "\u223c", "spades", "\u2660", "sub", "\u2282", "sube", "\u2286", "sum",
    "\u2211", "sup", "\u2283", "sup1", "\u00b9", "sup2", "\u00b2", "sup3",
    "\u00b3", "supe", "\u2287", "szlig", "\u00df", "Tau", "\u03a4", "tau",
    "\u03c4", "there4", "\u2234", "Theta", "\u0398", "theta", "\u03b8",
    "thetasym", "\u03d1", "thinsp", "\u2009", "THORN", "\u00de", "thorn",
    "\u00fe", "tilde", "\u02dc", "times", "\u00d7", "trade", "\u2122",
    "Uacute", "\u00da", "uacute", "\u00fa", "uarr", "\u2191", "uArr",
    "\u21d1", "Ucirc", "\u00db", "ucirc", "\u00fb", "Ugrave", "\u00d9",
    "ugrave", "\u00f9", "uml", "\u00a8", "upsih", "\u03d2", "Upsilon",
    "\u03a5", "upsilon", "\u03c5", "Uuml", "\u00dc", "uuml", "\u00fc",
    "weierp", "\u2118", "Xi", "\u039e", "xi", "\u03be", "Yacute", "\u00dd",
    "yacute", "\u00fd", "yen", "\u00a5", "yuml", "\u00ff", "Yuml", "\u0178",
    "Zeta", "\u0396", "zeta", "\u03b6", "zwj", "\u200d", "zwnj", "\u200c" };
}

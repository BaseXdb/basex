package org.basex.build.xml;

import static org.basex.build.BuildText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * This class parses the tokens that are delivered by the {@link XMLScanner} and
 * sends them to the specified database builder. This class is a more
 * tolerant alternative to Java's internal SAX parser, which is used by the
 * {@link SAXWrapper} class.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class XMLParser extends SingleParser {
  /** Strip namespaces. */
  private final boolean stripNS;
  /** Strip whitespace. */
  private final boolean stripWS;
  /** Whitespace handling. */
  private final BoolList strips = new BoolList();
  /** Scanner reference. */
  private final XMLScanner scanner;
  /** Names of opened elements. */
  private final TokenList elms = new TokenList();
  /** Allow document fragment as input. */
  private final boolean fragment;
  /** Closed root element. */
  private boolean closed;

  /**
   * Constructor.
   * @param source document source
   * @param options main options
   * @throws IOException I/O exception
   */
  public XMLParser(final IO source, final MainOptions options) throws IOException {
    this(source, options, false);
  }

  /**
   * Constructor.
   * @param source document source
   * @param options main options
   * @param frag allow parsing of document fragment
   * @throws IOException I/O exception
   */
  public XMLParser(final IO source, final MainOptions options, final boolean frag)
      throws IOException {
    super(source, options);
    scanner = new XMLScanner(source, options, frag);
    stripNS = options.get(MainOptions.STRIPNS);
    stripWS = options.get(MainOptions.STRIPWS);
    strips.push(stripWS);
    fragment = frag;
  }

  @Override
  public final void parse() throws IOException {
    // loop until all tokens have been processed
    scanner.more();
    while(true) {
      if(scanner.type == Type.TEXT) {
        final byte[] text = scanner.token.toArray();
        if(!elms.isEmpty() || fragment || !ws(text)) {
          if(strips.peek()) scanner.token.trim();
          builder.text(scanner.token.toArray());
        }
      } else if(scanner.type == Type.COMMENT) {
        builder.comment(scanner.token.toArray());
      } else if(scanner.type == Type.PI) {
        builder.pi(scanner.token.toArray());
      } else if(scanner.type == Type.EOF) {
        break;
      } else if(scanner.type != Type.DTD) {
        // L_BR, L_BR_CLOSE
        if(!fragment && closed) throw new BuildException(MOREROOTS, detailedInfo());
        if(!parseElement()) break;
        continue;
      }
      if(!scanner.more()) break;
    }
    scanner.close();
    if(!elms.isEmpty()) throw new BuildException(DOCOPEN, detailedInfo(), elms.pop());
  }

  @Override
  public void close() throws IOException {
    scanner.close();
  }

  /**
   * Parses an XML element name.
   * @return result of scanner step
   * @throws IOException I/O exception
   */
  private boolean parseElement() throws IOException {
    // close element
    if(scanner.type == Type.L_BR_CLOSE) {
      scanner.more();

      // get element name
      byte[] name = consumeToken(Type.ELEMNAME);
      if(stripNS) name = local(name);
      skipSpace();

      if(elms.isEmpty()) throw new BuildException(OPEN, detailedInfo(), name);
      final byte[] open = elms.pop();
      if(!eq(open, name)) throw new BuildException(CLOSINGELEM, detailedInfo(), name, open);
      strips.pop();

      builder.closeElem();
      if(elms.isEmpty()) closed = true;
      return consume(Type.R_BR);
    }

    consume(Type.L_BR);
    atts.reset();
    nsp.reset();

    // get element name
    byte[] en = consumeToken(Type.ELEMNAME);
    if(stripNS) en = local(en);
    skipSpace();

    // parse optional attributes
    while(scanner.type != Type.R_BR && scanner.type != Type.CLOSE_R_BR) {
      final byte[] an = consumeToken(Type.ATTNAME);
      skipSpace();
      consume(Type.EQ);
      skipSpace();
      consume(Type.QUOTE);
      byte[] av = EMPTY;
      if(scanner.type == Type.ATTVALUE) {
        av = scanner.token.toArray();
        scanner.more();
      }
      consume(Type.QUOTE);

      if(startsWith(an, XMLNS_COLON)) {
        // open namespace...
        if(!stripNS) nsp.add(local(an), av);
      } else if(eq(an, XMLNS)) {
        // open namespace...
        if(!stripNS) nsp.add(EMPTY, av);
      } else {
        // add attribute
        atts.add(an, av, stripNS);
      }

      if(scanner.type != Type.R_BR && scanner.type != Type.CLOSE_R_BR) {
        consume(Type.WS);
      }
    }

    // send empty element to builder
    if(scanner.type == Type.CLOSE_R_BR) {
      builder.emptyElem(en, atts, nsp);
      if(elms.isEmpty()) closed = true;
      return scanner.more();
    }

    // send start element
    builder.openElem(en, atts, nsp);
    elms.push(en);
    boolean strip = strips.peek();
    if(stripWS) {
      final int a = atts.get(DataText.XML_SPACE);
      if(a != -1) {
        final byte[] s = atts.value(a);
        if(eq(s, DataText.DEFAULT)) strip = true;
        else if(eq(s, DataText.PRESERVE)) strip = false;
      }
    }
    strips.push(strip);
    return consume(Type.R_BR);
  }

  /**
   * Checks if the current token matches the specified type.
   * @param type token type to be checked
   * @return result of scanner step
   * @throws IOException I/O exception
   */
  private boolean consume(final Type type) throws IOException {
    if(scanner.type == type) return scanner.more();
    throw new BuildException(PARSEINV, detailedInfo(), type.string, scanner.type.string);
  }

  /**
   * Returns the token for the specified token type. If the current token type
   * is wrong, a {@code null} reference is returned.
   * @param type token type
   * @return token or {@code null} if the token type is wrong
   * @throws IOException I/O exception
   */
  private byte[] consumeToken(final Type type) throws IOException {
    if(scanner.type == type) {
      final byte[] token = scanner.token.toArray();
      scanner.more();
      return token;
    }
    throw new BuildException(PARSEINV, detailedInfo(), type.string, scanner.type.string);
  }

  /**
   * Skips optional whitespace.
   * @throws IOException I/O exception
   */
  private void skipSpace() throws IOException {
    if(scanner.type == Type.WS) scanner.more();
  }

  @Override
  public final String detailedInfo() {
    return scanner.detailedInfo();
  }

  @Override
  public final double progressInfo() {
    return scanner.progressInfo();
  }
}

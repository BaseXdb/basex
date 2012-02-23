package org.basex.build.xml;

import static org.basex.core.Text.*;
import static org.basex.build.BuildText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.BuildException;
import org.basex.build.SingleParser;
import org.basex.build.BuildText.Type;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.util.list.TokenList;

/**
 * This class parses the tokens that are delivered by the {@link XMLScanner} and
 * sends them to the specified database builder. This class offers a more
 * tolerant alternative to Java's internal SAX parser, which is used by the
 * {@link SAXWrapper} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class XMLParser extends SingleParser {
  /** Scanner reference. */
  private final XMLScanner scanner;
  /** Opened tags. */
  private final TokenList tags = new TokenList();

  /**
   * Constructor.
   * @param source document source
   * @param target target path
   * @param prop database properties
   * @throws IOException I/O exception
   */
  public XMLParser(final IO source, final String target, final Prop prop)
      throws IOException {
    super(source, target);
    scanner = new XMLScanner(source, prop);
  }

  @Override
  public final void parse() throws IOException {
    try {
      // loop until all tokens have been processed
      scanner.more();
      while(true) {
        if(scanner.type == Type.TEXT) {
          builder.text(scanner.token.finish());
        } else if(scanner.type == Type.COMMENT) {
          builder.comment(scanner.token.finish());
        } else if(scanner.type == Type.PI) {
          builder.pi(scanner.token.finish());
        } else if(scanner.type == Type.EOF) {
          break;
        } else if(scanner.type != Type.DTD) {
          if(!parseTag()) break;
          continue;
        }
        if(!scanner.more()) break;
      }
      scanner.close();
      builder.encoding(scanner.encoding);
    } catch(final BuildException ex) {
      final String msg = ex.getMessage() + H_PARSE_ERROR;
      final BuildException e = new BuildException(msg);
      e.setStackTrace(ex.getStackTrace());
      throw e;
    }
  }

  @Override
  public void close() throws IOException {
    scanner.close();
  }

  /**
   * Parses an XML tag.
   * @throws IOException I/O exception
   * @return result of scanner step
   */
  private boolean parseTag() throws IOException {
    // find opening tag
    if(scanner.type == Type.L_BR_CLOSE) {
      scanner.more();

      // get tag name
      final byte[] tag = consumeToken(Type.TAGNAME);
      skipSpace();

      if(tags.isEmpty()) throw new BuildException(AFTERROOT, det());
      final byte[] open = tags.pop();
      if(!eq(open, tag)) throw new BuildException(CLOSINGTAG, det(), tag, open);

      builder.endElem();
      return consume(Type.R_BR);
    }

    consume(Type.L_BR);
    atts.reset();

    // get tag name
    final byte[] tag = consumeToken(Type.TAGNAME);
    skipSpace();

    // parse optional attributes
    while(scanner.type != Type.R_BR && scanner.type != Type.CLOSE_R_BR) {
      final byte[] attName = consumeToken(Type.ATTNAME);
      skipSpace();
      consume(Type.EQ);
      skipSpace();
      consume(Type.QUOTE);
      byte[] attValue = EMPTY;
      if(scanner.type == Type.ATTVALUE) {
        attValue = scanner.token.finish();
        scanner.more();
      }
      consume(Type.QUOTE);

      if(startsWith(attName, XMLNSC)) {
        // open namespace...
        builder.startNS(local(attName), attValue);
      } else if(eq(attName, XMLNS)) {
        // open namespace...
        builder.startNS(EMPTY, attValue);
      } else {
        // add attribute
        atts.add(attName, attValue);
      }

      if(scanner.type != Type.R_BR && scanner.type != Type.CLOSE_R_BR) {
        consume(Type.WS);
      }
    }

    // send start tag to the xml builder
    if(scanner.type == Type.CLOSE_R_BR) {
      builder.emptyElem(tag, atts);
      return scanner.more();
    }
    builder.startElem(tag, atts);
    tags.add(tag);
    return consume(Type.R_BR);
  }

  /**
   * Checks if the current token matches the specified type.
   * @param t token type to be checked
   * @return result of scanner step
   * @throws IOException I/O exception
   */
  private boolean consume(final Type t) throws IOException {
    if(scanner.type == t) return scanner.more();
    throw new BuildException(PARSEINV, det(), t.string, scanner.type.string);
  }

  /**
   * Returns the token for the specified token type. If the current token type
   * is wrong, a {@code null} reference is returned.
   * @param t token type
   * @return token or {@code null} if the token type is wrong
   * @throws IOException I/O exception
   */
  private byte[] consumeToken(final Type t) throws IOException {
    if(scanner.type == t) {
      final byte[] tok = scanner.token.finish();
      scanner.more();
      return tok;
    }
    throw new BuildException(PARSEINV, det(), t.string, scanner.type.string);
  }

  /**
   * Skips optional whitespaces.
   * @throws IOException I/O exception
   */
  private void skipSpace() throws IOException {
    if(scanner.type == Type.WS) scanner.more();
  }

  @Override
  protected final String det() {
    return scanner.det();
  }

  @Override
  public final double prog() {
    return scanner.prog();
  }
}

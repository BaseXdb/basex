package org.basex.build.xml;

import static org.basex.build.BuildText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.build.BuildException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * This class parses the tokens that are delivered by the
 * {@link XMLScanner} and sends them to the specified database builder.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XMLParser extends Parser {
  /** Scanner reference. */
  private XMLScanner scanner;
  /** Builder reference. */
  private Builder builder;

  /**
   * Constructor.
   * @param f file reference
   * @throws IOException I/O Exception
   */
  public XMLParser(final IO f) throws IOException {
    super(f);
    scanner = new XMLScanner(f);
  }

  @Override
  public void parse(final Builder build) throws IOException {
    builder = build;
    builder.encoding(scanner.encoding);

    // loop until all tokens have been processed
    scanner.more();
    while(true) {
      if(scanner.type == Type.TEXT) {
        builder.text(scanner.token.finish(), scanner.ws);
      } else if(scanner.type == Type.COMMENT) {
        builder.comment(scanner.token.finish());
      } else if(scanner.type == Type.DTD) {
      } else if(scanner.type == Type.PI) {
        builder.pi(scanner.token.finish()); 
      } else {
        if(!parseTag()) break;
        continue;
      }
      if(!scanner.more()) break;
    }
    scanner.finish();
  }

  /**
   * Parses an XML tag.
   * @throws IOException in case of parse or write problems
   * @return result of scanner step
   */
  private boolean parseTag() throws IOException {
    // find opening tag
    if(scanner.type == Type.L_BR_CLOSE) {
      scanner.more();
      
      // get tag name
      final byte[] tag = consumeToken(Type.TAGNAME);
      skipSpace();

      builder.endNode(tag);
      return consume(Type.R_BR);
    } else {
      consume(Type.L_BR);

      // start parsing for opening tag
      byte[][] atts = null;

      // get tag name
      final byte[] tag = consumeToken(Type.TAGNAME);
      skipSpace();

      // parse optional attributes
      int as = 0;
      while(scanner.type != Type.R_BR && scanner.type != Type.CLOSE_R_BR) {
        final byte[] attName = consumeToken(Type.ATTNAME);
        skipSpace();
        consume(Type.EQ);
        skipSpace();
        consume(Type.QUOTE);
        byte[] attValue = Token.EMPTY;
        if(scanner.type == Type.ATTVALUE) {
          attValue = scanner.token.finish();
          scanner.more();
        }
        consume(Type.QUOTE);

        // add attribute
        final byte[][] tmp = new byte[as + 2][];
        if(as > 0) System.arraycopy(atts, 0, tmp, 0, as);
        atts = tmp;
        atts[as++] = attName;
        atts[as++] = attValue;

        if(scanner.type != Type.R_BR && scanner.type != Type.CLOSE_R_BR) {
          consume(Type.WS);
        }
      }

      // send start tag to the xml builder
      if(scanner.type == Type.CLOSE_R_BR) {
        builder.emptyNode(tag, atts);
        return scanner.more();
      } else {
        builder.startNode(tag, atts);
        return consume(Type.R_BR);
      }
    }
  }

  /**
   * Checks if the current token matches the specified type.
   * @param t token type to be checked
   * @return result of scanner step
   * @throws BuildException build exception
   */
  private boolean consume(final Type t) throws BuildException {
    if(scanner.type != t) throw new BuildException(PARSEINVALID, det(),
        t.string, scanner.type.string);
    return scanner.more();
  }
  
  /**
   * Returns the token for the specified token type. If the current token
   * type is wrong, a null reference is returned.
   * @param t token type
   * @return token or null if the token type is wrong
   * @throws BuildException build exception
   */
  private byte[] consumeToken(final Type t) throws BuildException {
    if(scanner.type != t) {
      throw new BuildException(PARSEINVALID, det(), t.string,
          scanner.type.string);
    }
    final byte[] tok = scanner.token.finish();
    scanner.more();
    return tok;
  }
  
  /**
   * Skips optional whitespaces.
   * @throws BuildException build exception
   */
  private void skipSpace() throws BuildException {
    if(scanner.type == Type.WS) scanner.more();
  }

  @Override
  public String head() {
    return PROGCREATE;
  }

  @Override
  public String det() {
    return scanner.det();
  }

  @Override
  public double percent() {
    return scanner.percent();
  }
}

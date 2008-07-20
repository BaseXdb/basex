package org.basex.build.xml;

import static org.basex.build.BuildText.*;
import static org.basex.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.BuildException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.data.DataText;
import org.basex.io.IO;
 
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
    builder.startDoc(token(file.name()));

    // loop until all tokens have been processed
    scanner.more();
    while(true) {
      if(scanner.type == Type.TEXT) {
        builder.text(scanner.token, scanner.ws);
      } else if(scanner.type == Type.COMMENT) {
        builder.comment(scanner.token);
      } else if(scanner.type == Type.DTD) {
      } else if(scanner.type == Type.PI) {
        builder.pi(scanner.token); 
      } else {
        if(!parseTag()) break;
        continue;
      }
      if(!scanner.more()) break;
    }
    scanner.finish();
    
    builder.encoding(scanner.encoding);
    builder.endDoc();
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

      builder.endElem(tag);
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
        byte[] attValue = EMPTY;
        if(scanner.type == Type.ATTVALUE) {
          attValue = scanner.token.finish();
          scanner.more();
        }
        consume(Type.QUOTE);

        final int s = indexOf(attName, ':');
        if(s != -1 && startsWith(attName, DataText.XMLNSC)) {
          // open namespace...
          builder.startNS(substring(attName, s + 1), attValue);
        } else if(eq(attName, DataText.XMLNS)) {
          // open namespace...
          builder.startNS(EMPTY, attValue);
        } else {
          // add attribute
          final byte[][] tmp = new byte[as + 2][];
          if(as > 0) System.arraycopy(atts, 0, tmp, 0, as);
          atts = tmp;
          atts[as++] = attName;
          atts[as++] = attValue;
        }

        if(scanner.type != Type.R_BR && scanner.type != Type.CLOSE_R_BR) {
          consume(Type.WS);
        }
      }

      // send start tag to the xml builder
      if(scanner.type == Type.CLOSE_R_BR) {
        builder.emptyElem(tag, atts);
        return scanner.more();
      } else {
        builder.startElem(tag, atts);
        return consume(Type.R_BR);
      }
    }
  }

  /**
   * Checks if the current token matches the specified type.
   * @param t token type to be checked
   * @return result of scanner step
   * @throws IOException I/O Exception
   */
  private boolean consume(final Type t) throws IOException {
    if(scanner.type != t) throw new BuildException(PARSEINVALID, det(),
        t.string, scanner.type.string);
    return scanner.more();
  }
  
  /**
   * Returns the token for the specified token type. If the current token
   * type is wrong, a null reference is returned.
   * @param t token type
   * @return token or null if the token type is wrong
   * @throws IOException I/O Exception
   */
  private byte[] consumeToken(final Type t) throws IOException {
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
   * @throws IOException I/O Exception
   */
  private void skipSpace() throws IOException {
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

package org.basex.build.xml;

import static org.basex.build.BuildText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.BuildException;
import org.basex.build.Builder;
import org.basex.build.Parser;
import org.basex.io.BufferInput;
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
   * @param in input stream
   */
  public XMLParser(final BufferInput in) {
    super("tmp");
    scanner = new XMLScanner(in);
  }

  /**
   * Constructor.
   * @param fn input file
   * @throws IOException I/O Exception
   */
  public XMLParser(final String fn) throws IOException {
    super(fn);
    scanner = new XMLScanner(file);
  }

  @Override
  public void parse(final Builder build) throws IOException {
    builder = build;

    // start scanning
    scanner.next();
    // loop until all tokens have been processed
    while(scanner.more()) {
      if(scanner.type == Type.TEXT) {
        builder.text(scanner.token.finish(), scanner.chars);
        scanner.next();
      } else if(scanner.type == Type.COMMENT) {
        builder.comment(scanner.token.finish());
        scanner.next();
      } else if(scanner.type == Type.DTD) {
        new DTDParser(scanner, file, builder.tags, builder.atts, scanner.ents);
        scanner.next();
      } else if(scanner.type == Type.PI) {
        builder.pi(scanner.token.finish()); 
        scanner.next();
      } else if(scanner.type == Type.DECL) {
        builder.encoding(scanner.encoding);
        scanner.next();
      } else {
        parseTag();
      }
    }
    scanner.finish();
  }

  /**
   * Parses an XML tag.
   * @throws IOException in case of parse or write problems
   */
  private void parseTag() throws IOException {
    // find opening tag
    if(scanner.type == Type.L_BR_CLOSE) {
      scanner.next();
      
      // get tag name
      final byte[] tag = consumeToken(Type.TAGNAME);
      skipSpace();

      consume(Type.R_BR);
      builder.endNode(tag);
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
          scanner.next();
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
        scanner.next();
        builder.emptyNode(tag, atts);
      } else {
        consume(Type.R_BR);
        builder.startNode(tag, atts);
      }
    }
  }

  /**
   * Checks if the current token matches the specified type.
   * @param t token type to be checked
   * @throws BuildException build exception
   */
  private void consume(final Type t) throws BuildException {
    if(scanner.type != t) throw new BuildException(PARSEINVALID, det(),
        t.string, scanner.type.string);
    scanner.next();
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
    scanner.next();
    return tok;
  }
  
  /**
   * Skips optional whitespaces.
   * @throws BuildException build exception
   */
  private void skipSpace() throws BuildException {
    if(scanner.type == Type.WS) scanner.next();
  }

  @Override
  public String head() {
    return PROGCREATE;
  }

  @Override
  public String det() {
    return BaseX.info(SCANPOS, scanner.line, scanner.col);
  }

  @Override
  public double percent() {
    return (double) scanner.input.size() / scanner.input.length();
  }
}

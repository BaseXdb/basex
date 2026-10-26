package org.basex.query;

import org.basex.io.serial.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Query string builder.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryString {
  /** Characters after which no spaces should be added. */
  private static final String NO_SPACE = "|/<([ \n\t";
  /** Query string. */
  private final TokenBuilder tb = new TokenBuilder();
  /** Error output. */
  private final boolean error;
  /** Indicates if the current expression can be added without parentheses. */
  private boolean bare;

  /**
   * Constructor.
   */
  public QueryString() {
    this(false);
  }

  /**
   * Constructor.
   * @param error error output
   */
  public QueryString(final boolean error) {
    this.error = error;
  }

  /**
   * Adds a token.
   * @param ch character token to be added
   * @return self reference
   */
  public QueryString token(final char ch) {
    tb.add(ch);
    return this;
  }

  /**
   * Adds a token.
   * @param token token to be added
   * @return self reference
   */
  public QueryString token(final Object token) {
    return token(token, false);
  }

  /**
   * Adds a token at a position that accepts any single expression.
   * @param token token to be added
   * @return self reference
   */
  public QueryString single(final Object token) {
    return token(token, true);
  }

  /**
   * Adds a token.
   * @param token token to be added
   * @param sngl token is added at a position that accepts any single expression
   * @return self reference
   */
  private QueryString token(final Object token, final boolean sngl) {
    space();
    if(token instanceof final ExprInfo ei) {
      final boolean br = bare;
      bare = sngl;
      ei.toString(this);
      bare = br;
    } else if(token instanceof final AnnList al) {
      al.toString(this);
    } else {
      final byte[] t = Token.token(token);
      tb.add(last() == ' ' && Token.startsWith(t, ' ') ? Token.substring(t, 1) : t);
    }
    return this;
  }

  /**
   * Adds operands, separated by an operator, and wraps them with parentheses if required.
   * @param operands operands to be added
   * @param operator operator string
   * @return self reference
   */
  public QueryString operands(final Object[] operands, final String operator) {
    return tokens(operands, operator, !bare);
  }

  /**
   * Adds an opening parenthesis if the current expression must be wrapped.
   * @return self reference
   */
  public QueryString open() {
    return bare ? this : token("(");
  }

  /**
   * Adds a closing parenthesis if the current expression must be wrapped.
   * @return self reference
   */
  public QueryString close() {
    return bare ? this : token(')');
  }

  /**
   * Adds a function call.
   * @param function called function
   * @param args function arguments
   * @return self reference
   */
  public QueryString function(final Function function, final Object... args) {
    return function(function.definition(), args);
  }

  /**
   * Adds a function call.
   * @param definition function definition
   * @param args arguments
   * @return self reference
   */
  public QueryString function(final FuncDefinition definition, final Object... args) {
    return token(definition.args(error, args).trim());
  }

  /**
   * Adds concatenated tokens.
   * @param tokens tokens to be concatenated
   * @return self reference
   */
  public QueryString concat(final Object... tokens) {
    return token(Token.concat(tokens));
  }

  /**
   * Adds multiple tokens.
   * @param tokens tokens to be added
   * @return self reference
   */
  public QueryString tokens(final Object[] tokens) {
    return tokens(tokens, "");
  }

  /**
   * Adds multiple tokens, separated by the specified string.
   * @param tokens tokens to be added
   * @param separator separator string
   * @return self reference
   */
  public QueryString tokens(final Object[] tokens, final String separator) {
    return tokens(tokens, separator, false);
  }

  /**
   * Adds multiple tokens, separated by the specified string.
   * @param tokens tokens to be added
   * @param separator separator string
   * @param paren wrap with parentheses
   * @return self reference
   */
  public QueryString tokens(final Object[] tokens, final String separator, final boolean paren) {
    return tokens(tokens, separator, paren, false);
  }

  /**
   * Adds multiple tokens, separated by the specified string.
   * @param tokens tokens to be added
   * @param separator separator string
   * @param paren wrap with parentheses
   * @param sngl tokens are added at positions that accept any single expression
   * @return self reference
   */
  private QueryString tokens(final Object[] tokens, final String separator, final boolean paren,
      final boolean sngl) {
    if(paren) tb.add('(');
    else space();

    boolean more = false;
    for(final Object token : tokens) {
      if(more) {
        tb.add(last() == ' ' && Strings.startsWith(separator, ' ') ? separator.substring(1) :
          separator);
      } else {
        more = true;
      }
      token(token, sngl);
    }

    if(paren) tb.add(')');
    return this;
  }

  /**
   * Adds parameters or function arguments.
   * @param params parameters to be added
   * @return self reference
   */
  public QueryString params(final Object[] params) {
    return tokens(params, ", ", true, true);
  }

  /**
   * Adds function parameters with their declared or inferred types.
   * @param params parameters to be added
   * @return self reference
   */
  public QueryString params(final Var[] params) {
    tb.add('(');
    boolean more = false;
    for(final Var param : params) {
      if(more) tb.add(", ");
      more = true;
      param.toString(this, param.seqType());
    }
    tb.add(')');
    return this;
  }

  /**
   * Adds a token wrapped with parentheses.
   * @param token token to be added
   * @return self reference
   */
  public QueryString paren(final Object token) {
    return braced("(", token, ")");
  }

  /**
   * Adds a token wrapped with curly braces.
   * @param token token to be added
   * @return self reference
   */
  public QueryString brace(final Object token) {
    return braced(" { ", token, " }");
  }

  /**
   * Serializes a chopped version of the specified value.
   * @param value value
   * @return self reference
   */
  public QueryString value(final byte[] value) {
    tb.add(toValue(value));
    return this;
  }

  /**
   * Serializes a chopped version of the specified value with quotes.
   * @param value value
   * @return self reference
   */
  public QueryString quoted(final byte[] value) {
    tb.add(toQuoted(value, !error));
    return this;
  }

  /**
   * Serializes a direct comment constructor with a chopped version of its unescaped value.
   * @param value value
   * @return self reference
   */
  public QueryString comment(final byte[] value) {
    return concat(XMLToken.COMM_O, Serializer.value(value, false, false, true, false),
      XMLToken.COMM_C);
  }

  /**
   * Serializes a direct processing instruction with a chopped version of its unescaped value.
   * @param name name
   * @param value value
   * @return self reference
   */
  public QueryString pi(final byte[] name, final byte[] value) {
    return concat(XMLToken.PI_O, name, " ", Serializer.value(value, false, false, true, false),
      XMLToken.PI_C);
  }

  /**
   * Serializes a chopped version of the specified value as content of a direct constructor.
   * @param value value
   * @param attribute attribute value
   * @return self reference
   */
  public QueryString content(final byte[] value, final boolean attribute) {
    tb.add(toContent(value, attribute));
    return this;
  }

  /**
   * Adds a constructor function call or cast that creates an atomic item of the specified type.
   * @param type atomic type
   * @param value value
   * @return self reference
   */
  public QueryString constructor(final Type type, final byte[] value) {
    if(error) return quoted(value);
    if(type instanceof BasicType) return token(type).paren(toQuoted(value));
    return open().quoted(value).token(QueryText.CAST).token(QueryText.AS).token(type).close();
  }

  /**
   * Adds a braced token.
   * @param open opening brace
   * @param token token to be added
   * @param close closing brace
   * @return self reference
   */
  public QueryString braced(final String open, final Object token, final String close) {
    tb.add(open);
    single(token);
    final byte[] t = Token.token(close);
    tb.add(last() == ' ' && Token.startsWith(t, ' ') ? Token.substring(t, 1) : t);
    return this;
  }

  /**
   * Adds a newline.
   * @return self reference
   */
  public QueryString newline() {
    tb.add(Prop.NL);
    return this;
  }

  /**
   * Adds a space if appropriate.
   */
  private void space() {
    if(!Strings.contains(NO_SPACE, last())) tb.add(' ');
  }

  /**
   * Returns the last character of the query string.
   * @return last character, or a space if the query string is empty
   */
  private char last() {
    return tb.isEmpty() ? ' ' : (char) tb.get(tb.size() - 1);
  }

  /**
   * Returns the error flag.
   * @return flag
   */
  public boolean error() {
    return error;
  }

  @Override
  public String toString() {
    return tb.toString();
  }

  // STATIC METHODS ===============================================================================

  /**
   * Serializes a chopped version of the specified value.
   * @param value value
   * @return string
   */
  public static byte[] toValue(final byte[] value) {
    return Serializer.value(value, false, false, true);
  }

  /**
   * Serializes a chopped version of the specified value with quotes.
   * @param value value
   * @return token
   */
  public static byte[] toQuoted(final byte[] value) {
    return toQuoted(value, true);
  }

  /**
   * Serializes a chopped version of the specified value with quotes, optionally without escaping.
   * @param value value
   * @param escape escape ampersands and newlines (disabled for error messages)
   * @return token
   */
  public static byte[] toQuoted(final byte[] value, final boolean escape) {
    return Serializer.value(value, true, false, true, escape);
  }

  /**
   * Serializes an attribute of a direct constructor with a chopped version of its value.
   * @param name attribute name
   * @param value attribute value
   * @return token
   */
  public static byte[] toAttribute(final byte[] name, final byte[] value) {
    return Token.concat(name, "=\"", toContent(value, true), "\"");
  }

  /**
   * Serializes a chopped version of the specified value as content of a direct constructor.
   * @param value value
   * @param attribute attribute value
   * @return token
   */
  public static byte[] toContent(final byte[] value, final boolean attribute) {
    // whitespace-only text would be stripped as boundary whitespace
    final boolean ws = !attribute && Token.ws(value);
    final TokenBuilder tb = new TokenBuilder();
    int c = 0;
    for(final TokenParser tp = new TokenParser(value); tp.more(); c++) {
      if(c == 200) {
        tb.add(QueryText.DOTS);
        break;
      }
      final int cp = tp.next();
      if(ws || cp == '\r' || cp == '\n' || attribute && cp == '\t') {
        tb.add("&#x").add(Integer.toHexString(cp).toUpperCase()).add(';');
      } else if(cp == '&') {
        tb.add(XMLToken.E_AMP);
      } else if(cp == '<') {
        tb.add(XMLToken.E_LT);
      } else if(cp == '{' || cp == '}' || attribute && cp == '"') {
        tb.add(cp).add(cp);
      } else {
        tb.add(cp);
      }
    }
    return tb.finish();
  }
}

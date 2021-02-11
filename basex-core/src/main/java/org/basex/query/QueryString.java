package org.basex.query;

import org.basex.io.serial.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.util.*;

/**
 * Query string builder.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QueryString {
  /** Characters after which no spaces should be added. */
  private static final String NO_SPACE = "|/<([ \n\t";
  /** Query string. */
  private final TokenBuilder tb = new TokenBuilder();

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
    space();
    if(token instanceof ExprInfo) {
      ((ExprInfo) token).plan(this);
    } else if(token instanceof AnnList) {
      ((AnnList) token).plan(this);
    } else {
      final byte[] t = Token.token(token);
      tb.add(last() == ' ' && Token.startsWith(t, ' ') ? Token.substring(t, 1) : t);
    }
    return this;
  }

  /**
   * Adds a function call.
   * @param function called function
   * @param args function arguments
   * @return self reference
   */
  public QueryString function(final Function function, final Object... args) {
    token(function.args(args).trim());
    return this;
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
    if(paren) tb.add('(');
    else space();

    boolean more = false;
    for(final Object token : tokens) {
      if(more) tb.add(last() == ' ' && Strings.startsWith(separator, ' ') ?
        separator.substring(1) : separator);
      else more = true;
      token(token);
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
    return tokens(params, ", ", true);
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
   * Adds a token wrapped with square brackets.
   * @param token token to be added
   * @return self reference
   */
  public QueryString bracket(final Object token) {
    return braced("[", token, "]");
  }

  /**
   * Serializes a chopped version of the specified value.
   * @param value value
   * @return string
   */
  public QueryString value(final byte[] value) {
    tb.add(toValue(value));
    return this;
  }

  /**
   * Serializes a chopped version of the specified value with quotes.
   * @param value value
   * @return token
   */
  public QueryString quoted(final byte[] value) {
    tb.add(toQuoted(value));
    return this;
  }

  /**
   * Adds a braced token.
   * @param open opening brace
   * @param token token to be added
   * @param close closing brace
   * @return self reference
   */
  private QueryString braced(final String open, final Object token, final String close) {
    tb.add(open);
    token(token);
    final byte[] t = Token.token(close);
    tb.add(last() == ' ' && Token.startsWith(t, ' ') ? Token.substring(t, 1) : t);
    return this;
  }

  /**
   * Adds a space if appropriate.
   * @return self reference
   */
  private QueryString space() {
    if(!Strings.contains(NO_SPACE, last())) tb.add(' ');
    return this;
  }

  /**
   * Returns the last character of the query string.
   * @return last character, or a space if the query string is empty
   */
  private char last() {
    return tb.isEmpty() ? ' ' : (char) tb.get(tb.size() - 1);
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
    return Serializer.value(value, 0, true);
  }

  /**
   * Serializes a chopped version of the specified value with quotes.
   * @param value value
   * @return token
   */
  public static byte[] toQuoted(final byte[] value) {
    return Serializer.value(value, '"', true);
  }
}

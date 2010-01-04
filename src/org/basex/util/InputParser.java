package org.basex.util;

import static org.basex.util.Token.*;
import org.basex.core.Main;
import org.basex.io.IO;

/**
 * Simple query parser; can be overwritten to support more complex parsings.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class InputParser {
  /** Parsing exception. */
  private static final String FOUND = ", found \"%\"";
  /** Parsing exception. */
  private static final String INVENTITY = "Invalid entity \"%\".";

  /** Optional reference to query input. */
  public IO file;
  /** Input query. */
  public String qu;
  /** Current query position. */
  public int qp;
  /** Marked query position. */
  public int qm;
  /** Query length. */
  public int ql;

  /**
   * Constructor.
   * @param q input query
   */
  public final void init(final String q) {
    qu = q;
    ql = qu.length();
  }

  /**
   * Checks if the input is valid.
   * @return 0 if everything is valid
   */
  protected final int valid() {
    for(qp = ql; qp > 0; qp--) {
      if(!XMLToken.valid(qu.charAt(qp - 1))) return qu.charAt(qp - 1);
    }
    return -1;
  }

  /**
   * Checks if more characters are found.
   * @return current character
   */
  protected final boolean more() {
    return qp < ql;
  }

  /**
   * Returns the current character.
   * @return current character
   */
  protected final char curr() {
    return qp >= ql ? 0 : qu.charAt(qp);
  }

  /**
   * Checks if the current character equals the specified one.
   * @param ch character to be checked
   * @return result of check
   */
  protected final boolean curr(final int ch) {
    return curr() == ch;
  }

  /**
   * Remembers the current position.
   */
  protected final void mark() {
    qm = qp;
  }

  /**
   * Returns the next character.
   * @return result of check
   */
  protected final char next() {
    return qp + 1 >= ql ? 0 : qu.charAt(qp + 1);
  }

  /**
   * Returns next character.
   * @return next character
   */
  protected final char consume() {
    return qp >= ql ? 0 : qu.charAt(qp++);
  }

  /**
   * Peeks forward and consumes the character if it equals the specified one.
   * @param ch character to consume
   * @return true if character was found
   */
  protected final boolean consume(final int ch) {
    final boolean found = curr() == ch;
    if(found) qp++;
    return found;
  }

  /**
   * Checks if the specified character is a quote.
   * @param ch character to be checked
   * @return result
   */
  protected final boolean quote(final char ch) {
    return ch == '"' || ch == '\'';
  }

  /**
   * Consumes all whitespace characters from the beginning of the remaining
   * query.
   */
  protected final void consumeWS() {
    while(qp < ql) {
      final char ch = qu.charAt(qp);
      if(ch <= 0 || ch > ' ') break;
      qp++;
    }
    qm = qp - 1;
    return;
  }

  /**
   * Peeks forward and consumes the string if it equals the specified one.
   * @param str string to consume
   * @return true if string was found
   */
  protected final boolean consume(final String str) {
    int p = qp;
    final int l = str.length();
    if(p + l > ql) return false;
    for(int s = 0; s < l; s++) if(qu.charAt(p++) != str.charAt(s)) return false;
    qp = p;
    return true;
  }

  /**
   * Returns a "found" string, containing the current character.
   * @return completion
   */
  protected final byte[] found() {
    return curr() == 0 ? EMPTY : Main.inf(FOUND, curr());
  }

  /**
   * Parses and convert entities.
   * @param tb token builder
   * @return error string or null
   */
  protected final String ent(final TokenBuilder tb) {
    final int p = qp;
    if(consume('&')) {
      if(consume('#')) {
        final int b = consume('x') ? 16 : 10;
        int n = 0;
        do {
          final char c = curr();
          final boolean m = digit(c);
          final boolean h = b == 16 && (c >= 'a' && c <= 'f' ||
              c >= 'A' && c <= 'F');
          if(!m && !h) return invalidEnt(p);
          final int nn = n;
          n = n * b + (consume() & 15);
          if(n < nn) return invalidEnt(p);
          if(!m) n += 9;
        } while(!consume(';'));
        if(!XMLToken.valid(n)) return invalidEnt(p);
        tb.addUTF(n);
      } else {
        if(consume("lt")) {
          tb.add('<');
        } else if(consume("gt")) {
          tb.add('>');
        } else if(consume("amp")) {
          tb.add('&');
        } else if(consume("quot")) {
          tb.add('"');
        } else if(consume("apos")) {
          tb.add('\'');
        } else {
          return invalidEnt(p);
        }
        if(!consume(';')) return invalidEnt(p);
      }
      tb.ent = true;
    } else {
      char c = consume();
      if(c == 0x0d) {
        c = 0x0a;
        if(curr() == c) consume();
      }
      tb.add(c);
    }
    return null;
  }

  /**
   * Returns the current entity snippet.
   * @param p start position
   * @return entity
   */
  protected final String invalidEnt(final int p) {
    final String sub = qu.substring(p, Math.min(p + 20, ql));
    final int sc = sub.indexOf(';');
    final String ent = sc != -1 ? sub.substring(0, sc + 1) : sub;
    return Main.info(INVENTITY, ent);
  }

  /**
   * Returns the remaining, unscanned query substring.
   * @return query substring
   */
  protected final String rest() {
    final int e = Math.min(ql, qp + 15);
    return qu.substring(qp, e) + (e == ql ? "" : "...");
  }
}

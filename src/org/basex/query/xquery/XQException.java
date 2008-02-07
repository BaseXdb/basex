package org.basex.query.xquery;

import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.util.Token;

/**
 * This class indicates exceptions during query evaluation.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQException extends QueryException {
  /**
   * Constructor.
   * @param s error message
   * @param e message extension
   */
  public XQException(final String s, final Object... e) {
    super(Prop.xqerrcode ? s : s.replaceAll("^\\[[A-Z]{4}[0-9]{4}\\] ", ""), e);
  }

  /**
   * Constructor.
   * @param s xquery error
   * @param e error arguments
   */
  public XQException(final Object[] s, final Object... e) {
    super(num(s), e);
  }

  /**
   * Constructor.
   * @param s xquery error
   * @return string
   */
  private static String num(final Object[] s) {
    return Prop.xqerrcode ? String.format("[%s%04d] %s", s[0], s[1], s[2]) :
      s[2].toString();
  }

  /**
   * Returns the error message.
   * @return string
   */
  public byte[] msg() {
    final String s = getMessage();
    return Token.token(s.replaceAll("^\\[[A-Z]{4}[0-9]{4}\\] ", ""));
  }

  /**
   * Returns the error code.
   * @return string
   */
  public byte[] code() {
    final String s = getMessage();
    return Token.token(s.replaceAll("^\\[([A-Z]{4}[0-9]{4})\\].*", "$1"));
  }
}

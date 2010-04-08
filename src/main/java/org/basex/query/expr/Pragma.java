package org.basex.query.expr;

import java.io.IOException;

import static org.basex.query.QueryTokens.*;

import org.basex.data.Serializer;
import org.basex.query.item.QNm;
import org.basex.util.Token;

/**
 * Pragma.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Leo Woerteler
 */
public class Pragma extends Simple {
  
  /** QName. */
  public final QNm qName;
  
  /** PragmaContents. */
  public final byte[] pContent;
  
  /**
   * Constructor.
   * 
   * @param qn QName
   * @param content PragmaContents
   */
  public Pragma(final QNm qn, final byte[] content) {
    qName = qn;
    pContent = content;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, VAL, pContent);
    qName.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(PRAGMA + ' ' + qName + ' ');
    if (pContent.length > 0) sb.append(Token.string(pContent) + ' ');
    return sb.append(PRAGMA2).toString();
  }

}

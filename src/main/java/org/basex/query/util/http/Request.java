package org.basex.query.util.http;

import org.basex.query.iter.ItemCache;
import org.basex.util.hash.TokenMap;
import org.basex.util.list.ObjList;

/**
 * Container for parsed data from <http:request/>.
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class Request {
  /** Request attributes. */
  public final TokenMap attrs = new TokenMap();
  /** Request headers. */
  public final TokenMap headers = new TokenMap();
  /** Body or multipart attributes. */
  public final TokenMap payloadAttrs = new TokenMap();
  /** Body content. */
  public final ItemCache bodyContent = new ItemCache();
  /** Parts in case of multipart request. */
  public final ObjList<Part> parts = new ObjList<Part>();
  /** Indicator for multipart request. */
  public boolean isMultipart;

  /**
   * Container for parsed data from <part/> element.
   * @author BaseX Team 2005-12, BSD License
   * @author Rositsa Shadura
   */
  public static class Part {
    /** Part headers. */
    public final TokenMap headers = new TokenMap();
    /** Attributes of part body. */
    public final TokenMap bodyAttrs = new TokenMap();
    /** Content of part body. */
    public final ItemCache bodyContent = new ItemCache();
  }
}

package org.basex.query.func.sessions;

import jakarta.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionsListDetails extends SessionsFn {
  /** QName. */
  private static final QNm Q_SESSION = new QNm("session");
  /** QName. */
  private static final QNm Q_ID = new QNm("id");
  /** QName. */
  private static final QNm Q_CREATED = new QNm("created");
  /** QName. */
  private static final QNm Q_ACCESSED = new QNm("accessed");
  /** QName. */
  private static final QNm Q_EXPIRES = new QNm("expires");

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String id = toStringOrNull(arg(0), qc);

    final TokenList ids = id != null ? new TokenList(1).add(id) : SessionListener.ids();
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final byte[] key : ids) {
      final HttpSession session = SessionListener.get(Token.string(key));
      final long created = RequestState.created(session);
      final long accessed = RequestState.accessed(session);
      // skip sessions that were invalidated while the list was being built
      if(created == -1 || accessed == -1) continue;

      final FBuilder elem = FElem.build(Q_SESSION).attr(Q_ID, key);
      elem.attr(Q_CREATED, Dtm.local(created, info).string(info));
      elem.attr(Q_ACCESSED, Dtm.local(accessed, info).string(info));
      final int max = session.getMaxInactiveInterval();
      if(max > 0) elem.attr(Q_EXPIRES, Dtm.local(accessed + max * 1000L, info).string(info));
      vb.add(elem.finish());
    }
    return vb.value(this);
  }
}

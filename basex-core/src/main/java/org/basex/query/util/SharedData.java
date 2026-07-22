package org.basex.query.util;

import java.util.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Shared data references.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SharedData {
  /** Cached QNames. */
  private final TokenObjectMap<QNm> qnames = new TokenObjectMap<>();
  /** Cached tokens. */
  private final WeakTokenSet tokens = new WeakTokenSet();
  /** Cached record types. */
  private final IntObjectMap<ArrayList<RecordType>> recordTypes = new IntObjectMap<>();

  /**
   * Constructor.
   */
  public SharedData() {
    record(Types.RECORD);
  }

  /**
   * Parses and returns a shared QName.
   * @param token QName token
   * @param elem always resolve URI
   * @param qc query context
   * @param sc static context
   * @return QName, or {@code null} if QName cannot be parsed
   * @see QNm#parse(byte[], byte[], QueryContext, StaticContext, InputInfo)
   */
  public QNm parseQName(final byte[] token, final boolean elem, final QueryContext qc,
      final StaticContext sc) {
    final byte[] name = Token.trim(token);
    if(XMLToken.isQName(name)) {
      final byte[] prefix = Token.prefix(name);
      final byte[] uri = prefix.length != 0 || elem ? qc.ns.resolve(prefix, sc) : null;
      return qName(name, uri);
    }
    final byte[][] parsed = QNm.parseExpanded(name, false);
    return parsed != null ? qName(parsed[0], parsed[1]) : null;
  }

  /**
   * Returns a shared QName.
   * @param name local name with optional prefix
   * @return QName
   */
  public QNm qName(final byte[] name) {
    return qName(name, null);
  }

  /**
   * Returns a shared QName.
   * @param name local name with optional prefix
   * @param uri URI (can be {@code null})
   * @return QName
   */
  public QNm qName(final byte[] name, final byte[] uri) {
    synchronized(qnames) {
      return qnames.computeIfAbsent(
          uri != null ? Token.concat(name, Token.cpToken(' '), uri) : name,
          () -> new QNm(name, uri)
        );
    }
  }

  /**
   * Returns a shared token.
   * @param token token to be cached
   * @return shared token
   */
  public byte[] token(final byte[] token) {
    synchronized (tokens) {
      return token.length == 0 ? Token.EMPTY : tokens.put(token);
    }
  }

  /** Interval at which token interning re-evaluates the dedup rate. */
  private static final int INTERN_PROBE = 4096;

  /**
   * Interns the tokens of a sequence in place, replacing duplicates with shared instances.
   * @param values tokens to be interned (updated in place)
   * @param job interruptible job
   */
  public void tokens(final byte[][] values, final Job job) {
    final int vl = values.length;
    synchronized(tokens) {
      final int before = tokens.size();
      for(int v = 0; v < vl; v++) {
        if((v & INTERN_PROBE - 1) == 0) {
          job.checkStop();
          // stop if more than half of the tokens seen so far were distinct
          if(v >= INTERN_PROBE && tokens.size() - before << 1 > v) return;
        }
        final byte[] value = values[v];
        if(value.length != 0) values[v] = tokens.put(value);
      }
    }
  }

  /**
   * Creates a new record or returns an existing instance.
   * @param rt record type
   * @return new or already registered record type
   */
  public RecordType record(final RecordType rt) {
    synchronized(recordTypes) {
      final ArrayList<RecordType> types = recordTypes.computeIfAbsent(rt.fields().size(),
          ArrayList::new);
      for(final RecordType type : types) {
        if(type.equals(rt)) return type;
      }
      types.add(rt);
      return rt;
    }
  }
}

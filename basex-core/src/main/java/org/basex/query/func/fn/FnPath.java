package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnPath extends ContextFn {
  /** Path options. */
  public static class PathOptions extends Options {
    /** Option. */
    public static final ValueOption ORIGIN = new ValueOption("origin", Types.NODE_ZO);
    /** Option. */
    public static final BooleanOption LEXICAL = new BooleanOption("lexical", false);
    /** Option. */
    public static final ValueOption NAMESPACES = new ValueOption("namespaces", Types.MAP_ZO);
    /** Option. */
    public static final BooleanOption INDEXES = new BooleanOption("indexes", true);
  }
  /** LRU step cache. */
  private final Map<Long, CachedStep> cachedSteps = Collections.synchronizedMap(new StepCache());
  /** Last numbered nodes, indexed by a hash of their kind and name. */
  private final AtomicReferenceArray<NodeIndex> lastIndexes = new AtomicReferenceArray<>(8);

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    GNode node = toGNodeOrNull(contextArg(qc), qc);
    final XQMap map = toEmptyMap(arg(1), qc);
    final PathOptions options = toOptions(qc);
    if(node == null) return Empty.VALUE;

    final boolean indexes = options.get(PathOptions.INDEXES);
    final XQMap namespaces = toEmptyMap(options.get(PathOptions.NAMESPACES), qc);
    final boolean lexical = options.get(PathOptions.LEXICAL);
    final Value origin = options.get(PathOptions.ORIGIN);
    final boolean cache = map.structSize() == 0;

    final TokenList steps = new TokenList();
    final TokenBuilder tb = new TokenBuilder();
    boolean relative = false;
    while(true) {
      // check if string representation of step was cached
      final DBNode dbnode = node instanceof final DBNode dbn ? dbn : null;
      final Long cacheKey = cache && dbnode != null ?
        (long) dbnode.data().dbid << 32L | dbnode.pre() : null;
      final int mods = dbnode != null ? dbnode.data().modifications() : 0;
      final CachedStep cached = cacheKey != null ? cachedSteps.get(cacheKey) : null;
      byte[] step = cached != null && cached.modifications == mods ? cached.step : null;

      final GNode parent = node.parent();
      if(step == null) {
        final Type type = node.type;
        final Kind kind = type instanceof final NodeType nt ? nt.kind() : null;
        if(parent == null) {
          if(!kind.oneOf(Kind.DOCUMENT, Kind.JNODE)) {
            tb.add(name(Function.ROOT.definition().name, false, lexical, namespaces, qc)).add("()");
          }
          break;
        }
        // step: name/type
        final QNm qname = node.qname();
        if(kind == Kind.ATTRIBUTE) {
          tb.add('@').add(name(qname, true, lexical, namespaces, qc));
        } else if(kind == Kind.ELEMENT) {
          tb.add(name(qname, false, lexical, namespaces, qc));
        } else if(kind == Kind.PROCESSING_INSTRUCTION) {
          tb.add(kind.toString(Token.string(qname.local())));
        } else if(kind == Kind.NAMESPACE) {
          tb.add("namespace::");
          if(qname.local().length != 0) {
            tb.add(qname.local());
          } else {
            tb.add("*[").add(name(Function.LOCAL_NAME.definition().name, false, lexical,
                namespaces, qc)).add("()=\"\"]");
          }
        } else if(kind.oneOf(Kind.COMMENT, Kind.TEXT)) {
          tb.add(type.toString());
        } else if(node instanceof final JNode jnode && jnode.isItem()) {
          // item node: the single node of the item axis of a singleton value
          tb.add("item::*");
        } else if(parent instanceof JNode) {
          final Item key = ((JNode) node).key;
          final byte[] string = key.string(info);
          if(key instanceof AStr || key instanceof Atm || key instanceof Uri) {
            // string, untypedAtomic, anyURI: NCName, or quoted string
            tb.add(XMLToken.isNCName(string) ? string : QueryString.toQuoted(string));
          } else if(key instanceof ANum) {
            // numeric (includes array indexes)
            tb.add(string);
          } else if(key instanceof final QNm qnm) {
            // QName: EQName literal
            tb.add('#').add(qnm.eqName());
          } else if(key instanceof Bln) {
            // boolean
            tb.add(string).add("()");
          } else {
            // any other type: constructor function, wrapped in a selector step
            tb.add("child::{").add(key.type.toString()).add('(').add(QueryString.toQuoted(string)).
              add(")}");
          }
        }
        // optional index
        if(indexes && !kind.oneOf(Kind.ATTRIBUTE, Kind.NAMESPACE, Kind.JNODE)) {
          // reuse the index of the last numbered sibling (linear costs for sibling sequences)
          final int slot = kind.ordinal() * 31 +
            (qname != null ? Token.hashCode(qname.string()) : 0) & lastIndexes.length() - 1;
          NodeIndex last = lastIndexes.get(slot);
          if(last != null && last.modifications != mods) last = null;
          int index = 0;
          for(final GNode nd : node.precedingSiblingIter(true)) {
            qc.checkStop();
            if(nd.kind() != kind) continue;
            final QNm qnm = nd.qname();
            if(qnm == null || qnm.eq(qname)) {
              if(last != null && nd.is(last.node)) {
                index += last.index;
                break;
              }
              index++;
            }
          }
          if(last == null || !node.is(last.node)) {
            lastIndexes.set(slot, new NodeIndex(node, index, mods));
          }
          tb.add('[').addInt(index).add(']');
        }
        step = tb.next();
        if(cacheKey != null) cachedSteps.put(cacheKey, new CachedStep(step, mods));
      }
      steps.add(step);
      node = parent;

      // root node: finalize traversal
      if(origin instanceof final GNode nd && node.is(nd)) {
        relative = true;
        break;
      }
    }
    if(origin instanceof GNode && !relative) throw PATH_X.get(info, origin);

    // add all steps in reverse order
    for(int s = steps.size() - 1; s >= 0; --s) {
      if(!(tb.isEmpty() && origin instanceof GNode)) tb.add('/');
      tb.add(steps.get(s));
    }
    return Str.get(tb.isEmpty() ? Token.cpToken('/') : tb.finish());
  }

  /**
   * Returns a name string for the specified QName.
   * @param qnm QName
   * @param attr attribute flag
   * @param lexical lexical flag
   * @param namespaces namespaces
   * @param qc query context
   * @return name
   * @throws QueryException query exception
   */
  private byte[] name(final QNm qnm, final boolean attr, final boolean lexical,
      final XQMap namespaces, final QueryContext qc) throws QueryException {

    if(lexical) return qnm.string();
    for(final XQMap.Entry ns : namespaces.entries()) {
      if(Token.eq(qnm.uri(), toToken(ns.value(), qc))) {
        return new QNm(toToken(ns.key()), qnm.local(), qnm.uri()).string();
      }
    }
    return attr ? qnm.unique() : qnm.eqName();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst(true, false, cc.qc.focus.value);
  }

  /**
   * Numbered node.
   * @param node node
   * @param index index among the preceding siblings of the same kind and name
   * @param modifications number of database modifications when the index was computed
   */
  private record NodeIndex(GNode node, int index, int modifications) { }

  /**
   * Cached step.
   * @param step string representation of the step
   * @param modifications number of database modifications when the step was cached
   */
  private record CachedStep(byte[] step, int modifications) { }

  /**
   * Step cache.
   * @author BaseX Team, BSD License
   * @author Christian Gruen
   */
  private static final class StepCache extends LinkedHashMap<Long, CachedStep> {
    /** Constructor. */
    private StepCache() {
      super(16, 0.75f, true);
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<Long, CachedStep> eldest) {
      return size() > 1000;
    }
  }
}

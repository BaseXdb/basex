package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbCounts extends StandardFunc {
  /** Options. */
  public static final class CountsOptions extends Options {
    /** Option. */
    public static final ValueOption MISSING = new ValueOption("missing", ANY_ATOMIC_TYPE_ZO);
    /** Option. */
    public static final ValueOption WIDTH = new ValueOption("width", NUMERIC_ZO);
  }

  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final Value nodes = arg(0).value(qc);
    final FItem values = toFunction(arg(1), 1, qc);
    final CountsOptions options = options(2, CountsOptions::new, qc);
    final Value missing = options.get(CountsOptions.MISSING);
    final Value width = options.get(CountsOptions.WIDTH);
    final double w = width.isEmpty() ? 0 : ((ANum) width).dbl();
    if(!width.isEmpty() && !(w > 0))
      throw INVALIDVALUE_X_X.get(info, CountsOptions.WIDTH.name(), width);

    // simple path: values are read from the database table
    final NameTest[] tests = tests(values, nodes.seqType().type);
    final DbPres dbPres = tests != null ? dbPres(nodes) : null;
    final boolean[][] ids = dbPres != null ? ids(tests, dbPres.data) : null;

    final Counter counter = new Counter(missing.isEmpty() ? null : (Item) missing, w);
    if(ids != null) {
      final boolean attr = tests[tests.length - 1].kind == Kind.ATTRIBUTE;
      int c = 0;
      for(final int pre : dbPres.pres) {
        if((++c & 0xFFF) == 0) qc.checkStop();
        collect(dbPres.data, pre, ids, 0, attr, counter);
        counter.end();
      }
    } else {
      final HofArgs args = new HofArgs(1, values);
      for(final Item node : nodes) {
        for(final Item item : invoke(values, args.set(0, node), qc).atomValue(qc, info)) {
          counter.add(item);
        }
        counter.end();
      }
    }
    return counter.map();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(2, CountsOptions::new, cc);
    if(arg(1) instanceof final FItem func && tests(func, arg(0).seqType().type) != null) {
      cc.info(QueryText.OPTVALUES_X, func);
    }
    return this;
  }

  /**
   * Returns the name tests of a function whose body is a path with child and attribute steps.
   * @param func function
   * @param type type of the input nodes
   * @return name tests, or {@code null} if the values cannot be read from the table
   */
  private static NameTest[] tests(final FItem func, final Type type) {
    if(!(func instanceof final FuncItem fi) || fi.arity() != 1) return null;
    // the parameter must accept each single node without conversion
    final SeqType param = fi.funcType().argTypes[0];
    if(!param.occ.check(1) || !type.instanceOf(param.type)) return null;

    Expr body = fi.expr;
    // skip type check that only atomizes the result
    if(body instanceof final TypeCheck tc && tc.seqType().type == BasicType.ANY_ATOMIC_TYPE &&
        tc.expr.seqType().occ.instanceOf(tc.seqType().occ)) body = tc.expr;
    // coerced function item: continue with the original function
    if(body instanceof final DynFuncCall dfc && dfc.exprs.length == 2 &&
        dfc.exprs[1] instanceof final FItem inner && param(dfc.exprs[0], fi)) {
      return tests(inner, type);
    }
    if(!(body instanceof final Path path) || !param(path.root, fi)) return null;

    final int sl = path.steps.length;
    final NameTest[] tests = new NameTest[sl];
    for(int s = 0; s < sl; s++) {
      if(!(path.steps[s] instanceof final Step step) || step.exprs.length != 0 ||
        !(step.test instanceof final NameTest test) ||
        test.scope.oneOf(NameTest.Scope.ALL, NameTest.Scope.URI)) return null;
      if(step.axis == Axis.ATTRIBUTE ? s < sl - 1 || test.kind != Kind.ATTRIBUTE :
        step.axis != Axis.CHILD || !test.kind.oneOf(Kind.ELEMENT, Kind.NODE)) return null;
      tests[s] = test;
    }
    return tests;
  }

  /**
   * Checks if the expression is a reference to the parameter of a function.
   * @param expr expression (can be {@code null})
   * @param func function
   * @return result of check
   */
  private static boolean param(final Expr expr, final FuncItem func) {
    return expr instanceof final VarRef ref && ref.var.name.eq(func.paramName(0));
  }

  /**
   * Database and pre values of nodes.
   * @param data data reference
   * @param pres pre values
   */
  private record DbPres(Data data, int[] pres) { }

  /**
   * Returns the database and the pre values of nodes that are stored in the same database.
   * @param nodes nodes
   * @return database and pre values, or {@code null}
   */
  private static DbPres dbPres(final Value nodes) {
    if(nodes instanceof final DBNodeSeq seq) return new DbPres(seq.data(), seq.pres());
    Data data = null;
    final IntList pres = new IntList(nodes.size());
    for(final Item item : nodes) {
      if(!(item instanceof final DBNode node) || data != null && data != node.data()) return null;
      data = node.data();
      pres.add(node.pre());
    }
    return data != null ? new DbPres(data, pres.finish()) : null;
  }

  /**
   * Returns the ids of the database names that are matched by the name tests.
   * @param tests name tests
   * @param data data reference
   * @return flags for the matching name ids, or {@code null} if a name cannot be resolved
   */
  private static boolean[][] ids(final NameTest[] tests, final Data data) {
    final int tl = tests.length;
    final boolean[][] ids = new boolean[tl][];
    for(int t = 0; t < tl; t++) {
      final NameTest test = tests[t];
      final TokenList matches = test.dbNames(data);
      if(matches == null) return null;
      final Names names = test.kind == Kind.ATTRIBUTE ? data.attrNames : data.elemNames;
      ids[t] = new boolean[names.size() + 1];
      for(final byte[] name : matches) ids[t][names.index(name)] = true;
    }
    return ids;
  }

  /**
   * Collects the values of a path.
   * @param data data reference
   * @param pre pre value of the context node
   * @param ids flags for the matching name ids of the steps
   * @param s index of the current step
   * @param attr indicates if the last step is an attribute step
   * @param counter counter for the values
   * @throws QueryException query exception
   */
  private static void collect(final Data data, final int pre, final boolean[][] ids, final int s,
      final boolean attr, final Counter counter) throws QueryException {
    final int kind = data.kind(pre);
    final boolean[] names = ids[s];
    final boolean last = s == ids.length - 1;
    if(last && attr) {
      final int end = pre + data.attSize(pre, kind);
      for(int p = pre + 1; p < end; p++) {
        if(names[data.nameId(p)]) counter.add(data, p, true);
      }
    } else if(kind == Data.ELEM || kind == Data.DOC) {
      final int end = pre + data.size(pre, kind);
      for(int p = pre + data.attSize(pre, kind); p < end;) {
        final int k = data.kind(p);
        if(k == Data.ELEM && names[data.nameId(p)]) {
          if(last) counter.add(data, p, false);
          else collect(data, p, ids, s + 1, attr, counter);
        }
        p += data.size(p, k);
      }
    }
  }


  /** Value counter. */
  private final class Counter {
    /** Distinct keys. */
    private final HashItemSet keys = new HashItemSet(ItemSet.Mode.ATOMIC, info);
    /** Key for nodes without values (can be {@code null}). */
    private final Item missing;
    /** Bucket width ({@code 0} if values are not bucketed). */
    private final double width;
    /** Counts, indexed by the ids of the keys. */
    private int[] counts = new int[16];
    /** Number of the node that was last counted for a key, indexed by the ids of the keys. */
    private int[] counted = new int[16];
    /** Number of the current node. */
    private int node = 1;
    /** Indicates if a value was found for the current node. */
    private boolean found;

    /**
     * Constructor.
     * @param missing key for nodes without values (can be {@code null})
     * @param width bucket width ({@code 0} if values are not bucketed)
     */
    Counter(final Item missing, final double width) {
      this.missing = missing;
      this.width = width;
    }

    /**
     * Counts the value of an attribute or element that is read from the database table.
     * @param data data reference
     * @param pre pre value
     * @param attr attribute flag
     * @throws QueryException query exception
     */
    void add(final Data data, final int pre, final boolean attr) throws QueryException {
      count(width > 0 ? bucket(new DBNode(data, pre, attr ? Data.ATTR : Data.ELEM).dbl(info)) :
        Atm.get(attr ? data.text(pre, false) : data.atom(pre)));
    }

    /**
     * Counts a value.
     * @param value value
     * @throws QueryException query exception
     */
    void add(final Item value) throws QueryException {
      count(width > 0 ? bucket(toDouble(value)) : value);
    }

    /**
     * Finishes counting the values of a node.
     * @throws QueryException query exception
     */
    void end() throws QueryException {
      if(!found && missing != null) count(missing);
      found = false;
      node++;
    }

    /**
     * Returns the bucket of a number.
     * @param number number
     * @return bucket
     */
    private Item bucket(final double number) {
      return Dbl.get(Math.floor(number / width) * width);
    }

    /**
     * Counts a key; each distinct key of a node is counted once.
     * @param key key
     * @throws QueryException query exception
     */
    private void count(final Item key) throws QueryException {
      final int id = keys.put(key);
      if(id >= counts.length) {
        counts = Arrays.copyOf(counts, id << 1);
        counted = Arrays.copyOf(counted, id << 1);
      }
      if(counted[id] != node) {
        counted[id] = node;
        counts[id]++;
      }
      found = true;
    }

    /**
     * Returns the counts in the order in which the keys were found.
     * @return map
     * @throws QueryException query exception
     */
    XQMap map() throws QueryException {
      final int size = keys.size();
      final MapBuilder mb = new MapBuilder(size);
      for(int id = 1; id <= size; id++) mb.put(keys.key(id), Itr.get(counts[id]));
      return mb.map();
    }
  }
}

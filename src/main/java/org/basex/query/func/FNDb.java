package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import org.basex.core.User;
import org.basex.core.cmd.Info;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.InfoIndex;
import org.basex.core.cmd.List;
import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.index.IndexToken.IndexType;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.expr.IndexAccess;
import org.basex.query.ft.FTIndexAccess;
import org.basex.query.ft.FTWords;
import org.basex.query.item.DBNode;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.iter.ItemIter;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.path.NameTest;
import org.basex.query.util.DataBuilder;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.IntList;
import org.basex.util.XMLToken;

/**
 * Database functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNDb extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNDb(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case OPEN:    return open(ctx);
      case TEXTIDX: return textIndex(ctx);
      case ATTRIDX: return attributeIndex(ctx);
      case FTIDX:   return fulltextIndex(ctx);
      case FTMARK:  return fulltextMark(ctx);
      case LIST:    return list(ctx);
      case NODEID:  return node(ctx, true);
      case NODEPRE: return node(ctx, false);
      default:      return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case OPENID:  return open(ctx, true);
      case OPENPRE: return open(ctx, false);
      case SYSTEM:  return system(ctx);
      case INFO:    return info(ctx);
      case IDXINFO: return indexInfo(ctx);
      default:      return super.item(ctx, ii);
    }
  }

  /**
   * Performs the open function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter open(final QueryContext ctx) throws QueryException {
    final byte[] str = checkStr(expr[0], ctx);
    final int s = indexOf(str, '/');
    final byte[] db = s == -1 ? str : substring(str, 0, s);
    final byte[] path = s == -1 ? EMPTY : substring(str, s + 1);
    final DBNode n = ctx.resource.doc(db, true, true, input);
    final IntList il = n.data.doc(string(path));
    final NodIter col = new NodIter();
    for(int i = 0; i < il.size(); ++i) col.add(new DBNode(n.data, il.get(i)));
    return col;
  }

  /**
   * Performs the open-id and open-pre function.
   * @param ctx query context
   * @param id id flag
   * @return iterator
   * @throws QueryException query exception
   */
  private DBNode open(final QueryContext ctx, final boolean id)
      throws QueryException {

    final DBNode node = 
      ctx.resource.doc(checkStr(expr[0], ctx), true, true, input);
    final int v = (int) checkItr(expr[1], ctx);
    final int pre = id ? node.data.pre(v) : v;
    if(pre < 0 || pre >= node.data.meta.size) IDINVALID.thrw(input, this, v);
    return new DBNode(node.data, pre);
  }

  /**
   * Performs the text-index function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter textIndex(final QueryContext ctx) throws QueryException {
    final IndexContext ic = new IndexContext(ctx, data(ctx), null, true);
    if(!ic.data.meta.textindex) NOIDX.thrw(input, this);
    return new IndexAccess(input, expr[0], IndexType.TEXT, ic).iter(ctx);
  }

  /**
   * Performs the attribute-index function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter attributeIndex(final QueryContext ctx) throws QueryException {
    final IndexContext ic = new IndexContext(ctx, data(ctx), null, true);
    if(!ic.data.meta.attrindex) NOIDX.thrw(input, this);
    final IndexAccess ia = new IndexAccess(
        input, expr[0], IndexType.ATTRIBUTE, ic);

    // return iterator if no name test is specified
    if(expr.length < 2) return ia.iter(ctx);

    // parse and compile the name test
    final Item name = checkEmpty(expr[1].item(ctx, input));
    final QNm nm = new QNm(checkStr(name, ctx), ctx, input);

    final NameTest nt = new NameTest(nm, NameTest.Name.STD, true, input);
    // no results expected: return empty sequence
    if(!nt.comp(ctx)) return Empty.ITER;

    // wrap iterator with name test
    return new Iter() {
      final NodeIter ir = ia.iter(ctx);

      @Override
      public Item next() throws QueryException {
        Nod n;
        while((n = ir.next()) != null && !nt.eval(n));
        return n;
      }
    };
  }

  /**
   * Performs the fulltext-index function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter fulltextIndex(final QueryContext ctx) throws QueryException {
    final IndexContext ic = new IndexContext(ctx, data(ctx), null, true);
    if(!ic.data.meta.ftindex) NOIDX.thrw(input, this);

    final byte[] str = checkStr(expr[0], ctx);
    final FTWords words = new FTWords(input, ic.data, Str.get(str), ctx);
    return new FTIndexAccess(input, words, ic).iter(ctx);
  }

  /**
   * Performs the fulltext-mark function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter fulltextMark(final QueryContext ctx) throws QueryException {
    // name of the marker element; default is <mark/>
    final byte[] m = expr.length == 2 ? checkStr(expr[1], ctx) : null;
    if(m != null && !XMLToken.isQName(m)) Err.value(input, Type.QNM, m);

    return new Iter() {
      Iter ir;
      ItemIter ii;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(ii != null) {
            final Item it = ii.next();
            if(it != null) return it;
            ii = null;
          }
          final FTPosData ftd = ctx.ftpos;
          ctx.ftpos = new FTPosData();
          if(ir == null) ir = ctx.iter(expr[0]);
          Item it = ir.next();
          if(it != null) {
            final byte[] mark = m != null ? m : ctx.ftopt.mark;
            ii = DataBuilder.mark(checkDBNode(it), mark, ctx);
          }
          ctx.ftpos = ftd;
          if(it == null) return null;
        }
      }
    };
  }

  /**
   * Performs the list function.
   * @param ctx query context
   * @return iterator
   */
  private Iter list(final QueryContext ctx) {
    final ItemIter ii = new ItemIter();
    for(final String s : List.list(ctx.resource.context)) ii.add(Str.get(s));
    return ii;
  }

  /**
   * Performs the system function.
   * @param ctx query context
   * @return iterator
   */
  private Str system(final QueryContext ctx) {
    return Str.get(delete(Info.info(ctx.resource.context), '\r'));
  }

  /**
   * Performs the info function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Str info(final QueryContext ctx) throws QueryException {
    final boolean create = ctx.resource.context.user.perm(User.CREATE);
    final byte[] info = InfoDB.db(data(ctx).meta, false, true, create);
    return Str.get(delete(info, '\r'));
  }

  /**
   * Performs the index-info function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Str indexInfo(final QueryContext ctx) throws QueryException {
    final byte[] tp = checkStr(expr[0], ctx);
    final byte[] info = InfoIndex.info(string(tp), data(ctx));
    if(info.length == 0) NOIDX.thrw(input, this);
    return Str.get(delete(info, '\r'));
  }

  /**
   * Performs the node-id function.
   * @param ctx query context
   * @param id id flag
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter node(final QueryContext ctx, final boolean id)
      throws QueryException {

    return new Iter() {
      final Iter ir = expr[0].iter(ctx);

      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) return null;
        final DBNode node = checkDBNode(it);
        return Itr.get(id ? node.data.id(node.pre) : node.pre);
      }
    };
  }

  /**
   * Returns the data reference.
   * @param ctx query context
   * @return data reference
   * @throws QueryException query exception
   */
  private Data data(final QueryContext ctx) throws QueryException {
    final Data data = ctx.resource.data();
    if(data == null) NODBCTX.thrw(input, this);
    return data;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    if(def == FunDef.FTMARK) {
      final boolean fast = ctx.ftfast;
      ctx.ftfast = false;
      final Expr e = super.comp(ctx);
      ctx.ftfast = fast;
      return e;
    }
    return super.comp(ctx);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX && (def == FunDef.TEXTIDX || def == FunDef.ATTRIDX ||
        def == FunDef.FTIDX) || super.uses(u);
  }
}

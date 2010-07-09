package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.core.Prop;
import org.basex.data.SerializerProp;
import org.basex.data.XMLSerializer;
import org.basex.io.CachedOutput;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.TextInput;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryText;
import org.basex.query.expr.Expr;
import org.basex.query.item.Atm;
import org.basex.query.item.Bln;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.SeqType;
import org.basex.query.item.Str;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.SeqIter;
import org.basex.query.up.primitives.Put;
import org.basex.query.util.Err;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenMap;

/**
 * Generating functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNGen extends Fun {
  /** Cached file contents. */
  private final TokenMap contents = new TokenMap();
  
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case DATA:    return data(ctx);
      case COLL:    return collection(ctx);
      case PUT:     return put(ctx);
      case URICOLL: return uriCollection(ctx);
      default:      return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case DOC:         return doc(ctx);
      case DOCAVL:      return docAvailable(ctx);
      case PARSETXT:    return unparsedText(ctx);
      case PARSETXTAVL: return unparsedTextAvailable(ctx);
      case PARSEXML:    return parseXml(ctx);
      case SERIALIZE:   return serialize(ctx);
      default:          return super.atomic(ctx);
    }
  }

  @Override
  public Expr c(final QueryContext ctx) throws QueryException {
    if(func == FunDef.DOC)
      return expr[0].i() ? atomic(ctx) : this;
    if(func == FunDef.COLL)
      return expr.length != 0 && expr[0].i() ? iter(ctx).finish() : this;
    if(func == FunDef.PARSETXT) {
      return expr[0].i() && (expr.length == 1 || expr[1].i()) ?
        atomic(ctx) : this;
    }
    return this;
  }

  /**
   * Performs the data function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Iter data(final QueryContext ctx) throws QueryException {
    final Iter ir = ctx.iter(expr.length != 0 ? expr[0] : checkCtx(ctx));

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        return it != null ? atom(it) : null;
      }
    };
  }

  /**
   * Performs the collection function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private NodIter collection(final QueryContext ctx) throws QueryException {
    byte[] coll = null;
    if(expr.length != 0) {
      final Item it = expr[0].atomic(ctx);
      if(it == null) Err.empty(this);
      coll = checkStr(it);
    }
    return ctx.coll(coll);
  }

  /**
   * Performs the uri-collection function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private SeqIter uriCollection(final QueryContext ctx) throws QueryException {
    final NodIter coll = collection(ctx);
    final SeqIter ir = new SeqIter();
    Nod it = null;
    while((it = coll.next()) != null) ir.add(Uri.uri(it.base()));
    return ir;
  }

  /**
   * Performs the put function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Iter put(final QueryContext ctx) throws QueryException {
    checkAdmin(ctx);
    final byte[] file = checkStr(expr[1], ctx);
    final Item it = expr[0].atomic(ctx);

    if(it == null || it.type != Type.DOC && it.type != Type.ELM)
      Err.or(UPFOTYPE, expr[0]);

    final Uri u = Uri.uri(file);
    if(u == Uri.EMPTY || !u.valid()) Err.or(UPFOURI, file);
    ctx.updates.add(new Put((Nod) it, u), ctx);

    return Iter.EMPTY;
  }

  /**
   * Performs the doc function.
   * @param ctx query context
   * @return resulting node
   * @throws QueryException query exception
   */
  private Nod doc(final QueryContext ctx) throws QueryException {
    final Item it = expr[0].atomic(ctx);
    return it == null ? null : ctx.doc(checkStr(it), false, false);
  }

  /**
   * Performs the doc-available function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Bln docAvailable(final QueryContext ctx) throws QueryException {
    try {
      return Bln.get(doc(ctx) != null);
    } catch(final QueryException ex) {
      if(!ex.code().startsWith(QueryText.FODC)) throw ex;
      return Bln.FALSE;
    }
  }

  /**
   * Performs the unparsed-text function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Item unparsedText(final QueryContext ctx) throws QueryException {
    return unparsedText(ctx, false);
  }

  /**
   * Performs the unparsed-text-available function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Bln unparsedTextAvailable(final QueryContext ctx)
      throws QueryException {

    try {
      return Bln.get(unparsedText(ctx, true) != null);
    } catch(final QueryException ex) {
      if(!ex.code().startsWith(QueryText.FODC)) throw ex;
      return Bln.FALSE;
    }
  }

  /**
   * Performs the unparsed-text function. The result is optionally cached.
   * @param ctx query context
   * @param cache flag for caching the result
   * @return resulting item
   * @throws QueryException query exception
   */
  private Str unparsedText(final QueryContext ctx, final boolean cache)
      throws QueryException {

    final IO io = checkIO(expr[0], ctx);
    final String enc = expr.length < 2 ? null : string(checkStr(expr[1], ctx));
    try {
      final byte[] path = token(io.path());
      byte[] cont = contents.get(path);
      // check if content has already been parsed; if not, read original file
      if(cont == null) {
        cont = TextInput.content(io, enc).finish();
        if(cache) contents.add(path, cont);
      }
      return Str.get(cont);
    } catch(final IOException ex) {
      Err.or(NODOC, ex.getMessage() != null ? ex.getMessage() : ex.toString());
      return null;
    }
  }

  /**
   * Performs the parse-xml function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Nod parseXml(final QueryContext ctx) throws QueryException {
    final byte[] cont = checkStr(expr[0], ctx);
    Uri base = ctx.baseURI;
    if(expr.length == 2) {
      base = Uri.uri(checkStr(expr[1], ctx));
      if(!base.valid()) Err.or(DOCBASE, base);
    }
    
    final Prop prop = ctx.context.prop;
    final IO io = new IOContent(cont, string(base.str()));
    try {
      final Parser p = Parser.fileParser(io, prop, "");
      return new DBNode(MemBuilder.build(p, prop, ""), 0);
    } catch(final IOException ex) {
      Err.or(DOCWF, cont);
      return null;
    }
  }

  /**
   * Performs the serialize function.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext ctx) throws QueryException {
    final Item it = expr[0].atomic(ctx);
    if(it == null) Err.empty(this);
    final Nod nod = checkNode(it);

    // interpret query parameters
    final TokenBuilder tb = new TokenBuilder();
    if(expr.length == 2) {
      final Iter ir = expr[1].iter(ctx);
      Item n;
      while((n = ir.next()) != null) {
        final Nod p = checkNode(n);
        if(tb.size() != 0) tb.add(',');
        tb.add(p.nname()).add('=').add(p.str());
      }
    }
    try {
      // run serialization
      final CachedOutput co = new CachedOutput();
      nod.serialize(new XMLSerializer(co, new SerializerProp(tb.toString())));
      return Str.get(co.finish());
    } catch(final IOException ex) {
      throw new QueryException(ex.getMessage());
    }
  }
  
  /**
   * Atomizes the specified item.
   * @param it input item
   * @return atomized item
   */
  static Item atom(final Item it) {
    return it.node() ? it.type == Type.PI || it.type == Type.COM ?
        Str.get(it.str()) : new Atm(it.str()) : it;
  }

  @Override
  public boolean uses(final Use u, final QueryContext ctx) {
    return u == Use.UPD ? func == FunDef.PUT : super.uses(u, ctx);
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    if(func == FunDef.DATA) {
      final SeqType ret = expr[0].returned(ctx);
      return ret.type.node() ? new SeqType(Type.ATM, ret.occ) : ret;
    }
    return super.returned(ctx);
  }
}

package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.util.Err.ErrType;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Generating functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNGen extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNGen(final InputInfo ii, final Function f, final Expr[] e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case DATA:                return dataa(ctx);
      case COLLECTION:          return collection(ctx).iter();
      case URI_COLLECTION:      return uriCollection(ctx);
      case UNPARSED_TEXT_LINES: return unparsedTextLines(ctx);
      default:                  return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case DOC:                     return doc(ctx);
      case DOC_AVAILABLE:           return docAvailable(ctx);
      case UNPARSED_TEXT:           return unparsedText(ctx, false);
      case UNPARSED_TEXT_AVAILABLE: return unparsedText(ctx, true);
      case PUT:                     return put(ctx);
      case PARSE_XML:               return parseXml(ctx, false);
      case PARSE_XML_FRAGMENT:      return parseXml(ctx, true);
      case SERIALIZE:               return serialize(ctx);
      default:                      return super.item(ctx, ii);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case COLLECTION: return collection(ctx);
      default:         return super.value(ctx);
    }
  }

  @Override
  Expr opt(final QueryContext ctx) throws QueryException {
    if(sig == Function.DATA && expr.length == 1) {
      final SeqType t = expr[0].type();
      type = t.type.isNode() ? SeqType.get(AtomType.ATM, t.occ) : t;
    }
    return this;
  }

  /**
   * Performs the data function.
   * @param ctx query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Iter dataa(final QueryContext ctx) throws QueryException {
    final Iter ir = ctx.iter(expr.length != 0 ? expr[0] : checkCtx(ctx));

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) return null;
        if(it instanceof FItem) FIATOM.thrw(info, FNGen.this);
        return atom(it, info);
      }
    };
  }

  /**
   * Performs the collection function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Value collection(final QueryContext ctx) throws QueryException {
    // return default collection
    if(expr.length == 0) return ctx.resource.collection(info);

    // check if reference is valid
    final byte[] in = checkEStr(expr[0].item(ctx, info));
    if(!Uri.uri(in).isValid()) INVCOLL.thrw(info, in);
    return ctx.resource.collection(string(in), info);
  }

  /**
   * Performs the uri-collection function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Iter uriCollection(final QueryContext ctx) throws QueryException {
    final Iter coll = collection(ctx).iter();
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = coll.next();
        // all items will be nodes
        return it == null ? null : Uri.uri(((ANode) it).baseURI(), false);
      }
    };
  }

  /**
   * Performs the put function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Item put(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    final byte[] file = checkEStr(expr[1], ctx);
    final ANode nd = checkNode(checkNoEmpty(expr[0].item(ctx, info)));
    if(nd.type != NodeType.DOC && nd.type != NodeType.ELM) UPFOTYPE.thrw(info, expr[0]);

    final Uri u = Uri.uri(file);
    if(u == Uri.EMPTY || !u.isValid()) UPFOURI.thrw(info, file);
    final DBNode target = ctx.updates.determineDataRef(nd, ctx);

    final String uri = IO.get(u.toJava()).path();
    // check if all target paths are unique
    if(ctx.updates.putPaths.add(uri) < 0) UPURIDUP.thrw(info, uri);

    ctx.updates.add(new Put(info, target.pre, target.data, uri), ctx);
    return null;
  }

  /**
   * Performs the doc function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private ANode doc(final QueryContext ctx) throws QueryException {
    final Item it = expr[0].item(ctx, info);
    if(it == null) return null;
    final byte[] in = checkEStr(it);
    if(!Uri.uri(in).isValid()) INVDOC.thrw(info, in);
    return ctx.resource.doc(new QueryInput(string(in)), info);
  }

  /**
   * Performs the doc-available function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln docAvailable(final QueryContext ctx) throws QueryException {
    try {
      return Bln.get(doc(ctx) != null);
    } catch(final QueryException ex) {
      final Err err = ex.err();
      if(err != null && err.type == ErrType.FODC &&
          (err.num == 2 || err.num == 4)) return Bln.FALSE;
      throw ex;
    }
  }

  /**
   * Performs the unparsed-text function.
   * @param ctx query context
   * @param check only check if text is available
   * @return content
   * @throws QueryException query exception
   */
  private Item unparsedText(final QueryContext ctx, final boolean check)
      throws QueryException {

    checkCreate(ctx);
    final byte[] path = checkStr(expr[0], ctx);
    final IO base = ctx.sc.baseIO();
    if(base == null) throw STBASEURI.thrw(info);

    String enc = null;
    try {
      enc = encoding(1, WHICHENC, ctx);

      final String p = string(path);
      if(p.indexOf('#') != -1) FRAGID.thrw(info, p);
      if(!Uri.uri(p).isValid()) INVURL.thrw(info, p);

      IO io = base.merge(p);
      final String rp = ctx.resource.resources.get(io.path());
      if(rp != null) io = IO.get(rp);
      if(!io.exists()) throw RESNF.thrw(info, p);

      final InputStream is = io.inputStream();
      try {
        final TextInput ti = new TextInput(io).valid(true);
        if(enc != null) ti.encoding(enc);
        if(!check) return Str.get(ti.content());
        while(ti.read() != -1);
        return Bln.TRUE;
      } finally {
        is.close();
      }
    } catch(final QueryException ex) {
      if(check && ex.err().type != ErrType.XPTY) return Bln.FALSE;
      throw ex;
    } catch(final IOException ex) {
      if(check) return Bln.FALSE;
      if(ex instanceof EncodingException) INVCHARS.thrw(info, ex);
      if(ex instanceof InputException && enc == null) WHICHCHARS.thrw(info);
      throw RESNF.thrw(info, path);
    }
  }

  /**
   * Performs the unparsed-text-lines function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  Iter unparsedTextLines(final QueryContext ctx) throws QueryException {
    return textIter(unparsedText(ctx, false).string(info));
  }

  /**
   * Returns the specified text as lines.
   * @param str text input
   * @return result
   */
  static Iter textIter(final byte[] str) {
    // not I/O exception expected, as input is a main-memory array
    try {
      final NewlineInput nli = new NewlineInput(new ArrayInput(str));
      final TokenBuilder tb = new TokenBuilder();
      return new Iter() {
        @Override
        public Item next() {
          try {
            return nli.readLine(tb) == null ? null : Str.get(tb.finish());
          } catch(final IOException ex) {
            throw Util.notexpected(ex);
          }
        }
      };
    } catch(final IOException ex) {
      throw Util.notexpected(ex);
    }
  }

  /**
   * Returns a document node for the parsed XML input.
   * @param ctx query context
   * @param frag parse fragment
   * @return result
   * @throws QueryException query exception
   */
  private ANode parseXml(final QueryContext ctx, final boolean frag)
      throws QueryException {

    final Item item = expr[0].item(ctx, info);
    if(item == null) return null;

    final byte[] cont = checkEStr(expr[0], ctx);
    final IO io = new IOContent(cont, string(ctx.sc.baseURI().string()));
    final Prop prop = ctx.context.prop;
    final boolean chop = prop.is(Prop.CHOP);
    try {
      prop.set(Prop.CHOP, false);

      final Parser p = frag || prop.is(Prop.INTPARSE) ?
        new XMLParser(io, ctx.context.prop, frag) : new SAXWrapper(io, ctx.context.prop);
      return new DBNode(p);
    } catch(final IOException ex) {
      throw SAXERR.thrw(info, ex);
    } finally {
      prop.set(Prop.CHOP, chop);
    }
  }

  /**
   * Performs the serialize function.
   * @param ctx query context
   * @return result
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext ctx) throws QueryException {
    final ArrayOutput ao = new ArrayOutput();
    try {
      // run serialization
      Item it = expr.length > 1 ? expr[1].item(ctx, info) : null;
      final Serializer ser = Serializer.get(ao, FuncParams.serializerProp(it));
      final Iter ir = expr[0].iter(ctx);
      while((it = ir.next()) != null) ser.serialize(it);
      ser.close();
    } catch(final SerializerException ex) {
      throw ex.getCause(info);
    } catch(final IOException ex) {
      SERANY.thrw(info, ex);
    }
    return Str.get(ao.toArray());
  }

  @Override
  public boolean xquery3() {
    return sig == DATA && expr.length == 0 || oneOf(sig, SERIALIZE, UNPARSED_TEXT,
        UNPARSED_TEXT_LINES, UNPARSED_TEXT_AVAILABLE, PARSE_XML, URI_COLLECTION);
  }

  @Override
  public boolean uses(final Use u) {
    return
      u == Use.CNS && sig == PARSE_XML ||
      u == Use.UPD && sig == PUT ||
      u == Use.X30 && xquery3() ||
      u == Use.CTX && (sig == DATA && expr.length == 0 || sig == PUT) || super.uses(u);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(oneOf(sig, DATA) && expr.length == 0) {
      if(!visitor.lock(DBLocking.CTX)) return false;
    } else if(oneOf(sig, DOC_AVAILABLE, DOC, COLLECTION, URI_COLLECTION)) {
      if(expr.length == 0) {
        if(oneOf(sig, COLLECTION, URI_COLLECTION) && !visitor.lock(DBLocking.COLL))
          return false;
      } else if(!(expr[0] instanceof Str)) {
        if(!visitor.lock(null)) return false;
      } else {
        final QueryInput qi = new QueryInput(string(((Str) expr[0]).string()));
        if(qi.db == null && !visitor.lock(null)) return false;
        if(!visitor.lock(qi.db)) return false;
      }
    }
    return super.accept(visitor);
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return sig == COLLECTION || super.iterable();
  }
}

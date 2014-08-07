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
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.*;
import org.basex.query.util.Err.ErrType;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Generating functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNGen extends StandardFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNGen(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case DATA:                return data(qc);
      case COLLECTION:          return collection(qc).iter();
      case URI_COLLECTION:      return uriCollection(qc);
      case UNPARSED_TEXT_LINES: return unparsedTextLines(qc);
      default:                  return super.iter(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case DOC:                     return doc(qc);
      case DOC_AVAILABLE:           return docAvailable(qc);
      case UNPARSED_TEXT:           return unparsedText(qc, false);
      case UNPARSED_TEXT_AVAILABLE: return unparsedText(qc, true);
      case PUT:                     return put(qc);
      case PARSE_XML:               return parseXml(qc, false);
      case PARSE_XML_FRAGMENT:      return parseXml(qc, true);
      case SERIALIZE:               return serialize(qc);
      default:                      return super.item(qc, ii);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case COLLECTION: return collection(qc);
      default:         return super.value(qc);
    }
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    if(func == DATA && exprs.length == 1) {
      final SeqType t = exprs[0].seqType();
      seqType = t.type instanceof NodeType ? SeqType.get(AtomType.ATM, t.occ) : t;
    }
    return this;
  }

  /**
   * Performs the data function.
   * @param qc query context
   * @return resulting iterator
   * @throws QueryException query exception
   */
  private Iter data(final QueryContext qc) throws QueryException {
    final Iter ir = qc.iter(exprs.length == 0 ? checkCtx(qc) : exprs[0]);

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) return null;
        if(it instanceof FItem) throw FIATOM.get(info, it.type);
        return atom(it, info);
      }
    };
  }

  /**
   * Performs the collection function.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Value collection(final QueryContext qc) throws QueryException {
    // return default collection
    final Item it = exprs.length == 0 ? null : exprs[0].item(qc, info);
    if(it == null) return qc.resources.collection(info);

    // check if reference is valid
    final byte[] in = checkEStr(it);
    if(!Uri.uri(in).isValid()) throw INVCOLL.get(info, in);
    return qc.resources.collection(new QueryInput(string(in)), sc.baseIO(), info);
  }

  /**
   * Performs the uri-collection function.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Iter uriCollection(final QueryContext qc) throws QueryException {
    final Iter coll = collection(qc).iter();
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
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Item put(final QueryContext qc) throws QueryException {
    checkCreate(qc);
    final byte[] file = checkEStr(exprs[1], qc);
    final ANode nd = checkNode(exprs[0], qc);
    if(nd.type != NodeType.DOC && nd.type != NodeType.ELM) throw UPFOTYPE.get(info, exprs[0]);

    final Uri u = Uri.uri(file);
    if(u == Uri.EMPTY || !u.isValid()) throw UPFOURI.get(info, file);
    final Updates updates = qc.resources.updates();
    final DBNode target = updates.determineDataRef(nd, qc);

    final String uri = IO.get(string(u.string())).path();
    // check if all target paths are unique
    if(!updates.putPaths.add(uri)) throw UPURIDUP.get(info, uri);

    updates.add(new Put(target.pre, target.data, uri, info), qc);
    return null;
  }

  /**
   * Performs the doc function.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private ANode doc(final QueryContext qc) throws QueryException {
    final Item it = exprs[0].item(qc, info);
    if(it == null) return null;
    final byte[] in = checkEStr(it);
    if(!Uri.uri(in).isValid()) throw INVDOC.get(info, in);
    return qc.resources.doc(new QueryInput(string(in)), sc.baseIO(), info);
  }

  /**
   * Performs the doc-available function.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Bln docAvailable(final QueryContext qc) throws QueryException {
    try {
      return Bln.get(doc(qc) != null);
    } catch(final QueryException ex) {
      final Err err = ex.err();
      if(err != null) {
        final String num = err.code.length() == 8 ? err.code.substring(4) : "";
        if(err.is(ErrType.FODC) && (num.equals("0002") || num.equals("0004")) ||
           err.is(ErrType.BXDB) && num.equals("0006")) return Bln.FALSE;
      }
      throw ex;
    }
  }

  /**
   * Performs the unparsed-text function.
   * @param qc query context
   * @param check only check if text is available
   * @return content
   * @throws QueryException query exception
   */
  private Item unparsedText(final QueryContext qc, final boolean check) throws QueryException {
    checkCreate(qc);
    final byte[] path = checkStr(exprs[0], qc);
    final IO base = sc.baseIO();

    String enc = null;
    try {
      if(base == null) throw STBASEURI.get(info);
      enc = checkEncoding(1, WHICHENC, qc);

      final String p = string(path);
      if(p.indexOf('#') != -1) throw FRAGID.get(info, p);
      if(!Uri.uri(p).isValid()) throw INVURL.get(info, p);

      IO io = base.merge(p);
      final String[] rp = qc.resources.texts.get(io.path());
      if(rp != null && rp.length > 0) {
        io = IO.get(rp[0]);
        if(rp.length > 1) enc = rp[1];
      }
      if(!io.exists()) throw RESNF.get(info, p);

      final InputStream is = io.inputStream();
      try {
        final TextInput ti = new TextInput(io).encoding(enc).validate(true);
        if(!check) return Str.get(ti.content());
        while(ti.read() != -1);
        return Bln.TRUE;
      } finally {
        is.close();
      }
    } catch(final QueryException ex) {
      if(check && !ex.err().is(ErrType.XPTY)) return Bln.FALSE;
      throw ex;
    } catch(final IOException ex) {
      if(check) return Bln.FALSE;
      if(ex instanceof InputException) {
        final boolean inv = ex instanceof EncodingException || enc != null;
        throw (inv ? INVCHARS : WHICHCHARS).get(info, ex);
      }
      throw RESNF.get(info, path);
    }
  }

  /**
   * Performs the unparsed-text-lines function.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Iter unparsedTextLines(final QueryContext qc) throws QueryException {
    return textIter(unparsedText(qc, false).string(info));
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
            return nli.readLine(tb) ? Str.get(tb.toArray()) : null;
          } catch(final IOException ex) {
            throw Util.notExpected(ex);
          }
        }
      };
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Returns a document node for the parsed XML input.
   * @param qc query context
   * @param frag parse fragments
   * @return result
   * @throws QueryException query exception
   */
  private ANode parseXml(final QueryContext qc, final boolean frag) throws QueryException {
    final Item it = exprs[0].item(qc, info);
    if(it == null) return null;

    final IO io = new IOContent(checkStr(it), string(sc.baseURI().string()));
    try {
      final Parser parser;
      if(frag) {
        final MainOptions opts = new MainOptions();
        opts.set(MainOptions.CHOP, false);
        parser = new XMLParser(io, opts, true);
      } else {
        parser = Parser.xmlParser(io);
      }
      return new DBNode(parser);
    } catch(final IOException ex) {
      throw SAXERR.get(info, ex);
    }
  }

  /**
   * Performs the serialize function.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  private Str serialize(final QueryContext qc) throws QueryException {
    final Item it = exprs.length > 1 ? exprs[1].item(qc, info) : null;
    final SerializerOptions sopts = FuncOptions.serializer(it, info);
    return Str.get(serialize(exprs[0].iter(qc), sopts, SERANY));
  }

  @Override
  public boolean has(final Flag flag) {
    return (flag == Flag.X30 || flag == Flag.CTX) && func == DATA && exprs.length == 0 ||
        super.has(flag);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    if(oneOf(func, DATA) && exprs.length == 0) {
      if(!visitor.lock(DBLocking.CTX)) return false;
    } else if(oneOf(func, DOC_AVAILABLE, DOC, COLLECTION, URI_COLLECTION)) {
      if(exprs.length == 0) {
        if(oneOf(func, COLLECTION, URI_COLLECTION) && !visitor.lock(DBLocking.COLL))
          return false;
      } else if(!(exprs[0] instanceof Str)) {
        if(!visitor.lock(null)) return false;
      } else {
        final QueryInput qi = new QueryInput(string(((Str) exprs[0]).string()));
        if(qi.db == null && !visitor.lock(null)) return false;
        if(!visitor.lock(qi.db)) return false;
      }
    }
    return super.accept(visitor);
  }

  @Override
  public boolean iterable() {
    // collections will never yield duplicates
    return func == COLLECTION || super.iterable();
  }
}
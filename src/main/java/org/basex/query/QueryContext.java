package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.core.cmd.Set;
import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.io.serial.Serializer;
import org.basex.io.serial.SerializerException;
import org.basex.io.serial.SerializerProp;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.func.JavaMapping;
import org.basex.query.func.UserFuncs;
import org.basex.query.item.DBNode;
import org.basex.query.item.Dat;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Tim;
import org.basex.query.item.Type;
import org.basex.query.item.Types;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.up.Updates;
import org.basex.query.util.JDBCConnections;
import org.basex.query.util.Var;
import org.basex.query.util.VarContext;
import org.basex.query.util.json.JsonMapConverter;
import org.basex.util.InputInfo;
import org.basex.util.JarLoader;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.XMLToken;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTOpt;
import org.basex.util.hash.TokenMap;
import org.basex.util.list.IntList;

/**
 * This class provides query-specific methods and properties.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class QueryContext extends Progress {
  /** URL pattern (matching Clark and EQName notation). */
  private static final Pattern BIND =
      Pattern.compile("^((\"|')(.*?)\\2:|(\\{(.*?)\\}))(.+)$");

  /** Static context of an expression. */
  public StaticContext sc = new StaticContext();
  /** Variables. */
  public final VarContext vars = new VarContext();
  /** Functions. */
  public final UserFuncs funcs = new UserFuncs();

  /** Query resources. */
  public final QueryResources resource = new QueryResources(this);
  /** Database context. */
  public final Context context;
  /** XQuery version flag. */
  public boolean xquery3;

  /** Cached stop word files. */
  public HashMap<String, String> stop;
  /** Cached thesaurus files. */
  public HashMap<String, String> thes;
  /** Query options (are valid during query execution). */
  public HashMap<String, String> dbOptions = new HashMap<String, String>();
  /** Global options (will be set after query execution). */
  public HashMap<String, Object> globalOpt = new HashMap<String, Object>();

  /** Root expression of the query. */
  public Expr root;
  /** Current context value. */
  public Value value;
  /** Current context position. */
  public long pos = 1;
  /** Current context size. */
  public long size = 1;
  /** Optional initial context set. */
  Nodes nodes;

  /** Current full-text options. */
  public FTOpt ftopt = new FTOpt();
  /** Current full-text token. */
  public FTLexer fttoken;

  /** Current Date. */
  public Dat date;
  /** Current DateTime. */
  public Dtm dtm;
  /** Current Time. */
  public Tim time;

  /** Full-text position data (needed for highlighting of full-text results). */
  public FTPosData ftpos;
  /** Full-text token counter (needed for highlighting of full-text results). */
  public byte ftoknum;

  /** Pending updates. */
  public final Updates updates = new Updates();
  /** Indicates if this query might perform updates. */
  public boolean updating;

  /** Compilation flag: current node has leaves. */
  public boolean leaf;
  /** Compilation flag: GFLWOR clause performs grouping. */
  public boolean grouping;

  /** Number of successive tail calls. */
  public int tailCalls;
  /** Maximum number of successive tail calls. */
  public final int maxCalls;
  /** Counter for variable IDs. */
  public int varIDs;

  /** Pre-declared modules, containing module uri and their file paths. */
  final TokenMap modDeclared = new TokenMap();
  /** Parsed modules, containing the file path and module uri. */
  final TokenMap modParsed = new TokenMap();

  /** Serializer options. */
  SerializerProp serProp;
  /** Initial context value. */
  public Expr ctxItem;
  /** Java modules. */
  public HashMap<QueryModule, ArrayList<Method>> javaModules =
      new HashMap<QueryModule, ArrayList<Method>>();
  /** JAR modules. */
  public JarLoader jars;
  /** Opened connections to relational databases. */
  JDBCConnections jdbc;

  /** String container for query background information. */
  private final TokenBuilder info = new TokenBuilder();
  /** Info flag. */
  private final boolean inf;
  /** Optimization flag. */
  private boolean firstOpt = true;
  /** Evaluation flag. */
  private boolean firstEval = true;

  /**
   * Constructor.
   * @param ctx database context
   */
  public QueryContext(final Context ctx) {
    context = ctx;
    nodes = ctx.current();
    xquery3 = ctx.prop.is(Prop.XQUERY3);
    inf = ctx.prop.is(Prop.QUERYINFO) || Util.debug;
    final String path = ctx.prop.get(Prop.QUERYPATH);
    if(!path.isEmpty()) sc.baseURI(path);
    maxCalls = ctx.prop.num(Prop.TAILCALLS);
  }

  /**
   * Parses the specified query.
   * @param qu input query
   * @throws QueryException query exception
   */
  public void parse(final String qu) throws QueryException {
    root = new QueryParser(qu, this).parse(sc.baseIO(), null);
  }

  /**
   * Parses the specified module.
   * @param qu input query
   * @throws QueryException query exception
   */
  public void module(final String qu) throws QueryException {
    new QueryParser(qu, this).parse(sc.baseIO(), EMPTY);
  }

  /**
   * Compiles and optimizes the expression.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    // dump compilation info
    if(inf) compInfo(NL + QUERYCOMP);

    // temporarily set database values
    if(dbOptions != null) {
      for(final Entry<String, String> e : dbOptions.entrySet()) {
        Set.set(e.getKey(), e.getValue(), context.prop);
      }
    }

    if(ctxItem != null) {
      // evaluate initial expression
      try {
        value = ctxItem.value(this);
      } catch(final QueryException ex) {
        if(ex.err() != XPNOCTX) throw ex;
        // only {@link ParseExpr} instances may cause this error
        CTXINIT.thrw(((ParseExpr) ctxItem).input, ex.getMessage());
      }
    } else if(nodes != null) {
      // add full-text container reference
      if(nodes.ftpos != null) ftpos = new FTPosData();
      // cache the initial context nodes
      resource.compile(nodes);
    }

    // if specified, convert context item to specified type
    if(value != null && sc.initType != null) {
      value = sc.initType.promote(value, this, null);
    }

    try {
      // compile global functions.
      // variables will be compiled if called for the first time
      funcs.comp(this);
      // compile the expression
      root = root.comp(this);
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      XPSTACK.thrw(null);
    }

    // dump resulting query
    if(inf) info.add(NL + QUERYRESULT + funcs + root + NL);
  }

  /**
   * Returns a result iterator.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    try {
      // evaluate lazily if no updates are possible
      return updating ? value().iter() : iter(root);
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw XPSTACK.thrw(null);
    }
  }

  /**
   * Returns the result value.
   * @return result value
   * @throws QueryException query exception
   */
  public Value value() throws QueryException {
    try {
      final Value v = value(root);
      if(updating) {
        updates.applyUpdates();
        if(context.data() != null) context.update();
      }
      return v;

    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw XPSTACK.thrw(null);
    }
  }

  /**
   * Evaluates the expression with the specified context set.
   * @return resulting value
   * @throws QueryException query exception
   */
  Result execute() throws QueryException {
    // evaluates the query
    final Iter ir = iter();
    final ItemCache ic = new ItemCache();
    Item it;

    // check if all results belong to the database of the input context
    if(serProp == null && nodes != null) {
      final IntList pre = new IntList();

      while((it = ir.next()) != null) {
        checkStop();
        if(!(it instanceof DBNode)) break;
        if(it.data() != nodes.data) break;
        pre.add(((DBNode) it).pre);
      }

      // completed... return standard nodeset with full-text positions
      final int ps = pre.size();
      if(it == null) return ps == 0 ? ic :
          new Nodes(pre.toArray(), nodes.data, ftpos).checkRoot();

      // otherwise, add nodes to standard iterator
      for(int p = 0; p < ps; ++p) ic.add(new DBNode(nodes.data, pre.get(p)));
      ic.add(it);
    }

    // use standard iterator
    while((it = ir.next()) != null) {
      checkStop();
      ic.add(it);
    }
    return ic;
  }

  /**
   * Binds an object to a global variable. If the object is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type. If {@code "json"} is specified as data type,
   * the value is interpreted according to the rules specified in
   * {@link JsonMapConverter}.
   * @param name name of variable
   * @param val object to be bound
   * @param type data type
   * @throws QueryException query exception
   */
  public void bind(final String name, final Object val, final String type)
      throws QueryException {

    Object obj = val;
    if(type != null && !type.isEmpty()) {
      if(type.equals(QueryText.JSONSTR)) {
        obj = JsonMapConverter.parse(token(val.toString()), null);
      } else {
        final QNm nm = new QNm(token(type), this);
        if(!nm.hasURI() && nm.hasPrefix()) NOURI.thrw(null, nm);
        final Type typ = Types.find(nm, true);
        if(typ == null) NOTYPE.thrw(null, nm);
        obj = typ.cast(obj, null);
      }
    }
    bind(name, obj);
  }

  /**
   * Binds an value to a global variable. If the value is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type.
   * @param name name of variable
   * @param val value to be bound
   * @throws QueryException query exception
   */
  public void bind(final String name, final Object val) throws QueryException {
    final Expr ex = val instanceof Expr ? (Expr) val : JavaMapping.toValue(val);

    // remove optional $ prefix
    String nm = name.indexOf('$') == 0 ? name.substring(1) : name;
    byte[] uri = EMPTY;

    // check for namespace declaration
    final Matcher m = BIND.matcher(nm);
    if(m.find()) {
      String u = m.group(3);
      if(u == null) u = m.group(5);
      uri = token(u);
      nm = m.group(6);
    }
    final byte[] ln = token(nm);
    if(nm.isEmpty() || !XMLToken.isNCName(ln)) return;

    // bind variable
    final QNm qnm = uri.length == 0 ? new QNm(ln, this) : new QNm(ln, uri);
    final Var gl = vars.globals().get(qnm);
    if(gl == null) {
      // assign new variable
      vars.updateGlobal(Var.create(this, null, qnm).bind(ex, this));
    } else {
      // reset declaration state and bind new expression
      gl.declared = false;
      gl.bind(gl.type != null ? gl.type.type.cast(ex.item(this, null),
          this, null) : ex, this);
    }
  }

  /**
   * Recursively serializes the query plan.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  protected void plan(final Serializer ser) throws IOException {
    // only show root node if functions or variables exist
    final boolean r = funcs.size() != 0 || vars.globals().size != 0;
    if(r) ser.openElement(PLAN);
    funcs.plan(ser);
    vars.plan(ser);
    root.plan(ser);
    if(r) ser.closeElement();
  }

  /**
   * Evaluates the specified expression and returns an iterator.
   * @param e expression to be evaluated
   * @return iterator
   * @throws QueryException query exception
   */
  public Iter iter(final Expr e) throws QueryException {
    checkStop();
    return e.iter(this);
  }

  /**
   * Evaluates the specified expression and returns an iterator.
   * @param expr expression to be evaluated
   * @return iterator
   * @throws QueryException query exception
   */
  public Value value(final Expr expr) throws QueryException {
    checkStop();
    return expr.value(this);
  }

  /**
   * Returns the current data reference of the context value, or {@code null}.
   * @return data reference
   */
  public Data data() {
    return value != null ? value.data() : null;
  }

  /**
   * Creates a variable with a unique, non-clashing variable name.
   * @param ii input info
   * @param type type
   * @return variable
   */
  public Var uniqueVar(final InputInfo ii, final SeqType type) {
    return Var.create(this, ii, new QNm(token(varIDs)), type);
  }

  /**
   * Adds some optimization info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void compInfo(final String string, final Object... ext) {
    if(!inf) return;
    if(!firstOpt) info.add(QUERYSEP);
    firstOpt = false;
    info.addExt(string, ext).add(NL);
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   */
  public void evalInfo(final byte[] string) {
    if(!inf) return;
    if(firstEval) info.add(NL).add(QUERYEVAL).add(NL);
    info.add(QUERYSEP).add(string).add(NL);
    firstEval = false;
  }

  /**
   * Returns info on query compilation and evaluation.
   * @return query info
   */
  public String info() {
    return info.toString();
  }

  /**
   * Returns JDBC connections.
   * @return jdbc connections
   */
  public JDBCConnections jdbc() {
    if(jdbc == null) jdbc = new JDBCConnections();
    return jdbc;
  }

  /**
   * Returns the serialization properties.
   * @param opt return {@code null} reference if no properties are specified
   * @return serialization properties
   * @throws SerializerException serializer exception
   */
  public SerializerProp serProp(final boolean opt) throws SerializerException {
    // if available, use local query properties
    if(serProp != null) return serProp;

    final String serial = context.prop.get(Prop.SERIALIZER);
    if(opt && serial.isEmpty()) return null;

    // otherwise, apply global serialization option
    return new SerializerProp(serial);
  }

  @Override
  public String tit() {
    return QUERYEVAL;
  }

  @Override
  public String det() {
    return QUERYEVAL;
  }

  @Override
  public double prog() {
    return 0;
  }
}

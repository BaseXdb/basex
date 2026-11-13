package org.basex.query;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.util.parse.*;
import org.basex.query.var.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract parser for XQuery expressions. It contains everything that is shared by the
 * parser {@link QueryParser}, which is written by hand and processes the query string, and the
 * parser {@link RExQueryParser}, which is driven by a syntax tree.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class AQueryParser extends InputParser {
  /** Pattern for detecting library modules. */
  static final Pattern LIBMOD_PATTERN = Pattern.compile(
      "(xquery( version ?['\"].*?['\"])?( ?encoding ?['\"].*?['\"])? ?; ?)?module ");

  /** Reserved keywords. */
  static final TokenSet KEYWORDS = new TokenSet(
      ATTRIBUTE, COMMENT, DOCUMENT_NODE, ELEMENT, JNODE, NAMESPACE_NODE, NODE, XNODE,
      SCHEMA_ATTRIBUTE, SCHEMA_ELEMENT, PROCESSING_INSTRUCTION, TEXT, ARRAY, ENUM, FN,
      FUNCTION, IF, ITEM, MAP, RECORD, SWITCH, TYPE, TYPESWITCH);

  /** Query context. */
  public final QueryContext qc;

  /** Static context. */
  public final StaticContext sc;

  /** Parsed functions. */
  final ArrayList<StaticFunc> funcs = new ArrayList<>();

  /** List of modules to be parsed. */
  final ArrayList<ModInfo> modules = new ArrayList<>();

  /** References to named types (resolved after all type declarations have been parsed). */
  final QNmMap<TypeRef> typeRefs = new QNmMap<>();
  /** Identity of the record declarations of the parsed module. */
  Object recordIdentity = new Object();
  /** Declarations of derived record types (declared after the module has been parsed). */
  final QNmMap<DerivedRecord> extensions = new QNmMap<>();

  /** Local variable. */
  final LocalVars localVars = new LocalVars(this);

  /** Current XQDoc string. */
  final TokenBuilder docBuilder = new TokenBuilder();

  /** Start of the last skipped whitespace without XQDoc comments. */
  private int wsStart = -1;

  /** End of the last skipped whitespace without XQDoc comments. */
  private int wsEnd;

  /** Function or variable declaration that is currently parsed (can be {@code null}). */
  String declaration;

  /** XQDoc string of module. */
  String moduleDoc = "";

  /** Error hint for a keyword that was parsed as a name (can be {@code null}). */
  Hint hint;

  /**
   * Error hint for a keyword that was parsed as a name.
   * @param error error
   * @param pos query position after the keyword
   * @param ext error arguments
   */
  record Hint(QueryError error, int pos, Object... ext) { }

  /**
   * Type constructor data.
   * @param sc static context
   * @param name type name
   * @param seqType declared sequence type
   * @param anns annotations
   * @param doc xqdoc string
   * @param info input info
   * @param funcs functions of the declaring module
   */
  record TypeCnstr(StaticContext sc, QNm name, SeqType seqType, AnnList anns, String doc,
      InputInfo info, ArrayList<StaticFunc> funcs) { }

  /**
   * Constructor.
   * @param query query string
   * @param uri base URI (can be {@code null}; only passed on if not bound to static context yet)
   * @param qctx query context
   * @param sctx static context (can be {@code null})
   */
  AQueryParser(final String query, final String uri, final QueryContext qctx,
      final StaticContext sctx) {

    super(query);
    qc = qctx;
    sc = sctx != null ? sctx : new StaticContext(qctx);
    if(uri != null) sc.baseURI(uri);
    Counts.add(this);
  }

  /**
   * Counts the instantiations of the parsers. If the system property {@code queryparser.counts}
   * is set, the counts are written to the standard error stream when the JVM exits.
   */
  private static final class Counts {
    /** Names of the parsers. */
    private static final String[] NAMES = { "manual", "rex" };
    /** Counters. */
    private static final java.util.concurrent.atomic.AtomicIntegerArray COUNTS =
        new java.util.concurrent.atomic.AtomicIntegerArray(NAMES.length);

    static {
      if(System.getProperty("queryparser.counts") != null) {
        Runtime.getRuntime().addShutdownHook(new Thread(Counts::log));
      }
    }

    /**
     * Counts an instantiation.
     * @param parser parser
     */
    static void add(final AQueryParser parser) {
      COUNTS.incrementAndGet(parser instanceof RExQueryParser ? 1 : 0);
    }

    /** Writes the counts as a Markdown table. */
    private static void log() {
      final String head = "Instantiation Count";
      int width = "Parser".length();
      for(final String name : NAMES) width = Math.max(width, name.length());
      final StringBuilder sb = new StringBuilder(
          String.format("%" + width + "s | %s |%n", "Parser", head));
      sb.append("-".repeat(width)).append(":|").append("-".repeat(head.length() + 2)).append('|').
        append(System.lineSeparator());
      for(int n = 0; n < NAMES.length; n++) {
        sb.append(String.format("%" + width + "s | %-" + head.length() + "d |%n", NAMES[n],
            COUNTS.get(n)));
      }
      System.err.println();
      System.err.print(sb);
    }
  }

  /**
   * Creates a query parser.
   * @param type query parser type
   * @param query query string
   * @param uri base URI (can be {@code null}; only passed on if not bound to static context yet)
   * @param qctx query context
   * @param sctx static context (can be {@code null})
   * @return parser
   */
  static AQueryParser get(final MainOptions.QueryParserType type, final String query,
      final String uri, final QueryContext qctx, final StaticContext sctx) {
    return switch(type) {
      case REX -> new RExQueryParser(query, uri, qctx, sctx);
      default -> new QueryParser(query, uri, qctx, sctx);
    };
  }

  /**
   * Parses the "MainModule" rule.
   * @return module
   * @throws QueryException query exception
   */
  abstract MainModule parseMain() throws QueryException;

  /**
   * Parses the "LibraryModule" rule.
   * @param root indicates if this library is or is not imported by another module
   * @return module
   * @throws QueryException query exception
   */
  abstract LibraryModule parseLibrary(boolean root) throws QueryException;

  /**
   * Parses a sequence type.
   * @return sequence type
   * @throws QueryException query exception
   */
  abstract SeqType parseSeqType() throws QueryException;

  /**
   * Parses a sequence type.
   * @param type type string
   * @param qctx query context
   * @param sctx static context (can be {@code null})
   * @return sequence type
   * @throws QueryException query exception
   */
  static SeqType parseSeqType(final String type, final QueryContext qctx,
      final StaticContext sctx) throws QueryException {
    return get(qctx.queryParser(), type, null, qctx, sctx).parseSeqType();
  }

  /**
   * Initializes the parsing process.
   * @throws QueryException query exception
   */
  final void init() throws QueryException {
    final IO baseIO = sc.baseIO();
    path = baseIO == null ? null : baseIO.path();
    if(!more()) throw error(QUERYEMPTY);

    // checks if the query string contains invalid characters
    for(int i = 0; i < length; i++) {
      final int cp = input[i];
      if(!XMLToken.valid10(cp)) {
        pos = i;
        throw error(MODLEINV_X, currentAsString());
      }
    }
  }

  /**
   * Resolves references to the named types that are visible in the current module, and defers
   * the remaining references until all modules have been parsed.
   */
  final void resolveTypeRefs() {
    for(final QNm nm : typeRefs) {
      final TypeRef ref = typeRefs.get(nm);
      final Type type = sc.type(nm);
      if(type != null) ref.resolve(type);
      else qc.deferredTypeRefs.add(ref);
    }
  }

  /**
   * Checks function calls, variable references and updating semantics.
   * @param main main module; {@code null} for library modules
   * @throws QueryException query exception
   */
  final void check(final MainModule main) throws QueryException {
    // declare derived record types whose base types were declared later (cyclic imports)
    if(!qc.extensions.isEmpty()) {
      final QNmMap<DerivedRecord> exts = new QNmMap<>();
      for(final DerivedRecord dr : qc.extensions) exts.put(dr.decl.name, dr);
      declareExtensions(exts, false);
      qc.extensions.clear();
    }
    // reject declarations that clash with imported types
    for(final TypeDecl td : qc.typeDecls) {
      final TypeDecl pub = qc.namedTypes.get(td.name);
      if(pub != null && pub != td && td.info.sc().imports.contains(td.name.uri())) {
        throw DUPLTYPE_X.get(td.info, td.name.string());
      }
    }
    // reject item type declarations across modules that depend on themselves
    checkTypes(qc.namedTypes.values());
    // resolve deferred (cross-module) type references
    resolveDeferredTypeRefs();
    // check referenced map key types and cast targets
    for(final DeferredRef dr : qc.deferredMapKeys) {
      if(!dr.ref.instanceOf(BasicType.ANY_ATOMIC_TYPE)) throw MAPKEYATOMIC_X.get(dr.info, dr.ref);
    }
    qc.deferredMapKeys.clear();
    for(final DeferredRef dr : qc.deferredCastTargets) checkCastTarget(dr.ref, false, dr.info);
    qc.deferredCastTargets.clear();
    // declare constructor functions for named item types
    for(final TypeCnstr tc : qc.typeCnstrs) {
      if(cnstrType(tc.seqType().type)) declareTypeCnstr(tc);
    }
    qc.typeCnstrs.clear();
    // resolve function calls
    qc.functions.resolve();
    // resolve variable references
    qc.vars.resolve();
    // check memoized functions
    qc.functions.checkMemo();

    if(qc.contextValue != null && qc.contextValue.expr.has(Flag.UPD)) throw error(UPCTX);

    if(qc.updating) {
      // check updating semantics if updating expressions exist
      qc.functions.checkUp();
      qc.vars.checkUp();
      if(main != null) main.expr.checkUp();
      // check if main expression is updating
      qc.updating = main != null && main.expr.has(Flag.UPD);
    }
  }

  /**
   * Checks if a named item type has a constructor function.
   * @param type declared type
   * @return result of check
   */
  private static boolean cnstrType(final Type type) {
    final Type tp = TypeRef.deref(type);
    return tp.instanceOf(BasicType.ANY_ATOMIC_TYPE) &&
        !tp.oneOf(BasicType.ANY_ATOMIC_TYPE, BasicType.NOTATION);
  }

  /**
   * Declares a constructor function for a named item type.
   * @param tc constructor function information
   * @throws QueryException query exception
   */
  private void declareTypeCnstr(final TypeCnstr tc) throws QueryException {
    final InputInfo ii = tc.info();
    localVars.pushContext(false);
    final Params params = new Params();
    // generalized atomic type: cast the supplied argument to the declared type
    final SeqType st = tc.seqType().type.seqType(Occ.ZERO_OR_ONE);
    params.add(new QNm(VALUEE), Types.ANY_ATOMIC_TYPE_ZO, new ContextValue(ii), ii);
    params.seqType(st).finish(qc, localVars);
    final Expr expr = new Cast(ii, new VarRef(null, params.vars()[0]), st);
    final VarScope vs = localVars.popContext();
    final StaticFunc func = qc.functions.declare(tc.sc(), tc.name(), params, expr, tc.anns(),
        tc.doc(), vs, ii);
    tc.funcs().add(func);
  }

  /**
   * Declares a record constructor function for the specified record type.
   * @param rt record type
   * @param doc documentation
   * @param ii input info
   * @throws QueryException query exception
   */
  final void declareShapeConstructor(final RecordType rt, final String doc, final InputInfo ii)
      throws QueryException {

    // fixed fields are no parameters; fields without initializer come first
    final TokenObjectMap<ShapeField> fields = rt.fields();
    final int fs = fields.size();
    final int[] indexes = new int[fs + 1];
    localVars.pushContext(false);
    final Params params = new Params();
    for(final boolean defaults : new boolean[] { false, true }) {
      for(int f = 1; f <= fs; f++) {
        final ShapeField rf = fields.value(f);
        if(!rf.isFixed() && (rf.init() != null) == defaults) {
          indexes[f] = params.size();
          params.add(new QNm(fields.key(f)), rf.seqType(), rf.init(), ii);
        }
      }
    }
    params.seqType(rt.seqType()).finish(qc, localVars);

    // fixed fields are assigned the values of their initializing expressions
    final Var[] pv = params.vars();
    final Expr[] args = new Expr[fs];
    for(int f = 1; f <= fs; f++) {
      final ShapeField rf = fields.value(f);
      args[f - 1] = rf.isFixed() ? rf.init() : new VarRef(null, pv[indexes[f]]);
    }
    final Expr expr = ShapeConstructor.get(ii, rt, args);
    final VarScope vs = localVars.popContext();

    final StaticFunc func = qc.functions.declare(sc, rt.name(), params, expr, rt.anns(), doc, vs,
        ii);
    funcs.add(func);
  }

  /**
   * Imports all modules parsed in the prolog.
   * @throws QueryException query exception
   */
  final void importModules() throws QueryException {
    for(final ModInfo mi : modules) importModule(mi);
  }

  /**
   * Imports a single module.
   * @param mi module import
   * @throws QueryException query exception
   */
  private void importModule(final ModInfo mi) throws QueryException {
    final byte[] uri = mi.uri;
    if(mi.paths.isEmpty()) {
      // no paths specified: skip statically available modules; try to resolve module URI
      if(Functions.staticURI(uri) || qc.resources.modules().addImport(string(uri), this, mi.info))
        return;
      // module not found
      throw error(WHICHMOD_X, mi.info, uri);
    }
    // parse supplied paths
    for(final byte[] pth : mi.paths) {
      // modules pre-declared by the caller require no permissions
      if(!mi.declared) checkExternal(string(pth), mi.info);
      module(string(pth), string(uri), mi.info);
    }
  }

  /**
   * Parses the specified module, checking function and variable references at the end.
   * @param pth input path
   * @param uri base URI of module
   * @param info input info
   * @throws QueryException query exception
   */
  public final void module(final String pth, final String uri, final InputInfo info)
      throws QueryException {

    final IO io = sc.location(pth, uri);
    if(io == null) throw error(WHICHMODFILE_X, info, pth);
    final byte[] tPath = token(qc.modulePath(io));

    // check if module has already been parsed
    final byte[] tUri = token(uri), pUri = qc.modParsed.get(tPath);
    if(pUri != null) {
      if(!eq(tUri, pUri)) throw error(WRONGMODULE_X_X_X, info, io.name(), uri, pUri);
    }
    else {
      qc.modParsed.put(tPath, tUri);

      // read module
      final String query;
      try {
        query = io.readString();
      } catch(final IOException ex) {
        throw error(WHICHMODFILE_X, info, io).cause(ex);
      }

      final AQueryParser qp = get(qc.queryParser(), query, io.path(), qc, null);

      // check if import and declaration URI match
      final LibraryModule lib = qp.parseLibrary(false);
      qc.libs.put(tPath, lib);
      final byte[] muri = lib.sc.module.uri();
      if(!uri.equals(string(muri))) throw error(WRONGMODULE_X_X_X, info, io.name(), uri, muri);
    }
  }

  /**
   * Rejects item type declarations that depend on themselves.
   * @param tds type declarations
   * @throws QueryException query exception
   */
  static void checkTypes(final Iterable<TypeDecl> tds) throws QueryException {
    final Cycles<TypeDecl> cycles = new Cycles<>() {
      @Override
      protected Iterable<TypeDecl> next(final TypeDecl td) {
        final ArrayList<TypeDecl> list = new ArrayList<>();
        for(final QNm ref : td.refs) {
          final TypeDecl target = td.info.sc().typeDecl(ref);
          if(target != null) list.add(target);
        }
        return list;
      }

      @Override
      protected QueryException error(final TypeDecl td) {
        return CIRCTYPE_X.get(td.info, td.name.string());
      }
    };
    for(final TypeDecl td : tds) cycles.visit(td);
  }

  /**
   * Returns a declared or imported type.
   * @param name type name
   * @return type, or {@code null} if it is unknown
   */
  SeqType declaredType(final QNm name) {
    final TypeDecl td = sc.typeDecl(name);
    return td != null ? td.seqType : null;
  }

  /**
   * Resolves references that could not be resolved within their module, against the public types
   * of the imported modules.
   * @throws QueryException query exception
   */
  void resolveDeferredTypeRefs() throws QueryException {
    for(final TypeRef ref : qc.deferredTypeRefs) {
      if(ref.resolved()) continue;
      final Type type = ref.info().sc().type(ref.name());
      if(type == null) throw TYPEUNKNOWN_X.get(ref.info(), BasicType.similar(ref.name()));
      ref.resolve(type);
    }
    qc.deferredTypeRefs.clear();
  }

  /**
   * Checks if the current user is allowed to access external resources.
   * @param location location of the resource
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public void checkExternal(final String location, final InputInfo info) throws QueryException {
    if(!sc.maxPerm.has(Perm.ADMIN)) throw error(EXTERNALRESOURCE_X, info, location);
    if(!qc.parseResources && !qc.user.has(Perm.ADMIN)) {
      throw error(BASEX_PERMISSION_X_X, info, Perm.ADMIN, location);
    }
  }

  /**
   * Checks if the specified name equals a reserved keyword.
   * @param name name
   * @param p position of the first character of the name
   * @return result of check
   */
  boolean reserved(final QNm name, final int p) {
    if(p + 1 < input.length && input[p] == 'Q' && input[p + 1] == '{') return false;
    return !name.hasPrefix() && KEYWORDS.contains(name.string());
  }

  /**
   * Checks the name of a type declaration.
   * @param qn type name
   * @return type name
   * @throws QueryException query exception
   */
  final QNm checkTypeName(final QNm qn) throws QueryException {
    if(sc.types.contains(qn) || extensions.contains(qn)) {
      throw error(DUPLTYPE_X, qn.string());
    }
    if(NSGlobal.reserved(qn.uri())) throw error(TYPERESERVED_X, qn.string());
    return qn;
  }

  /**
   * Declares derived record types, after their base record types.
   * @param exts derived record types
   * @param defer defer declarations whose base types are unknown
   * @throws QueryException query exception
   */
  static void declareExtensions(final QNmMap<DerivedRecord> exts, final boolean defer)
      throws QueryException {
    if(exts.isEmpty()) return;
    final Cycles<DerivedRecord> cycles = new Cycles<>() {
      @Override
      protected Iterable<DerivedRecord> next(final DerivedRecord ext) {
        final DerivedRecord base = exts.get(ext.decl.base);
        return base != null ? Collections.singletonList(base) : Collections.emptyList();
      }
      @Override
      protected void finish(final DerivedRecord ext) throws QueryException {
        ext.parser.declareExtension(ext, defer);
      }
      @Override
      protected QueryException error(final DerivedRecord ext) {
        return RECORDBASE_X.get(ext.decl.info, ext.decl.base.prefixString());
      }
    };
    for(final DerivedRecord ext : exts.values()) cycles.visit(ext);
  }

  /**
   * Declares a derived record type.
   * @param dr derived record type
   * @param defer defer the declaration if the base type is unknown
   * @throws QueryException query exception
   */
  private void declareExtension(final DerivedRecord dr, final boolean defer)
      throws QueryException {
    final RecordDecl rd = dr.decl;
    // references in item type declarations are resolved later
    Type tp = sc.type(rd.base);
    for(final QNmSet names = new QNmSet(); tp instanceof final TypeRef ref && !ref.resolved() &&
        names.add(ref.name());) tp = sc.type(ref.name());
    // the base type may be declared by a module that is still being parsed (cyclic imports)
    if(defer && (tp == null || tp instanceof final TypeRef ref && !ref.resolved())) {
      qc.extensions.add(dr);
      return;
    }
    tp = TypeRef.deref(tp);
    if(!(tp instanceof final RecordType ext) || ext.any()) {
      throw RECORDBASE_X.get(rd.info, rd.base.prefixString());
    }
    // inherited fields come first; redeclared fixed fields keep their position
    final TokenObjectMap<ShapeField> fields = new TokenObjectMap<>(), efields = ext.fields();
    for(final byte[] key : efields) fields.put(key, efields.get(key));
    for(final byte[] key : rd.fields) {
      ShapeField field = rd.fields.get(key);
      final ShapeField ef = fields.get(key);
      if(ef != null) {
        if(!ef.isFixed() || !field.isFixed()) throw DUPFIELD_X.get(rd.info, key);
        final SeqType fst = field.seqType(), est = ef.seqType();
        if(rd.untyped.contains(key)) field = field.with(est);
        else if(!fst.instanceOf(est)) throw RECORDFIELD_X_X_X.get(rd.info, key, est, fst);
      }
      fields.put(key, field);
    }
    final RecordType rt = declareRecord(rd, fields, ext);
    // references of a deferred declaration were not resolved within the module
    final TypeRef ref = typeRefs.get(rd.name);
    if(ref != null) ref.resolve(rt);
  }

  /**
   * Returns the shared reference to a named type.
   * @param name type name
   * @return type reference
   */
  final TypeRef typeRef(final QNm name) {
    return typeRef(name, info());
  }

  /**
   * Returns the shared reference to a named type.
   * @param name type name
   * @param ii input info of the first reference
   * @return type reference
   */
  final TypeRef typeRef(final QNm name, final InputInfo ii) {
    return typeRefs.computeIfAbsent(name, () -> new TypeRef(name, ii));
  }

  /**
   * Declares a labeled record type and its constructor function.
   * @param rd record declaration
   * @param fields all fields, including inherited ones
   * @param base base record type (can be {@code null})
   * @return record type
   * @throws QueryException query exception
   */
  final RecordType declareRecord(final RecordDecl rd, final TokenObjectMap<ShapeField> fields,
      final RecordType base) throws QueryException {
    // records of different declarations are different types
    final RecordType rt = new RecordType(fields, rd.name, rd.anns, recordIdentity, rd.constraint,
        base);
    // record types may be recursive: references are not followed
    declareType(rd.name, rt.seqType(), new QNmSet(), rd.anns, rd.info);
    declareShapeConstructor(rt, rd.doc, rd.info);
    return rt;
  }

  /**
   * Declaration of a labeled record type.
   * @param name record name
   * @param base name of the base type (can be {@code null})
   * @param fields declared fields
   * @param untyped names of the declared fields without type
   * @param anns annotations
   * @param constraint integrity constraint (can be {@code null})
   * @param doc documentation
   * @param info input info
   */
  record RecordDecl(QNm name, QNm base, TokenObjectMap<ShapeField> fields, TokenSet untyped,
      AnnList anns, Expr constraint, String doc, InputInfo info) { }

  /**
   * Declaration of a derived record type.
   * @param parser parser of the declaring module
   * @param decl record declaration
   */
  record DerivedRecord(AQueryParser parser, RecordDecl decl) { }

  /**
   * Type reference that is checked after all modules have been parsed.
   * @param ref type reference
   * @param info input info of the reference
   */
  record DeferredRef(TypeRef ref, InputInfo info) { }

  /**
   * Declares a named item type.
   * @param qn type name
   * @param st declared type
   * @param refs names of the referenced types
   * @param anns annotations
   * @param ii input info
   * @throws QueryException query exception
   */
  void declareType(final QNm qn, final SeqType st, final QNmSet refs, final AnnList anns,
      final InputInfo ii) throws QueryException {
    final TypeDecl td = new TypeDecl(qn, st, refs, anns, ii);
    qc.typeDecls.add(td);
    if(!anns.contains(Annotation.PRIVATE)) {
      if(sc.module != null) {
        if(!qn.eqUri(sc.module)) throw MODULENS_X.get(ii, qn.prefixString());
        // modules with the same URI must not declare the same type
        if(qc.namedTypes.contains(qn)) throw DUPLTYPE_X.get(ii, qn.string());
        qc.namedTypes.put(qn, td);
      }
    }
    sc.types.put(qn, td);
  }

  /**
   * Iteration over multiple arrays/maps.
   * @param func function to apply
   * @param expr expression
   * @param ii input info
   * @return map expression
   */
  static Expr forEach(final Function func, final Expr expr, final InputInfo ii) {
    return new CachedMap(ii, expr, func.get(ii, new ContextValue(ii)));
  }

  /**
   * Checks if a union operator is found.
   * @return result of check
   * @throws QueryException query exception
   */
  boolean isUnion() throws QueryException {
    if(wsConsumeWs(UNION)) return true;
    final int p = pos;
    if(consume("|") && !consume("|")) return true;
    pos = p;
    return false;
  }

  /**
   * Wraps every non-{@link Expr#navigational() navigational} path step but the leading one in a
   * {@link DynamicStep} for JNode navigation.
   * @param root root expression (can be {@code null})
   * @param steps path steps
   * @return rewritten steps
   */
  Expr[] dynamicSteps(final Expr root, final Expr[] steps) {
    for(int s = 0; s < steps.length; s++) {
      final Expr step = steps[s];
      if((root != null || s > 0) && !step.navigational()) {
        steps[s] = new DynamicStep(step.info(info()), step);
      }
    }
    return steps;
  }

  // methods for query suggestions

  /**
   * Performs an optional check init.
   */
  void checkInit() { }

  /**
   * Performs an optional axis check.
   * @param axis axis
   */
  @SuppressWarnings("unused")
  void checkAxis(final Axis axis) { }

  /**
   * Performs an optional test check.
   * @param ei expression info
   * @param element element flag
   */
  @SuppressWarnings("unused")
  void checkTest(final ExprInfo ei, final boolean element) { }

  /**
   * Checks a predicate.
   * @param open open flag
   */
  @SuppressWarnings("unused")
  void checkPred(final boolean open) { }

  /**
   * Finalizes a QName by assigning its namespace URI.
   * @param name QName to be resolved
   * @param elemNS default element namespace or {@code null}
   * @param info input info
   * @throws QueryException query exception
   */
  void resolveQNm(final QNm name, final byte[] elemNS, final InputInfo info)
      throws QueryException {
    if(name.hasPrefix()) {
      name.uri(qc.ns.resolve(name.prefix(), sc));
      if(!name.hasURI()) throw error(NOURI_X, info, undeclared(name.prefix()));
    } else if(elemNS != null) {
      name.uri(elemNS);
    }
  }

  /**
   * Returns an undeclared prefix, followed by a hint if the module with this prefix was removed.
   * @param prefix prefix
   * @return prefix and hint
   */
  static String undeclared(final byte[] prefix) {
    final String pref = string(prefix), removed = Removed.prefix(pref);
    return removed != null ? Util.info("% (%)", pref, removed) : pref;
  }

  /**
   * Skips a string literal, including its quotes.
   * @param quote quote character
   * @throws QueryException query exception
   */
  void skipStringLiteral(final int quote) throws QueryException {
    consume(); // opening quote
    while(true) {
      if(!more()) throw error(NOQUOTE_X, found());
      if(consume(quote)) {
        if(!consume(quote)) break;
      } else {
        consume();
      }
    }
  }

  /**
   * Skips a comment, including nested comments.
   * @throws QueryException query exception
   */
  void skipComment() throws QueryException {
    consume();
    consume(); // '(:'
    for(int depth = 1; depth > 0;) {
      if(!more()) throw error(COMCLOSE);
      if(consume("(:")) ++depth;
      else if(consume(":)")) --depth;
      else consume();
    }
  }

  /**
   * Skips a pragma.
   * @return result of check
   * @throws QueryException query exception
   */
  boolean skipPragma() throws QueryException {
    final int p = pos;
    pos += 2; // '(#'
    // a pragma is followed by whitespace: '(#name' is a parenthesized QName literal
    if(!consumeWS()) {
      pos = p;
      return false;
    }
    while(!consume("#)")) {
      if(!more()) throw error(PRAGMAINV);
      consume();
    }
    return true;
  }

  /**
   * Skips all characters up to and including the specified terminator.
   * @param end terminator string
   * @throws QueryException query exception
   */
  void skipUntil(final String end) throws QueryException {
    while(!consume(end)) {
      if(!more()) throw error(WRONGCHAR_X_X, end, found());
      consume();
    }
  }

  /**
   * Checks if the specified character ends a value, so that a following {@code <} is a comparison
   * operator rather than the start of a direct constructor.
   * @param cp character
   * @return result of check
   */
  static boolean valueChar(final int cp) {
    return cp == ')' || cp == ']' || cp == '}' || cp == '*' || cp == '"' || cp == '\'' ||
        cp == '`' || XMLToken.isNCChar(cp);
  }

  /**
   * Returns a string item.
   * @param tb token builder
   * @param strip strip flag
   * @return string item or {@code null}
   */
  Str text(final TokenBuilder tb, final boolean strip) {
    final byte[] text = tb.toArray();
    return text.length == 0 || strip && !sc.spaces && ws(text) ? null : Str.get(text);
  }

  /**
   * Checks if a type is eligible as cast target (unresolved references are skipped).
   * @param type type to check
   * @param atomic type must be a generalized atomic type
   * @param ii input info
   * @throws QueryException query exception
   */
  static void checkCastTarget(final Type type, final boolean atomic, final InputInfo ii)
      throws QueryException {
    if(TypeRef.unresolved(type)) return;
    final Type tp = TypeRef.deref(type);
    // choice item type: all alternatives must be generalized atomic types
    if(tp instanceof final ChoiceItemType cit) {
      for(final Type alt : cit.types) checkCastTarget(alt, true, ii);
      return;
    }
    // generalized atomic type, enumeration type
    if(tp instanceof EnumType) return;
    if(tp instanceof final BasicType bt && bt.atomic() != null &&
        !bt.oneOf(BasicType.NOTATION, BasicType.ANY_ATOMIC_TYPE, BasicType.ANY_SIMPLE_TYPE))
      return;
    // schema list type; array, map, record types (components are checked while casting)
    if(!atomic && (tp instanceof ListType || tp instanceof ArrayType || tp instanceof MapType))
      return;
    throw INVALIDCAST_X.get(ii, type);
  }

  /**
   * Consumes an update keyword followed by 'node' or 'nodes'.
   * @param keyword keyword
   * @return result of check
   * @throws QueryException query exception
   */
  boolean updateKeyword(final String keyword) throws QueryException {
    final int p = pos;
    if(!wsConsumeWs(keyword)) return false;
    if(wsConsumeWs(NODE) || wsConsumeWs(NODES)) return true;
    addHint(EXPECTAFTER_X_X, "'node'", keyword);
    pos = p;
    return false;
  }

  /**
   * Parses and converts entities.
   * @param tb token builder
   * @return true if an entity was found
   * @throws QueryException query exception
   */
  boolean entity(final TokenBuilder tb) throws QueryException {
    final int p = pos;
    final boolean entity = consume('&');
    if(entity) {
      if(consume('#')) {
        final int b = consume('x') ? 0x10 : 10;
        boolean ok = true;
        int n = 0;
        do {
          final int cp = current();
          final boolean m = digit(cp);
          final boolean h = b == 0x10 && (cp >= 'a' && cp <= 'f' || cp >= 'A' && cp <= 'F');
          if(!m && !h) entityError(p, INVENTITY_X);
          final long nn = n;
          n = n * b + (consume() & 0xF);
          if(n < nn) ok = false;
          if(!m) n += 9;
        } while(!consume(';'));
        if(!ok || !XMLToken.valid11(n)) entityError(p, INVCHARREF_X);
        tb.add(n);
      } else {
        if(consume("lt")) {
          tb.add('<');
        } else if(consume("gt")) {
          tb.add('>');
        } else if(consume("amp")) {
          tb.add('&');
        } else if(consume("quot")) {
          tb.add('"');
        } else if(consume("apos")) {
          tb.add('\'');
        } else {
          entityError(p, INVENTITY_X);
        }
        if(!consume(';')) entityError(p, INVENTITY_X);
      }
    } else {
      tb.add(consume());
    }
    return entity;
  }

  /**
   * Raises an entity error.
   * @param start start position
   * @param code error code
   * @throws QueryException query exception
   */
  void entityError(final int start, final QueryError code) throws QueryException {
    final String sub = substring(start, Math.min(start + 20, length)).toString();
    final int semi = sub.indexOf(';');
    throw error(code, semi == -1 ? sub + DOTS : sub.substring(0, semi + 1));
  }

  /**
   * Skips whitespace, raises an error if the specified string cannot be consumed.
   * @param string expected string
   * @throws QueryException query exception
   */
  void wsCheck(final String string) throws QueryException {
    if(!wsConsume(string)) throw error(WRONGCHAR_X_X, string, found());
  }

  /**
   * Consumes the specified string and surrounding whitespace.
   * @param string string to consume (words must not be followed by letters)
   * @return true if token was found
   * @throws QueryException query exception
   */
  boolean wsConsumeWs(final String string) throws QueryException {
    final int p = pos;
    if(wsConsume(string)) {
      if(skipWs() || !XMLToken.isNCChar(string.charAt(string.length() - 1)) ||
          !XMLToken.isNCChar(current()))
        return true;
      pos = p;
    }
    return false;
  }

  /**
   * Consumes the specified two strings or jumps back to the old query position. If the strings are
   * found, the cursor is placed after the first token.
   * @param string string to consume (words must not be followed by letters)
   * @param suggest register an error hint if the subsequent strings are not found
   * @param strings subsequent strings
   * @return result of check
   * @throws QueryException query exception
   */
  boolean wsConsumeWs(final String string, final boolean suggest, final String... strings)
      throws QueryException {

    final int p1 = pos;
    if(!wsConsumeWs(string)) return false;
    final int p2 = pos;
    for(final String s : strings) {
      if(wsConsume(s)) {
        pos = p2;
        hint = null;
        return true;
      }
    }
    if(suggest) {
      pos = p2;
      addHint(EXPECTAFTER_X_X, "'" + strings[0] + "'", string);
    } else {
      hint = null;
    }
    pos = p1;
    return false;
  }

  /**
   * Skips whitespace, consumes the specified string and ignores trailing characters.
   * @param string string to consume
   * @return true if string was found
   * @throws QueryException query exception
   */
  boolean wsConsume(final String string) throws QueryException {
    skipWs();
    return consume(string);
  }

  /**
   * Consumes all whitespace characters from the remaining query.
   * @return true if whitespace was found
   * @throws QueryException query exception
   */
  boolean skipWs() throws QueryException {
    final int i = pos;
    // the same whitespace is skipped again whenever the parser backtracks
    if(i == wsStart) {
      pos = wsEnd;
      return i != pos;
    }
    boolean xqdoc = false;
    while(more()) {
      final int cp = current();
      if(cp == '(' && next() == ':') {
        xqdoc |= comment();
      } else {
        if(cp == 0 || cp > ' ') break;
        ++pos;
      }
    }
    // XQDoc comments are scanned again: they update the documentation string
    if(!xqdoc) {
      wsStart = i;
      wsEnd = pos;
    }
    return i != pos;
  }

  /**
   * Consumes a comment.
   * @return {@code true} if the comment is an XQDoc comment
   * @throws QueryException query exception
   */
  boolean comment() throws QueryException {
    ++pos;
    final boolean xqdoc = next() == '~';
    if(xqdoc) {
      docBuilder.reset();
      ++pos;
    }
    comment(false, xqdoc);
    return xqdoc;
  }

  /**
   * Consumes a comment.
   * @param nested nested flag
   * @param xqdoc xqdoc flag
   * @throws QueryException query exception
   */
  void comment(final boolean nested, final boolean xqdoc) throws QueryException {
    while(++pos < length) {
      int curr = current();
      if(curr == '(' && next() == ':') {
        ++pos;
        comment(true, xqdoc);
        curr = current();
      }
      if(curr == ':' && next() == ')') {
        pos += 2;
        if(!nested && moduleDoc.isEmpty() && !documented()) {
          moduleDoc = docBuilder.toString().trim();
          docBuilder.reset();
        }
        return;
      }
      if(xqdoc) docBuilder.add(curr);
    }
    throw error(COMCLOSE);
  }

  /**
   * Checks if a declaration follows that adopts the documentation of a comment.
   * @return result of check
   */
  boolean documented() {
    final int p = pos;
    consumeWS();
    boolean found = consume(DECLARE);
    if(found) {
      consumeWS();
      found = current() == '%' || consume(FUNCTION) || consume(VARIABLE);
    }
    pos = p;
    return found;
  }

  /**
   * Consumes all following whitespace characters.
   * @return true if whitespace was found
   */
  boolean consumeWS() {
    final int i = pos;
    while(more()) {
      final int cp = current();
      if(cp == 0 || cp > ' ') return i != pos;
      ++pos;
    }
    return true;
  }

  /**
   * Returns the error of a registered hint, or the supplied error.
   * @param error query error (can be {@code null})
   * @return error
   */
  QueryException hintError(final QueryError error) {
    if(hint == null) return error(error);
    pos = hint.pos();
    return error(hint.error(), hint.ext());
  }

  /**
   * Registers a hint for a keyword that was parsed as a name if it is followed by an operand.
   * @param error error
   * @param ext error arguments
   */
  void addHint(final QueryError error, final Object... ext) {
    final int cp = current();
    final boolean operand = XMLToken.isNCStartChar(cp) || digit(cp) || quote(cp) || cp == '$' ||
      cp == '{';
    hint = operand ? new Hint(error, pos, ext) : null;
  }

  /**
   * Checks if the operand of the specified operator exists.
   * @param expr operand (can be {@code null})
   * @param op operator
   * @return operand
   * @throws QueryException query exception
   */
  Expr operand(final Expr expr, final String op) throws QueryException {
    if(expr == null) throw error(EXPECTAFTER_X_X, "expression", op);
    return expr;
  }

  /**
   * Adds an expression to the specified array.
   * @param ar input array
   * @param expr new expression (can be {@code null})
   * @throws QueryException query exception
   */
  void add(final ExprList ar, final Expr expr) throws QueryException {
    if(expr == null) throw error(INCOMPLETE);
    ar.add(expr);
  }

  /**
   * Creates the specified error.
   * @param error error to be thrown
   * @param arg error arguments
   * @return error
   */
  QueryException error(final QueryError error, final Object... arg) {
    return error(error, info(), arg);
  }

  /**
   * Creates the specified error.
   * @param error error to be thrown
   * @param info input info
   * @param arg error arguments
   * @return error
   */
  public QueryException error(final QueryError error, final InputInfo info, final Object... arg) {
    return error.get(info, arg);
  }

  /**
   * Checks if the specified XQuery string is a library module.
   * @param query query string
   * @return result of check
   */
  public static boolean isLibrary(final String query) {
    return LIBMOD_PATTERN.matcher(removeComments(query, 80)).lookingAt();
  }

  /**
   * Removes comments from the specified string and returns the first characters of a query.
   * @param query query string
   * @param max maximum length of string to return
   * @return result
   */
  public static String removeComments(final String query, final int max) {
    final StringBuilder sb = new StringBuilder();
    boolean s = false;
    final int ql = query.length();
    for(int m = 0, c = 0; c < ql && sb.length() < max; c++) {
      final char ch = query.charAt(c);
      if(ch == '(' && c + 1 < ql && query.charAt(c + 1) == ':') {
        if(m == 0 && !s) {
          sb.append(' ');
          s = true;
        }
        ++m;
        ++c;
      } else if(m != 0 && ch == ':' && c + 1 < ql && query.charAt(c + 1) == ')') {
        --m;
        ++c;
      } else if(m == 0) {
        if(ch > ' ') sb.append(ch);
        else if(!s) sb.append(' ');
        s = ch <= ' ';
      }
    }
    if(sb.length() >= max) sb.append(Text.DOTS);
    return sb.toString().trim();
  }

  @Override
  public final InputInfo info() {
    return new InputInfo(this, sc, declaration);
  }

}

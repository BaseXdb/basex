package org.basex.query.func.fn;

import static org.basex.query.QueryContext.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.func.fn.FnLoadXQueryModule.LoadXQueryModuleOptions.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class FnLoadXQueryModule extends StandardFunc {
  @Override
  public XQMap value(final QueryContext qc) throws QueryException {
    final byte[] modUri = toToken(arg(0), qc);
    if(modUri.length == 0) throw MODULE_URI_EMPTY.get(info);

    final LoadXQueryModuleOptions opt = toOptions(qc);
    final Value hints = opt.contains(LOCATION_HINTS) ? opt.get(LOCATION_HINTS) : Empty.VALUE;
    // untrusted modules are restricted to write permissions
    final boolean trusted = trusted(opt, TRUSTED, qc);
    final Perm perm = perm(qc).min(trusted ? Perm.ADMIN : Perm.WRITE);

    // check for cached result (callers must not bypass the location check)
    Map<String, XQMap> modCache = null;
    String cacheKey = null;
    if(hints.size() == 1 && hintsOnly(opt) && qc.has(Perm.ADMIN, null, info)) {
      QueryContext qcAnc = qc;
      while(qcAnc.parent != null) qcAnc = qcAnc.parent;
      modCache = qcAnc.caches.moduleCache().get();
      cacheKey = new TokenBuilder(modUri).add('#').add(toToken(hints.itemAt(0))).add('#').
          add(perm.toString()).toString();
      if(modCache.containsKey(cacheKey)) return modCache.get(cacheKey);
    }

    final List<IO> srcs = new ArrayList<>();
    final String cont = opt.get(CONTENT);
    final boolean inline = cont != null && !cont.isEmpty();
    if(inline) {
      srcs.add(new IOContent(cont));
    } else {
      final String uri = Token.string(modUri);
      final StringList locs = new StringList();
      for(final Item hint : hints) locs.add(toString(hint));
      // modules pre-declared by the caller or installed in the repository require no permissions
      final boolean provided = locs.isEmpty();
      if(provided) {
        final TokenList files = qc.modDeclared.get(modUri);
        if(files != null) {
          for(final byte[] file : files) locs.add(Token.string(file));
        } else {
          for(final IOFile file : qc.resources.modules().load(uri, info)) locs.add(file.path());
        }
      }
      if(locs.isEmpty()) throw MODULE_NOT_FOUND_X.get(info, modUri);
      for(final String loc : locs) {
        final IO src = info.sc().location(loc, uri, info);
        if(src == null) throw WHICHMODFILE_X.get(info, loc);
        if(src.isExternal() && !provided) checkPerm(qc, Perm.ADMIN);
        srcs.add(src);
      }
    }

    final QNmMap<Value> bindings = new QNmMap<>();
    if(opt.contains(VARIABLES)) {
      final XQMap vars = ((XQMap) opt.get(VARIABLES)).coerceTo(Types.QNAME_ITEM_SEQ_MAP, qc, info,
          null);
      vars.forEach((key, value) -> bindings.put((QNm) key, value));
    }

    if(opt.contains(VENDOR_OPTIONS)) {
      ((XQMap) opt.get(VENDOR_OPTIONS)).coerceTo(Types.QNAME_ITEM_SEQ_MAP, qc, info, null);
    }

    if(opt.contains(XQUERY_VERSION)) {
      String version = Token.string(((Item) opt.get(XQUERY_VERSION)).string(info));
      if(!version.contains(".")) version += ".0";
      if(!isSupported(version)) throw MODULE_XQUERY_VERSION_X.get(info, version);
    }

    final QueryContext mqc = new QueryContext(qc, null);
    mqc.maxPerm = perm;
    mqc.inlineModule = inline;
    for(final byte[] uri : qc.modDeclared) mqc.modDeclared.put(uri, qc.modDeclared.get(uri));
    int nParsed = 0;
    final Value ctx = contextValue(opt);
    if(ctx != null) {
      mqc.contextValue = new ContextScope(ctx, mqc.contextType, new VarScope(), sc(), null, null);
      mqc.finalContext = true;
    }
    for(final IO src : srcs) {
      // skip modules that were imported by a previously loaded module
      final byte[] parsed = mqc.modParsed.get(Token.token(mqc.modulePath(src)));
      if(parsed != null) {
        if(!Token.eq(modUri, parsed)) throw MODULE_FOUND_OTHER_X_X.get(info, modUri, src.path());
        continue;
      }
      final AModule lib;
      try {
        final String path = src.path(), content = src.readString();
        final String uri = path.isEmpty() ? Token.string(sc().baseURI().string()) : path;
        lib = mqc.parse(content, uri);
      } catch(final IOException ex) {
        throw WHICHMODFILE_X.get(info, src).cause(ex);
      } catch(final QueryException ex) {
        throw ex.error() == EXTERNALRESOURCE_X ? ex :
          MODULE_STATIC_ERROR_X_X.get(info, modUri, ex.getLocalizedMessage()).cause(ex);
      }
      try {
        mqc.vars.bindExternal(mqc, bindings, false);
      } catch(final QueryException ex) {
        throw ex.error() != INVTYPE_X ? ex : MODULE_PARAMETER_TYPE_X_X.get(info, modUri,
            ex.getLocalizedMessage()).cause(ex);
      }
      if(ctx != null && lib.sc.contextType != null && !lib.sc.contextType.instance(ctx)) {
        throw MODULE_CONTEXT_TYPE_X_X.get(info, modUri, ctx.seqType());
      }
      mqc.compile(true);
      if(mqc.main != null) throw MODULE_FOUND_MAIN_X.get(info, modUri);
      final Iterator<byte[]> it = mqc.modParsed.values().iterator();
      for(int i = 0; i < nParsed; i++) it.next();
      final byte[] uri = it.next();
      if(!Token.eq(modUri, uri)) {
        final String path = src.path();
        throw path.isEmpty() ? MODULE_FOUND_OTHER_X.get(info, modUri)
                             : MODULE_FOUND_OTHER_X_X.get(info, modUri, path);
      }
      nParsed = mqc.modParsed.size();
    }

    // code of the loaded module refers to its own global context value
    final QueryFocus global = mqc.globalFocus(null);
    for(final StaticFunc sf : mqc.functions) sf.sc.global = global;
    for(final StaticVar var : mqc.vars) var.sc.global = global;

    final QNmMap<Map<Integer, Expr>> funcs = new QNmMap<>();
    for(final StaticFunc sf : mqc.functions) {
      if(sf.updating()) mqc.updating();
      if(!sf.anns.contains(Annotation.PRIVATE) && Token.eq(sf.name.uri(), modUri)) {
        for(int a = sf.minArity(); a <= sf.arity(); a++) {
          final FuncBuilder fb = new FuncBuilder(sf.info, a, true);
          final Expr item = Functions.item(sf, fb, mqc);
          funcs.computeIfAbsent(sf.name, HashMap::new).put(a, item);
        }
      }
    }

    final MapBuilder functions = new MapBuilder();
    for(final QNm qnm : funcs) {
      final MapBuilder arities = new MapBuilder();
      for(final Map.Entry<Integer, Expr> entry : funcs.get(qnm).entrySet()) {
        arities.put(Itr.get(entry.getKey()), entry.getValue().value(mqc));
      }
      functions.put(qnm, arities.map());
    }

    final MapBuilder variables = new MapBuilder();
    for(final StaticVar var : mqc.vars) {
      if(!var.anns.contains(Annotation.PRIVATE) && Token.eq(var.name.uri(), modUri)) {
        variables.put(var.name, var.value(mqc));
      }
    }

    final XQMap map = XQMap.get(Records.LOAD_XQUERY_MODULE.get(),
        variables.map(), functions.map());
    if(modCache != null) modCache.put(cacheKey, map);
    return map;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    // locked resources cannot be detected statically
    return visitor.unknownLocks() && super.accept(visitor);
  }

  /**
   * Checks if no other option than the location hints was supplied.
   * @param opt options
   * @return result of check
   */
  private static boolean hintsOnly(final LoadXQueryModuleOptions opt) {
    for(final Option<?> option : opt) {
      if(option != LOCATION_HINTS && opt.contains(option)) return false;
    }
    return true;
  }

  /**
   * Returns the initial context value of the library module.
   * @param opt options
   * @return context value (can be {@code null})
   * @throws QueryException query exception
   */
  private Value contextValue(final LoadXQueryModuleOptions opt) throws QueryException {
    final Value value = opt.get(CONTEXT_VALUE), item = opt.get(CONTEXT_ITEM);
    if(value != null && item != null) throw MODULE_CONTEXT_OPTIONS.get(info);
    return value != null ? value : item;
  }

  /**
   * Options for fn:load-xquery-module, with values only for supplied options.
   */
  public static final class LoadXQueryModuleOptions extends Options {
    /** load-xquery-module option xquery-version. */
    public static final ValueOption XQUERY_VERSION = new ValueOption("xquery-version",
        Types.DECIMAL_O, null);
    /** load-xquery-module option location-hints. */
    public static final ValueOption LOCATION_HINTS =
        new ValueOption("location-hints", Types.STRING_ZM);
    /** load-xquery-module option content. */
    public static final StringOption CONTENT = new StringOption("content", null, Types.STRING_ZO);
    /** load-xquery-module option context-value. */
    public static final ValueOption CONTEXT_VALUE = new ValueOption("context-value", Types.ITEM_ZM);
    /** load-xquery-module option context-item. */
    public static final ValueOption CONTEXT_ITEM = new ValueOption("context-item", Types.ITEM_ZO,
        null);
    /** load-xquery-module option variable. */
    public static final ValueOption VARIABLES = new ValueOption("variables", Types.MAP_O, null);
    /** load-xquery-module option vendor-options. */
    public static final ValueOption VENDOR_OPTIONS = new ValueOption("vendor-options",
        Types.MAP_O, null);
    /** load-xquery-module option trusted. */
    public static final BooleanOption TRUSTED = new BooleanOption(CommonOptions.TRUSTED, false);

    /**
     * Constructor.
     */
    public LoadXQueryModuleOptions() {
      super(false);
    }
  }
}

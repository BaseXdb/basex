package org.basex.query.func.inspect;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class contains functions for generating a xqDoc documentation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class XQDoc extends Inspect {
  /** Namespace URI. */
  private static final byte[] XQDOC_URI = token("http://www.xqdoc.org/1.0");
  /** Token: xqdoc. */
  private static final byte[] XQDOC_PREFIX = token("xqdoc");

  /**
   * Constructor.
   * @param qc query context
   * @param info input info (can be {@code null})
   */
  XQDoc(final QueryContext qc, final InputInfo info) {
    super(qc, info);
  }

  @Override
  public FNode parse(final IOContent content) throws QueryException {
    final AModule module = parseModule(content);
    final FBuilder xqdoc = element("xqdoc").ns();
    final FBuilder control = element("control");
    control.node(element("date").text(qc.dateTime().datm.string(info)));
    control.node(element("version").text("1.1"));
    xqdoc.node(control);

    final String type = module instanceof LibraryModule ? "library" : "main";
    final FBuilder mod = element("module").attr(Q_TYPE, type);
    if(module instanceof LibraryModule) {
      mod.node(element("uri").text(module.sc.module.uri()));
      mod.node(element("name").text(content.name()));
    } else {
      mod.node(element("uri").text(content.name()));
    }
    comment(module, mod);
    xqdoc.node(mod);

    // imports
    final FBuilder imports = element("imports");
    for(final byte[] uri : module.modules) {
      imports.node(element("import").attr(Q_TYPE, "library").node(element("uri").text(uri)));
    }
    xqdoc.node(imports);

    // namespaces
    final FBuilder namespaces = element("namespaces");
    final TokenObjectMap<byte[]> nsCache = new TokenObjectMap<>();
    for(final byte[] prefix : module.namespaces) {
      nsCache.put(prefix, module.namespaces.get(prefix));
    }
    for(final QNm name : module.options) {
      nsCache.put(name.prefix(), name.uri());
    }
    final QueryBiConsumer<QNm, StaticDecl> addNs = (name, sd) -> {
      if(name.hasPrefix()) nsCache.put(name.prefix(), name.uri());
      for(final Ann ann : sd.anns) {
        final byte[] uri = ann.name().uri();
        if(uri.length > 0) nsCache.put(NSGlobal.prefix(uri), uri);
      }
    };
    for(final StaticVar sv : module.vars) addNs.accept(sv.name, sv);
    for(final StaticFunc sf : module.funcs) addNs.accept(sf.funcName(), sf);
    for(final byte[] prefix : nsCache) {
      final FBuilder namespace = element("namespace");
      namespace.attr(Q_PREFIX, prefix).attr(Q_URI, nsCache.get(prefix));
      namespaces.node(namespace);
    }
    xqdoc.node(namespaces);

    // variables
    final FBuilder variables = element("variables");
    for(final StaticVar sv : module.vars) {
      final FBuilder variable = element("variable");
      variable.node(element("name").text(sv.name.string()));
      comment(sv, variable);
      annotations(sv.anns, variable);
      type(sv.seqType(), variable);
      variables.node(variable);
    }
    xqdoc.node(variables);

    // functions
    final FBuilder functions = element("functions");
    for(final StaticFunc sf : module.funcs) {
      final int al = sf.arity();
      final byte[] name = sf.funcName().string();
      final FuncType tp = sf.funcType();
      final FBuilder function = element("function").attr(Q_ARITY, al);
      comment(sf, function);
      function.node(element("name").text(name));
      annotations(sf.anns, function);

      final QueryString qs = new QueryString();
      qs.token(DECLARE).token(sf.anns).token(FUNCTION).token(name).token('(');
      for(int a = 0; a < al; a++) {
        final Var var = sf.params[a];
        if(a > 0) qs.token(SEP);
        qs.concat("$", var.name.string()).token(AS).token(tp.argTypes[a]);
      }
      qs.token(')').token(AS).token(tp.declType);
      if(sf.expr == null) qs.token(Q_EXTERNAL);

      function.node(element("signature").text(qs));
      if(al != 0) {
        final FBuilder fparameters = element("parameters");
        for(int a = 0; a < al; a++) {
          final FBuilder fparameter = element("parameter");
          fparameter.node(element("name").text(sf.params[a].name.string()));
          type(tp.argTypes[a], fparameter);
          fparameters.node(fparameter);
        }
        function.node(fparameters);
      }
      final FBuilder rtrn = element("return");
      type(sf.seqType(), rtrn);
      functions.node(function.node(rtrn));
    }
    xqdoc.node(functions);

    // options
    final FBuilder options = element("options");
    options(module.options, options, false);
    xqdoc.node(options);

    return xqdoc.finish();
  }

  @Override
  protected FBuilder element(final String name) {
    return FElem.build(new QNm(XQDOC_PREFIX, name, XQDOC_URI));
  }

  @Override
  protected FBuilder element(final byte[] name) {
    return eq(name, DOC_TAGS) ? element(string(name)) : element("custom").attr(Q_TAG, name);
  }

  /**
   * Creates a comment element.
   * @param scope scope
   * @param parent parent element
   * @throws QueryException query exception
   */
  private void comment(final StaticScope scope, final FBuilder parent) throws QueryException {
    final TokenObjectMap<TokenList> map = scope.doc();
    if(map != null) {
      final FBuilder comment = element("comment");
      comment(map, comment);
      parent.node(comment);
    }
  }

  /**
   * Creates annotation elements.
   * @param anns annotations
   * @param parent parent node
   * @throws QueryException query exception
   */
  private void annotations(final AnnList anns, final FBuilder parent) throws QueryException {
    if(!anns.isEmpty()) {
      final FBuilder annotations = element("annotations");
      annotations(anns, annotations, false);
      parent.node(annotations);
    }
  }

  /**
   * Creates a type element.
   * @param st sequence type
   * @param parent parent node
   */
  private void type(final SeqType st, final FBuilder parent) {
    if(st == null) return;
    final FBuilder type = element("type").text(st.typeString());
    final String occ = st.occ.toString();
    if(!occ.isEmpty()) type.attr(Q_OCCURRENCE, occ);
    parent.node(type);
  }
}

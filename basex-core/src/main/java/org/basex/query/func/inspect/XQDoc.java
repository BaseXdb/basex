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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class XQDoc extends Inspect {
  /** Namespace uri. */
  private static final byte[] URI = token("http://www.xqdoc.org/1.0");
  /** Prefix. */
  private static final byte[] PREFIX = token("xqdoc");

  /**
   * Constructor.
   * @param qc query context
   * @param info input info
   */
  XQDoc(final QueryContext qc, final InputInfo info) {
    super(qc, info);
  }

  @Override
  public FNode parse(final IOContent content) throws QueryException {
    final AModule module = parseModule(content);
    final FBuilder xqdoc = FElem.build(new QNm(PREFIX, PREFIX, URI)).declareNS();
    final FBuilder control = element("control");
    control.add(element("date").add(qc.dateTime().datm.string(info)));
    control.add(element("version").add("1.1"));
    xqdoc.add(control);

    final String type = module instanceof LibraryModule ? "library" : "main";
    final FBuilder mod = element("module").add("type", type);
    if(module instanceof LibraryModule) {
      mod.add(element("uri").add(module.sc.module.uri()));
      mod.add(element("name").add(content.name()));
    } else {
      mod.add(element("uri").add(content.name()));
    }
    comment(module, mod);
    xqdoc.add(mod);

    // imports
    final FBuilder imports = element("imports");
    for(final byte[] uri : module.modules) {
      imports.add(element("import").add("type", "library").add(element("uri").add(uri)));
    }
    xqdoc.add(imports);

    // namespaces
    final FBuilder namespaces = element("namespaces");
    final TokenMap nsCache = new TokenMap();
    for(final byte[] prefix : module.namespaces) {
      nsCache.put(prefix, module.namespaces.get(prefix));
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
      namespace.add("prefix", prefix).add("uri", nsCache.get(prefix));
      namespaces.add(namespace);
    }
    xqdoc.add(namespaces);

    // variables
    final FBuilder variables = element("variables");
    for(final StaticVar sv : module.vars) {
      final FBuilder variable = element("variable");
      variable.add(element("name").add(sv.name.string()));
      comment(sv, variable);
      annotations(sv.anns, variable);
      type(sv.seqType(), variable);
      variables.add(variable);
    }
    xqdoc.add(variables);

    // functions
    final FBuilder functions = element("functions");
    for(final StaticFunc sf : module.funcs) {
      final int al = sf.arity();
      final byte[] name = sf.funcName().string();
      final FuncType tp = sf.funcType();
      final FBuilder function = element("function").add("arity", al);
      comment(sf, function);
      function.add(element("name").add(name));
      annotations(sf.anns, function);

      final QueryString qs = new QueryString();
      qs.token(DECLARE).token(sf.anns).token(FUNCTION).token(name).token('(');
      for(int a = 0; a < al; a++) {
        final Var var = sf.params[a];
        if(a > 0) qs.token(SEP);
        qs.concat("$", var.name.string()).token(AS).token(tp.argTypes[a]);
      }
      qs.token(')').token(AS).token(tp.declType);
      if(sf.expr == null) qs.token("external");

      function.add(element("signature").add(qs));
      if(al != 0) {
        final FBuilder fparameters = element("parameters");
        for(int a = 0; a < al; a++) {
          final FBuilder fparameter = element("parameter");
          fparameter.add(element("name").add(sf.params[a].name.string()));
          type(tp.argTypes[a], fparameter);
          fparameters.add(fparameter);
        }
        function.add(fparameters);
      }
      final FBuilder rtrn = element("return");
      type(sf.seqType(), rtrn);
      functions.add(function.add(rtrn));
    }
    xqdoc.add(functions);

    return xqdoc.finish();
  }

  @Override
  protected FBuilder element(final String name) {
    return FElem.build(new QNm(PREFIX, name, URI));
  }

  @Override
  protected FBuilder element(final byte[] name) {
    return eq(name, DOC_TAGS) ? element(string(name)) : element("custom").add("tag", name);
  }

  /**
   * Creates a comment element.
   * @param scope scope
   * @param parent parent element
   * @throws QueryException query exception
   */
  private void comment(final StaticScope scope, final FBuilder parent) throws QueryException {
    final TokenObjMap<TokenList> map = scope.doc();
    if(map != null) {
      final FBuilder comment = element("comment");
      comment(map, comment);
      parent.add(comment);
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
      annotation(anns, annotations, false);
      parent.add(annotations);
    }
  }

  /**
   * Creates a type element.
   * @param st sequence type
   * @param parent parent node
   */
  private void type(final SeqType st, final FBuilder parent) {
    if(st == null) return;
    final FBuilder type = element("type").add(st.typeString());
    final String occ = st.occ.toString();
    if(!occ.isEmpty()) type.add("occurrence", occ);
    parent.add(type);
  }
}

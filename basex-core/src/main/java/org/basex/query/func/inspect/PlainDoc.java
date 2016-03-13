package org.basex.query.func.inspect;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class contains functions for generating a plain XQuery documentation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class PlainDoc extends Inspect {
  /**
   * Constructor.
   * @param qc query context
   * @param info input info
   */
  PlainDoc(final QueryContext qc, final InputInfo info) {
    super(qc, info);
  }

  /**
   * Parses a module and returns an inspection element.
   * @return inspection element
   * @throws QueryException query exception
   */
  FElem context() throws QueryException {
    final FElem root = elem("context", null);
    for(final StaticVar sv : qc.vars) variable(sv, root);
    for(final StaticFunc sf : qc.funcs.funcs()) function(sf.name, sf, sf.funcType(), sf.anns, root);
    return root;
  }

  @Override
  public FElem parse(final IO io) throws QueryException {
    parseQuery(io);
    final FElem root = elem("module", null);
    if(module instanceof LibraryModule) {
      final QNm name = ((LibraryModule) module).name;
      root.add("prefix", name.string());
      root.add("uri", name.uri());
    }

    final TokenObjMap<TokenList> doc = module.doc();
    if(doc != null) comment(doc, root);
    for(final StaticVar sv : module.vars().values()) variable(sv, root);
    for(final StaticFunc sf : module.funcs().values())
      function(sf.name, sf, sf.funcType(), sf.anns, root);
    return root;
  }

  /**
   * Creates a description for the specified variable.
   * @param sv static variable
   * @param parent node
   * @return resulting value
   * @throws QueryException query exception
   */
  private FElem variable(final StaticVar sv, final FElem parent) throws QueryException {
    final FElem variable = elem("variable", parent);
    variable.add("name", sv.name.string());
    if(sv.name.uri().length != 0) variable.add("uri", sv.name.uri());
    type(sv.seqType(), variable);
    comment(sv, variable);
    annotation(sv.anns, variable, true);
    return variable;
  }

  /**
   * Creates a description for the specified function.
   * @param fname name of function
   * @param sf function reference
   * @param ftype function type
   * @param anns annotations
   * @param parent node
   * @return resulting value
   * @throws QueryException query exception
   */
  FElem function(final QNm fname, final StaticFunc sf, final FuncType ftype, final AnnList anns,
      final FElem parent) throws QueryException {

    final FElem function = elem("function", parent);
    if(fname != null) {
      function.add("name", fname.string());
      if(fname.uri().length != 0) function.add("uri", fname.uri());
    }

    final TokenObjMap<TokenList> doc = sf != null ? sf.doc() : null;
    final int al = ftype.argTypes.length;
    QNm[] names = null;
    if(sf != null) {
      names = new QNm[al];
      for(int n = 0; n < al; n++) names[n] = sf.args[n].name;
    }

    for(int a = 0; a < al; a++) {
      final FElem argument = elem("argument", function);
      if(names != null) {
        final byte[] name = names[a].string();
        final byte[] uri = names[a].uri();
        argument.add("name", name);
        if(uri.length != 0) argument.add("uri", uri);

        final byte[] pdoc = doc(doc, name);
        if(pdoc != null) add(pdoc, argument);
      }
      type(ftype.argTypes[a], argument);
    }

    annotation(anns, function, true);

    if(doc != null) {
      for(final byte[] key : doc) {
        if(eq(key, DOC_PARAM, DOC_RETURN)) continue;
        for(final byte[] value : doc.get(key)) {
          final FElem elem = eq(key, DOC_TAGS) ? elem(string(key), function) :
            elem("tag", function).add("name", key);
          add(value, elem);
        }
      }
    }

    final SeqType rt = sf != null ? sf.seqType() : ftype.type;
    final FElem ret = type(rt, elem("return", function));
    final TokenList returns = doc != null ? doc.get(DOC_RETURN) : null;
    if(returns != null) for(final byte[] val : returns) add(val, ret);
    return function;
  }

  /**
   * Creates an element.
   * @param name name of element
   * @param parent parent node
   * @return element node
   */
  @Override
  protected FElem elem(final String name, final FElem parent) {
    final FElem elem = new FElem(name);
    if(parent != null) parent.add(elem);
    return elem;
  }

  /**
   * Creates a comment element.
   * @param scope scope
   * @param parent parent element
   */
  private void comment(final StaticScope scope, final FElem parent) {
    final TokenObjMap<TokenList> tags = scope.doc();
    if(tags != null) comment(tags, parent);
  }

  @Override
  protected FElem elem(final byte[] tag, final FElem parent) {
    final String t = string(tag);
    return elem(eq(tag, DOC_TAGS) ? t : t + "_tag", parent);
  }

  /**
   * Returns an element with type information.
   * @param st sequence type
   * @param elem element
   * @return element
   */
  private static FElem type(final SeqType st, final FElem elem) {
    if(st != null) {
      elem.add("type", st.typeString());
      final String occ = st.occ.toString();
      if(!occ.isEmpty()) elem.add("occurrence", occ);
    }
    return elem;
  }
}

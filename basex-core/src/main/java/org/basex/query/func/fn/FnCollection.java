package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.regex.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.http.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnCollection extends Docs {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return collection(false, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = super.opt(cc);
    // sharpen the type (options may assign non-XML media types to file extensions)
    if(expr == this && COLLECTION.is(this) && !defined(1)) {
      final byte[] uri = staticUri();
      final QueryInput qi = uri != null ? queryInput(uri) : null;
      if(qi != null && qi.dbName != null) exprType.assign(Types.DOCUMENT_ZM);
    }
    return expr;
  }

  @Override
  boolean stable(final QueryContext qc) throws QueryException {
    return arg(1) instanceof Value && toOptions(arg(1), new CollectionOptions(), qc).
        get(CollectionOptions.STABLE);
  }

  @Override
  public final boolean ddo() {
    return true;
  }

  /**
   * Returns the resources of a collection, or the URIs of the files of a directory.
   * @param uris return URIs of unopened directories
   * @param qc query context
   * @return resources or URIs
   * @throws QueryException query exception
   */
  final Value collection(final boolean uris, final QueryContext qc) throws QueryException {
    final CollectionOptions options = toOptions(arg(1), new CollectionOptions(), qc);
    final DocOptions docOpts = options.xmlOptions();
    check(docOpts, false);

    // return default collection or parse specified collection
    QueryInput qi = queryInput;
    if(qi == null) {
      final Expr source = arg(0);
      final Item uri = source.atomItem(qc, info);
      if(!uri.isEmpty()) {
        qi = queryInput(toToken(uri, source));
        if(qi == null) throw INVCOLL_X.get(info, uri);
      }
    }
    final ValueBuilder vb = new ValueBuilder(qc);
    final Value value = qc.resources.collection(qi, options, uris, qc, info);

    // filter resources by URI (listed URIs were already filtered)
    final RegExpr regExpr = options.regExpr(qc, info);
    if(regExpr == null || value.type.instanceOf(BasicType.ANY_URI)) {
      vb.add(value);
    } else {
      for(final Item item : value) {
        if(!(item instanceof final XNode node) ||
            regExpr.matcher(string(node.baseURI()), qc).find()) vb.add(item);
      }
    }

    // add resources of a directory that are assigned to media types and not parsed as XML
    final Map<String, MediaType> types = options.mediaTypes(info);
    if(!types.isEmpty() && qi != null) {
      for(final IOFile file : qc.resources.others(qi, docOpts, options, qc, info)) {
        final String name = file.name();
        final int i = name.lastIndexOf('.');
        final MediaType type = i != -1 ? types.get(name.substring(i + 1)) : null;
        if(type != null) {
          vb.add(uris ? Uri.get(file.url()) : resource(file, type, docOpts, options, qc));
        }
      }
    }
    return vb.value(this);
  }

  /**
   * Parses a resource of a directory that was skipped by the collection parser.
   * @param file file
   * @param type media type
   * @param docOpts options for parsing XML resources
   * @param options collection options
   * @param qc query context
   * @return parsed resource, or empty sequence if it is erroneous and errors are ignored
   * @throws QueryException query exception
   */
  private Value resource(final IOFile file, final MediaType type, final DocOptions docOpts,
      final CollectionOptions options, final QueryContext qc) throws QueryException {
    try {
      if(type.isXml()) {
        return qc.resources.doc(new QueryInput(file.path(), sc()), docOpts, qc, info, false);
      }
      final Str source = Str.get(file.path());
      final StandardFunc func;
      if(type.isJSON()) {
        func = JSON_DOC.get(info, source, options.get(CollectionOptions.JSON_OPTIONS));
      } else if(type.isCSV()) {
        func = CSV_DOC.get(info, source, options.get(CollectionOptions.CSV_OPTIONS));
      } else if(type.is(MediaType.TEXT_HTML)) {
        func = HTML_DOC.get(info, source, options.get(CollectionOptions.HTML_OPTIONS));
      } else if(type.isText()) {
        func = UNPARSED_TEXT.get(info, source, options.get(CollectionOptions.TEXT_OPTIONS));
      } else {
        func = UNPARSED_BINARY.get(info, source);
      }
      return func.value(qc);
    } catch(final QueryException ex) {
      // type errors (e.g., invalid parsing options) are not caused by the resource
      if(options.get(CollectionOptions.IGNORE_ERRORS) && !ex.matches(ErrType.XPTY)) {
        return Empty.VALUE;
      }
      throw ex;
    }
  }
}

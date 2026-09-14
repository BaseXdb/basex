package org.basex.io.parse;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Handler that builds a document node from XML events.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NodeHandler implements XmlHandler {
  /** No namespace declarations. */
  private static final Atts NO_NSP = new Atts();

  /** Shared data references. */
  private final SharedData shared = new SharedData();
  /** Document. */
  private final FBuilder doc;
  /** Defer the completion of elements until the document is finished. */
  private final boolean deferred;
  /** Open elements. */
  private final ArrayDeque<Elem> elems = new ArrayDeque<>();
  /** Number of open elements with namespace declarations. */
  private int declared;
  /** Root element (can be {@code null}). */
  private Elem root;
  /** Most recently opened element (can be {@code null}). */
  private FBuilder current;

  /**
   * Constructor.
   * @param uri base URI
   * @param deferred defer the completion of elements until the document is finished
   */
  public NodeHandler(final String uri, final boolean deferred) {
    doc = FDoc.build(token(uri));
    this.deferred = deferred;
  }

  @Override
  public void openElem(final byte[] name, final Atts atts, final Atts nsp) {
    final byte[] uri = uri(prefix(name), nsp);
    final FBuilder elem = FElem.build(uri != null ? shared.qName(name, uri) : shared.qName(name));
    final int ns = nsp.size();
    for(int n = 0; n < ns; n++) elem.ns(nsp.name(n), nsp.value(n));
    final int as = atts.size();
    for(int a = 0; a < as; a++) elem.attr(shared.qName(atts.name(a)), shared.token(atts.value(a)));

    final Elem e = new Elem(elem, ns != 0 ? new Atts(nsp) : NO_NSP, deferred);
    if(ns != 0) declared++;
    if(root == null) root = e;
    elems.push(e);
    current = elem;
  }

  @Override
  public void text(final byte[] value) {
    if(value.length == 0) return;
    final Elem elem = elems.peek();
    final FTxt text = new FTxt(shared.token(value));
    if(deferred) elem.children.add(text);
    else elem.builder.node(text);
  }

  @Override
  public void closeElem() {
    final Elem elem = elems.pop();
    if(elem.nsp != NO_NSP) declared--;
    // the root element is attached to the document when the handler is finished
    final Elem parent = elems.peek();
    if(parent == null) return;
    if(deferred) parent.children.add(elem);
    else parent.builder.node(elem.builder);
  }

  /**
   * Returns the most recently opened element.
   * @return element (can be {@code null})
   */
  public FBuilder current() {
    return current;
  }

  /**
   * Finishes the document.
   * @return document node
   */
  public FNode finish() {
    if(root != null) {
      if(deferred) complete(root);
      doc.node(root.builder);
    }
    return doc.finish();
  }

  /**
   * Attaches the deferred children of an element.
   * @param elem element
   */
  private static void complete(final Elem elem) {
    for(final Object child : elem.children) {
      if(child instanceof final Elem e) {
        complete(e);
        elem.builder.node(e.builder);
      } else {
        elem.builder.node((GNode) child);
      }
    }
  }

  /**
   * Resolves the namespace URI of a prefix.
   * @param prefix prefix
   * @param nsp namespace declarations of the element to be opened
   * @return URI (can be {@code null})
   */
  private byte[] uri(final byte[] prefix, final Atts nsp) {
    byte[] uri = nsp.value(prefix);
    if(uri == null && declared != 0) {
      for(final Elem elem : elems) {
        uri = elem.nsp.value(prefix);
        if(uri != null) break;
      }
    }
    return uri;
  }

  /** Element under construction. */
  private static final class Elem {
    /** Builder. */
    private final FBuilder builder;
    /** Namespace declarations. */
    private final Atts nsp;
    /** Deferred children (can be {@code null}). */
    private final ArrayList<Object> children;

    /**
     * Constructor.
     * @param builder builder
     * @param nsp namespace declarations
     * @param deferred defer children
     */
    private Elem(final FBuilder builder, final Atts nsp, final boolean deferred) {
      this.builder = builder;
      this.nsp = nsp;
      children = deferred ? new ArrayList<>(1) : null;
    }
  }
}

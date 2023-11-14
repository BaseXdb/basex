package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnElementsToMaps extends StandardFunc {
  /** Options. */
  public static class ElementsOptions extends Options {
    /** Option. */
    public static final BooleanOption UNIFORM = new BooleanOption("uniform", false);
    /** Option. */
    public static final OptionsOption<Options> LAYOUTS =
        new OptionsOption<>("layouts", new Options());
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ANodeList elements = new ANodeList();
    final Iter iter = arg(0).iter(qc);
    for(Item item; (item = iter.next()) != null;) elements.add(toElem(item, qc));
    final ElementsOptions options = toOptions(arg(1), new ElementsOptions(), false, qc);

    final QNmMap<Layout> layouts = new QNmMap<>();
    for(final Map.Entry<String, String> entry : options.get(ElementsOptions.LAYOUTS).
        free().entrySet()) {
      final QNm name = QNm.parse(Token.token(entry.getKey()), sc);
      final Layout layout = Layout.get(entry.getValue());
      if(layout == null) throw INVALIDOPT_X.get(info, "Unknown layout: " + entry.getValue() + '.');
      layouts.put(name, layout);
    }

    if(options.get(ElementsOptions.UNIFORM)) {
      // collect distinct element names
      final QNmMap<ANodeList> names = new QNmMap<>();
      for(final ANode element : elements) {
        for(final ANode desc : element.descendantOrSelfIter()) {
          if(!element(desc)) continue;
          final QNm name = desc.qname();
          if(layouts.contains(name)) continue;
          ANodeList descs = names.get(name);
          if(descs == null) {
            descs = new ANodeList();
            names.put(name, descs);
          }
          descs.add(desc.finish());
        }
      }
      // assign layout
      for(final QNm name : names) {
        if(!layouts.contains(name)) {
          final ANodeList nodes = names.get(name);
          for(final Layout l : Layout.VALUES) {
            if(((Checks<ANode>) l::matches).all(nodes)) {
              layouts.put(name, l);
              break;
            }
          }
        }
      }
    }

    // create result
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final ANode element : elements) {
      final Value value = layout(element, layouts).apply(element, qc);
      return XQMap.entry(nodeName(element), value, info);
    }
    return vb.value(this);
  }

  /**
   * Returns a matching layout for the specified node.
   * @param node node
   * @param layouts custom layouts
   * @return layout
   */
  private Layout layout(final ANode node, final QNmMap<Layout> layouts) {
    final Layout l = layouts.get(node.qname());
    if(l == null) {
      for(final Layout layout : Layout.VALUES) {
        if(layout.matches(node)) return layout;
      }
    }
    return l;
  }

  /**
   * Checks if a node is a non-empty text.
   * @param node node
   * @return result of check
   */
  static boolean nonEmptyText(final ANode node) {
    return node.type == NodeType.TEXT && Token.normalize(node.string()).length != 0;
  }

  /**
   * Checks if a node is an element.
   * @param node node
   * @return result of check
   */
  static boolean element(final ANode node) {
    return node.type == NodeType.ELEMENT;
  }

  /**
   * Returns an attribute map.
   * @param node node
   * @return attributes
   * @throws QueryException query exception
   */
  static MapBuilder attributes(final ANode node) throws QueryException {
    final MapBuilder mb = new MapBuilder();
    for(final ANode attr : node.attributeIter()) {
      mb.put(nodeName(attr, "@"), attr.string());
    }
    return mb;
  }

  /**
   * Returns the node name.
   * @param node node
   * @return name
   */
  static Str nodeName(final ANode node) {
    return nodeName(node, null);
  }

  /**
   * Returns the node name.
   * @param node node
   * @param prefix (can be {@code null})
   * @return name
   */
  static Str nodeName(final ANode node, final String prefix) {
    byte[] name = node.qname().internal();
    if(prefix != null) name = Token.concat(prefix, name);
    return Str.get(name);
  }

  /**
   * Serializes a node.
   * @param node node
   * @param method method
   * @return name
   * @throws QueryException query exception
   */
  static Str serialize(final ANode node, final String method) throws QueryException {
    final SerializerOptions sopts = new SerializerOptions();
    sopts.set(SerializerOptions.METHOD, method);
    try {
      return Str.get(node.serialize(sopts).finish());
    } catch(final QueryIOException ex) {
      throw ex.getCause(null);
    }
  }

  /** Layouts. */
  private enum Layout {
    /** Layout 'empty': empty(*|text()|@*). */
    EMPTY() {
      @Override
      boolean matches(final ANode node) {
        return EMPTY_PLUS.matches(node) && node.attributeIter().next() == null;
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) {
        return Str.EMPTY;
      }
    },
    /** Layout 'empty-plus': @* and empty(*|text()). */
    EMPTY_PLUS() {
      @Override
      boolean matches(final ANode node) {
        return ((Checks<ANode>) c -> !c.type.oneOf(NodeType.ELEMENT, NodeType.TEXT)).
            all(node.childIter());
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) throws QueryException {
        return attributes(node).map();
      }
    },
    /** Layout 'simple': empty(*|@*). */
    SIMPLE() {
      @Override
      boolean matches(final ANode node) {
        return SIMPLE_PLUS.matches(node) && node.attributeIter().next() == null;
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) {
        return Str.get(node.string());
      }
    },
    /** Layout 'simple-plus': empty(*). */
    SIMPLE_PLUS() {
      @Override
      boolean matches(final ANode node) {
        return ((Checks<ANode>) c -> !element(c)).all(node.childIter());
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) throws QueryException {
        return attributes(node).put("#content", node.string()).map();
      }
    },
    /** Layout 'list':
     * *[2] and all-equal(*!node-name()) and empty(text()[normalize-space()]) and empty(@*). */
    LIST() {
      @Override
      boolean matches(final ANode node) {
        return LIST_PLUS.matches(node) && node.attributeIter().next() == null;
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) throws QueryException {
        if(!LIST_PLUS.matches(node)) return MIXED.apply(node, qc);
        final ValueBuilder vb = new ValueBuilder(qc);
        for(final ANode child : node.childIter()) {
          if(element(child)) vb.add(Str.get(child.string()));
        }
        return vb.value();
      }
    },
    /** Layout 'list-plus':
     * *[2] and all-equal(*!node-name()) and empty(text()[normalize-space()]). */
    LIST_PLUS() {
      @Override
      boolean matches(final ANode node) {
        QNm name = null;
        int c = 0;
        for(final ANode child : node.childIter()) {
          if(element(child)) {
            if(name == null) name = child.qname();
            else if(!name.eq(child.qname())) return false;
            c++;
          } else if(nonEmptyText(child)) {
            return false;
          }
        }
        return c > 1;
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) throws QueryException {
        if(!LIST_PLUS.matches(node)) return MIXED.apply(node, qc);
        for(final ANode child : node.childIter()) {
          if(element(child)) {
            final MapBuilder mb = attributes(node);
            final Value list = LIST.apply(node, qc);
            if(!list.isEmpty()) mb.put(nodeName(child), list);
            return mb.map();
          }
        }
        throw Util.notExpected();
      }
    },
    /** Layout 'record':
     * * and all-different(*!node-name()) and empty(text()[normalize-space()]). */
    RECORD() {
      @Override
      boolean matches(final ANode node) {
        final QNmSet names = new QNmSet();
        for(final ANode child : node.childIter()) {
          if(element(child) && !names.add(child.qname()) || nonEmptyText(child)) return false;
        }
        return !names.isEmpty();
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) throws QueryException {
        if(((Checks<ANode>) FnElementsToMaps::nonEmptyText).any(node.childIter())) {
          return MIXED.apply(node, qc);
        }
        final MapBuilder mb = attributes(node);
        for(final ANode child : node.childIter()) {
          if(element(child)) mb.put(nodeName(child), child.string());
        }
        return mb.map();
     }
    },
    /** Layout 'sequence': empty(text()[normalize-space()]). */
    SEQUENCE() {
      @Override
      boolean matches(final ANode node) {
        return ((Checks<ANode>) c -> !nonEmptyText(c)).all(node.childIter());
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) throws QueryException {
        final ValueBuilder vb = new ValueBuilder(qc);
        for(final ANode attr : node.attributeIter()) {
          vb.add(new MapBuilder().put(nodeName(attr, "@"), attr.string()).map());
        }
        for(final ANode child : node.childIter()) {
          if(element(child)) {
            vb.add(new MapBuilder().put(nodeName(child), child.string()).map());
          }
        }
        return vb.value();
      }
    },
    /** Layout 'mixed': *. */
    MIXED() {
      @Override
      boolean matches(final ANode node) {
        return ((Checks<ANode>) FnElementsToMaps::element).any(node.childIter());
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) throws QueryException {
        final ValueBuilder vb = new ValueBuilder(qc);
        for(final ANode attr : node.attributeIter()) {
          vb.add(new MapBuilder().put(nodeName(attr, "@"), attr.string()).map());
        }
        for(final ANode child : node.childIter()) {
          if(element(child)) {
            vb.add(new MapBuilder().put(nodeName(child), child.string()).map());
          } else if(child.type == NodeType.TEXT) {
            vb.add(Str.get(child.string()));
          } else if(child.type == NodeType.COMMENT) {
            vb.add(new MapBuilder().put("#comment", child.string()).map());
          } else if(child.type == NodeType.PROCESSING_INSTRUCTION) {
            vb.add(new MapBuilder().put("#processing-instruction", child.name()).
                put("#data", child.string()).map());
          }
        }
        return vb.value();
      }
    },
    /** Layout 'xml'. */
    XML() {
      @Override
      boolean matches(final ANode node) {
        return false;
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) throws QueryException {
        return serialize(node, "xml");
      }
    },
    /** Layout 'xhtml'. */
    XHTML() {
      @Override
      boolean matches(final ANode node) {
        return false;
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) throws QueryException {
        return serialize(node, "xhtml");
      }
    },
    /** Layout 'html'. */
    HTML() {
      @Override
      boolean matches(final ANode node) {
        return false;
      }
      @Override
      Value apply(final ANode node, final QueryContext qc) throws QueryException {
        return serialize(node, "html");
      }
    };

    /**
     * Checks if a layout matches for the given element node.
     * @param node node
     * @return result of check
     */
    abstract boolean matches(ANode node);

    /**
     * Creates a map value for the given node.
     * @param node node
     * @param qc query context
     * @return map
     * @throws QueryException query exception
     */
    abstract Value apply(ANode node, QueryContext qc) throws QueryException;

    /** Cached enums (faster). */
    public static final Layout[] VALUES = values();

    /**
     * Returns a layout matching the specified string.
     * @param layout layout string
     * @return layout, or {@code null} if no match is found
     */
    public static Layout get(final String layout) {
      for(final Layout l : VALUES) {
        if(l.toString().equals(layout)) return l;
      }
      return null;
    }

    @Override
    public String toString() {
      return EnumOption.string(name());
    }
  }
}

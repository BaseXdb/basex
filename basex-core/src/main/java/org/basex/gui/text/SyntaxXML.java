package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class defines syntax highlighting for XML files: markup without embedded code.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SyntaxXML extends SyntaxMarkup {
  /** Attribute that preserves whitespace. */
  private static final byte[] SPACE = token("xml:space");

  /** Element names of the current completion scan (can be {@code null}). */
  private TokenSet elements;
  /** Attribute names of the current completion scan (can be {@code null}). */
  private TokenSet attributes;
  /** Name of the last start tag of the current completion scan (can be {@code null}). */
  private byte[] element;

  @Override
  public byte[] commentOpen() {
    return XMLToken.COMM_O;
  }

  @Override
  public byte[] commentEnd() {
    return XMLToken.COMM_C;
  }

  @Override
  boolean boundarySpace(final byte[] text) {
    // 'xml:space' turns the whitespace between tags into significant text
    return indexOf(text, SPACE) == -1;
  }

  @Override
  ArrayList<ArrayList<Completion>> completions(final byte[] text, final int pos) {
    elements = new TokenSet();
    attributes = new TokenSet();

    // collect the names of the document and the element that is open at the specified position
    final TokenList stack = new TokenList();
    byte[] open = null;
    reset();
    for(int p = 0, tl = text.length; p < tl;) {
      final int cl = cl(text, p);
      color(text, p, p + cl);
      // the name of a start tag is pushed when the element is opened, and popped by its end tag
      if(elementOpen(text, p)) stack.add(element);
      else if(modeBefore() == ETAG && modeAfter() != ETAG && !stack.isEmpty()) stack.pop();
      if(p < pos) open = stack.isEmpty() ? null : stack.peek();
      p += cl;
    }

    final ArrayList<Completion> list;
    final int slash = back(text, pos);
    if(cp(text, slash) == '/' && prev(text, slash) == '<') {
      // an end tag is closed by the name of the innermost open element
      list = open == null ? new ArrayList<>() : candidates(new TokenSet(open), "");
    } else {
      // in a start tag, the element name follows the angle bracket, all other names are attributes
      final boolean tag = cp(text, pos) == '<';
      list = candidates(tag ? elements : attributes, tag ? "<" : "");
    }
    elements = null;
    attributes = null;
    element = null;
    return single(list);
  }

  @Override
  boolean completable() {
    // completions are proposed in tags, including the position after the opening angle bracket
    final int after = modeAfter();
    return after == TAG || after == ETAG;
  }

  @Override
  void classify(final byte[] text, final int start, final int end) {
    // the element name directly follows the opening angle bracket
    if(elements == null) return;
    final byte[] name = substring(text, start, end);
    if(start > 0 && text[start - 1] == '<') {
      elements.add(name);
      element = name;
    } else {
      attributes.add(name);
    }
  }
}

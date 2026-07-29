package org.basex.gui.text;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;
import org.basex.util.hash.*;

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
  ArrayList<ArrayList<Completion>> completions(final byte[] text) {
    elements = new TokenSet();
    attributes = new TokenSet();
    scan(text, text.length);

    final ArrayList<ArrayList<Completion>> lists = new ArrayList<>();
    lists.add(completions(elements, true));
    lists.add(completions(attributes, false));
    elements = null;
    attributes = null;
    return lists;
  }

  @Override
  boolean completable(final int ch) {
    // completions are proposed in tags and directly after an opening angle bracket
    return code() || ch == '<';
  }

  @Override
  void classify(final byte[] text, final int start, final int end) {
    // the element name directly follows the opening angle bracket
    if(elements == null) return;
    final TokenSet names = start > 0 && text[start - 1] == '<' ? elements : attributes;
    names.add(substring(text, start, end));
  }

  /**
   * Converts the specified names to completion candidates.
   * @param names names
   * @param tags add candidates that include the opening angle bracket
   * @return candidates
   */
  private static ArrayList<Completion> completions(final TokenSet names, final boolean tags) {
    final ArrayList<Completion> list = new ArrayList<>();
    for(final byte[] name : names) {
      final String string = string(name);
      list.add(new Completion(string.toLowerCase(Locale.ENGLISH), string, string, false));
      if(tags) {
        final String tag = '<' + string;
        list.add(new Completion(tag.toLowerCase(Locale.ENGLISH), tag, tag, true));
      }
    }
    return list;
  }
}

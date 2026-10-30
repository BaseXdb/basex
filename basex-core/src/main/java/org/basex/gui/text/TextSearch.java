package org.basex.gui.text;

import static org.basex.util.Token.*;

import javax.swing.*;
import javax.swing.Timer;

import org.basex.gui.text.SearchBar.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Searches the text of a {@link TextEditor} and keeps track of its hits.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class TextSearch {
  /** Delay before a text change triggers a new search (ms). */
  private static final int SEARCH_DELAY = 100;

  /** Text editor. */
  private final TextEditor editor;
  /** Collapses the searches of consecutive text changes into a single one. */
  private final Timer timer;

  /** Start and end positions of search terms (initially empty). */
  private IntList[] results = { new IntList(), new IntList() };
  /** Index of the current search hit ({@code -1} if none). */
  private int hit = -1;
  /** Indicates whether the current search hit was reached by a selecting jump. */
  private boolean selected;
  /** Context of the current search results (can be {@code null}). */
  private SearchContext context;
  /** Context of the most recently started search (can be {@code null}). */
  private SearchContext request;
  /** Search thread (can be {@code null}). */
  private Thread thread;
  /** Id of the most recent search. */
  private long id;
  /** Result of the last decoding (accessed by the search thread and the event dispatch thread). */
  private volatile Decoded decoded;

  /**
   * A decoded text and the byte array it was decoded from (compared by identity).
   * @param text text
   * @param string decoded text
   */
  private record Decoded(byte[] text, String string) { }

  /**
   * Constructor.
   * @param editor text editor
   */
  TextSearch(final TextEditor editor) {
    this.editor = editor;
    timer = new Timer(SEARCH_DELAY, e -> {
      if(request != null) start(request, false);
    });
    timer.setRepeats(false);
  }

  /**
   * Sets a new search context and searches at once.
   * @param sc search context (can be {@code null})
   * @param jump jump to next search result
   */
  void find(final SearchContext sc, final boolean jump) {
    timer.stop();
    if(sc != null) start(sc, jump);
  }

  /**
   * Repeats the last requested search, but not before the changes have come to a rest.
   */
  void textChanged() {
    if(request != null) timer.restart();
  }

  /**
   * Starts a search. It runs in a separate thread; its results are adopted, and accessed,
   * in the event dispatch thread.
   * @param sc search context
   * @param jump jump to next search result
   */
  private void start(final SearchContext sc, final boolean jump) {
    request = sc;
    // interrupt old search thread
    final Thread old = thread;
    if(old != null) old.interrupt();

    // start new search on a snapshot of the current text
    final byte[] txt = editor.text();
    final long i = ++id;
    final Thread t = new Thread(() -> {
      try {
        final IntList[] res = sc.search(txt, decode(txt));
        SwingUtilities.invokeLater(() -> {
          // discard the results of a search that has been superseded
          if(i != id) return;
          results(res, sc);
          if(sc.bar != null) sc.bar.refresh(editor, sc, jump);
        });
      } catch(final Exception ex) {
        // search was interrupted, or failed unexpectedly
        Util.debug(ex);
      }
    });
    t.setDaemon(true);
    t.start();
    thread = t;
  }

  /**
   * Returns the specified text as string; the result of the last call is cached.
   * Two threads may decode the same text at once, but none of them is ever blocked.
   * @param txt text
   * @return decoded text
   */
  private String decode(final byte[] txt) {
    final Decoded dec = decoded;
    if(dec != null && dec.text() == txt) return dec.string();
    final String str = string(txt);
    decoded = new Decoded(txt, str);
    return str;
  }

  /**
   * Adopts the results of a search.
   * @param res start and end positions of the hits (must not be modified)
   * @param sc search context (can be {@code null})
   */
  void results(final IntList[] res, final SearchContext sc) {
    results = res;
    hit = -1;
    context = sc;
  }

  /**
   * Returns the context of the last search.
   * @return search context (can be {@code null})
   */
  SearchContext context() {
    return context;
  }

  /**
   * Returns the start and end positions of the search hits.
   * @return positions (must not be modified)
   */
  IntList[] results() {
    return results;
  }

  /**
   * Returns the index of the current search hit.
   * @return index, or {@code -1}
   */
  int index() {
    return hit;
  }

  /**
   * Returns the number of search hits.
   * @return number of hits
   */
  int size() {
    return results[0].size();
  }

  /**
   * Returns the start position of a hit.
   * @param h index of the hit
   * @return position
   */
  int start(final int h) {
    return results[0].get(h);
  }

  /**
   * Returns the end position of a hit.
   * @param h index of the hit
   * @return position
   */
  int end(final int h) {
    return results[1].get(h);
  }

  /**
   * Checks if the specified range is a search hit.
   * @param s start position
   * @param e end position
   * @return result of check
   */
  boolean isHit(final int s, final int e) {
    final int h = results[0].sortedIndexOf(s);
    return h >= 0 && end(h) == e;
  }

  /**
   * Returns the index of the hit that encloses the caret, or of the first hit after it (wrapping).
   * @return index, or {@code -1} if there are no hits
   */
  int caretHit() {
    final IntList starts = results[0];
    final int sl = starts.size(), pos = editor.pos();
    if(sl == 0) return -1;
    final int s = starts.sortedIndexOf(pos);
    if(s >= 0) return s;
    final int i = -s - 1;
    // caret inside the preceding hit; otherwise, next hit, or first one
    return i > 0 && pos < end(i - 1) ? i - 1 : i == sl ? 0 : i;
  }

  /**
   * Replaces the hits in the specified range.
   * @param rc replace context
   * @param s start position
   * @param e end position
   * @return new range of the replaced text
   */
  int[] replace(final ReplaceContext rc, final int s, final int e) {
    final byte[] txt = editor.text();
    return rc.replace(context, txt, decode(txt), s, e);
  }

  /**
   * Replaces the search hit at the caret.
   * @param rc replace context
   * @return selection offsets, or {@code null} if there is no hit
   */
  int[] replaceHit(final ReplaceContext rc) {
    final int h = caretHit();
    if(h < 0) return null;
    hit = h;
    return replace(rc, start(h), end(h));
  }

  /**
   * Determines the hit to jump to and makes it the current one.
   * @param dir search direction
   * @param select select hit
   * @return index, or {@code -1} if there are no hits
   */
  int jump(final SearchDir dir, final boolean select) {
    final int sl = size();
    if(sl == 0) {
      hit = -1;
      return -1;
    }

    // a zero-width hit is no selection: treat a hit we jumped to as the current one
    final int pos = editor.pos();
    final boolean current = editor.isSelected() || selected && hit >= 0 && hit < sl &&
        start(hit) == pos;
    // a directional jump off a hit lowers the key: a hit at the caret still lies ahead of it
    final boolean back = select && !current && dir != SearchDir.CURRENT;
    int s = results[0].sortedIndexOf(back ? pos - 1 : pos);
    s = switch(dir) {
      case CURRENT -> s < 0 ? -s - 1 : s;
      case FORWARD -> s < 0 ? -s - 1 : s + 1;
      case BACKWARD -> s < 0 ? -s - 2 : s - 1;
    };
    if(s < 0) s = sl - 1;
    else if(s == sl) s = 0;
    hit = s;
    selected = select;
    return s;
  }
}

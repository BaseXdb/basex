package org.basex.gui.view.editor;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;

import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.*;
import org.basex.gui.*;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.query.*;

/**
 * This class extends the text editor by XQuery features.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class EditorArea extends Editor {
  /** Pattern for detecting library modules. */
  private static final Pattern LIBMOD_PATTERN = Pattern.compile(
    "^(xquery( version ['\"].*?['\"])?( encoding ['\"].*?['\"])?; ?)?module namespace.*");

  /** File label. */
  final BaseXLabel label;

  /** File in tab. */
  IOFile file;
  /** Timestamp. */
  long tstamp;
  /** Flag for modified content. */
  boolean modified;
  /** Last input. */
  byte[] last;
  /** This flag indicates if the input is an XQuery main module. */
  boolean xquery = true;
  /** This flag indicates if the input is a command script. */
  boolean script = true;

  /** View reference. */
  private final EditorView view;

  /**
   * Constructor.
   * @param v view reference
   * @param f file reference
   */
  EditorArea(final EditorView v, final IOFile f) {
    super(true, v.gui);
    view = v;
    file = f;
    label = new BaseXLabel(f.name());
    setSyntax(f, false);

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        if(opened() && !modified) {
          try {
            // reload file that has been modified
            final long t = tstamp;
            if(file.timeStamp() != t) {
              setText(new IOFile(file.path()).read());
              tstamp = t;
            }
          } catch(final IOException ex) { /* ignored */ }
        }
      }
    });
  }

  /**
   * Returns {@code true} if a file exists for the given text.
   * @return result of check
   */
  boolean opened() {
    return tstamp != 0;
  }

  @Override
  public void setText(final byte[] t) {
    last = getText();
    super.setText(t);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    super.mouseReleased(e);
    view.pos.setText(pos());
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    final byte[] t = text.text();
    super.keyPressed(e);
    if(t != text.text()) error(-1);
    else view.pos.setText(pos());
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    final byte[] t = text.text();
    super.keyTyped(e);
    if(t != text.text()) error(-1);
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    super.keyReleased(e);
    if(!e.isActionKey() && !modifier(e)) {
      release(EXEC.is(e) ? Action.EXECUTE : Action.CHECK);
    }
  }

  @Override
  protected void release(final Action action) {
    view.refreshControls(false);
    final byte[] in = getText();
    final boolean eq = eq(in, last);
    if(eq && action == Action.CHECK) return;
    last = in;

    gui.context.prop.set(Prop.QUERYPATH, file.path());
    script = file.hasSuffix(IO.BXSSUFFIX);
    xquery = !script && !opened() || file.hasSuffix(IO.XQSUFFIXES);
    String input = string(in);
    if(xquery) {
      // check if input is/might be an xquery main module
      if(input.isEmpty()) input = "()";
      xquery = !module(in);
      if(xquery && (action == Action.EXECUTE || gui.gprop.is(GUIProp.EXECRT))) {
        // execute query if forced, or if realtime execution is activated
        gui.execute(true, new XQuery(input));
      } else {
        // parse query
        final QueryContext ctx = new QueryContext(gui.context);
        try {
          if(!xquery) ctx.module(input);
          else ctx.parse(input);
          view.info(OK, true, false);
        } catch(final QueryException ex) {
          view.info(ex.getMessage(), false, false);
        }
      }
    } else if(action == Action.EXECUTE && script) {
      // execute query if forced, or if realtime execution is activated
      gui.execute(true, new Execute(input));
    } else if(script || file.hasSuffix(IO.XMLSUFFIXES)) {
      try {
        new EmptyBuilder(new IOContent(in), gui.context).build();
        if(script) new CommandParser(input, gui.context).parse();
        view.info(OK, true, false);
      } catch(final Exception ex) {
        view.info(ex.getMessage(), false, false);
      }
    }
  }

  /**
   * Returns the currently assigned file.
   * @return file
   */
  IOFile file() {
    return file;
  }

  /**
   * Sets the file reference.
   * @param f file
   */
  void file(final IOFile f) {
    file = f;
    tstamp = f.timeStamp();
    setSyntax(file, true);
    hist.save();
  }

  /**
   * Highlights the error.
   * @param pos error position
   */
  void jumpError(final int pos) {
    requestFocusInWindow();
    setCursor(pos);
  }

  /**
   * Analyzes the first 80 characters to decide if the query is a module.
   * @param qu query to check
   * @return result of check
   */
  private static boolean module(final byte[] qu) {
    final String start = QueryProcessor.removeComments(string(qu), 80);
    return LIBMOD_PATTERN.matcher(start).matches();
  }
}

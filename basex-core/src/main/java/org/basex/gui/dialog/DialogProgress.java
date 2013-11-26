package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Dialog window for displaying the progress of a command execution.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class DialogProgress extends BaseXDialog implements ActionListener {
  /** Maximum value of progress bar. */
  private static final int MAX = 600;
  /** Refresh action. */
  private final Timer timer = new Timer(100, this);
   /** Information label. */
  private BaseXLabel info;
  /** Memory usage. */
  private BaseXMem mem;
  /** Executed command. */
  private Command command;
  /** Progress bar. */
  private JProgressBar bar;

  /**
   * Default constructor.
   * @param main main window
   * @param cmd progress reference
   */
  private DialogProgress(final GUI main, final Command cmd) {
    super(main, "");
    init(main, cmd);
  }

  /**
   * Default constructor.
   * @param dialog dialog window
   * @param cmd progress reference
   */
  private DialogProgress(final BaseXDialog dialog, final Command cmd) {
    super(dialog, "");
    init(dialog, cmd);
  }

  /**
   * Initializes all components.
   * @param parent parent component
   * @param cmd progress reference
   */
  private void init(final Component parent, final Command cmd) {
    info = new BaseXLabel(" ", true, true);
    set(info, BorderLayout.NORTH);

    if(cmd.supportsProg()) {
      bar = new JProgressBar(0, MAX);
      set(bar, BorderLayout.CENTER);
    } else {
      bar = null;
    }
    BaseXLayout.setWidth(info, MAX);

    final BaseXBack s = new BaseXBack(new BorderLayout()).border(10, 0, 0, 0);
    final BaseXBack m = new BaseXBack(new TableLayout(1, 2, 5, 0));
    mem = new BaseXMem(this, false);
    m.add(new BaseXLabel(MEMUSED_C));
    m.add(mem);
    s.add(m, BorderLayout.WEST);

    if(cmd.stoppable()) {
      final BaseXButton cancel = new BaseXButton(CANCEL, this);
      s.add(cancel, BorderLayout.EAST);
    }
    set(s, BorderLayout.SOUTH);

    command = cmd;
    timer.start();
    pack();
    setLocationRelativeTo(parent);
  }

  @Override
  public void cancel() {
    command.stop();
    close();
  }

  @Override
  public void close() {
    dispose();
  }

  @Override
  public void dispose() {
    timer.stop();
    super.dispose();
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    setTitle(command.title());
    final String detail = command.detail();
    info.setText(detail.isEmpty() ? " " : detail);
    mem.repaint();
    if(bar != null) bar.setValue((int) (command.progress() * MAX));
  }

  /**
   * Runs the specified commands, decorated by a progress dialog, and
   * calls {@link BaseXDialog#action} if the dialog is closed.
   * @param dialog reference to the dialog window
   * @param cmds commands to be run
   */
  public static void execute(final BaseXDialog dialog, final Command... cmds) {
    execute(dialog, null, cmds);
  }

  /**
   * Runs the specified commands, decorated by a progress dialog, and
   * calls {@link BaseXDialog#action} if the dialog is closed.
   * @param gui reference to the main window
   * @param cmds commands to be run
   */
  public static void execute(final GUI gui, final Command... cmds) {
    execute(gui, null, null, cmds);
  }

  /**
   * Runs the specified commands, decorated by a progress dialog, and
   * calls {@link BaseXDialog#action} if the dialog is closed.
   * @param dialog reference to the calling dialog window
   * @param post post-processing step
   * @param cmds commands to be run
   */
  public static void execute(final BaseXDialog dialog, final Runnable post, final Command... cmds) {
    execute(dialog.gui, dialog, post, cmds);
  }

  /**
   * Runs the specified commands, decorated by a progress dialog, and
   * calls {@link BaseXDialog#action} if the dialog is closed.
   * @param gui reference to the main window
   * @param dialog reference to the dialog window (may be {@code null})
   * @param post post-processing step
   * @param cmds commands to be run
   */
  private static void execute(final GUI gui, final BaseXDialog dialog, final Runnable post,
      final Command... cmds) {

    for(final Command cmd : cmds) {
      // reset views
      final boolean newData = cmd.newData(gui.context);
      if(newData) gui.notify.init();

      // create wait dialog
      final DialogProgress wait = dialog != null ? new DialogProgress(dialog, cmd) :
        new DialogProgress(gui, cmd);

      // start command thread
      new Thread() {
        @Override
        public void run() {
          // execute command
          final Performance perf = new Performance();
          gui.updating = cmd.updating(gui.context);
          boolean ok = true;
          String info;
          try {
            cmd.execute(gui.context);
            info = cmd.info();
          } catch(final BaseXException ex) {
            ok = false;
            info = Util.message(ex);
          } finally {
            gui.updating = false;
          }

          // return status information
          final String time = perf.toString();
          gui.info.setInfo(info, cmd, time, ok, true);
          gui.status.setText(Util.info(TIME_NEEDED_X, time));

          // close progress window and show error if command failed
          wait.dispose();
          if(!ok) BaseXDialog.error(gui, info.equals(INTERRUPTED) ? COMMAND_CANCELED : info);
        }
      }.start();

      // show progress windows until being disposed
      wait.setVisible(true);

      // initialize views if database was closed before
      if(newData) gui.notify.init();
      else if(cmd.updating(gui.context)) gui.notify.update();
    }
    if(dialog != null) dialog.action(dialog);
    if(post != null) post.run();
  }
}

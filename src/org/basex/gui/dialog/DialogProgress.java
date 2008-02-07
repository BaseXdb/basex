package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import org.basex.core.Progress;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;

/**
 * Dialog window for displaying the progress of a process.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogProgress extends Dialog {
  /** Information label. */
  private BaseXLabel info;
  /** Progress bar. */
  private JProgressBar progress;
  /** Dialog title. */
  private String title;
  /** Current progress length. */
  private int ww;
  /** stopped flag. */
  private boolean stopped;

  /**
   * Default Constructor.
   * @param par parent frame
   * @param msg waiting message.
   * @param prg showing a progress bar
   */
  public DialogProgress(final JFrame par, final String msg, final boolean prg) {
    super(par, msg, false);

    info = new BaseXLabel(" ", true);
    set(info, BorderLayout.NORTH);
    
    if(!prg) {
      BaseXLayout.setWidth(info, 600);
    } else {
      ww = 320;
      progress = new JProgressBar(0, ww);
      progress.setPreferredSize(new Dimension(ww, 16));
      set(progress, BorderLayout.CENTER);
    }
    
    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());
    p.setBorder(10, 0, 0, 0);
    p.add(new BaseXButton(BUTTONCANCEL, HELPSTOP, this), BorderLayout.EAST);
    set(p, BorderLayout.SOUTH);
    finish(par);
  }
  
  /**
   * Sets progress information and value.
   * @param proc current process
   */
  public void setProgress(final Progress proc) {
    if(proc == null) return;
    info.setText(proc.detail());

    final String header = proc.title(); 
    if(!header.equals(title)) {
      title = header;
      setTitle(header);
    }
    if(progress != null) {
      final int v = progress.getValue();
      final int nv = (int) (proc.progress() * ww);
      if(v != nv) progress.setValue(nv);
    }
  }

  @Override
  public void cancel() {
    stopped = true;
    dispose();
  }

  /**
   * States if progress has been stopped.
   * @return true if progress has been stopped.
   */
  public boolean stopped() {
    return stopped;
  }
}

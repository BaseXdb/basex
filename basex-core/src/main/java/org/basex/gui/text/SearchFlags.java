package org.basex.gui.text;

/**
 * Search modes: match case, whole word, regular expression, dot matches all.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param mcase match case
 * @param word whole word
 * @param regex regular expression
 * @param dotall dot matches all
 */
public record SearchFlags(boolean mcase, boolean word, boolean regex, boolean dotall) {
}

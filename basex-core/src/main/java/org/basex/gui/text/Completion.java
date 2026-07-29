package org.basex.gui.text;

/**
 * Candidate for the code completion of a text.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param match string to be matched with the input (lower case)
 * @param label string to be displayed
 * @param value string to be inserted, in which an underscore indicates the new cursor position
 * @param alias alternative spelling of another candidate, only matched by its full name
 */
record Completion(String match, String label, String value, boolean alias) { }

INSTALLER ======================================================================

This Installer has to be compiled with the Nullsoft Scriptable Install System.
--> (http://nsis.sourceforge.net)
 
COMPILING INSTALLER ------------------------------------------------------------

Install NSIS and compile the BaseX.nsi-File with NSIS.

NOTES --------------------------------------------------------------------------

The installer automatically gets the basex*.jar from the target folder.
Furthermore it gets the BaseX.ico-File from the images folder.
For each new version the version name in the BaseX.nsi-File and
the jsi.ini-File has to be set.

The installed Service can be found in the Service Directory under the
name 'BaseXServer'. The DBPath and Home Directory of the service are the
Installationfolder.

FILES --------------------------------------------------------------------------

BaseX.nsi: Main installer file with all functions and definitions.
Options.ini: Custom Options panel for Installation options.
Jsl.exe: Java Service Launcher (http://jslwin.sourceforge.net)
Jsl.ini: Configuration file for JSL
.basex: Flag for the right Home-Folder Configuration

--------------------------------------------------------------------------------
 
 Any kind of feedback is welcome; please check out the online documentation at
 http://basex.org/documentation and tell us if you run into any troubles
 while installing and running BaseX: basex-talk@mailman.uni-konstanz.de
 
 BaseX Team, 2010
 
============================================= DBIS Group, University of Konstanz
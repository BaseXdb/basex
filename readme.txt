 
 BASEX ================================================================
 
 Welcome to the source files of BaseX!
 
 To get BaseX running as smooth as possible, please consider the
 following notes:
 
 COMPILING BASEX ------------------------------------------------------
 
 Beside JDK 1.5, JUnit is currently needed to compile the complete
 BaseX sources. If you are using another environment than Eclipse or
 don't have JUnit installed, just delete the 'test' package inside
 the project and rebuild the project.
 
 You can launch the following classes, which are all placed in the
 main package org.basex:
 
 BaseX        : console mode
 BaseXServer  : server instance, waiting for requests
 BaseXClient  : console mode, interacting with the server
 BaseXWin     : graphical user interface
 
 Moreover, try -h to have a look at the available command line options.
 For example, you can use BaseX to process XPath queries without
 entering the console.
 
 USING ECLIPSE --------------------------------------------------------
 
 BaseX is being developed with the Eclipse environment. Some style
 guidelines are integrated in the Subversion/CVS version of BaseX;
 they are being embedded as soon as you open the project.
 
 RUNNING BASEX --------------------------------------------------------
 
 Follow the following steps to start BaseX with Eclipse:
 
 - Press Run -> Run...
 - Create a new "Java Application" launch configuration
 - Select "basex" as "Project"
 - Choose a "Main class" (e.g. org.basex.BaseXWin
   for the graphical user interface)
 - Launch the project via 'Run'
 
 ADDING CHECKSTYLE ----------------------------------------------------
 
 Some additional guidelines are defined via the CheckStyle plugin:
 
 - Open Eclipse
 - Press Help -> Software Updates -> Find and Install
 - Press "Search for new features to install"
 - Press "New Remote Site" and enter 'Eclipse' and the following
   URL http://eclipse-cs.sourceforge.net/update 
 - Follow the installation procedure and restart Eclipse
 
 ----------------------------------------------------------------------
 
 Any kind of feedback is welcome; just tell us if you run into any
 troubles while installing and running BaseX: basex@inf.uni-konstanz.de
 
 Have a nice day,
 BaseX team 2008
 
 =================================== DBIS Group, University of Konstanz

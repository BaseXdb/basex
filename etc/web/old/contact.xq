import module namespace hp="http://www.basex.org/" at "hp.xqm"; 

declare variable $file := "contact";
declare variable $title := "Contact";

declare variable $cont := 
  <div id="main">
  <h1>Contact</h1>
  <p>Feel free to send an email to <code>info@basex.org</code>
  to get more information.<br/>
  You find further information on the project on our <a
  href="http://www.inf.uni-konstanz.de/dbis/research/basex">Research Page</a> and
  on <a href="http://sourceforge.net/projects/basex/">Sourceforge</a>.
  </p>

  <h2>Students @ BaseX</h2>
  <p>
    You can do your bachelor/master project/thesis in the scope of the BaseX
  project. If you are interested or have additional ideas/interests/questions,
  just drop an e-mail or come around.
  </p>
  <p>
    If you like to work with us, maybe it is of interest for you to get some
  internal facts from your fellow students:
  </p> 

  <ul>
    <li>Elmedin Dedovic (B.Sc. Cand.) works on XQuery FS Commands</li>
    <li>Sebastian Faller (M. Sc. Cand.) works on View Schemas, Layout Views</li>
    <li>Sebastian Gath (M. Sc. Cand.) works on fulltext indexes</li>
    <li>Tino Gruse (B.Sc. Cand.) works on Namespaces</li>
    <li>Jörg Hauser (B.Sc. Cand.) works on TreeMap Layouts</li>
    <li>Lukas Kircher (B.Sc. Cand.) has written an e-mail extractor and is currently
    involved in frontend development</li>
    <li>Hannes Schwarz (M. Sc. Cand.) works on BX Filesystem Commands</li>
    <li>Fatih Ulusoy (B.Sc. Cand.) works on Backend (Opt./Indexes)</li>
    <li>Andreas Weiler (M. Sc. Cand.) works on DTD support</li>
  </ul>

  <h2>Alumni</h2>
  <ul>
    <li>Tim Petrowsky (B.Sc.)</li>
    <li>Tobias Pflug (B.Sc.)</li>
  </ul>
  </div> 
;

hp:print($title, $file, $cont)

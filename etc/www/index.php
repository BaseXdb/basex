<?
$title = "Welcome!";
$img = "<a href='frontend.php'><img style='margin-left:25px;float:right' src='gfx/screenshot.png' border='0'/></a>";
include("inc/header.inc");
include("inc/logger.php.inc");
?>
<p>
  BaseX is a native XML database.
  It features compact storage structures, efficient XPath and XQuery implementations
  and a visual frontend, facilitating interactive access to
  the data. BaseX is written in Java and freely available for <a href="download.php">download</a>.
  It is developed by the Database and Information Systems Group at the University of Konstanz.
</p>

<h2>News</h2>
<p class="arrow"><b>BaseX 5.0</b> has been released.
It offers a bunch of new features:</p>
<ul>
 <li> Support of <b>XQuery/XPath Full-Text 1.0</b>,
 based on the W3C Candidate Recommendation.<br/>
 BaseX offers the most complete implementation which is currently available.</li>
 <li> <b>XQuery Conformance</b>: reaching 99.9% of the W3C XQuery Test Suite.</li>
 <li> Implementation of <b>XQuery API for Java (XQJ)</b> and
   <b>XML:DB API (XAPI)</b></li>
 <li> New <b>Scatterplot</b> View, advanced <b>Table View</b></li>
 <li> Improved <b>XQuery Editor</b> with syntax highlighting and error feedback.</li>
 <li> <b>GUI Auto-Completion</b> features for command, XPath and XQuery input.</li>
 <li> <b>Internationalization</b>. Currently supported: English, Japanese, German.</li>
</ul>

<!--
<p class="arrow">Release 4.0 of BaseX offers many features such as:
<ul>
 <li> Support of <b>XQuery 1.0</b>, reaching 99.3% of the
 <a href="http://www.w3.org/XML/Query/test-suite/XQTSReportSimple.html">W3C XQuery Test Suite</a></li>
 <li> Partial support of <b>XQuery Full-Text</b>, based on the
 <a href="http://www.w3.org/TR/xpath-full-text-10/">W3C Working Draft</a></li>
 <li> Several <b>Indexes</b>, including a full-text index<br/>
      (currently only applied by the XPath 1.0 implementation)</li>
 <li> GUI interactions for <b>XML Updates</b></li>
 <li> A <b>Query Panel</b> for entering XPath and XQuery</li>
 <li> A <b>Table View</b> for a flat view of regular XML documents</li>
 <li> A <b>Help View</b> for interactive feedback on the GUI features</li>
 <li> A revised command syntax (try 'help' in the console or the GUI command mode)</li>
</ul>
</p>
-->
<h2>Contact</h2>
<p>
If you are working with BaseX, feel free to drop us a note at <code>info@basex.org</code>.
<br/>
<a href="http://www.ipligence.com/webmaps/s/?u=cc78f4c90ba3a6a733bcd815f08557ad&color=1&a=year"><img src="http://www.ipligence.com/webmaps/m/?u=cc78f4c90ba3a6a733bcd815f08557ad&size=small&color=1&a=year" alt="ip-location" border="0" width="1" height="1"></a>
<a href="https://sourceforge.net/project/stats/detail.php?group_id=192179&ugn=basex&type=prdownload&mode=alltime&package_id=0"><img src="gfx/BaseX.png" border="0" width="1"></a>
</p>

</div>

<? include("inc/footer.inc"); ?>

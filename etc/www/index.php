<?echo '<?xml version="1.0" encoding="ISO-8859-1" ?>';
$webpage= "index.php";
include("inc/header.inc");
include("inc/nav.inc"); 
include("inc/logger.php.inc");
?>

<div id="main">

    <!-- ...... welcome ............................................... -->
    <h1>Welcome to BaseX</h1>
    <p>
        BaseX is a native XML database.
        <br /><br /> 
        It features compact storage structures, efficient XPath and XQuery implementations and a visual <a href="frontend.php">frontend</a>, facilitating visual access to the stored data.  BaseX is written in Java and freely available for <a href="download.php">download</a>.  It is developed by the Database and Information Systems Group at the University of Konstanz. 
    </p>
<p>
Release 4.0 of BaseX offers many new features such as...
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
</div>

<? include("inc/footer.inc"); ?>


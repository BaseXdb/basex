<? include("inc/header.inc"); ?>

<p>
BaseX comes as an executable <code>.jar</code> (Java Archive), which
automatically determines the correct system settings (i.e. paths and
environment variables) for BaseX.
</p>

<p>By default, the graphical interface of BaseX is launched.<br/>
To manually start BaseX you have (at least) the following options:
<blockquote>
<h3>Console Version</h3>
  <code>java -cp BaseX.jar org.basex.BaseX</code><br/>
<h3>GUI Version, granting more memory</h3>
  <code>java -Xmx512m -jar BaseX.jar</code><br/><br/>
</blockquote>

Please use <a href="http://java.sun.com/javase/downloads">Java 5</a>
or later; find more information in the <a href="faq.php">FAQ</a>.
</p>

<? include("inc/footer.inc"); ?>

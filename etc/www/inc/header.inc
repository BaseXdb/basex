<?
$id = preg_replace('/.*\/(.*)\.php/','$1',$_SERVER[SCRIPT_NAME]);
#$basex = "http://phobos101.inf.uni-konstanz.de/basex/";
$basex = "";

$links = array("index", "download", "documentation",
  "frontend", "publications", "contact");

$titles = array(
  "index" => "Home",
  "download" => "Download",
  "documentation" => "Documentation",
  "frontend" => "Visual BaseX",
  "publications" => "Publications",
  "contact" => "Contact"
);

$desc = array(
  "index" => "What's that BaseX thing?",
  "download" => "Get it on your own system!",
  "documentation" => "How can I use it?",
  "frontend" => "Have a look.",
  "publications" => "Reports and presentations about BaseX",
  "contact" => "Guys behind the project"
);

$pages[$top ? $top : $id] = 'selected';
if(!$title) $title = $titles[$top ? $top : $id];

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<!-- Pristine 1.0, Design copyright Matt Dibb 2006, www.mdibb.net -->
<head>
<? print $meta; ?>
  <meta http-equiv="Content-Language" content="en-us"/>
  <meta name="author" content="BaseX Team"/>
  <meta name="description" content="BaseX - Processing and Visualizing XML Data"/>
  <meta name="keywords" content="BaseX,XML,XQuery,Database,Fulltext,Visualization"/>
  <meta name="robots" content="all"/>
  <!--<base href="http://www.inf.uni-konstanz.de/dbis/basex/"/>-->
  <link rel="stylesheet" type="text/css" href="style.css" />
  <link rel="SHORTCUT ICON" href="gfx/BaseX.ico" type="image/ico" />
  <title>BaseX &ndash; <? print $title; ?></title>
</head>
<body <? if($load) print "onLoad='$load'"; ?>>
  <div id="content">
    <div id="header">
      <a href="index.php"><img src="gfx/BaseX.png" alt="BaseX Logo"/></a>
      <p>Processing and Visualizing XML Data<br/>with a native XML Database</p>
    </div>
    <div id="body">
    <div id='links'><p>
<?
foreach($links as $l) {
	print "<span".($pages[$l] ? " class='$pages[$l]'" : "").">";
	print "<a href='$l.php'>$titles[$l]</a></span><br/>\n";
	print "$desc[$l]<br/><br/>\n";
}

?>
    </p></div>
    <div id="main">
<?
  $fn = "inc/".($top ? $top : $id).".inc";
  if(file_exists($fn)) {
		include($fn);
	} else {
    print "$img<h1>$title</h1>\n";
	}

?>


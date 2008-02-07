module namespace hp = "http://www.basex.org/";
declare boundary-space preserve;

declare variable $hp:menu := (
  "Home", "Download", "Documentation", "Visual BaseX", "Publications", "Contact"
);
declare variable $hp:links := (
  "index", "download", "documentation", "frontend", "publications", "contact"
);
declare variable $hp:sub := (
  "What's that BaseX thing?", "Get it on your own system!", "How can I use it?",
  "Have a look.", "Reports and presentations.", "Guys behind the project."
);
declare variable $hp:size := count($hp:menu);

declare variable $hp:selected := attribute class { "selected" };

declare function hp:link($link) {
	concat($link, ".xq")

	(:
	BaseXWebServer: concat($link, ".xq")
	Servlet:        concat("xquery?page=", $link)
	JSP:            concat("xquery.jsp?page=", $link)
	:)
};

(: Prints all. :)
declare function hp:print($title, $file, $cont) {
<html>
<!-- Created with BaseX WebServer. -->
<!--
	Pristine 1.0 (modified by BaseX Team)

	Design copyright Matt Dibb 2006
	www.mdibb.net

	Please feel free to use and modify this template for use on your site.  I
	dont mind if you use it for your personal site or a commercial site, but I do
	insist that it is not sold or given away in some "50,000 Templates!" package
	or something like that.
-->
<head>
	<meta http-equiv="Content-Language" content="en-gb" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="style.css" />
	<link rel="SHORTCUT ICON" href="gfx/BaseX.ico" />
	<title>BaseX - { $title }</title>
</head>
<body>
<div id="content">
<div id="header">
<a href="index.xq"><img src="gfx/BaseX.png" alt="BaseX Logo"/></a>
	<p>Visual exploration and querying of XML data</p>
</div>
<div id="body">
<div id="links">{
for $i in 1 to $hp:size
let $ln := $hp:links[$i]
return <p>{
element span { (if($file = $ln) then ($hp:selected) else (),
<a href="{ hp:link($ln) }">{ $hp:menu[$i] }</a>) } }<br/>
{ $hp:sub[$i] }<br/>
</p>
}
</div>
{ $cont }
</div>
<div id="footer">BaseX WebServer - © 2008 DBIS Group. U Konstanz<br/><br/></div>
</div>
</body>
</html>
};

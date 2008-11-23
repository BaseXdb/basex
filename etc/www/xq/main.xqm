module namespace hp = "http://www.basex.org/";
declare boundary-space preserve;

declare variable $hp:doc := doc('contents')/basex;
declare variable $hp:link := replace(basex:filename(), "\.xq", "");

declare function hp:write() {
  hp:write($hp:doc/page[@link = $hp:link]/text/node())
};

declare function hp:write($text) {
let $pages := $hp:doc/pages/page,
    $page := $pages[@link = $hp:link],
    $menu := data(if($page/@menu) then $page/@menu else $hp:link),
    $title := data(if($page/@alternative) then $page/@alternative
              else $page/@title)
return
<html>
<head>{ $hp:doc/html/header/node() }
    <meta name="date" content="{ current-dateTime() }"/>
    <title>BaseX – { $title }</title>
</head>
<body>
  <div id="content">{ $hp:doc/html/top/node() }
    <div id="body">
      <div id="links">{
        for $p in $pages[@title]
        return <p>{
        element span {
          (if($p/@link = $menu) then attribute class { "selected" } else (),
          <a href="{ data($p/@link) }.xq">{ data($p/@title) }</a>) } }<br/>
          { data($p/@sub) }<br/>
        </p>}
      </div>
      <div id="main">{
        <h1>{ $title }</h1>,
        $text
      }</div>
      { $hp:doc/html/footer/node() }
    </div>
  </div>
</body>
</html>
};

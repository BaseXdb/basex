import module namespace hp="http://www.basex.org/" at "main.xqm";

hp:write((
  for $files in $hp:doc/downloads/files
  return (
    <h2>{ data($files/@desc) }</h2>,
    <p>{ if($files/text) then $files/text/node() else (),
      if($files/file) then
      <table cellspacing='0' cellpadding='0' border='0'>{
        let $path := data($files/@path)
        for $file in $files/file
        return <tr><td width="200">
          <img src="gfx/download.gif"/>
          <a href="{ concat($path, $file/@link) }">{ data($file/@name) }</a>
        </td><td>{ data($file/@desc) }
        </td></tr>
      }</table> else ()
    }</p>
  )
))

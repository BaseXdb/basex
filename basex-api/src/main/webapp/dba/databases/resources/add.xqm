(:~
 : Add resources.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace _ = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';
import module namespace html = 'dba/html' at '../../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'databases';
(:~ Sub category :)
declare variable $_:SUB := 'database';

(:~
 : Form for adding a new resource.
 : @param  $name    entered name
 : @param  $opts    chosen parsing options
 : @param  $path    database path
 : @param  $binary  store as binary
 : @param  $error   error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/add")
  %rest:query-param("name",   "{$name}")
  %rest:query-param("opts",   "{$opts}")
  %rest:query-param("path",   "{$path}")
  %rest:query-param("binary", "{$binary}")
  %rest:query-param("error",  "{$error}")
  %output:method("html")
function _:add(
  $name    as xs:string,
  $opts    as xs:string*,
  $path    as xs:string?,
  $binary  as xs:string?,
  $error   as xs:string?
) as element(html) {
  cons:check(),

  let $opts := if($opts = 'x') then $opts else 'chop'
  return tmpl:wrap(map { 'top': $_:CAT, 'error': $error },
    <tr>
      <td>
        <form action="add" method="post" enctype="multipart/form-data" autocomplete="off">
          <h2>
            <a href="{ $_:CAT }">Databases</a> »
              { html:link($name, $_:SUB, map { 'name': $name }) } »
              { html:button('add', 'Add') }
          </h2>
          <!-- dummy value; prevents reset of options when nothing is selected -->
          <input type="hidden" name="opts" value="x"/>
          <input type="hidden" name="name" value="{ $name }"/>
          <table>
            <tr>
              <td>Input:</td>
              <td>{
                <input type="file" name="file" id="file"/>,
                html:focus('file')
              }</td>
            </tr>
            <tr>
              <td>Database Path:</td>
              <td>
                <input type="text" name="path" value="{ $path }"/>
              </td>
            </tr>
            <tr>
              <td>Binary Storage:</td>
              <td>{ html:checkbox('binary', 'true', $binary = 'true', '') }</td>
            </tr>
            <tr>
              <td colspan="2">{
                <h3>Parsing Options</h3>,
                html:option('intparse', 'Use internal XML parser', $opts),
                html:option('dtd', 'Parse DTDs and entities', $opts),
                html:option('stripns', 'Strip namespaces', $opts),
                html:option('chop', 'Chop whitespaces', $opts),
                html:option('xinclude', 'Use XInclude', $opts)
              }</td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Adds a resource.
 : @param  $name    database
 : @param  $opts    chosen parsing options
 : @param  $path    database path
 : @param  $file    uploaded file
 : @param  $binary  store as binary file
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/add")
  %rest:form-param("name",   "{$name}")
  %rest:form-param("opts",   "{$opts}")
  %rest:form-param("path",   "{$path}")
  %rest:form-param("file",   "{$file}")
  %rest:form-param("binary", "{$binary}")
function _:add-post(
  $name    as xs:string,
  $opts    as xs:string*,
  $path    as xs:string,
  $file    as map(*),
  $binary  as xs:string?
) {
  cons:check(),
  try {
    let $key := map:keys($file)
    let $path := if(not($path) or ends-with($path, '/')) then ($path || $key) else $path
    let $content := $file($key)
    return if(not($key)) then (
      error((), "No input specified.")
    ) else if(util:eval('db:exists($n, $p)', map { 'n': $name, 'p': $path })) then (
      error((), 'Resource already exists: ' || $path || '.')
    ) else (
      if($binary) then (
        util:update('db:store($n, $p, $c)', map { 'n': $name, 'p': $path, 'c': $content })
      ) else (
        let $xml := try {
          convert:binary-to-string($content)
        } catch * {
          error($err:code, replace($err:description, '^.*\): ', ''))
        }
        return util:update('db:add($n, $x, $p)', map { 'n': $name, 'x': $xml, 'p': $path })
      ),
      db:output(web:redirect($_:SUB,
        map { 'name': $name, 'path': $path, 'info': 'Added resource: ' || $name }))
    )
  } catch * {
    db:output(web:redirect("add", map {
      'error': $err:description,
      'name': $name,
      'opts': $opts,
      'path': $path,
      'binary': $binary
    }))
  }
};

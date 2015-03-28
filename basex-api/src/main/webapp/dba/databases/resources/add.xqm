(:~
 : Add resources.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
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
 : @param $name      entered name
 : @param $resource  entered resource
 : @param $binary    store as binary
 : @param $error     error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("dba/add")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("binary",   "{$binary}")
  %rest:query-param("error",    "{$error}")
  %output:method("html")
function _:add(
  $name      as xs:string,
  $resource  as xs:string?,
  $binary    as xs:string?,
  $error     as xs:string?
) as element(html) {
  cons:check(),
  tmpl:wrap(map { 'top': $_:CAT, 'error': $error },
    <tr>
      <td>
        <form action="add" method="post" enctype="multipart/form-data" autocomplete="off">
          <h2>
            <a href="{ $_:CAT }">Databases</a> »
              { html:link($name, $_:SUB, map { 'name': $name }) } »
              { html:button('add', 'Add') }
          </h2>
          <input type="hidden" name="name" value="{ $name }"/>
          <table>
            <tr>
              <td>Resource:</td>
              <td>
                <input type="file" name="file" id="file"/>
                { html:focus('file') }
              </td>
            </tr>
            <tr>
              <td>Path (optional):</td>
              <td>
                <input type="text" name="resource" value="{ $resource }"/>
              </td>
            </tr>
            <tr>
              <td>Binary Storage:</td>
              <td>{ html:checkbox('binary', 'true', $binary = 'true', '') }</td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Adds a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $file      uploaded file
 : @param  $binary    store as binary file
 :)
declare
  %updating
  %rest:POST
  %rest:path("dba/add")
  %rest:form-param("name",     "{$name}")
  %rest:form-param("resource", "{$resource}")
  %rest:form-param("file",     "{$file}")
  %rest:form-param("binary",   "{$binary}")
function _:add-post(
  $name      as xs:string,
  $resource  as xs:string,
  $file      as map(*),
  $binary    as xs:string?
) {
  cons:check(),
  try {
    let $key := map:keys($file)
    let $path := if($resource) then $resource else $key
    let $content := $file($key)
    return if(util:eval('db:exists($n, $p)', map { 'n': $name, 'p': $path })) then (
      error((), 'Resource already exists: ' || $path || '.')
    ) else (
      if($binary) then (
        util:update('db:store($n, $p, $c)', map { 'n': $name, 'p': $path, 'c': $content })
      ) else (
        let $xml := try {
          convert:binary-to-string($content)
        } catch * { error($err:code, replace($err:description, '^.*\): ', '')) }
        return util:update('db:add($n, $x, $p)', map { 'n': $name, 'x': $xml, 'p': $path })
      ),
      db:output(web:redirect($_:SUB,
        map { 'name': $name, 'resource': $path, 'info': 'Added resource: ' || $name }))
    )
  } catch * {
    db:output(web:redirect("add", map { 'name': $name, 'resource': $resource,
      'binary': $binary, 'error': $err:description })
    )
  }
};

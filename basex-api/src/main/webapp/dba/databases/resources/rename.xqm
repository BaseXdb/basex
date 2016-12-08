(:~
 : Rename resource.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';
import module namespace html = 'dba/html' at '../../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../../modules/tmpl.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Form for renaming a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $target    target path
 : @param  $error     error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/rename")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("target",   "{$target}")
  %rest:query-param("error",    "{$error}")
  %output:method("html")
function dba:rename(
  $name      as xs:string,
  $resource  as xs:string,
  $target    as xs:string?,
  $error     as xs:string?
) as element(html) {
  cons:check(),
  tmpl:wrap(map { 'top': $dba:SUB, 'error': $error },
    <tr>
      <td>
        <form action="rename" method="post" autocomplete="off">
          <input type="hidden" name="name" value="{ $name }"/>
          <input type="hidden" name="resource" value="{ $resource }"/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, map { 'name': $name }), ' » ',
            html:link($resource, $dba:SUB, map { 'name': $name, 'resource': $resource }), ' » ',
            html:button('rename', 'Rename')
          }</h2>
          <table>
            <tr>
              <td>New path:</td>
              <td>
                <input type="text" name="target" id="target"
                       value="{ head(($target, $resource)) }"/>
                { html:focus('target') }
                <div class='small'/>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Renames a database resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $target    new name of resource
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/rename")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("target",   "{$target}")
function dba:rename(
  $name      as xs:string,
  $resource  as xs:string,
  $target    as xs:string
) {
  cons:check(),
  try {
    if(db:exists($name, $target)) then (
      error((), 'Resource already exists: ' || $target || '.')
    ) else (
      db:rename($name, $resource, $target),
      cons:redirect($dba:SUB, map {
        'info': 'Resource was renamed.', 'name': $name, 'resource': $target
      })
    )
  } catch * {
    cons:redirect("rename", map {
      'error': $err:description, 'name': $name, 'resource': $resource, 'target': $target
    })
  }
};

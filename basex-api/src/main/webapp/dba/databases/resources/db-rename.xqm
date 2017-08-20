(:~
 : Rename resource.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';
import module namespace html = 'dba/html' at '../../modules/html.xqm';

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
  %rest:path("/dba/db-rename")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("target",   "{$target}")
  %rest:query-param("error",    "{$error}")
  %output:method("html")
function dba:db-rename(
  $name      as xs:string,
  $resource  as xs:string,
  $target    as xs:string?,
  $error     as xs:string?
) as element(html) {
  cons:check(),
  html:wrap(map { 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form action="db-rename" method="post" autocomplete="off">
          <input type="hidden" name="name" value="{ $name }"/>
          <input type="hidden" name="resource" value="{ $resource }"/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, map { 'name': $name }), ' » ',
            html:link($resource, $dba:SUB, map { 'name': $name, 'resource': $resource }), ' » ',
            html:button('db-rename', 'Rename')
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
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/db-rename")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("target",   "{$target}")
function dba:db-rename(
  $name      as xs:string,
  $resource  as xs:string,
  $target    as xs:string
) as empty-sequence() {
  cons:check(),
  try {
    if(db:exists($name, $target)) then (
      error((), 'Resource already exists.')
    ) else (
      db:rename($name, $resource, $target),
      cons:redirect($dba:SUB, map {
        'name': $name, 'resource': $target, 'info': 'Resource was renamed.'
      })
    )
  } catch * {
    cons:redirect('db-rename', map {
      'name': $name, 'resource': $resource, 'target': $target, 'error': $err:description
    })
  }
};

(:~
 : Rename resource.
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
 : Form for renaming a resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $newname   new name of resource
 : @param  $error     error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("dba/rename")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("newname",  "{$newname}")
  %rest:query-param("error",    "{$error}")
  %output:method("html")
function _:rename(
  $name      as xs:string,
  $resource  as xs:string,
  $newname   as xs:string?,
  $error     as xs:string?
) as element(html) {
  cons:check(),
  tmpl:wrap(map { 'top': $_:SUB, 'error': $error },
    <tr>
      <td>
        <form action="rename" method="post" autocomplete="off">
          <input type="hidden" name="name" value="{ $name }"/>
          <input type="hidden" name="resource" value="{ $resource }"/>
          <h2>
            <a href="{ $_:CAT }">Databases</a> »
            { html:link($name, $_:SUB, map { 'name': $name } ) } »
            { html:link($resource, $_:SUB, map { 'name': $name, 'resource': $resource }) } »
            { html:button('rename', 'Rename') }
          </h2>
          <table>
            <tr>
              <td>New name:</td>
              <td>
                <input type="text" name="newname"
                  value="{ ($newname, $resource)[1] }" id="newname"/>
                { html:focus('newname') }
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
 : @param  $newname   new name of resource
 :)
declare
  %updating
  %rest:POST
  %rest:path("dba/rename")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:query-param("newname",  "{$newname}")
function _:rename(
  $name      as xs:string,
  $resource  as xs:string,
  $newname   as xs:string
) {
  cons:check(),
  try {
    if(util:eval('db:exists($n, $m)', map { 'n' : $name, 'm': $newname })) then (
      error((), 'Resource already exists: ' || $newname || '.')
    ) else (
      util:update("db:rename($n, $r, $m)",
        map { 'n': $name, 'r': $resource, 'm': $newname }
      ),
      db:output(web:redirect($_:SUB, map {
        'info': 'Resource was renamed.',
        'name': $name,
        'resource': $newname
      }))
    )
  } catch * {
    db:output(web:redirect("rename", map {
      'error': $err:description,
      'name': $name,
      'resource': $resource,
      'newname': $newname
    }))
  }
};

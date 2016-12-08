(:~
 : Copy database.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Form for copying a database.
 : @param  $name     name of database
 : @param  $newname  new name
 : @param  $error    error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/copy")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("newname", "{$newname}")
  %rest:query-param("error",   "{$error}")
  %output:method("html")
function dba:copy(
  $name     as xs:string,
  $newname  as xs:string?,
  $error    as xs:string?
) as element(html) {
  cons:check(),
  tmpl:wrap(map { 'top': $dba:SUB, 'error': $error },
    <tr>
      <td>
        <form action="copy" method="post" autocomplete="off">
          <input type="hidden" name="name" value="{ $name }"/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, map { 'name': $name }), ' » ',
            html:button('copy', 'Copy')
          }</h2>
          <table>
            <tr>
              <td>New name:</td>
              <td>
                <input type="text" name="newname"
                  value="{ head(($newname, $name)) }" id="newname"/>
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
 : Copies a database.
 : @param  $name     name of database
 : @param  $newname  new name
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/copy")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("newname", "{$newname}")
function dba:copy(
  $name     as xs:string,
  $newname  as xs:string
) {
  cons:check(),
  try {
    if(db:exists($newname)) then (
      error((), 'Database already exists: ' || $newname || '.')
    ) else (
      db:copy($name, $newname)
    ),
    cons:redirect($dba:SUB, map { 'info': 'Database was copied.', 'name': $newname })
  } catch * {
    cons:redirect("copy", map { 'error': $err:description, 'name': $name, 'newname': $newname })
  }
};

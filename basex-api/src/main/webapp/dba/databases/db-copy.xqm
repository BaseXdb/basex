(:~
 : Copy database.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

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
  %rest:path("/dba/db-copy")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("newname", "{$newname}")
  %rest:query-param("error",   "{$error}")
  %output:method("html")
function dba:db-copy(
  $name     as xs:string,
  $newname  as xs:string?,
  $error    as xs:string?
) as element(html) {
  html:wrap(map { 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form action="db-copy" method="post" autocomplete="off">
          <input type="hidden" name="name" value="{ $name }"/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, map { 'name': $name }), ' » ',
            html:button('db-copy', 'Copy')
          }</h2>
          <table>
            <tr>
              <td>New name:</td>
              <td>
                <input type="text" name="newname" value="{ head(($newname, $name)) }" id="newname"/>
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
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/db-copy")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("newname", "{$newname}")
function dba:db-copy(
  $name     as xs:string,
  $newname  as xs:string
) as empty-sequence() {
  try {
    if(db:exists($newname)) then (
      error((), 'Database already exists.')
    ) else (
      db:copy($name, $newname)
    ),
    util:redirect($dba:SUB, map { 'name': $newname, 'info': 'Database was copied.' })
  } catch * {
    util:redirect('db-copy', map { 'name': $name, 'newname': $newname, 'error': $err:description })
  }
};

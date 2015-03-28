(:~
 : Rename database.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util'  at '../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'databases';
(:~ Sub category :)
declare variable $_:SUB := 'database';

(:~
 : Form for renaming a database.
 : @param  $name     name of database
 : @param  $newname  new name
 : @param  $error    error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("dba/alter-db")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("newname", "{$newname}")
  %rest:query-param("error",   "{$error}")
  %output:method("html")
function _:alter(
  $name     as xs:string,
  $newname  as xs:string?,
  $error    as xs:string?
) as element(html) {
  cons:check(),
  tmpl:wrap(map { 'top': $_:SUB, 'error': $error },
    <tr>
      <td>
        <form action="alter-db" method="post" autocomplete="off">
          <input type="hidden" name="name" value="{ $name }"/>
          <h2>
            <a href="{ $_:CAT }">Databases</a> »
            { html:link($name, $_:SUB, map { 'name': $name } ) } »
            { html:button('alter', 'Rename') }
          </h2>
          <table>
            <tr>
              <td>Name:</td>
              <td>
                <input type="text" name="newname"
                  value="{ ($newname, $name)[1] }" id="newname"/>
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
 : Renames a database.
 : @param  $name     name of database
 : @param  $newname  new name
 :)
declare
  %updating
  %rest:POST
  %rest:path("dba/alter-db")
  %rest:query-param("name",    "{$name}")
  %rest:query-param("newname", "{$newname}")
function _:alter(
  $name     as xs:string,
  $newname  as xs:string
) {
  cons:check(),
  try {
    util:update("if(db:exists($newname)) then (
      error((), 'Database already exists: ' || $newname || '.')
    ) else (
      db:alter($name, $newname)
    )", map { 'name': $name, 'newname': $newname }),
    db:output(web:redirect($_:SUB, map {
      'info': 'Database was renamed.',
      'name': $newname
    }))
  } catch * {
    db:output(web:redirect("alter-db", map {
      'error': $err:description,
      'name': $name,
      'newname': $newname
    }))
  }
};

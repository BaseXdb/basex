(:~
 : Copy database.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

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
  %rest:POST
  %rest:path('/dba/db-copy')
  %rest:query-param('name',    '{$name}')
  %rest:query-param('newname', '{$newname}')
  %rest:query-param('error',   '{$error}')
  %output:method('html')
  %output:html-version('5')
function dba:db-copy(
  $name     as xs:string,
  $newname  as xs:string?,
  $error    as xs:string?
) as element(html) {
  html:wrap({ 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form method='post' autocomplete='off'>
          <input type='hidden' name='name' value='{ $name }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, { 'name': $name }), ' » ',
            html:button('db-copy-do', 'Copy')
          }</h2>
          <table>
            <tr>
              <td>New name:</td>
              <td>
                <input type='text' name='newname' value='{ $newname otherwise $name }'
                  autofocus='autofocus'/>
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
  %rest:path('/dba/db-copy-do')
  %rest:query-param('name',    '{$name}')
  %rest:query-param('newname', '{$newname}')
function dba:db-copy-do(
  $name     as xs:string,
  $newname  as xs:string
) as empty-sequence() {
  try {
    if(db:exists($newname)) then (
      error((), 'Database already exists.')
    ) else (
      db:copy($name, $newname)
    ),
    utils:redirect($dba:SUB, { 'name': $newname, 'info': 'Database was copied.' })
  } catch * {
    utils:redirect('db-copy', { 'name': $name, 'newname': $newname, 'error': $err:description })
  }
};

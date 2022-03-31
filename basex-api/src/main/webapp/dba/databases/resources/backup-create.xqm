(:~
 : Create backup.
 :
 : @author Christian Grün, BaseX Team 2005-22, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../../lib/html.xqm';
import module namespace util = 'dba/util' at '../../lib/util.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Form for creating a backup.
 : @param  $name  database
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/backup-create')
  %rest:query-param('name', '{$name}')
  %output:method('html')
function dba:backup-create(
  $name  as xs:string
) as element(html) {
  html:wrap(map { 'header': ($dba:CAT, $name) },
    <tr>
      <td>
        <form action='backup-create' method='post' autocomplete='off'>
          <input type='hidden' name='name' value='{ $name }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, map { 'name': $name }), ' » ',
            html:button('backup-create', 'Create Backup')
          }</h2>
          <table>
            <tr>
              <td>Comment (optional):</td>
              <td>
                <input type='text' name='comment' id='comment' size='64'/>
                { html:focus('comment') }
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
 : Creates a backup.
 : @param  $name     database
 : @param  $comment  comment
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/backup-create')
  %rest:query-param('name',    '{$name}')
  %rest:query-param('comment', '{$comment}')
function dba:db-rename(
  $name     as xs:string,
  $comment  as xs:string
) as empty-sequence() {
  try {
    db:create-backup($name, $comment),
    util:redirect($dba:SUB, map { 'name': $name, 'info': 'Backup was created.' })
  } catch * {
    util:redirect($dba:SUB, map { 'name': $name, 'error': $err:description })
  }
};

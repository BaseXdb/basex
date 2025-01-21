(:~
 : Create backup.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Create backup.
 : @param  $name      database (empty string for general data)
 : @param  $comment   comment
 : @param  $compress  compress files
 : @param  $do        perform update
 : @return form or redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/backup-create')
  %rest:form-param('name',     '{$name}', '')
  %rest:form-param('comment',  '{$comment}')
  %rest:form-param('compress', '{$compress}')
  %rest:form-param('do',       '{$do}')
  %output:method('html')
  %output:html-version('5')
function dba:backup-create(
  $name      as xs:string,
  $comment   as xs:string?,
  $compress  as xs:string?,
  $do        as xs:string?
) {
  html:update($do, { 'header': ($dba:CAT, $name) }, fn() {
    <tr>
      <td>
        <form method='post' autocomplete='off'>
          <input type='hidden' name='do' value='do'/>
          <input type='hidden' name='name' value='{ $name }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            (html:link($name, $dba:SUB, { 'name': $name }), ' » ')[$name],
            html:button('backup-create', 'Create Backup')
          }</h2>
          <table>
            <tr>
              <td>Comment:</td>
              <td>
                <input type='text' name='comment' size='64' placeholder='optional' autofocus=''/>
              </td>
            </tr>
            <tr>
              <td>Compress Files:</td>
              <td>{
                html:checkbox('compress', 'true', true(), '')
              }</td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  }, fn() {
    db:create-backup($name, { 'comment': $comment, 'compress': boolean($compress) }),
    utils:redirect($dba:SUB, { 'name': $name, 'info': 'Backup was created.' })
  })
};

(:~
 : Creates backups.
 : @param  $names  names of databases
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/backups-create')
  %rest:form-param('name', '{$names}')
function dba:backups-create(
  $names  as xs:string*
) {
  try {
    $names ! db:create-backup(.),
    utils:redirect($dba:CAT, { 'info': utils:info($names, 'database', 'backed up') })
  } catch * {
    utils:redirect($dba:CAT, { 'error': $err:description })
  }
};

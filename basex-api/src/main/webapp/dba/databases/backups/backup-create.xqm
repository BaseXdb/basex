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
function dba:backup-create(
  $name      as xs:string,
  $comment   as xs:string?,
  $compress  as xs:string?,
  $do        as xs:string?
) {
  html:update($do, { 'header': ($dba:CAT, $name) }, fn() {
    <div class='panel'>
      <form method='post' autocomplete='off'>
        <input type='hidden' name='do' value='do'/>
        <input type='hidden' name='name' value='{ $name }'/>
        <h2>{
          html:link('Databases', $dba:CAT), ' » ',
          (html:link($name, $dba:SUB, { 'name': $name }), ' » ')[$name],
          html:button('backup-create', 'Create Backup')
        }</h2>
        {
          html:field('Comment:',
            <input type='text' name='comment' size='64' placeholder='optional' autofocus=''/>),
          html:field('Compress Files:', html:checkbox('compress', 'true', true(), ''))
        }
      </form>
    </div>
  }, fn() {
    db:create-backup($name, { 'comment': $comment, 'compress': boolean($compress) }),
    utils:redirect($dba:SUB, { 'name': $name, 'info': 'Backup was created.' })
  })
};

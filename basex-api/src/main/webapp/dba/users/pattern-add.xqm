(:~
 : Add pattern.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';
(:~ Sub category :)
declare variable $dba:SUB := 'user';

(:~
 : Add pattern.
 : @param  $name     username
 : @param  $pattern  entered pattern
 : @param  $perm     chosen permission
 : @param  $do       perform update
 : @return form or redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/pattern-add')
  %rest:form-param('name',    '{$name}')
  %rest:form-param('pattern', '{$pattern}')
  %rest:form-param('perm',    '{$perm}', 'write')
  %rest:form-param('do',      '{$do}')
  %output:method('html')
function dba:pattern-add(
  $name     as xs:string,
  $pattern  as xs:string?,
  $perm     as xs:string,
  $do       as xs:string?
) {
  html:update($do, { 'header': ($dba:CAT, $name) }, fn() {
    <div class='panel'>
      <form method='post' autocomplete='off'>
        <input type='hidden' name='do' value='do'/>
        <input type='hidden' name='name' value='{ $name }'/>
        <h2>{
          html:link('Users', $dba:CAT), ' » ',
          html:link($name, $dba:SUB, { 'name': $name }), ' » ',
          html:button('pattern-add', 'Add Pattern')
        }</h2>
        {
          html:field('Pattern:', (
            <input type='text' name='pattern' value='{ $pattern }' autofocus=''/>, '&#xa0;',
            <span class='note'>…support for <a target='_blank'
              href='https://docs.basex.org/main/Commands#Glob_Syntax'>glob syntax</a>.</span>
          )),
          html:field('Permission:',
            <select name='perm' size='3'>{
              for $p in $config:PERMISSIONS[position() = 1 to 3]
              return element option { attribute selected { }[$p = $perm], $p }
            }</select>)
        }
      </form>
    </div>
  }, fn() {
    user:grant($name, $perm, $pattern),
    utils:redirect($dba:SUB, { 'name': $name, 'info': 'Pattern was created.' })
  })
};

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
  %output:html-version('5')
function dba:pattern-add(
  $name     as xs:string,
  $pattern  as xs:string?,
  $perm     as xs:string,
  $do       as xs:string?
) {
  html:update($do, { 'header': ($dba:CAT, $name) }, fn() {
    <tr>
      <td>
        <form method='post' autocomplete='off'>
          <input type='hidden' name='do' value='do'/>
          <input type='hidden' name='name' value='{ $name }'/>
          <h2>{
            html:link('Users', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, { 'name': $name }), ' » ',
            html:button('pattern-add', 'Add Pattern')
          }</h2>
          <table>
            <tr>
              <td>Pattern:</td>
              <td>
                <input type='text' name='pattern' value='{ $pattern }' autofocus=''/>  
                <span class='note'>…support for <a target='_blank'
                  href='https://docs.basex.org/wiki/Commands#Glob_Syntax'>glob syntax</a>.</span>
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td>Permission:</td>
              <td>
                <select name='perm' size='3'>{
                  for $p in $config:PERMISSIONS[position() = 1 to 3]
                  return element option { attribute selected { }[$p = $perm], $p }
                }</select>
                <div class='small'/>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  }, fn() {
    user:grant($name, $perm, $pattern),
    utils:redirect($dba:SUB, { 'name': $name, 'info': 'Pattern was created.' })
  })
};

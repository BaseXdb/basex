(:~
 : Add new pattern.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
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
 : Form for adding a new pattern.
 : @param  $name     username
 : @param  $pattern  entered pattern
 : @param  $perm     chosen permission
 : @param  $error    error string
 : @return page
 :)
declare
  %rest:GET
  %rest:POST
  %rest:path('/dba/pattern-add')
  %rest:query-param('name',    '{$name}')
  %rest:query-param('pattern', '{$pattern}')
  %rest:query-param('perm',    '{$perm}', 'write')
  %rest:query-param('error',   '{$error}')
  %output:method('html')
  %output:html-version('5')
function dba:pattern-add(
  $name     as xs:string,
  $pattern  as xs:string?,
  $perm     as xs:string,
  $error    as xs:string?
) as element(html) {
  html:wrap({ 'header': ($dba:CAT, $name), 'error': $error },
    <tr>
      <td>
        <form method='post' autocomplete='off'>
          <h2>{
            html:link('Users', $dba:CAT), ' » ',
            html:link($name, $dba:SUB, { 'name': $name }), ' » ',
            html:button('pattern-add-do', 'Add Pattern')
          }</h2>
          <input type='hidden' name='name' value='{ $name }'/>
          <table>
            <tr>
              <td>Pattern:</td>
              <td>
                <input type='text' name='pattern' value='{ $pattern }' autofocus='autofocus'/>  
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
  )
};

(:~
 : Creates a pattern.
 : @param  $name     username
 : @param  $perm     permission
 : @param  $pattern  pattern
 : @return redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/pattern-add-do')
  %rest:query-param('name',    '{$name}')
  %rest:query-param('perm',    '{$perm}')
  %rest:query-param('pattern', '{$pattern}')
function dba:pattern-add-do(
  $name     as xs:string,
  $perm     as xs:string,
  $pattern  as xs:string
) as empty-sequence() {
  try {
    user:grant($name, $perm, $pattern),
    utils:redirect($dba:SUB, { 'name': $name, 'info': 'Pattern was created.' })
  } catch * {
    utils:redirect('pattern-add', {
      'name': $name, 'perm': $perm, 'pattern': $pattern, 'error': $err:description
    })
  }
};

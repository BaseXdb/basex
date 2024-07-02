(:~
 : User page.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/users';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace html = 'dba/html' at '../lib/html.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'users';
(:~ Sub category :)
declare variable $dba:SUB := 'user';

(:~
 : Returns a single user page.
 : @param  $name     username
 : @param  $newname  new name
 : @param  $pw       password
 : @param  $perm     permission
 : @param  $error    error string
 : @param  $info     info string
 : @return page
 :)
declare
  %rest:GET
  %rest:path('/dba/user')
  %rest:query-param('name',     '{$name}')
  %rest:query-param('newname',  '{$newname}')
  %rest:query-param('pw',       '{$pw}')
  %rest:query-param('perm',     '{$perm}')
  %rest:query-param('error',    '{$error}')
  %rest:query-param('info',     '{$info}')
  %output:method('html')
  %output:html-version('5')
function dba:user(
  $name     as xs:string,
  $newname  as xs:string?,
  $pw       as xs:string?,
  $perm     as xs:string?,
  $error    as xs:string?,
  $info     as xs:string?
) as element(html) {
  let $user := user:list-details($name)
  let $admin := $name eq 'admin'
  return html:wrap({ 'header': ($dba:CAT, $name), 'info': $info, 'error': $error },
    <tr>
      <td>
        <form method='post' autocomplete='off'>
          <!--  prevent chrome from auto-completing form -->
          <input style='display:none' type='text' name='fake1'/>
          <input style='display:none' type='password' name='fake2'/>
          <h2>{
            html:link('Users', $dba:CAT), ' » ',
            $name, ' » ',
            html:button('user-update', 'Update')
          }</h2>
          <input type='hidden' name='name' value='{ $name }'/>
          <table>{
            let $admin := $name eq 'admin' return (
              if($admin) then <input type='hidden' name='newname' value='admin'/> else (
                <tr>
                  <td>Name:</td>
                  <td>
                    <input type='text' name='newname'
                      value='{ $newname otherwise $name }' autofocus='autofocus'/>
                    <div class='small'/>
                  </td>
                </tr>
              ),
              <tr>
                <td>Password:</td>
                <td>
                  <input type='password' name='pw' value='{ $pw }' autofocus='autofocus'/>
                  &#xa0;
                  <span class='note'>
                    …only changed if a new one is entered<br/>
                  </span>
                  <div class='small'/>
                </td>
              </tr>,
              if($admin) then <input type='hidden' name='perm' value='admin'/> else (
                <tr>
                  <td>Permission:</td>
                  <td>
                    <select name='perm' size='5'>{
                      let $prm := $perm otherwise $user/@permission
                      for $p in $config:PERMISSIONS
                      return element option { if($p = $prm) then attribute selected { }, $p }
                    }</select>
                    <div class='small'/>
                  </td>
                </tr>
              ),
              <tr>
                <td>Information:</td>
                <td>
                  <textarea name='info' id='editor' spellcheck='false'>{
                    serialize(user:info($name), { 'indent': true() } )
                  }</textarea>
                </td>
              </tr>,
              html:js('loadCodeMirror("xml", true);')
            )
          }</table>
        </form>
      </td>
      <td class='vertical'/>
      <td>{
        if($admin) then () else <_>
          <h3>Local Permissions</h3>
          <form method='post'>
            <input type='hidden' name='name' value='{ $name }' id='name'/>
            <div class='small'/>
            {
              let $headers := (
                { 'key': 'pattern', 'label': 'Pattern' },
                { 'key': 'permission', 'label': 'Local Permission' }
              )
              let $entries := $user/database ! {
                'pattern': @pattern,
                'permission': @permission
              }
              let $buttons := if($admin) then () else (
                html:button('pattern-add', 'Add…'),
                html:button('pattern-drop', 'Drop', ('CHECK', 'CONFIRM'))
              )
              return html:table($headers, $entries, $buttons)
            }
          </form>
          <div class='note'>
            A global permission can be overwritten by a local permission.<br/>
            Local permissions are applied to those databases that match<br/>
            a specified pattern. The pattern is based on the <a target='_blank'
              href='https://docs.basex.org/wiki/Commands#Glob_Syntax'>glob syntax</a>.<br/>
          </div>
        </_>/node()
      }</td>
    </tr>
  )
};

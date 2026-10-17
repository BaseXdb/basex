import module namespace mail = 'http://basex.org/modules/mail';

mail:send({
  (: authentication :)
  'host': 'smtp.your-mail-provider.org',
  'ssl': true(),
  'user': 'from@your-mail-provider.org',
  'password': '.......',
  (: mail contents :)
  'from': 'from@your-mail-provider.org',
  'to': 'to@your-mail-recipient.org',
  'subject': 'Subject',
  'message': 'Message'
})

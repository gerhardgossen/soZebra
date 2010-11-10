SELECT *
FROM bodies
NATURAL LEFT JOIN
  (SELECT messageid, group_concat(email, "|") AS to_email, group_concat(name, "|") AS to_name
  FROM recipients NATURAL JOIN people
  WHERE reciptype = 'to'
  GROUP BY messageid) AS `to`
NATURAL LEFT JOIN
  (SELECT messageid, group_concat(email, "|") AS cc_email, group_concat(name, "|") AS cc_name
  FROM recipients NATURAL JOIN people
  WHERE reciptype = 'cc'
  GROUP BY messageid) AS cc
NATURAL LEFT JOIN
  (SELECT messageid, group_concat(email, "|") AS bcc_email, group_concat(name, "|") AS bcc_name
  FROM recipients NATURAL JOIN people
  WHERE reciptype = 'bcc'
  GROUP BY messageid) AS bcc
NATURAL LEFT JOIN
  (SELECT messageid, headervalue as sender, name AS sendername
  FROM headers LEFT JOIN people ON (headervalue = email)
  WHERE headername = 'From') AS sender
NATURAL LEFT JOIN
  (SELECT messageid, headervalue as subject
  FROM headers
  WHERE headername = 'Subject') AS subject

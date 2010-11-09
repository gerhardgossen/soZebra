CREATE OR REPLACE VIEW messages AS SELECT *
FROM bodies
NATURAL LEFT JOIN 
  (SELECT messageid, array_agg(email) AS to_email, array_agg(name) AS to_name
  FROM recipients NATURAL JOIN people 
  WHERE reciptype = 'to'
  GROUP BY messageid) AS "to"
NATURAL LEFT JOIN
  (SELECT messageid, array_agg(email) AS cc_email, array_agg(name) AS cc_name
  FROM recipients NATURAL JOIN people 
  WHERE reciptype = 'cc'
  GROUP BY messageid) AS cc
NATURAL LEFT JOIN
  (SELECT messageid, array_agg(email) AS bcc_email, array_agg(name) AS bcc_name
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

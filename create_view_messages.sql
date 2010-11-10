CREATE OR REPLACE VIEW complete_messages AS
SELECT messageid, body, subject, p1.email as sender_email, p1.name as sender_name, to_email, to_name, cc_email, cc_name, bcc_email, bcc_name
FROM
  messages
NATURAL LEFT JOIN
  bodies
JOIN
  people p1 ON (senderid = personid)
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



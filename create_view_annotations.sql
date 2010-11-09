CREATE VIEW message_annotations AS
SELECT messageid, array_agg(annvalue) AS values, array_agg(lineorder) AS linesnumbers
FROM zoneannotations NATURAL JOIN zonelines
WHERE errorid = 'n'
GROUP BY messageid
package de.ovgu.findke.gossen.sozebra

import cc.mallet.types._
import scala.collection.JavaConversions._

class MessageIterator(withLabels: Boolean) extends Iterator[Instance] {
    // workaroud for missing ResultSet#hasNext
    private var didNext = false
    private var hasNextVal = false
    protected val cursor = getCursor(withLabels)

    private def getCursor(withLabels: Boolean) = {
        import java.sql._

        Class.forName("org.postgresql.Driver");
        val con = DriverManager.getConnection( "jdbc:postgresql:zonerelease", "enron", "enron");
        con.setAutoCommit(false);

        val stmt = con.createStatement();
        stmt.setFetchSize(50);
        val rs = stmt.executeQuery("""
                SELECT messageid, body, to_email, to_name, cc_email, cc_name,
                        bcc_email, bcc_name, sender_email, sender_name, subject
            """ + (if (withLabels) ", values, linenumbers" else "") +
            """
                FROM complete_messages
            """ + (if (withLabels) " NATURAL RIGHT JOIN message_annotations" else "")
            );
        rs
    }

    private def getParticipants (cursor: java.sql.ResultSet, field: String): List[Participant] = {
        val names     = sqlArrayToList[String](cursor.getArray(field + "_name" ))
        val addresses = sqlArrayToList[String](cursor.getArray(field + "_email"))
        (names zip addresses) map { case (n, a) => new Participant(name = n, address = a) }
    }

    private def sqlArrayToList[T] (array: java.sql.Array): List[T] = {
        array.getArray.asInstanceOf[Array[T]] toList
    }

    private val labelAlphabet = new LabelAlphabet

    def hasNext: Boolean = {
        if (!didNext) {
            hasNextVal = cursor.next();
            didNext = true;
        }
        return hasNextVal;
    }

    def next: Instance = {
        if (!didNext) {
            cursor.next();
        }
        val source = new Message(
            subject = cursor.getString("subject"),
            body    = cursor.getString("body"),
            sender  = new Participant(
                name    = cursor.getString("sender_name"),
                address = cursor.getString("sender_email")
            ),
            recipients  = getParticipants(cursor, "to") ++
                getParticipants(cursor, "cc") ++ getParticipants(cursor, "bcc")
        )
        val lineTokenSequence = new TokenSequence()
        lineTokenSequence.addAll(source.lines map { new Token(_) })
        val data = new FeatureVectorSequence(new Alphabet, lineTokenSequence) // attributes
        val target = if (withLabels) {
            val values = sqlArrayToList[String](cursor.getArray("values"))
            val lines  = sqlArrayToList[String](cursor.getArray("linenumbers"))
            val labels = new Array[Label](lineTokenSequence.size)
            (lines zip values) foreach { case (line, value) =>
                labels(Integer.parseInt(line)) = labelAlphabet.lookupLabel(value, true)
            }
            new LabelSequence(labels)
        } else
            null // class label, TODO
        val name = cursor.getString("messageid")

        didNext = false;
        return new Instance(data, target, name, source)
    }
}

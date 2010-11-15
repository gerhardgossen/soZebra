package de.ovgu.findke.gossen.sozebra

import cc.mallet.types._
import scala.collection.JavaConversions._
import scala.collection.mutable.{ArrayBuffer,Buffer}

class MessageIterator(withLabels: Boolean) extends java.util.Iterator[Instance] {
    // workaroud for missing ResultSet#hasNext
    private var didNext = false
    private var hasNextVal = false
    protected val (connection, cursor) = getCursor(withLabels)

    private def getCursor(withLabels: Boolean) = {
        import java.sql._

        Class.forName("org.postgresql.Driver");
        val con = DriverManager.getConnection( "jdbc:postgresql:zonerelease", "enron", "enron");
        con.setAutoCommit(false);

        val stmt = con.createStatement();
        stmt.setFetchSize(10);
        val rs = stmt.executeQuery("""
                SELECT messageid, body, to_email, to_name, cc_email, cc_name,
                        bcc_email, bcc_name, sender_email, sender_name, subject
            """ + (if (withLabels) ", values, linenumbers" else "") +
            """
                FROM complete_messages
            """ + (if (withLabels) " NATURAL RIGHT JOIN message_annotations" else "")
            );
        (con, rs)
    }

    private def getParticipants (cursor: java.sql.ResultSet, field: String): List[Participant] = {
        val names     = sqlArrayToList[String](cursor.getArray(field + "_name" ))
        val addresses = sqlArrayToList[String](cursor.getArray(field + "_email"))
        (names zip addresses) map { case (n, a) => new Participant(name = n, address = a) }
    }

    private def sqlArrayToList[T] (array: java.sql.Array): List[T] = {
        array match {
            case null  => List[T]()
            case array => array.getArray.asInstanceOf[Array[T]] toList
        }
    }

    val labelAlphabet = new LabelAlphabet
    private val featureAlphabet = new Alphabet

    def hasNext: Boolean = {
        if (!didNext) {
            hasNextVal = cursor.next();
            didNext = true;
        }
        if (!hasNextVal && cursor != null) {
            cursor.close
            connection.close
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
        val data = new FeatureVectorSequence(featureAlphabet, lineTokenSequence, false, true, true) // attributes
        val target = if (withLabels) {
            val values = sqlArrayToList[Int](cursor.getArray("values"))
            val lines  = sqlArrayToList[Int](cursor.getArray("linenumbers"))
            val labels: Buffer[Label] = ArrayBuffer.fill(1 + lines.max)(labelAlphabet.lookupLabel(0, true))
            (lines zip values) foreach { case (line, value) =>
                labels(line.intValue) = labelAlphabet.lookupLabel(value, true)
            }
            new LabelSequence(labels toArray)
        } else
            null // class label, TODO
        val name = cursor.getString("messageid")

        didNext = false;
        return new Instance(data, target, name, source)
    }

    def remove = throw new UnsupportedOperationException
}

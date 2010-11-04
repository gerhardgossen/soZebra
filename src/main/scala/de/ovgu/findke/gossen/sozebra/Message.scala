package de.ovgu.findke.gossen.sozebra

import javax.mail.{Address, Session}
import javax.mail.internet.{MimeMessage, InternetAddress}
import java.io.ByteArrayInputStream
import scala.io.Source
import cc.mallet.types.TokenSequence

class Message(header: String, body: String) {
    val message = {
        val inputString = header + "\r\n\r\n" + body
        val ioStream = new ByteArrayInputStream(inputString.getBytes("UTF-8"))
        new MimeMessage(Session.getInstance(null), ioStream)
    }

    private def toInternetAddress(address: Address) =
        address match {
            case a1:InternetAddress => a1
            case _ => throw new RuntimeException("Unexpected address: " + address)
        }

    private val lexer = new cc.mallet.util.CharSequenceLexer
    private def tokenize(string: String): TokenSequence = {
        import cc.mallet.extract.{StringSpan, StringTokenization}
        lexer.setCharSequence (string);
        val ts = new StringTokenization (string);
        while (lexer.hasNext()) {
            lexer.next();
            ts.add (new StringSpan (string, lexer.getStartOffset (), lexer.getEndOffset ()));
        }
        ts
    }

    lazy val subject = message getSubject
    lazy val lines: List[String] = Source.fromInputStream(message getInputStream).getLines toList
    lazy val tokenizedLines: List[TokenSequence] = lines.map { tokenize(_) }
    lazy val sender = new Participant( toInternetAddress( message.getFrom()(0) ) )
    lazy val recipients: List[Participant] = {
        val recipients = message.getAllRecipients
        recipients.toList.map { r => new Participant(toInternetAddress(r)) }
    }
}

case class Participant(val name: String, val address: String) {
    def this (address: InternetAddress) = this(address.getPersonal, address.getAddress)
}

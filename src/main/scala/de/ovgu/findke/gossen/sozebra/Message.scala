package de.ovgu.findke.gossen.sozebra

import cc.mallet.types.TokenSequence

class Message(val subject: String, body: String, val sender: Participant, val recipients: List[Participant]) {
    private val lexer = new cc.mallet.util.CharSequenceLexer
    private def tokenize(string: String): TokenSequence = {
        import cc.mallet.extract.{StringSpan, StringTokenization}
        lexer.setCharSequence (string);
        val ts = new StringTokenization (string);
        while (lexer.hasNext) {
            lexer.next;
            ts.add (new StringSpan (string, lexer.getStartOffset, lexer.getEndOffset));
        }
        ts
    }

    lazy val lines: List[String] = body.split("\\r?\\n") toList
    lazy val tokenizedLines: List[TokenSequence] = lines.map { tokenize(_) }
}

case class Participant(val name: String, val address: String)

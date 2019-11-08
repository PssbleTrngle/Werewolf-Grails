package werwolf

class Message {

    String text

    static belongsTo = [ sender: User ]

    static constraints = {
        sender nullable: true
    }
}

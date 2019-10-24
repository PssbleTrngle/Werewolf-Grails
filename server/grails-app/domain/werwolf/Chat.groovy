package werwolf

class Chat {

    String name

    static belongsTo = User
    static hasMany = [ messages: Message, users: User ]

    static constraints = {
        name nullable: true
    }
}

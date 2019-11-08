package werwolf

class Chat {

    int id
    String name

    static belongsTo = User
    static hasMany = [ messages: Message, users: User ]

    static constraints = {
        name nullable: true
    }
}

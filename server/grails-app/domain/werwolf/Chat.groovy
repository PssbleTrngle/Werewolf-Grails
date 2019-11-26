package werwolf

/**
*   A chat between two to many users.
*   Created at the start of a game to enable communication between
*   all participating users
*/
class Chat {

    int id
    String name

    static belongsTo = User
    static hasMany = [ messages: Message, users: User ]

    static constraints = {
        name nullable: true
    }
}

package werwolf

/**
*   A logged in decision of a specific user on a screen
*   Mostly used for voting (ex.: who to lynch)
*
*   If voted for a user, they are referenced as 'target'
*   else, if voted for an option (ex.: 'Ready') it is saved as 'selection'
*/
class Decision {

    int id
    String selection

    static belongsTo = [ user: User, target: User, screen: Screen ]

    static constraints = {
        target nullable: true
        selection nullable: true
    }
}

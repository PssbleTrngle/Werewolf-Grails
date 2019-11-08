package werwolf

class Decision {

    int id
    String selection

    static belongsTo = [ user: User, target: User, screen: Screen ]

    static constraints = {
        target nullable: true
        selection nullable: true
    }
}

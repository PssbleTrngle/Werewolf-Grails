package werwolf

import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

class User {

    int id
    String name
    String token
    boolean dead = false

    Role unwrapRole() {
        (Role) GrailsHibernateUtil.unwrapIfProxy(getRole())
    }

    static belongsTo = [ game: Game, role: Role, screen: Vote ]
    static hasMany = [ chats: Chat ]

    static constraints = {
        game nullable: true
        role nullable: true
        screen nullable: true
        token unique: true
    }

    static mapping = {
        sort: 'id'
    }

    void kill() {
        this.setNextAction(Action.get('dead'))
        Role role = GrailsHibernateUtil.unwrapIfProxy(this.getRole())
        this.setDead(!role.onDeath(this))
    }

    void setNextAction(Action next) {
        assert next != null

        withTransaction({
            Vote nextScreen = new Vote(action: next.name, game: this.game)
            next.getVoters(this.game.users, this).each({ other ->
                if (other.screen?.action == next.name)
                    nextScreen = other.screen
            })

            this.setProperty('screen', nextScreen.save())
            this.save()
        })
    }

}

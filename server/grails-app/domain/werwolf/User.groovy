package werwolf

import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import werwolf.screen.ScreenDead

class User {

    int id
    String name
    String token
    boolean dead = false

    Role unwrapRole() {
        (Role) GrailsHibernateUtil.unwrapIfProxy(getRole())
    }

    Screen unwrapScreen() {
        (Screen) GrailsHibernateUtil.unwrapIfProxy(getScreen())
    }

    static belongsTo = [game: Game, role: Role, screen: Screen ]
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
        this.setNextScreen(new ScreenDead())
        Role role = (Role) GrailsHibernateUtil.unwrapIfProxy(this.getRole())
        this.setDead(!role.onDeath(this))
    }

    void setNextScreen(Screen next) {
        assert next != null

        withTransaction({
            next.getVoters(this.game.users, this).each({ other ->
                if (other.unwrapScreen()?.getKey() == next.getKey())
                    next = other.unwrapScreen()
            })
            next.setGame(this.getGame())
            next.save()

            this.setScreen(next)
            this.save()
        })
    }

}

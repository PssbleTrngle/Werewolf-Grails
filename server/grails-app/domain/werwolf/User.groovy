package werwolf

import javax.script.ScriptEngine

class User{

    int id
    String name
    String token
    boolean dead = false

    static belongsTo = [ game: Game, role: Role, screen: Vote ]
    static hasMany = [ chats: Chat ]

    static constraints = {
        game nullable: true
        role nullable: true
        screen nullable: true
    }

    static mapping = {
        sort 'id'
    }

    void setDead(boolean dead) {
        if(dead) withTransaction({
            setNextAction(Action.get(role.deathAction ?: 'dead'))
            save()
        })

        this.dead = dead;
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

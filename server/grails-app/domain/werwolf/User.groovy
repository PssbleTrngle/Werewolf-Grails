package werwolf

import javax.script.ScriptEngine
import javax.swing.Action

class User{

    int id
    String name
    String token
    boolean dead = false

    static belongsTo = [ game: Game, role: Role, screen: Vote ]

    static constraints = {
        game nullable: true
        role nullable: true
        screen nullable: true
    }

    static mapping = {
        sort 'id'
    }

    void setDead(boolean dead) {
        if(dead) User.withTransaction({
            Vote screen = new Vote(action: 'dead', game: game).save()
            setProperty('screen', screen)
            save()
        })

        super.setDead(dead)
    }

}

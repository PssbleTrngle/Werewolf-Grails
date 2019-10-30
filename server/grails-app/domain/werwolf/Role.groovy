package werwolf

import grails.rest.Resource

abstract class Role {

    /** *
     * @return boolean: true if death should be canceled
     */
    boolean onDeath(User user) { false }

    int id
    String name
    String nightAction

    abstract boolean triggerWin(Game game);
    boolean hasWon(Game game) { triggerWin(game) }

    static constraints = {
        nightAction nullable: true
    }
}

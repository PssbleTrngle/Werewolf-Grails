package werwolf

import grails.rest.Resource

class Role {

    /** *
     * @return boolean: true if death should be canceled
     */
    boolean onDeath(User user) { false }

    int id
    String name
    String nightAction

    static hasMany = [ user: User ]

    static constraints = {
        nightAction nullable: true
    }
}

package werwolf

import grails.rest.Resource

class Role {

    int id
    String name
    String nightAction
    String deathAction

    static hasMany = [ user: User ]

    static constraints = {
        nightAction nullable: true
        deathAction nullable: true
    }
}

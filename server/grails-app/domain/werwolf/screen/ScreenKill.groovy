package werwolf.screen

import org.grails.datastore.gorm.GormEntity
import werwolf.User
import werwolf.Screen

abstract class ScreenKill extends Screen {

    boolean skippable() { false }

    @Override
    void run(Set<User> users, def selection) {
        if(selection instanceof GormEntity && selection.instanceOf(User.class))
            ((User) selection).kill()
    }

    @Override
    String[] options() {
        return this.skippable() ? ['skip'] : new String[0]
    }
}

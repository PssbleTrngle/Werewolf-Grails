package werwolf.role


import werwolf.User
import werwolf.Screen
import werwolf.screen.ScreenHunt

class RoleHunter extends RoleVillager {

    @Override
    boolean onDeath(User user) {
        println "Before: ${Screen.count()}"
        user.setNextScreen(new ScreenHunt())
        println "After: ${Screen.count()}"
        false
    }

}

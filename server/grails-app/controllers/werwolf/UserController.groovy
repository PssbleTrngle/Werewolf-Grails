package werwolf

class UserController {
	static responseFormats = ['json', 'xml']
    static allowedMethods = [index: 'GET', save: 'POST']

    /**
    *   Creates a game with 4 dummy accounts for debug purposes
    */
    static createDevGame(User user) {
        User.withTransaction({

            List<User> bots = [ user ]
            def token = System.currentTimeMillis()
            for(i in 1..4)
                bots.add(new User('name': "User $i", 'token': "$i:$token").save())

            Game.withTransaction {
                println "Creating Game for User $user.token"
                Game.createGame(bots)
            }

        })
    }

    /**
    *   Returns the user or creates a new one
    *   @param token the generated token
    *   @returns If found, the user model
    */
    static User getUser(String token) {

        User existing = User.findByToken(token)
        if (existing) return existing

        User.withTransaction({
            new User('name': token, 'token': token).save()
        })

        null
    }

    /**
    *   Returns the user as a JSON response
    */
    def index() {
        String token = params['token']
        assert token != null

        [user: getUser(token)]
    }

}

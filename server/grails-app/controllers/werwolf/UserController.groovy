package werwolf

class UserController {
	static responseFormats = ['json', 'xml']
    static allowedMethods = [index: 'GET', save: 'POST']

    static void setUser(User user) {
        assert user != null
        println "Set user id to $user.id"
    }

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

    static User getUser(String token) {

        User existing = User.findByToken(token)
        if (existing) return existing

        User.withTransaction({
            new User('name': token, 'token': token).save()
        })

        null
    }

    def save() {
        def json = request.getJSON()
        User user = User.findByName(json['user'])
        [user: user]
    }

    def index() {
        String token = params['token']
        assert token != null

        [user: getUser(token)]
    }

}

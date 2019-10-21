package werwolf

class ActionController {
	static responseFormats = ['json', 'xml']
    static allowedMethods = [save: 'POST']

    def save() {
        def json = request.getJSON()
        String token = json['token']
        String actionName = json['action'].toString()
        Action action = Action.get(actionName)
        User target = User.get(json['target'])
        String option = json['option']

        assert action != null
        assert token != null

        User user = UserController.getUser(token)
        assert user?.game instanceof Game

        Vote screen = user?.screen
        assert screen != null

        /* Check if action is valid for vote */
        boolean valid =
                (target != null && action.getTargets(user.game.users, user).find({User t -> t.id == target.id})) ||
                (option != null && action.options.contains(option))
        assert valid

        /* Check if user has already voted */
        Decision existing = Decision.find({ d ->
            d.user.id == user.id && d.screen.id == screen.id
        })
        assert !existing

        Decision.withTransaction({

            new Decision(selection: option, user: user, target: target, screen: screen).save()
            Vote.withTransaction({
                screen.save()
                screen.checkDone()
                user.game.checkDone()
            })

        })

        [ success: true ]
    }

}

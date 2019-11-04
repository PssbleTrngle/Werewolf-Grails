package werwolf.screen

class ScreenDead extends ScreenStatic {

    ScreenDead() {
        super('You are dead')
    }

    @Override
    String getKey() {
        return 'dead'
    }

}

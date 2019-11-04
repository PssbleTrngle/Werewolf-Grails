package werwolf.screen

class ScreenReady extends ScreenStatic {

    ScreenReady() {
        super('Are you ready?')
    }

    @Override
    String getKey() {
        return 'ready'
    }

}

package ba.adnan.simplemonriandroidsdkapp;

class Config {
    private Config(){}

    //TODO: replace with your merchant's authenticity token
    public static String authenticityToken() {
        return "6a13d79bde8da9320e88923cb3472fb638619ccb";
    }

    public static boolean isDevelopmentMode(){
        return true;
    }
}

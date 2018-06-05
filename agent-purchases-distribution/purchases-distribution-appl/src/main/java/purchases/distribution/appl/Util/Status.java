package purchases.distribution.appl.Util;


public enum Status{
    PLAIN("plain point"),
    DELIVER("deliver"),
    GET("get order"),
    MAIN("agents daily point"),
    CURRENT("is a current point");

    private String description;
    Status(String description){
        this.description = description;
    }
}

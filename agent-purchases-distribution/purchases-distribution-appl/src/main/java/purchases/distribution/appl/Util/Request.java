package purchases.distribution.appl.Util;

import jade.core.AID;

public class Request {
    public AID partner;
    public String address;
    public String convId;
    public double price = 0;

    public Request(AID partner, String address, String convId, double price){
        this.partner = partner;
        this.address = address;
        this.convId  = convId;
        this.price = price;
    }
}

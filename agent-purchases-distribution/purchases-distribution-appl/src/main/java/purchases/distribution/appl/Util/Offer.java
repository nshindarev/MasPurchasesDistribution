package purchases.distribution.appl.Util;

import jade.core.AID;

public class Offer {
    public AID partner;
    public double price;
    public String convId;

    public Offer(AID partner, double price, String convId){
        this.partner = partner;
        this.price = price;
        this.convId  = convId;
    }
}

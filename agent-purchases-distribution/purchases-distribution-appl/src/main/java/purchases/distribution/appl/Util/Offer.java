package purchases.distribution.appl.Util;

import java.util.ArrayList;
import jade.core.AID;

public class Offer {
    public AID partner;
    public double price;
    public String convId;
    public ArrayList<String> supply_chain;

    public Offer(AID partner, double price, String convId, ArrayList<String> supply_chain){
        this.partner = partner;
        this.price = price;
        this.convId  = convId;
        this.supply_chain = supply_chain;
        System.out.println("offer: " + supply_chain.toString());
    }
}

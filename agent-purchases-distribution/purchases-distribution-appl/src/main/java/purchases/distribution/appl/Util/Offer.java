package purchases.distribution.appl.Util;

public class Offer {

    public double payment = 0;
    public String newNode;


    public Offer(String node, double price){
        this.newNode = node;
        this.payment = price;
    }
}

package purchases.distribution.appl.Util;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

class Node {
    public final String name;
    public final int type;

    public static final int PICK = 1;
    public static final int DROP = 2;
    public static final int MAIN = 3;

    public Node(String name, int type){
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString(){
        return name;
    }
}

public class Route {
    private List<String> mainPoints;
    private String       pickPoint;
    private HashSet<String>  dropPoints;

    private ArrayList<Node> points;

    public Route(List<String> mainPoints, String pickPoint, HashSet<String> dropPoints){
        assert (pickPoint == null) == dropPoints.isEmpty();
        this.mainPoints = mainPoints;
        this.pickPoint  = pickPoint;
        this.dropPoints = new HashSet<String>(dropPoints);
        points = new ArrayList<Node>();
        generateNearest();
    }

    private static double dist(Node a, Node b){
        return DataPool.getShortestPaths().shortestDistance(a.name, b.name);
    }

    private void generateNearest(){
        int current_main = 0;

        boolean addedPick = pickPoint == null;

        ArrayList<Node> mains = new ArrayList<Node>();
        for(String name : mainPoints)
            mains.add(new Node(name, Node.MAIN));

        if(addedPick){
            points = mains;
            return;
        }

        HashSet<Node> drops = new HashSet<Node>();
        for(String name : dropPoints)
            drops.add(new Node(name, Node.DROP));

        Node pick = new Node(pickPoint, Node.PICK);

        points.add(mains.get(current_main++));
        Node prev = mains.get(0);
        while(current_main != mainPoints.size() - 1){
            Node next_main = mains.get(current_main);
            Node current = next_main;
            if(addedPick){
                double min = dist(prev, next_main);
                for(Node node : drops){
                    double distance = dist(prev, node);
                    if(distance < min){
                        current = node;
                        min = distance;
                    }
                }
            } else {
                double distance1 = dist(prev, next_main);
                double distance2 = dist(prev, pick);
                current = distance1 < distance2 ? next_main : pick;
            }
            points.add(current);
            prev = current;
            if(current.type == Node.DROP) drops.remove(current);
            else if(current.type == Node.PICK) addedPick = true;
            else current_main++;
        }
        if(!addedPick) points.add(pick);
        for(Node node : drops)
            points.add(node);
        points.add(mains.get(current_main++));
    }

    public double length(){
        double total = 0;
        for(int i = 1; i < points.size(); i++)
            total += dist(points.get(i - 1), points.get(i));
        return total;
    }

    public Route addDropPoint(String name){
        if(dropPoints.contains(name)) return this;
        dropPoints.add(name);
        Route newRoute = new Route(mainPoints, dropPoints.size() == 1 ? DataPool.getStorageName() : pickPoint, dropPoints);
        dropPoints.remove(name);
        return newRoute;
    }

    public Route removeDropPoint(String name){
        if(!dropPoints.contains(name)) return this;
        dropPoints.remove(name);
        Route newRoute = new Route(mainPoints, dropPoints.isEmpty() ? null : pickPoint, dropPoints);
        dropPoints.add(name);
        return newRoute;
    }

    public Route changePickPoint(String name){
        return new Route(mainPoints, name, dropPoints);
    }

    @Override
    public String toString(){
        return points.toString();
    }
}

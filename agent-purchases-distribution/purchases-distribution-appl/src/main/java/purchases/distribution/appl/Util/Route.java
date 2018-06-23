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
        char t = 'x';
        switch(type){
        case PICK: t = 'P'; break;
        case DROP: t = 'D'; break;
        case MAIN: t = 'M'; break;
        }
        return name + t;
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
        //generateNearest();
        generateOptimal();
    }

    private Route(ArrayList<Node> nodes){
        this.points = nodes;
        mainPoints = new ArrayList<String>();
        dropPoints = new HashSet<String>();
        for(Node n : nodes){
            switch(n.type){
            case Node.PICK:
                pickPoint = n.name;
                break;
            case Node.DROP:
                dropPoints.add(n.name);
                break;
            case Node.MAIN:
                mainPoints.add(n.name);
            }
        }
    }

    private static double dist(Node a, Node b){
        return DataPool.getShortestPaths().shortestDistance(a.name, b.name);
    }

    private ArrayList<Node> optimalRoute(List<Node> current, List<Node> mains, Node pick, HashSet<Node> drops){
        ArrayList<Node> best = null;
        if(mains.isEmpty() && pick == null && drops.isEmpty()) return new ArrayList<Node>(current);
        if(pick != null){
            current.add(pick);
            ArrayList<Node> tmp = optimalRoute(current, mains, null, drops);
            current.remove(current.size() - 1);
            if(best == null || length(tmp) < length(best))
                best = tmp;
        }
        if(pick == null && !drops.isEmpty()){
            HashSet<Node> dropsTmp = (HashSet<Node>) drops.clone();
            for(Node n : drops){
                current.add(n);
                dropsTmp.remove(n);
                ArrayList<Node> tmp = optimalRoute(current, mains, null, dropsTmp);
                dropsTmp.add(n);
                current.remove(current.size() - 1);
                if(best == null || length(tmp) < length(best))
                    best = tmp;
            }
        }
        if((pick == null && drops.isEmpty()) || mains.size() > 1){
            current.add(mains.get(0));
            ArrayList<Node> tmp = optimalRoute(current, mains.subList(1, mains.size()), pick, drops);
            current.remove(current.size() - 1);
            if(best == null || length(tmp) < length(best))
                best = tmp;
        }
        return best;
    }

    private void generateOptimal(){
        ArrayList<Node> mains = new ArrayList<Node>();
        for(String name : mainPoints)
            mains.add(new Node(name, Node.MAIN));
        HashSet<Node> drops = new HashSet<Node>();
        for(String name : dropPoints)
            drops.add(new Node(name, Node.DROP));
        Node pick = pickPoint == null ? null : new Node(pickPoint, Node.PICK);
        ArrayList<Node> current = new ArrayList<Node>();
        current.add(mains.get(0));
        points = optimalRoute(current, mains.subList(1, mains.size()), pick, drops);
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

    private double length(ArrayList<Node> nodes){
        double total = 0;
        for(int i = 1; i < nodes.size(); i++){
            //System.out.println("going to add: " + nodes.get(i - 1).name + ' ' + nodes.get(i).name + ' ' + dist(nodes.get(i - 1), nodes.get(i)));
            total += dist(nodes.get(i - 1), nodes.get(i));
        }
        return total;
    }

    public double length(){
        return length(points);
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

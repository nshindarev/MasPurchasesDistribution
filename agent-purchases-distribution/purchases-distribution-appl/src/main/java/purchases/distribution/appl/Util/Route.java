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

    public Node(String name, char type){
        this.name = name;
        switch(type){
        case 'P': this.type = PICK; break;
        case 'D': this.type = DROP; break;
        case 'M': this.type = MAIN; break;
        default: this.type = -1;
        }
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
    private ArrayList<String> full = null;
    private double len = -1;

    public Route(List<String> mainPoints, String pickPoint, HashSet<String> dropPoints){
        this.mainPoints = mainPoints;
        this.pickPoint  = pickPoint;
        this.dropPoints = new HashSet<String>(dropPoints);
        points = new ArrayList<Node>();
        if(dropPoints.size() < 10) generateOptimal();
        else generateNearest();
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

    public static Route fromString(String str){
        String[] parts = str.substring(1, str.length() - 1).split(", ");
        ArrayList<Node> nodes = new ArrayList<Node>();
        for(String part : parts)
            nodes.add(new Node(part.substring(0, part.length() - 1), part.charAt(part.length() - 1)));
        return new Route(nodes);
    }

    private static double dist(String a, String b){
        return DataPool.getShortestPaths().shortestDistance(a, b);
    }

    private static double dist(Node a, Node b){
        return DataPool.getShortestPaths().shortestDistance(a.name, b.name);
    }

    private static String middle(String a, String b){
        if(a.equals(b)) return a;
        List<String> path = DataPool.getShortestPaths().getShortestPath(a, b).getVertexList();
        String best = null;
        double diff = Double.MAX_VALUE;
        for(String node : path){
            double d = Math.abs(dist(a, node) - dist(b, node));
            if(d < diff){
                best = node;
                diff = d;
            }
        }
        return best;
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
        if(len > 0) return len;
        return len = length(points);
    }

    public Route addDropPoint(String name){
        if(dropPoints.contains(name)) return this;
        dropPoints.add(name);
        Route newRoute = new Route(mainPoints, pickPoint, dropPoints);
        dropPoints.remove(name);
        return newRoute;
    }

    public Route removeDropPoint(String name){
        if(!dropPoints.contains(name)) return this;
        dropPoints.remove(name);
        Route newRoute = new Route(mainPoints, pickPoint, dropPoints);
        dropPoints.add(name);
        return newRoute;
    }

    public Route changePickPoint(String name){
        return new Route(mainPoints, name, dropPoints);
    }

    public ArrayList<String> expand(){
        if(full != null) return full;
        full = new ArrayList<String>();
        full.add(points.get(0).name);
        for(int i = 1; i < points.size(); i++){
            String prev = points.get(i-1).name;
            String curr = points.get(i).name;
            full.add(curr);
            //if(prev.equals(curr)) continue;
            //List<String> path = DataPool.getShortestPaths().getShortestPath(prev, curr).getVertexList();
            //for(int j = 1; j < path.size(); j++){
            //    full.add(path.get(j));
            //}
        }
        return full;
    }

    public String optimalMiddlePoint(Route that){
        ArrayList<String> full_this = expand();
        ArrayList<String> full_that = that.expand();
        String best = null;
        double max = 1e-5;
        for(String here : full_this)
        for(String there: full_that){
            String mid = middle(here, there);
            Route new_this = this.changePickPoint(mid);
            Route new_that = that.addDropPoint(mid);
            double profit = this.length() - new_this.length() + that.length() - new_that.length();
            if(profit > max){
                max = profit;
                best = mid;
            }
        }
        if(best == null) return null;
        //Route new_this = this.changePickPoint(best);
        //Route new_that = that.addDropPoint(best);
        //double improvement = this.length() - new_this.length() + that.length() - new_that.length();
        //System.out.println("new_this: " + new_this.length());
        //System.out.println("new_that: " + new_that.length());
        //System.out.println("this: " + new_this.toString());
        //System.out.println("this improvement: " + improvement);
        //System.out.println("that: " + new_that.toString());
        //System.out.println("that improvement: " + (that.length() - new_that.length()));
        //System.out.println("profit: " + max);
        return best;
    }

    @Override
    public String toString(){
        return points.toString();
    }
}

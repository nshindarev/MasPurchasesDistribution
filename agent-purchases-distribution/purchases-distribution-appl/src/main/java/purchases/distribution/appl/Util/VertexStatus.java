package purchases.distribution.appl.Util;

import java.util.LinkedList;
import java.util.List;

public class VertexStatus{
    public final String name;
    private List<Status> list;

    public VertexStatus (String s){
        this.name = s;
        this.list = new LinkedList<>();
        this.list.add(Status.PLAIN);
    }

    public VertexStatus (String s, Status initStatus){
        this.name = s;
        this.list = new LinkedList<>();
        this.addStatus(initStatus);
    }

    void addStatus(Status newStatus){
        switch (newStatus){
            case MAIN:
                if(!list.contains(Status.MAIN)){
                    list.add(Status.MAIN);
                    list.add(Status.CURRENT);
                }
                break;
            case GET:
                if(!list.contains(Status.GET)) list.add(Status.GET);
                break;
            case PLAIN:
                if (list.isEmpty()) list.add(Status.PLAIN);
                break;
            case CURRENT:
                if (!list.contains(Status.CURRENT)) list.add(Status.CURRENT);
                break;
            case DELIVER:
                if (!list.contains(Status.DELIVER)) list.add(Status.DELIVER);
                break;
        }
    }

    void popStatus(Status removable){
        if (list != null){
            if (list.size()>1){
                switch (removable){
                    case DELIVER: case GET:
                        list.remove(removable);
                        break;
                }
            }
            else {
                list = new LinkedList<>();
                list.add(Status.PLAIN);
            }
        }
    }

    public static List<VertexStatus> fromStrings (List<String> vertices, Status statuses){
        List<VertexStatus> rez = new LinkedList<>();
        for(String vertex: vertices){
            rez.add(new VertexStatus(vertex, statuses));
        }
        return rez;
    }

    public boolean isCurrent(){
        if (this.list.contains(Status.CURRENT)) return true;
        return false;
    }
    public boolean isMain(){
        if (this.list.contains(Status.MAIN)) return true;
        return false;
    }
    public boolean isDeliver(){
        if (this.list.contains(Status.DELIVER)) return true;
        return false;
    }
    public boolean isGet(){
        if (this.list.contains(Status.GET)) return true;
        return false;
    }
    public boolean isPlain(){
        if (this.list.contains(Status.PLAIN)) return true;
        return false;
    }

    @Override
    public String toString(){
        return this.name;
    }

    @Override
    public boolean equals(Object vertex){
        if(vertex instanceof VertexStatus){
           return  ((VertexStatus) vertex).name.equals(this.name);
        }
        else return false;
    }
}

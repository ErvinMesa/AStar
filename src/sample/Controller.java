package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;


import java.io.FileInputStream;
import java.util.*;

public class Controller {
    @FXML
    ImageView image;

    @FXML
    Canvas canvas = new Canvas();

    private Node start_Node,end_Node;

    private Image img;

    @FXML
    public void initialize(){
        try{
            FileInputStream imagepath = new FileInputStream("src/data/Untitled2.jpg");
            img = new Image(imagepath);
            image.setImage(img);
            canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    GraphicsContext gc = canvas.getGraphicsContext2D();
                    System.out.println("X: "+ event.getX()+" Y:"+event.getY());
                    System.out.println(getColor(img,(int)event.getX(),(int)event.getY())[1]);
                    if(start_Node == null) {
                        start_Node = new Node((int)event.getX(),(int)event.getY());
                        gc.setFill(Color.GREEN);
                        gc.fillOval(start_Node.getX() - 5, start_Node.getY() - 5, 10.0, 10.0);
                    }
                    else{
                        end_Node = new Node((int)event.getX(),(int)event.getY());
                        gc.setFill(Color.RED);
                        gc.fillOval(end_Node.getX() - 5, end_Node.getY() - 5, 10.0, 10.0);
                        findPath();
                    }
                }
            });
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
    public void clearBoard(){
        start_Node = null;
        end_Node = null;
        gc = canvas.getGraphicsContext2D();
        gc.clearRect(0,0,img.getWidth(),img.getHeight());
    }
    public void setEnd(){
        clearBoard();
        gc.setFill(Color.GREEN);
        gc.fillOval(start_Node.getX() - 5, start_Node.getY() - 5, 10.0, 10.0);
    }

    private int[] getColor(Image img, int x,int y){
        int a,r,g,b;
        PixelReader pxReader = img.getPixelReader();
        int argbval = pxReader.getArgb(x,y);
        a=(argbval>>24)&0xff;
        r=(argbval>>16)&0xff;
        g=(argbval>>8)&0xff;
        b=(argbval)&0xff;
        return new int[]{a,r,g,b};
    }
    Node[][] area;
    HashSet<Node> closed = new HashSet<>();
    PriorityQueue<Node> open;
    GraphicsContext gc;

    private List<Node> findPath(){
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.YELLOW);
        Node parent = start_Node;
        area = new Node[(int)img.getWidth()][(int)img.getHeight()];
        open = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return Integer.compare(o1.cost,o2.cost);
            }
        });

        for(int i = 0;i < area.length; i++){
            for(int j = 0;j < area[0].length;j++){
                Node node = new Node(i,j);
                node.setCost(getHCost(end_Node));
                area[i][j] = node;
            }
        }
        open.add(parent);
        Node curr;
        while(!open.isEmpty()){
                curr = open.poll();
                closed.add(curr);
                //System.out.println(curr.getX()+" "+curr.getY());
                if(curr.equals(end_Node)){
                    getPath(curr);
                }
                getSurroundingNodes(curr);
                //curr = lowestCost(open);
        }
        return new ArrayList<Node>();
    }

    private List<Node> getPath(Node curr) {
        List<Node> path = new ArrayList<Node>();
        path.add(curr);
        Node parent;
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLUE);
        while ((parent = curr.parent) != null) {
            gc.fillRect(curr.getX(),curr.getY(),2,2);
            path.add(0, parent);
            curr = parent;
        }
        return path;
    }

    private void getSurroundingNodes(Node curr){
        int x = curr.x;
        int y = curr.y;
        if(y+1 < area[0].length) {
            checkDuplicate(curr, x, y + 1);
        }
        if(x+1 < area.length) {
            checkDuplicate(curr, x + 1, y);
        }
        if(x-1 > 0) {
            checkDuplicate(curr, x - 1, y);
        }
        if(y-1 > 0) {
            checkDuplicate(curr, x, y - 1);
        }
    }
    private void checkDuplicate(Node node,int x, int y){
        gc = canvas.getGraphicsContext2D();
        Node adjacentNode = area[x][y];
        //gc.fillRect(adjacentNode.x, adjacentNode.y, 2, 2);
        if (!checkWall(adjacentNode) && !closed.contains(adjacentNode)) {
            if (!open.contains(adjacentNode)) {
                adjacentNode.parent = node;
                adjacentNode.setCost(node.cost + 10);
                open.add(adjacentNode);
            } else {
                boolean changed = adjacentNode.checkBetterPath(node, 10);
                if (changed) {
                    open.remove(adjacentNode);
                    open.add(adjacentNode);
                }
            }
        }
    }

    public Node lowestCost(Set<Node> open){
        Iterator<Node> openiterator = open.iterator();
        Node min = openiterator.next();
        while(openiterator.hasNext()){
            Node next = openiterator.next();
            if(min.cost > next.cost){
                min = next;
            }
        }
        return min;
    }
    private boolean checkWall(Node n){
        int[] color = getColor(img,n.getX(),n.getY());
        if(color[1] < 255 &&
                color[2] < 255 &&
                color[3] < 255)
            {
                System.out.println(color[1]+" "+color[2]+" "+color[3]);
            return true;
        }
        return false;
    }
    private int getHCost(Node node){
        return (Math.abs(end_Node.x-node.x)+Math.abs(end_Node.y-node.y));
    }
    //(Math.abs(start_Node.x-node.x)+Math.abs(start_Node.y-node.y))
    private class Node{
        Node parent;
        int x,y;
        int cost = -1;

        private Node(int x, int y){
            this.x = x;
            this.y = y;
        }
        public Node(int x, int y,int cost){
            this.x = x;
            this.y = y;
            this.cost = cost;
        }
        private void setCost(int cost){
            this.cost = cost;
        }
        private int getX(){
            return x;
        }
        private int getY(){
            return y;
        }
        private boolean equals(Node comp){
            return (comp.x == this.x && comp.y == this.y);
        }
        public boolean checkBetterPath(Node curr, int cost) {
            int gCost = curr.cost+ cost;
            if (gCost < this.cost) {
                this.parent = curr;
                setCost(cost);
                return true;
            }
            return false;
        }
    }
}

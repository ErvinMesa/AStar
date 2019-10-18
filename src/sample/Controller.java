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
    private int MOVEMENTCOST = 10;
    private int CIRCLESIZES = 10;
    private Color startColor = Color.GREEN;
    private Color endColor = Color.RED;
    private Color pathColor = Color.BLUE;
    //Setup the mouse click event
    //and load the image from the data file
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
                        gc.setFill(startColor);
                        gc.fillOval(start_Node.getX() - (int)(CIRCLESIZES/2), start_Node.getY() - (int)(CIRCLESIZES/2), CIRCLESIZES, CIRCLESIZES);
                    }
                    else{
                        end_Node = new Node((int)event.getX(),(int)event.getY());
                        gc.setFill(endColor);
                        gc.fillOval(end_Node.getX() - (int)(CIRCLESIZES/2), end_Node.getY() - (int)(CIRCLESIZES/2), CIRCLESIZES, CIRCLESIZES);
                        findPath();
                    }
                }
            });
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
    //Clear board and reset start & end node
    public void clearBoard(){
        start_Node = null;
        end_Node = null;
        gc = canvas.getGraphicsContext2D();
        gc.clearRect(0,0,img.getWidth(),img.getHeight());
    }
    //Removes end node
    public void setEnd(){
        clearBoard();
        gc.setFill(endColor);
        gc.fillOval(start_Node.getX() -(int)(CIRCLESIZES/2), start_Node.getY() -(int)(CIRCLESIZES/2), CIRCLESIZES, CIRCLESIZES);
    }
    //The main pathfinding method
    //gc is for drawing on the board
    private List<Node> findPath(){
        Node parent = start_Node;
        area = new Node[(int)img.getWidth()][(int)img.getHeight()];
        open = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return Integer.compare(o1.cost,o2.cost);
            }
        }); //Use a priority queue(prioritizing nodes with the lowest cost)

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
        }
        return new ArrayList<Node>();
    }

    private List<Node> getPath(Node curr) {
        List<Node> path = new ArrayList<Node>();
        path.add(curr);
        Node parent;
        gc = canvas.getGraphicsContext2D();
        gc.setFill(pathColor);
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
            checkDuplicate(curr, x, y + 1); // down
        }
        if(x+1 < area.length) {
            checkDuplicate(curr, x + 1, y); // right
        }
        if(x-1 > 0) {
            checkDuplicate(curr, x - 1, y); // left
        }
        if(y-1 > 0) {
            checkDuplicate(curr, x, y - 1); // up
        }
    }
    private void checkDuplicate(Node node,int x, int y){
        gc = canvas.getGraphicsContext2D(); //drawing
        Node adjacentNode = area[x][y]; // set adjNode to the node at x and y
        //gc.fillRect(adjacentNode.x, adjacentNode.y, 2, 2);

        //If its not a wall or it is in the closed list don't do anything
        if (!checkWall(adjacentNode) && !closed.contains(adjacentNode)) {
            if (!open.contains(adjacentNode)) {
                adjacentNode.parent = node;
                adjacentNode.setCost(node.cost + MOVEMENTCOST);
                open.add(adjacentNode);
            } else {
                //If a new path is opened up check for a better cost value
                boolean changed = adjacentNode.checkBetterPath(node, MOVEMENTCOST);
                if (changed) {
                    open.remove(adjacentNode);
                    open.add(adjacentNode);
                }
            }
        }
    }
    //This determined if a node is in a wall
    private boolean checkWall(Node n){
        int[] color = getColor(img,n.getX(),n.getY()); //Get the color Argb
        //Copy paste the if statement to add more walls
        if(color[1] < 255 && // Red
                color[2] < 255 && // Green
                color[3] < 255) // Blue
            {
                //System.out.println(color[1]+" "+color[2]+" "+color[3]);
            return true; //if its not white then its a wall
        }
        return false; //If its white then its not a wall
    }

    //Gets the color code from the image
    private int[] getColor(Image img, int x,int y){
        int a,r,g,b;
        PixelReader pxReader = img.getPixelReader();
        int argbval = pxReader.getArgb(x,y); //Get the argb values
        a=(argbval>>24)&0xff; // Get alpha
        r=(argbval>>16)&0xff; // Get red
        g=(argbval>>8)&0xff; // Get green
        b=(argbval)&0xff; // Get blue
        return new int[]{a,r,g,b};
    }

    private Node[][] area; //area nodes
    private HashSet<Node> closed = new HashSet<>(); //closed node set
    private PriorityQueue<Node> open; //open node set
    private GraphicsContext gc; //canvas


    //AStar
    //f(n) = g(n) + h(n)
    //f = total cost
    //g = cost from the start
    //h = estimated cost from the end

    //Gets the heuristic cost or the h value
    private int getHCost(Node node){
        return (Math.abs(end_Node.x-node.x)+Math.abs(end_Node.y-node.y));
    }

    //Node class
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

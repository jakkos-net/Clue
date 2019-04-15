/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clue.action;

import clue.player.Player;
import clue.tile.Tile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Represents a player moving from Tile s to Tile tiles
 *
 * @author slb35
 */
public class MoveAction extends Action {

    private Tile s;
    private Tile t;
    private int boardWidth;
    private int boardHeight;

    /**
     * Creates a new MoveAction
     *
     * 
     * @param t destination Tile
     * @param boardWidth
     * @param boardHeight
     * @param player Player to move
     */
    public MoveAction(Player player, Tile t, int boardWidth, int boardHeight) {
        super(player);
        this.actionType = ActionType.MOVE;
        this.s = player.getPosition();
        this.t = t;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
    }

    /**
     * Executes the MoveAction. result stores whether or not Tile t is a valid
     * destination from Tile s.
     */
    @Override
    public void execute() {
        if (t.isFull()){
            result = false;
        }
        else{
            result = BFS();  
        }    
        
    }

    /**
     * gets the destination Tile
     *
     * @return tile tiles
     */
    public Tile getTile() {
        return t;
    }
    
    private boolean BFS(){   
        System.out.println("BFS");
        boolean visited[][] = new boolean[boardWidth][boardHeight];
        
        for(boolean []a : visited){
            Arrays.fill(a,false);
        }
        visited[s.getX()][s.getY()] = true;
        
        LinkedList<LinkedList<Tile>> pathList = new LinkedList<>();      
        LinkedList<Tile> newPath = new LinkedList<>();
        LinkedList<Tile> currentPath;
        
        newPath.add(s);
        pathList.add(newPath);
      
        while(true){
            
            if (pathList.isEmpty()){
                System.out.println("no valid path found");
                return false;
            }
            
            currentPath = pathList.get(0);
            //System.out.println("expanding a path to have one extra distance from source");
            //System.out.println(currentPath.size());
            
            for (Tile currentTile : currentPath.getLast().getAdjacent()){//try to explore all the tiles adjacent to the last tile in the path
                if (currentTile == t){//shortest path found to target
                    currentPath.add(currentTile);
                    return currentPath.size() < player.getMoves();
                }
                else if (currentTile.isFull() || visited[currentTile.getX()][currentTile.getY()]){//if the tile is full or is already visited, do not explore it
                    continue;
                }
                
                newPath = new LinkedList<>();
                for (Tile ti : currentPath){
                    newPath.add(ti);
                }
                
                newPath.add(currentTile);//add new path to pathList (current path + newly found tile)
                
                pathList.add(newPath);
                
//                System.out.println("current paths:");
//                for (LinkedList<Tile> storedPath : pathList){
//                    System.out.println("path:");
//                    for (Tile ti: storedPath){
//                        System.out.println("   "+ti.getX() +","+ ti.getY());
//                    }
//                }
           
                visited[currentTile.getX()][currentTile.getY()] = true;//tile is now marked as visited    
            }
            pathList.remove(currentPath);
  
        }
    }
}

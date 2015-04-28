package Players.Pz_Hw;

import Engine.Logger;
import Interface.Coordinate;
import Interface.PlayerModule;
import Interface.PlayerMove;


import java.util.*;

/**
 * Created by Paul Zenie, pxz5572 on 3/31/2015.
 */
public class Pz_Hw implements PlayerModule {

    private int playerID;
    private Map<Coordinate, HashSet<Coordinate>> graph;
    private Map<Integer, Coordinate> map;
    private int walls;
    private Map<Integer, Integer> idWalls;
    private Map<Coordinate, Boolean> wallMap = new HashMap<>();

    @Override
    public void init(Logger logger, int i, int walls, Map<Integer, Coordinate> map) {
    /*
    Initializes your player module. In this method, be sure to set up your data structures and pre-populate them with the starting board configuration. All state should be stored in your player class.
    Parameters:
        logger, - reference to the logger class
        i, - the id of this player (1 up to 4, for a four-player game)
        walls, - the number of walls this player has
        map, - locations of other players (null coordinate means invalid player; 1-based indexing)
     */
        this.playerID = i;
        this.map = map;
        this.walls = walls;
        this.idWalls = new HashMap<Integer, Integer>();
        idWalls.put(playerID, this.walls);
        for(int b=2; b <= map.size(); b++){
            idWalls.put(b,this.walls);
        }
        this.graph = new HashMap<Coordinate, HashSet<Coordinate>>();
        for(int l = 0; l <= 10; l++){
            for(int k = 0; k <= 10; k++){
                HashSet<Coordinate> neighbors = new HashSet<Coordinate>();
                if(l>0){
                    neighbors.add(new Coordinate(l-1, k));
                }
                if(l<8){
                    neighbors.add(new Coordinate(l+1,k));
                }
                if(k>0){
                    neighbors.add(new Coordinate(l, k-1));
                }
                if(k<8){
                    neighbors.add(new Coordinate(l, k+1));
                }
                graph.put((new Coordinate(l,k)), neighbors);
            }
        }
    }

    @Override
    public void lastMove(PlayerMove playerMove) {
    /*
    Notifies you that a move was just made. Use this function to update your board state accordingly. You may assume that all moves are given to you in the order that they are made.
    Parameters:
        playerMove - the move
    */
        System.out.println("in lastMove... " + playerMove);
        Integer player = playerMove.getPlayerId();
        if (playerMove.isMove()){
            Coordinate m9 = getPlayerLocation(playerID);
            Coordinate newcord = new Coordinate(playerMove.getEndRow(), playerMove.getEndCol());
            map.put(player, newcord);
        }
        else if (!playerMove.isMove()){
            int cwalls = idWalls.get(player);
            idWalls.put(player, cwalls-1);
            placeWalls(playerMove);

        }
    }

    public void placeWalls(PlayerMove playerMove){
        /*
        Places the wall at the specified coordinates in playerMove
         */
        int o = playerMove.getEndCol();
        int u = playerMove.getEndRow();
        boolean rowc = false;
        boolean colc = false;
        if (o != playerMove.getStartCol() ){
            o--;
            rowc = true;
            wallMap.put(playerMove.getStart(), true);
            wallMap.put(new Coordinate(playerMove.getStartRow(), playerMove.getStartCol()+1),false);
            wallMap.put(playerMove.getEnd(), true);

        }
        else if (u != playerMove.getStartRow()){
            u--;
            colc = true;
            wallMap.put(playerMove.getStart(), true);
            wallMap.put(new Coordinate(playerMove.getStartRow()+1, playerMove.getStartCol()),false);
            wallMap.put(playerMove.getEnd(), true);
        }
        Coordinate c = new Coordinate(playerMove.getStartRow(), playerMove.getStartCol());
        Coordinate c1 = new Coordinate(u, o);

        if(rowc == true){
            Coordinate c9 = new Coordinate(c.getRow()-1, c.getCol());
            Coordinate c8 = new Coordinate(c1.getRow()-1, c1.getCol());
            graph.get(c).remove(c9);
            graph.get(c9).remove(c);
            graph.get(c8).remove(c1);
            graph.get(c1).remove(c8);
        }
        else if (colc == true) {
            Coordinate c9 = new Coordinate(c.getRow(), c.getCol() - 1);
            Coordinate c8 = new Coordinate(c1.getRow(), c1.getCol() - 1);
            graph.get(c).remove(c9);
            graph.get(c9).remove(c);
            graph.get(c8).remove(c1);
            graph.get(c1).remove(c8);
        }
    }
    @Override
    public void playerInvalidated(int i) {
    /*
    Notifies you that an opponent player made a bad move and has been invalidated. When this method is called, be sure to update your state and remove the invalidated opponent from the board.
    Parameters:
        i, - the id of the invalid player (1-based)
     */

    }

    @Override
    public PlayerMove move() {
        /*
        Called when it's this player's turn to make a move. This function needs to return the move that you want to make If you return an invalid move, your player will be invalidated
        Returns:
            a PlayerMove object
         */
        List<PlayerMove> moves = new LinkedList<>(allPossibleMoves());
        Collections.shuffle(moves);
        return moves.get(0);
    }

    @Override
    public int getID() {
        /*
        Return the 1-based player ID of this player.
        Returns:
            the 1-based player ID of this player.
         */
        return playerID;
    }

    @Override
    public Set<Coordinate> getNeighbors(Coordinate coordinate) {
        /*
        Returns the subset of the four adjacent cells to which a piece could move due to lack of walls. The system calls this function only to verify that a Player's implementation is correct. However it is likely also handy for most strategy implementations.
        Parameters:
            coordinate - the "current location"
        Returns:
            a set of adjacent coordinates (up-down-left-right only) that are not blocked by walls
         */
        return graph.get(coordinate);
    }

    @Override
    public List<Coordinate> getShortestPath(Coordinate start, Coordinate end) {
        /*
        Returns any valid shortest path between two coordinates, if one exists. The system calls this function only to verify that your implementation is correct.
        You may also use it to test your code.
        Parameters:
            start - the start coordinate
            end - the end coordinate
        Returns:
            an ordered list of Coordinate objects representing a path that must go from the start coordinate to the end coordinate. If no path exists, return an empty list.
         */

        List<Coordinate> dispenser = new LinkedList<Coordinate>();
        dispenser.add(start);

        Map<Coordinate, Coordinate> predecessors = new HashMap<Coordinate, Coordinate>();

        while (!dispenser.isEmpty()){
            Coordinate current = dispenser.remove(0);
            if(current == end){
                break;
            }
            for(Coordinate nbr : getNeighbors(current)){
                if(!predecessors.containsKey(nbr)){
                    predecessors.put(nbr, current);
                    dispenser.add(nbr);
                }
            }
        }
        return constructPath(predecessors, start, end);
    }
    private List<Coordinate> constructPath(Map<Coordinate, Coordinate>     predecessors,
                                           Coordinate startNode, Coordinate finishNode) {

        // use predecessors to work backwards from finish to start,
        // all the while dumping everything into a linked list
        List<Coordinate> path = new LinkedList<Coordinate>();

        if (predecessors.containsKey(finishNode)) {
            Coordinate currNode = finishNode;
            while (currNode != startNode) {
                path.add(0, currNode);
                currNode = predecessors.get(currNode);
            }
            path.add(0, startNode);
        }

        return path;
    }

    @Override
    public int getWallsRemaining(int i) {
        /*
        Get the remaining walls for your player.
        Parameters:
            i - 1-based player ID number
        Returns:
            the remaining walls for your player
         */
        return idWalls.get(i);
    }

    @Override
    public Coordinate getPlayerLocation(int i) {
        /*
        Get the location of a given player.
        Parameters:
            i - 1 -based player ID number
        Returns:
            the location of a given player
         */
        return map.get(i);
    }

    @Override
    public Map<Integer, Coordinate> getPlayerLocations() {
        /*
        Get the location of every player. (1-based index)
        Returns:
            a map representation of the location of every player.
         */
        return map;
    }

    @Override
    public Set<PlayerMove> allPossibleMoves() {
        /*
        Get a set of all possible, valid moves.
        Returns:
            a set of all possible, valid moves.
         */
        Set<PlayerMove> allWallMoves = getAllWallMoves();
        return allWallMoves;
    }

    public Set<PlayerMove> getAllWallMoves(){
        /*
         * Generates and returns the set of all valid next wall
         * moves in the game.
         * @return set of wall moves
         */
        Set<PlayerMove> walls = new HashSet<>();
        for(int i=0; i<8; i++){
            for(int j=0; j<8; j+=2){
                for(int k=0; k<8; k++){
                    for(int l=0; l<8; l+=2){
                        walls.add(new PlayerMove(0, false, new Coordinate(i,j), new Coordinate(k,l)));
                    }
                }
            }
        }
        for(int i=0; i<8; i+=2){
            for(int j=0; j<8; j++){
                for(int k=0; k<8; k+=2){
                    for(int l=0; l<8; l++){
                        walls.add(new PlayerMove(0, false, new Coordinate(i,j), new Coordinate(k,l)));
                    }
                }
            }
        }
        for(PlayerMove wall: walls){
            if(!isValidWall(wall)){
                walls.remove(wall);
            }
        }
        return walls;
    }

    public boolean isValidWall(PlayerMove wall){
        /*
        Checks if wall position is valid
        Returns
            true if valid; false if not.
         */
        boolean valid = true;
        boolean horizontal = false;
        boolean verticle = false;
        int o = wall.getEndCol();
        int u = wall.getEndRow();

        if (o != wall.getStartCol() ) {
            horizontal = true;
        }

        else if (u != wall.getStartRow() ) {
            verticle = true;
        }

        if(wallMap.containsKey(wall.getStart())){
            if (horizontal){
                if(wallMap.containsKey(new Coordinate(wall.getStartRow(), wall.getStartCol()+1))) {
                    valid = false;
                }
            }
            else if (verticle){
                if(wallMap.containsKey(new Coordinate(wall.getStartRow()+1, wall.getStartCol()))){
                    valid = false;
                }
            }
        }

        if(wallMap.containsKey(wall.getEnd())){
            if (horizontal){
                if(wallMap.containsKey(new Coordinate(wall.getEndRow(), wall.getEndCol()-1))) {
                    valid = false;
                }
            }
            else if (verticle){
                if(wallMap.containsKey(new Coordinate(wall.getEndRow()-1, wall.getEndCol()))){
                    valid = false;
                }
            }
        }

        if (horizontal){
            Coordinate c = new Coordinate(wall.getStartRow(), wall.getStartCol()+1);
            if(wallMap.containsKey(c) && !wallMap.get(c)){
                valid = false;
            }
        }

        if (verticle){
            Coordinate c = new Coordinate(wall.getStartRow()+1, wall.getStartCol());
            if(wallMap.containsKey(c) && !wallMap.get(c)){
                valid = false;
            }
        }

        if (valid){
            boolean start = false;
            boolean middle = false;
            boolean end = false;
            if (horizontal) {
                if(wallMap.containsKey(wall.getStart())){
                    start = true;
                }
                else {
                    wallMap.put(wall.getStart(), true);
                }
                if(wallMap.containsKey(new Coordinate(wall.getStartRow(), wall.getStartCol() + 1))){
                    middle = true;
                }
                else {
                    wallMap.put(new Coordinate(wall.getStartRow(), wall.getStartCol() + 1), false);
                }
                if(wallMap.containsKey(wall.getEnd())){
                    end = true;
                }
                else {
                    wallMap.put(wall.getEnd(), true);
                }

            } else if (verticle) {
                if(wallMap.containsKey(wall.getStart())){
                    start = true;
                }
                else {
                    wallMap.put(wall.getStart(), true);
                }
                if(wallMap.containsKey(new Coordinate(wall.getStartRow()+1, wall.getStartCol()))){
                    middle = true;
                }
                else {
                    wallMap.put(new Coordinate(wall.getStartRow()+1, wall.getStartCol()), false);
                }
                if(wallMap.containsKey(wall.getEnd())){
                    end = true;
                }
                else {
                    wallMap.put(wall.getEnd(), true);
                }
            }

            for (int i = 1; i <= map.size(); i++) {
                Coordinate c = map.get(i);
                if(wallMap.containsKey(new Coordinate(c.getRow()+1,c.getCol()+1)) &&
                        wallMap.containsKey(new Coordinate(c.getRow()+1,c.getCol())) &&
                        wallMap.containsKey(new Coordinate(c.getRow(),c.getCol()+1)) &&
                        wallMap.containsKey(new Coordinate(c.getRow(),c.getCol()))){
                    valid = false;
                }
            }

            if (horizontal) {
                if(!start) {
                    wallMap.remove(wall.getStart());
                }
                if(!middle) {
                    wallMap.remove(new Coordinate(wall.getStartRow(), wall.getStartCol() + 1));
                }
                if(!end) {
                    wallMap.remove(wall.getEnd());
                }
            }
            else if (verticle) {
                if(!start) {
                    wallMap.remove(wall.getStart());
                }
                if(!middle) {
                    wallMap.remove(new Coordinate(wall.getStartRow()+1, wall.getStartCol()));
                }
                if(!end) {
                    wallMap.remove(wall.getEnd());
                }
            }
        }
        if (valid){
            boolean start = false;
            boolean middle = false;
            boolean end = false;
            if (horizontal) {
                if(wallMap.containsKey(wall.getStart())){
                    start = true;
                }
                if(wallMap.containsKey(new Coordinate(wall.getStartRow(), wall.getStartCol() + 1))){
                    middle = true;
                }
                if(wallMap.containsKey(wall.getEnd())){
                    end = true;
                }
            }
            else if (verticle) {
                if(wallMap.containsKey(wall.getStart())){
                    start = true;
                }
                if(wallMap.containsKey(new Coordinate(wall.getStartRow()+1, wall.getStartCol()))){
                    middle = true;
                }
                if(wallMap.containsKey(wall.getEnd())){
                    end = true;
                }
            }

            placeWalls(wall);

            if (getShortestPath(new Coordinate(8,4), new Coordinate(0,4)).isEmpty()){
                valid = false;
            }
            if (map.size() > 1){
                if (getShortestPath(new Coordinate(0,4), new Coordinate(8,4)).isEmpty()){
                    valid = false;
                }
            }
            if (map.size() >2){
                if (getShortestPath(new Coordinate(4,0), new Coordinate(4,8)).isEmpty()){
                    valid = false;
                }
            }
            if (map.size() == 4){
                if (getShortestPath(new Coordinate(4,8), new Coordinate(4,0)).isEmpty()){
                    valid = false;
                }
            }

            if (horizontal) {
                if(!start) {
                    wallMap.remove(wall.getStart());
                }
                if(!middle) {
                    wallMap.remove(new Coordinate(wall.getStartRow(), wall.getStartCol() + 1));
                }
                if(!end) {
                    wallMap.remove(wall.getEnd());
                }
            }
            else if (verticle) {
                if (!start) {
                    wallMap.remove(wall.getStart());
                }
                if (!middle) {
                    wallMap.remove(new Coordinate(wall.getStartRow() + 1, wall.getStartCol()));
                }
                if (!end) {
                    wallMap.remove(wall.getEnd());
                }
            }
            Coordinate c = new Coordinate(wall.getStartRow(), wall.getStartCol());
            Coordinate c1 = new Coordinate(wall.getEndRow(), wall.getEndCol());

            if(horizontal == true){
                Coordinate c9 = new Coordinate(c.getRow()-1, c.getCol());
                Coordinate c8 = new Coordinate(c1.getRow()-1, c1.getCol());
                graph.get(c).add(c9);
                graph.get(c9).add(c);
                graph.get(c8).add(c1);
                graph.get(c1).add(c8);
            }
            else if (verticle == true) {
                Coordinate c9 = new Coordinate(c.getRow(), c.getCol() - 1);
                Coordinate c8 = new Coordinate(c1.getRow(), c1.getCol() - 1);
                graph.get(c).add(c9);
                graph.get(c9).add(c);
                graph.get(c8).add(c1);
                graph.get(c1).add(c8);
            }
        }
        return valid;
    }

}

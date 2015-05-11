package Players.Pz_Hw;

import Engine.Logger;
import Interface.Coordinate;
import Interface.PlayerModule;
import Interface.PlayerMove;
import java.util.*;

/**
 * Created by Paul Zenie, pxz5572 on 3/31/2015.
 */
public class Pz_Hw implements PlayerModule, Cloneable{

    private int playerID;
    private int player2ID;
    private int endrow1;
    private int endrow2;
    private Map<Coordinate, HashSet<Coordinate>> graph;
    private Map<Integer, Coordinate> map;
    private int walls;
    private Map<Integer, Integer> idWalls;
    private Map<Coordinate, Boolean> wallMap = new HashMap<>();
    private Map<Coordinate,Coordinate> locatewalls = new HashMap<>();
    private transient Map<Coordinate, HashSet<Coordinate>> graphCopy = new HashMap<>();

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
        if(playerID == 1){
            player2ID = 2;
            endrow1 = 0;
            endrow2 = 8;
        }
        else{
            player2ID = 1;
            endrow1 = 8;
            endrow2 = 0;

        }
        this.map = map;
        this.walls = walls;
        this.idWalls = new HashMap<>();
        for(int b=1; b <= map.size(); b++){
            idWalls.put(b,this.walls);
        }
        this.graph = new HashMap<>();
        for(int l = 0; l <= 8; l++){
            for(int k = 0; k <= 8; k++){
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
        //System.out.println("in lastMove... " + playerMove);
        Integer player = playerMove.getPlayerId();
        if (playerMove.isMove()){
            Coordinate m9 = getPlayerLocation(playerID);
            Coordinate newcord = new Coordinate(playerMove.getEndRow(), playerMove.getEndCol());
            map.put(player, newcord);
        }
        else if (!playerMove.isMove()){
            locatewalls.put(playerMove.getStart(),playerMove.getEnd());
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
            if(!wallMap.containsKey(playerMove.getStart())){
                wallMap.put(playerMove.getStart(), true);
            }
            if(!wallMap.containsKey((new Coordinate(playerMove.getStartRow(), playerMove.getStartCol() + 1)))) {
                wallMap.put(new Coordinate(playerMove.getStartRow(), playerMove.getStartCol() + 1), false);
            }
            if(wallMap.containsKey((new Coordinate(playerMove.getStartRow(), playerMove.getStartCol() + 1))) && wallMap.get(new Coordinate(playerMove.getStartRow(), playerMove.getStartCol() + 1))) {
                wallMap.replace(new Coordinate(playerMove.getStartRow(), playerMove.getStartCol() + 1), false);
            }
            if(!wallMap.containsKey(playerMove.getEnd())) {
                wallMap.put(playerMove.getEnd(), true);
            }
        }
        else if (u != playerMove.getStartRow()){
            u--;
            colc = true;
            if(!wallMap.containsKey(playerMove.getStart())) {
                wallMap.put(playerMove.getStart(), true);
            }
            if(!wallMap.containsKey((new Coordinate(playerMove.getStartRow()+1, playerMove.getStartCol())))) {
                wallMap.put(new Coordinate(playerMove.getStartRow() + 1, playerMove.getStartCol()), false);
            }
            if(wallMap.containsKey((new Coordinate(playerMove.getStartRow()+1, playerMove.getStartCol()))) && wallMap.get(new Coordinate(playerMove.getStartRow()+1, playerMove.getStartCol()))) {
                wallMap.replace(new Coordinate(playerMove.getStartRow()+1, playerMove.getStartCol()), false);
            }
            if(!wallMap.containsKey(playerMove.getEnd())) {
                wallMap.put(playerMove.getEnd(), true);
            }
        }
        Coordinate c = new Coordinate(playerMove.getStartRow(), playerMove.getStartCol());
        Coordinate c1 = new Coordinate(u, o);

        if(rowc == true){
            Coordinate c9 = new Coordinate(c.getRow()-1, c.getCol());
            Coordinate c8 = new Coordinate(c1.getRow()-1, c1.getCol());
            graph.get(c).remove(c9);
            if(graph.containsKey(c9)) {
                graph.get(c9).remove(c);
            }
            if(graph.containsKey(c8)) {
                graph.get(c8).remove(c1);
            }
            graph.get(c1).remove(c8);
        }
        else if (colc == true) {
            Coordinate c9 = new Coordinate(c.getRow(), c.getCol() - 1);
            Coordinate c8 = new Coordinate(c1.getRow(), c1.getCol() - 1);
            graph.get(c).remove(c9);
            if(graph.containsKey(c9)) {
                graph.get(c9).remove(c);
            }
            if(graph.containsKey(c8)) {
                graph.get(c8).remove(c1);
            }
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
        map.remove(i);
    }

    @Override
    public PlayerMove move() {
        /*
        Called when it's this player's turn to make a move. This function needs to return the move that you want to make If you return an invalid move, your player will be invalidated
        Returns:
            a PlayerMove object
         */
        List<PlayerMove> moves = new LinkedList<>(allPossibleMoves());
        try {
            PlayerMove p = new PlayerMove(playerID, true, null, null);
            List<Coordinate> max = getShortestPath(new Coordinate(getPlayerLocation(playerID)), new Coordinate(endrow1, 0));  //our shortest path
            List<Coordinate> max1 = getShortestPath(new Coordinate(getPlayerLocation(player2ID)), new Coordinate(endrow2, 0));  //their shortest path
            for (int i = 0; i < 9; i++) {
                List<Coordinate> s1 = getShortestPath(new Coordinate(getPlayerLocation(playerID)), new Coordinate(endrow1, i));
                if (s1.size() < max.size() && s1.size() > 0) {
                    max = s1;
                }
            }
            for (int i = 0; i < 9; i++) {
                List<Coordinate> s1 = getShortestPath(new Coordinate(getPlayerLocation(player2ID)), new Coordinate(endrow2, i));
                if (s1.size() < max1.size() && s1.size() > 0) {
                    max1 = s1;
                }
            }

            Coordinate jump = new Coordinate(getPlayerLocation(playerID).getRow()-1,getPlayerLocation(playerID).getCol());
            Coordinate jump1 = new Coordinate(getPlayerLocation(playerID).getRow(),getPlayerLocation(playerID).getCol()-1);
            if(getPlayerLocation(player2ID) == jump && !wallMap.containsKey(new Coordinate(jump.getRow(), jump.getCol()+1)) && !wallMap.containsKey(jump)){
                p = new PlayerMove(playerID, true, getPlayerLocation(playerID), new Coordinate(jump.getRow()-1, jump.getCol()));
            }

            else if(getPlayerLocation(player2ID) == jump1 && !wallMap.containsKey(new Coordinate(jump1.getRow()+1, jump1.getCol())) && !wallMap.containsKey(jump1)){
                p = new PlayerMove(playerID, true, getPlayerLocation(playerID), new Coordinate(jump1.getRow(), jump1.getCol()-1));
            }

            else if (max1.size() < max.size() && getWallsRemaining(playerID) > 0 && max1.size() != 0 && max1.size() != 0) {
                boolean done = false;
                Coordinate c = max1.get(max1.size()-1);
                List<Coordinate> maxxest = max1;
                for (PlayerMove pieceMove : moves) {
                    if (!getPathCopy(pieceMove, c).isEmpty() && getPathCopy(pieceMove, c).size() > maxxest.size()) {
                        p = pieceMove;
                        maxxest = getPathCopy(pieceMove, c);
                        done = true;
                    }
                }

                if(!done && max1.size() == 2){
                    Coordinate c2 = getPlayerLocation(player2ID);
                    Coordinate c3 = new Coordinate(c2.getRow()+1, c2.getCol());
                    Coordinate c4 = new Coordinate(c2.getRow()+1, c2.getCol()+2);
                    Coordinate c5 = new Coordinate(c2.getRow()+1, c2.getCol()-1);
                    Coordinate c6 = new Coordinate(c2.getRow()+1, c2.getCol()+1);
                    if(isValidWall(new PlayerMove(playerID, false, c3, c4))) {
                        p = new PlayerMove(playerID, false, c3, c4);
                        done = true;
                    }
                    else if(isValidWall(new PlayerMove(playerID, false, c5, c6))){
                        p = new PlayerMove(playerID, false, c5, c6);
                        done = true;
                    }
                }
                if (!done && moves.contains(new PlayerMove(playerID, true, max.get(0), max.get(1))) && max1.size() != 0 && max.size() != 0) {
                    p = new PlayerMove(playerID, true, max.get(0), max.get(1));
                }
                else if(!done){
                    Collections.shuffle(moves);
                    p = moves.get(0);
                }
            }

            else if (moves.contains(new PlayerMove(playerID, true, max.get(0), max.get(1))) && max1.size() != 0 && max.size() != 0) {
                p = new PlayerMove(playerID, true, max.get(0), max.get(1));
            }

            else {
                Collections.shuffle(moves);
                p = moves.get(0);
            }
            return p;
        }
        catch (IndexOutOfBoundsException e){
            Collections.shuffle(moves);
            PlayerMove p = moves.get(0);
            return p;
        }
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

        List<Coordinate> dispenser = new LinkedList<>();
        dispenser.add(start);

        Map<Coordinate, Coordinate> predecessors = new HashMap<>();

        while (!dispenser.isEmpty()){
            Coordinate current = dispenser.remove(0);
            if(current == end){
                break;
            }
            for(Coordinate nbr : getNeighbors(current)){
                if(!predecessors.containsKey(nbr)){
                    predecessors.put(nbr, current);
                    if(nbr.getCol() >=0 && nbr.getRow()>=0){
                        dispenser.add(nbr);
                    }
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
        Set<PlayerMove> allPieceMoves = allPieceMoves();
        allWallMoves.addAll(allPieceMoves);
        return allWallMoves;
    }

    public Set<PlayerMove> getAllWallMoves(){
        /*
         * Generates and returns the set of all valid next wall
         * moves in the game.
         * @return set of wall moves
         */
        Set<PlayerMove> walls = new HashSet<>();
        Set<PlayerMove> wallsFinal = new HashSet<>();
        if (getWallsRemaining(1) >0) {
            for (int i = 1; i <= 8; i++) {
                for (int k = 0; k + 2 <= 9; k++) {
                    PlayerMove player = new PlayerMove(playerID, false, new Coordinate(i, k), new Coordinate(i, k + 2));
                    walls.add(player);
                }
            }
            for (int i = 0; i + 2 <= 9; i++) {
                for (int k = 1; k <= 8; k++) {
                    PlayerMove player = new PlayerMove(playerID, false, new Coordinate(i, k), new Coordinate(i + 2, k));
                    walls.add(player);
                }
            }

            for (PlayerMove wall : walls) {
                if (isValidWall(wall)) {
                    wallsFinal.add(wall);
                }
            }
        }
        return wallsFinal;
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
            if(wall.getStartCol() < 0 || wall.getStartCol() > 7 || wall.getStartRow()==0 || wall.getStartRow() == 9){
                valid = false;
            }
        }

        else if (u != wall.getStartRow() ) {
            verticle = true;
            if(wall.getStartRow() < 0 || wall.getStartRow() > 7 || wall.getStartCol()==0 || wall.getStartCol()==9){
                valid = false;
            }
        }

        if(wallMap.containsKey(wall.getStart())){
            if (horizontal){
                if(wallMap.containsKey(new Coordinate(wall.getStartRow(), wall.getStartCol()+1)) && !wallMap.get(wall.getStart())){
                    valid = false;
                }
                else {
                    if (wallMap.containsKey(new Coordinate(wall.getStartRow(), wall.getStartCol() + 1)) && !wallMap.get(new Coordinate(wall.getStartRow(), wall.getStartCol() + 1))) {
                        valid = false;
                    }
                }
            }
            else if (verticle){
                if(wallMap.containsKey(new Coordinate(wall.getStartRow()+1, wall.getStartCol())) && !wallMap.get(wall.getStart())){
                    valid = false;
                }
                else {
                    if (wallMap.containsKey(new Coordinate(wall.getStartRow() + 1, wall.getStartCol())) && !wallMap.get(new Coordinate(wall.getStartRow() + 1, wall.getStartCol()))) {
                        valid = false;
                    }
                }
            }
        }

        if(wallMap.containsKey(wall.getEnd())){
            if (horizontal){
                if(wallMap.containsKey(new Coordinate(wall.getEndRow(), wall.getEndCol()-1))&& !wallMap.get(wall.getEnd())) {
                valid = false;
                }
                else {
                    if (wallMap.containsKey(new Coordinate(wall.getEndRow(), wall.getEndCol() - 1)) && !wallMap.get(new Coordinate(wall.getEndRow(), wall.getEndCol() - 1))) {
                        valid = false;
                    }
                }
            }
            else if (verticle){
                if(wallMap.containsKey(new Coordinate(wall.getEndRow()-1, wall.getEndCol()))&& !wallMap.get(wall.getEnd())){
                    valid = false;
                }
                else {
                    if (wallMap.containsKey(new Coordinate(wall.getEndRow() - 1, wall.getEndCol())) && !wallMap.get(new Coordinate(wall.getEndRow() - 1, wall.getEndCol()))) {
                        valid = false;
                    }
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
            for(Coordinate c: graph.keySet()){
                HashSet<Coordinate> h = new HashSet<>();
                for(Coordinate cord: graph.get(c)){
                    Coordinate cord1 = new Coordinate(cord.getRow(),cord.getCol());
                    h.add(cord1);
                }
                Coordinate c1 = new Coordinate(c.getRow(), c.getCol());
                graphCopy.put(c1, h);
            }
            placeWallsCopy(wall);
            for(int i=0; i<9; i++) {
                if (getShortestPathCopy(getPlayerLocation(playerID), new Coordinate(endrow2, i)).isEmpty()) {
                    valid = false;
                    break;
                } else if (getShortestPathCopy(getPlayerLocation(player2ID), new Coordinate(endrow1, i)).isEmpty()) {
                    valid = false;
                    break;
                }
            }
            graphCopy.clear();
        }

        return valid;
    }

    public List<Coordinate> getPathCopy(PlayerMove wall, Coordinate cordinate){
        for(Coordinate c: graph.keySet()){
            HashSet<Coordinate> h = new HashSet<>();
            for(Coordinate cord: graph.get(c)){
                Coordinate cord1 = new Coordinate(cord.getRow(),cord.getCol());
                h.add(cord1);
            }
            Coordinate c1 = new Coordinate(c.getRow(), c.getCol());
            graphCopy.put(c1, h);
        }
        placeWallsCopy(wall);
        List<Coordinate> l = getShortestPathCopy(getPlayerLocation(player2ID), cordinate);
        graphCopy.clear();
        return l;
    }

    public void placeWallsCopy(PlayerMove playerMove){
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
        }
        else if (u != playerMove.getStartRow()){
            u--;
            colc = true;
        }
        Coordinate c = new Coordinate(playerMove.getStartRow(), playerMove.getStartCol());
        Coordinate c1 = new Coordinate(u, o);

        if(rowc == true){
            Coordinate c9 = new Coordinate(c.getRow()-1, c.getCol());
            Coordinate c8 = new Coordinate(c1.getRow()-1, c1.getCol());
            if(graphCopy.containsKey(c)) {
                graphCopy.get(c).remove(c9);
            }
            if(graphCopy.containsKey(c9)) {
                graphCopy.get(c9).remove(c);
            }
            if(graphCopy.containsKey(c8)) {
                graphCopy.get(c8).remove(c1);
            }
            if(graphCopy.containsKey(c1)) {
                graphCopy.get(c1).remove(c8);
            }
        }
        else if (colc == true) {
            Coordinate c9 = new Coordinate(c.getRow(), c.getCol() - 1);
            Coordinate c8 = new Coordinate(c1.getRow(), c1.getCol() - 1);
            if(graphCopy.containsKey(c)) {
                graphCopy.get(c).remove(c9);
            }
            if(graphCopy.containsKey(c9)) {
                graphCopy.get(c9).remove(c);
            }
            if(graphCopy.containsKey(c8)) {
                graphCopy.get(c8).remove(c1);
            }
            if(graphCopy.containsKey(c1)) {
                graphCopy.get(c1).remove(c8);
            }

        }
    }

    public Set<Coordinate> getNeighborsCopy(Coordinate coordinate) {
        /*
        Returns the subset of the four adjacent cells to which a piece could move due to lack of walls. The system calls this function only to verify that a Player's implementation is correct. However it is likely also handy for most strategy implementations.
        Parameters:
            coordinate - the "current location"
        Returns:
            a set of adjacent coordinates (up-down-left-right only) that are not blocked by walls
         */

        return graphCopy.get(coordinate);
    }


    public List<Coordinate> getShortestPathCopy(Coordinate start, Coordinate end) {
        /*
        Returns any valid shortest path between two coordinates, if one exists. The system calls this function only to verify that your implementation is correct.
        You may also use it to test your code.
        Parameters:
            start - the start coordinate
            end - the end coordinate
        Returns:
            an ordered list of Coordinate objects representing a path that must go from the start coordinate to the end coordinate. If no path exists, return an empty list.
         */

        List<Coordinate> dispenser = new LinkedList<>();
        dispenser.add(start);

        Map<Coordinate, Coordinate> predecessors = new HashMap<>();

        while (!dispenser.isEmpty()){
            Coordinate current = dispenser.remove(0);
            if(current == end){
                break;
            }
            for(Coordinate nbr : getNeighborsCopy(current)){
                if(!predecessors.containsKey(nbr)){
                    predecessors.put(nbr, current);
                    if(nbr.getCol() >=0 && nbr.getRow()>=0){
                        dispenser.add(nbr);
                    }
                }
            }
        }
        return constructPath(predecessors, start, end);
    }


    /** Starts off by getting where i am. create the four adjacent squares. first for loop check for walls, if there is
     * remove the possible move. second for loop, loops through the current location of every player, if there is a
     * player remove that coordinate from my hashmap, at the same time, i check for walls of the new coordinate
     * if there is walls, i check if i can move a diagonal.if there isn't a wall that means i can jump.
     * I create playermove for everything in my hashmap and put in hashset, return it.
     * **/
    public Set<PlayerMove> allPieceMoves(){
        ArrayList<Coordinate> outofbound = new ArrayList<Coordinate>();
        HashSet<Coordinate> map1 = new HashSet<Coordinate>();
        Map<Integer,Coordinate> locations = getPlayerLocations();
        Coordinate mylocation = getPlayerLocation(playerID);
        Coordinate up = new Coordinate(mylocation.getRow() - 1, mylocation.getCol());
        Coordinate down = new Coordinate(mylocation.getRow() + 1, mylocation.getCol());
        Coordinate left = new Coordinate(mylocation.getRow(), mylocation.getCol() - 1);
        Coordinate right = new Coordinate(mylocation.getRow(), mylocation.getCol() + 1);
        HashSet<PlayerMove> allmove = new HashSet<PlayerMove>();
        for (Coordinate cord : getNeighbors(mylocation)){
            map1.add(cord);
        }
        for (Integer g: locations.keySet()) {
            Coordinate lo = locations.get(g);
            if (map1.contains(lo)) {
                map.remove(lo);
                Set<Coordinate> neigb = getNeighbors(lo);
                if (lo == up) {
                    if (neigb.contains(new Coordinate(lo.getRow() - 1, lo.getCol()))) {
                        map1.add(new Coordinate(lo.getRow() - 1, lo.getCol()));
                    } else {
                        for (Coordinate cod : neigb) {
                            map1.add(cod);
                        }
                    }
                }
                if (lo == down) {
                    if (neigb.contains(new Coordinate(lo.getRow() + 1, lo.getCol()))) {
                        map1.add(new Coordinate(lo.getRow() + 1, lo.getCol()));
                    } else {
                        for (Coordinate cod : neigb) {
                            map1.add(cod);
                        }
                    }
                }
                if (lo == left) {
                    if (neigb.contains(new Coordinate(lo.getRow(), lo.getCol() - 1))) {
                        map1.add(new Coordinate(lo.getRow(), lo.getCol() - 1));
                    } else {
                        for (Coordinate cod : neigb) {
                            map1.add(cod);
                        }
                    }
                }
                if (lo == right) {
                    if (neigb.contains(new Coordinate(lo.getRow(), lo.getCol() + 1))) {
                        map1.add(new Coordinate(lo.getRow(), lo.getCol() + 1));
                    } else {
                        for (Coordinate cod : neigb) {
                            map1.add(cod);
                        }
                    }
                }
            }
        }
        for (Integer g: locations.keySet()){
            if (map1.contains(locations.get(g))){
                map1.remove(locations.get(g));
            }
        }
        for (Coordinate move: map1){
            if (move.getCol()>8 || move.getRow()>8){
                outofbound.add(move);
            }
        }
        for (Coordinate extra :outofbound){
            map1.remove(extra);
        }
        for (Coordinate demmoves: map1) {
            PlayerMove allmoves = new PlayerMove(playerID, true, mylocation, demmoves);
            allmove.add(allmoves);
        }
        return allmove;
    }

}



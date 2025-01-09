package com.spacecomplexity.longboilife.game.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.spacecomplexity.longboilife.game.building.Building;
import com.spacecomplexity.longboilife.game.building.BuildingCategory;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.globals.Constants;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.MainCamera;
import com.spacecomplexity.longboilife.game.pathways.PathwayPositions;
import com.spacecomplexity.longboilife.game.tile.Tile;
import com.spacecomplexity.longboilife.game.tile.TileType;
import com.spacecomplexity.longboilife.game.world.World;

/**
 * A class to handle calculation of satisfaction score.
 */
public class Satisfaction {
    // Scuffed way to get around world not being a global
    private static World world = MainCamera.camera().world;
    
    private static AStar pathfinder;
    
    /**
     * Struct that stores a building and a distance.
     */
    public static class BuildingDistance {
        public double distance;
        public Building building;
        
        public BuildingDistance(double Distance, Building Building) {
            this.distance = Distance;
            this.building = Building;
        }
    }
    
    public static class AStar {
        private PriorityQueue<Cell> queue;
        private List<Vector2Int> visited;
        private Stack<Vector2Int> shortestPath;
        private double minDistance = Double.MAX_VALUE;
        
        
        public AStar() {
            queue = new PriorityQueue<Cell>();
            visited = new ArrayList<Vector2Int>();
            shortestPath = new Stack<Vector2Int>();
        }
        
        public void reset() {
            queue = new PriorityQueue<Cell>();
            visited = new ArrayList<Vector2Int>();
            shortestPath = new Stack<Vector2Int>();
            minDistance = Double.MAX_VALUE;
        }
        
        public class Cell implements Comparable<Cell> {
            private Vector2Int tile;
            private double heuristic;
            
            public Cell(Vector2Int Tile, double Heuristic) {
                this.tile = Tile;
                this.heuristic = Heuristic;
            }
            
            @Override
            public int compareTo(Cell other) {
                return Double.compare(this.heuristic, other.heuristic);
            }
            
            @Override
            public boolean equals(Object other) {
                if (other == null) {
                    return false;
                }
                
                if (other.getClass() != this.getClass()) {
                    return false;
                }
                
                Cell cell = (Cell)other;
                if (cell.tile == null) {
                    return false;
                }
                
                if (cell.tile.x == this.tile.x && 
                    cell.tile.y == this.tile.y) {
                    return true;
                }
                
                return false;
            }
        }
        
        
        private Vector2Int[] getNeighbours(Vector2Int tile) {
            Vector2Int[] neighbours = new Vector2Int[4];
            
            neighbours[0] = new Vector2Int(tile.x - 1, tile.y);
            neighbours[1] = new Vector2Int(tile.x, tile.y + 1);
            neighbours[2] = new Vector2Int(tile.x + 1, tile.y);
            neighbours[3] = new Vector2Int(tile.x, tile.y - 1);
            
            return neighbours;
        }
        
        private boolean isTraversable(Vector2Int tileLocation) {
            Tile tile = world.getTile(tileLocation);
            if (tile == null) {
                // Outside of map
                return false;
            }
            
            Building buildingOnTile = tile.getBuildingRef();
            
            // If the tile is water then the tile cannot be traversed
            if (tile.getType() == TileType.WATER) {
                return false;
            }
            
            // If there is no building on the tile, or the building on the tile is a road
            if (buildingOnTile == null ||
                buildingOnTile.getType() == BuildingType.ROAD) {
                
                // Tile is traversable
                return true;
            }
            
            return false;
        }
        
        private boolean isVisited(Vector2Int tile) { 
            return visited.contains(tile);
        }
        
        private boolean isPlannedToVisit(Cell tile) {
            return queue.contains(tile);
        }
        
        private double getMoveSpeed(Vector2Int tileLocation) {
            Tile tile = world.getTile(tileLocation);
            Building building = tile.getBuildingRef();
            if (building == null) {
                return 1;
            }
            
            BuildingType buildingType = building.getType();
            
            if (buildingType == BuildingType.ROAD) {
                // Move twice as fast on roads
                return 2;
            }
            // Else move at normal speed
            return 1;
        }
        
        private double getHeuristic(Vector2Int tile, Vector2Int goal) {
            // How many times faster you move while on this tile
            double moveSpeed = getMoveSpeed(tile);
            
            // Grid-based distance away from the goal, taking into account the moveSpeed of the current tile (and assuming default move speed for all other tiles)
            double heuristic = Math.abs(goal.x - tile.x) + Math.abs(goal.y - tile.y) 
                    + (1 / moveSpeed) - 1;
            return heuristic;
        }
        
        private void backtrackTo(Vector2Int tile) {
            while (!shortestPath.empty() && shortestPath.pop() != tile) {}
        }
        
        private double getPathLength() {
            if (shortestPath.empty()) {
                // No possible path was found
                return Double.MAX_VALUE;
            }
            
            
            double length = 0;
            
            for (Vector2Int tile : shortestPath) {
                double moveSpeed = getMoveSpeed(tile);
                length += 1 / moveSpeed;
            }
            
            return length;
        }
        
        public double pathfind(Vector2Int tile, Vector2Int goal) {
            if (tile.equals(goal)) {
                return getPathLength();
            }
            
            visited.add(tile);
            boolean backtracking = true;
            
            for (Vector2Int neighbour : getNeighbours(tile)) {
                // If the tile is traversable and hasn't already been added to the queue/visited
                if (isTraversable(neighbour) && 
                    !isVisited(neighbour) &&
                    !isPlannedToVisit(new Cell(neighbour, getHeuristic(neighbour, goal)))) {
                    
                    // Get distance away from the goal from this tile, taking into account the move speed of that tile (i.e. whether it is a road or not)
                    double heuristic = getHeuristic(neighbour, goal);
                    // Add tile to priority queue
                    queue.add(new Cell(neighbour, heuristic));
                    
                    double nextShortest = queue.peek().heuristic;
                    if (heuristic <= nextShortest) {
                        backtracking = false;
                    }
                }
            }
            
            if (queue.isEmpty()) {
                // No possible path
                // Should raise a warning to the player for the accommodation building
                // i.e. something like "Warning! Students cannot reach this building."
                return Double.MAX_VALUE;
            }
            if (minDistance > 200 && minDistance != Double.MAX_VALUE) {
                // Shortest possible path is way too long
                return minDistance;
            }
            
            // Get the tile with the lowest heuristic
            Cell nextTile = queue.poll();
            minDistance = nextTile.heuristic;
            
            if (backtracking) {
                // Removes elements from shortestPath until it reaches nextTile
                backtrackTo(nextTile.tile);
            }
            else {
                shortestPath.add(nextTile.tile);
            }
            
            // Continue pathfinding from the best option tile (the one with the lowest heuristic)
            return pathfind(nextTile.tile, goal);
        }
    }
    
    public static double getBuildingDistance(Vector2Int start, Vector2Int end) {
        // Clears the leftover data from any previous pathfinding operations
        pathfinder.reset();
        
        return pathfinder.pathfind(start, end);
    }
    
    public static List<BuildingDistance> getBuildingDistances(Building startBuilding, List<Building> targetBuildings) {
        List<BuildingDistance> buildingDistances = new ArrayList<BuildingDistance>();
        
        for (Building building : targetBuildings) {
            
            // Slightly move the start/end positions to be in line with the entrance to buildings
            Vector2Int start = startBuilding.getPosition().add(new Vector2Int(1, -1));
            Vector2Int end = building.getPosition().add(new Vector2Int(1, -1));
            
            // Get the shortest distance from the start to the building position
            double distance = getBuildingDistance(start, end);
            
            // Add the distance and building to the list
            BuildingDistance bd = new BuildingDistance(distance, building);
            buildingDistances.add(bd);
        }
        
        return buildingDistances;
    }
    
    public static List<Building> getAccommodationBuildings(List<Building> buildings) {
        List<Building> accommodationBuildings = new ArrayList<Building>();
        
        for (Building building : buildings) {
            if (building.getType().getCategory() == BuildingCategory.ACCOMMODATION) {
                accommodationBuildings.add(building);
            }
        }
        return accommodationBuildings;
    }
    
    public static List<Building> getUtilityBuildings(List<Building> buildings) {
        List<Building> utilityBuildings = new ArrayList<Building>();
        
        for (Building building : buildings) {
            if (building.getType().getCategory() != BuildingCategory.ACCOMMODATION
                && building.getType().getCategory() != BuildingCategory.PATHWAY) {
                
                utilityBuildings.add(building);
            }
        }
        return utilityBuildings;
    }
    
    /**
     * (See explanation within the function for now, will update this later)
     *
     * @param world the world reference for buildings.
     */
    public static void updateSatisfactionScore(World world) {
        // New implementation of satisfaction score:
        
        /** 
         * Retrieve distance of accommodation building to other utility buildings 
         *      (via A* pathfinding)
         *
         * Check if the building is in range to receive the full amount of satisfaction score
         *      e.g. if a building is within 50 tiles award the full amount, 
         *           if its within 100 award half
         *           if its further than 100 award nothing
         *
         * Each utility building will have a different amount of contribution to satisfaction score
         *      e.g. Lecture hall could be worth 50%
         *           Greggs could be 5%
         *           Gym could be 20%
         *           Library could be 30%
         *
         * Each utility building has a cap on the amount it can contribute to satisfaction score
         * And a bonus to satisfaction score is given for increased variety
         *      e.g. An accommodation building could look like this when hovered over:
         *              Satisfaction 100%
         *              Breakdown:
         *                  Lecture Hall: +50% (cap)
         *                  Gym: +20%
         *                  2x Greggs: +10%
         *                  Variety Bonus (at least 3 different utility buildings): +20%
         *           Or like this with events included:
         *              Satisfaction 15%
         *              Breakdown:
         *                  19x Greggs: +20% (cap)
         *                  Dirty Paths: -5%
         *                  Alien Invasion: -10%
         *                  Half-Price Sausage Rolls: +10%
         *                  etc...
         *
         * Average the satisfaction score of all accommodation buildings
         */


        pathfinder = new AStar();
        Vector<Building> buildings = world.getBuildings();

        List<Building> accommodationBuildings = getAccommodationBuildings(buildings);
        List<Building> utilityBuildings = getUtilityBuildings(buildings);
        
        for (Building accommodation : accommodationBuildings) {
            List<BuildingDistance> buildingDistances = getBuildingDistances(accommodation, utilityBuildings);
            
            double satisfaction = 0;
            for (BuildingDistance bd : buildingDistances) {
                // Calculate the amount of satisfaction gained for this building being this distance away
            }
        }
    }
}

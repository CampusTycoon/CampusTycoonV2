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
        public int distance;
        public Building building;
        
        public BuildingDistance(int Distance, Building Building) {
            this.distance = Distance;
            this.building = Building;
        }
    }
    
    public static class AStar {
        private PriorityQueue<Cell> queue;
        private List<Vector2Int> visited;
        private Stack<Vector2Int> shortestPath;
        private double minDistance = Double.MAX_VALUE;
        private PathwayPositions[][] map;
        
        
        public AStar(PathwayPositions[][] Paths) {
            this.map = Paths;
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
                return Double.compare(other.heuristic, this.heuristic);
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
        
        private Boolean isTraversable(Vector2Int tileLocation) {
            Tile tile = world.getTile(tileLocation);
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
        
        private Boolean isVisited(Vector2Int tile) { 
            return visited.contains(tile);
        }
        
        private Boolean isPlannedToVisit(Vector2Int tile) {
            return queue.contains(tile);
        }
        
        private double getMoveSpeed(Vector2Int tileLocation) {
            Tile tile = world.getTile(tileLocation);
            BuildingType buildingType = tile.getBuildingRef().getType();
            
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
            
            // Grid-based distance away from the goal, taking into account the moveSpeed of the current tile
            double heuristic = Math.abs(goal.x - tile.x) + Math.abs(goal.y - tile.y) 
                    + (1 / moveSpeed) - 1;
            return heuristic;
        }
        
        private void backtrackTo(Vector2Int tile) {
            while (shortestPath.pop() != tile) {}
        }
        
        public int pathfind(Vector2Int tile, Vector2Int goal) {            
            if (tile == goal) {
                return shortestPath.size(); // TODO: Change this to include moveSpeed stuff
            }
            
            
            Boolean backtracking = true;
            
            for (Vector2Int neighbour : getNeighbours(tile)) {
                // If the tile is traversable and hasn't already been added to the queue/visited
                if (isTraversable(neighbour) && 
                    !isVisited(neighbour) &&
                    !isPlannedToVisit(neighbour)) {
                        // Get distance away from the goal from this tile, taking into account the move speed of that tile (i.e. whether it is a road or not)
                        double heuristic = getHeuristic(neighbour, goal);
                        // Add tile to priority queue
                        queue.add(new Cell(neighbour, heuristic));
                        
                        if (heuristic < minDistance) {
                            backtracking = false;
                        }
                }
            }
            
            // Get the tile with the lowest heuristic
            Vector2Int nextTile = queue.poll().tile;
            
            if (backtracking) {
                // Removes elements from shortestPath until it reaches nextTile
                backtrackTo(nextTile);
            }
            else {
                // 
                shortestPath.add(nextTile);
            }
            
            // Continue pathfinding from the best option tile (the one with the lowest heuristic)
            return pathfind(nextTile, goal);
        }
    }
    
    public static int getBuildingDistance(Vector2Int start, Vector2Int end) {
        // Not sure what this function is for other than function name abstraction
        
        
        return pathfinder.pathfind(start, end);
    }
    
    public static List<BuildingDistance> getBuildingDistances(Vector2Int start, List<Building> buildings) {
        List<BuildingDistance> buildingDistances = new ArrayList<BuildingDistance>();
        
        for (Building building : buildings) {
            if (building.getType() == BuildingType.ROAD) {
                // Skip roads
                continue;
            }
            
            // Get the shortest distance from the start to the building position
            int distance = getBuildingDistance(start, building.getPosition());
            
            // Add the distance and building to the list
            BuildingDistance bd = new BuildingDistance(distance, building);
            buildingDistances.add(bd);
        }
        
        return buildingDistances;
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


        pathfinder = new AStar(world.pathways);
        Vector<Building> buildings = world.getBuildings();

        // Map containing buildings split into categories
        HashMap<BuildingCategory, Vector<Building>> categorisedBuildings = new HashMap<>();
        
        
        
        
        
        
        
        

        // Get categories of buildings which will affect satisfaction score
        Set<BuildingCategory> searchBuildingCategories = new HashSet<>(Constants.satisfactoryDistance.keySet());
        // Temporarily add accommodation category so that accommodation buildings will be split out of the main building array
        searchBuildingCategories.add(BuildingCategory.ACCOMMODATION);
        // Initialise empty vectors
        for (BuildingCategory category : searchBuildingCategories) {
            categorisedBuildings.put(category, new Vector<>());
        }
        // Split buildings into categories
        for (Building building : buildings) {
            BuildingCategory category = building.getType().getCategory();
            if (searchBuildingCategories.contains(category)) {
                categorisedBuildings.get(category).add(building);
            }
        }
        // Remove the accommodation category
        searchBuildingCategories.remove(BuildingCategory.ACCOMMODATION);

        // Check if any categories are empty, hence missing required buildings
        boolean emptyCategory = false;
        for (BuildingCategory category : searchBuildingCategories) {
            if (categorisedBuildings.get(category).isEmpty()) {
                emptyCategory = true;
            }
        }

        // If there is are not required buildings this will be the default satisfaction modifier
        float satisfactionModifier = -2000f;

        if (!emptyCategory) {
            // Get the maximum distance if items were placed on other sides of the map
            float mapMax = new Vector2Int(world.getWidth(), world.getHeight()).mag();
            satisfactionModifier = Float.MAX_VALUE;

            // For every accommodation building
            for (Building accomidationBuilding : categorisedBuildings.get(BuildingCategory.ACCOMMODATION)) {
                float modifier = 0;

                // Go through each other category of building
                for (BuildingCategory category : searchBuildingCategories) {
                    float closest = mapMax;
                    // Find the closest of this category of building
                    for (Building building : categorisedBuildings.get(category)) {
                        float distance = (accomidationBuilding.getPosition().subtract(building.getPosition())).mag();
                        if (distance < closest) {
                            closest = distance;
                        }
                    }

                    // Update the modifier by the constants defined with the satisfactory distance from the category
                    modifier += Constants.satisfactoryDistance.get(category) - closest;

                }

                // Set the overall satisfaction modifier to the worst modifier from all accommodations
                satisfactionModifier = Math.min(satisfactionModifier, modifier);
            }
        }

        GameState gameState = GameState.getState();
        
        // Update whether the last satisfaction modifier was positive
        boolean newSatisfactionModifierPositive = satisfactionModifier >= 0;
        if (newSatisfactionModifierPositive != gameState.satisfactionModifierPositive) {
            // Reset satisfaction velocity direction flips
            gameState.satisfactionScoreVelocity = 0;
        }
        gameState.satisfactionModifierPositive = newSatisfactionModifierPositive;

        // Increase satisfaction velocity based on the satisfaction modifier
        float newSatisfactionVelocity = gameState.satisfactionScoreVelocity + (satisfactionModifier / 1000000f);
        // Limit satisfaction velocity to -1% to 1%
        newSatisfactionVelocity = Math.max(-0.01f, Math.min(newSatisfactionVelocity, 0.01f));
        gameState.satisfactionScoreVelocity = newSatisfactionVelocity;

        // Update satisfaction score with velocity
        float newSatisfactionScore = gameState.satisfactionScore + gameState.satisfactionScoreVelocity * Gdx.graphics.getDeltaTime();
        // Limit satisfaction score between 0% and 10% * number of accommodation buildings
        newSatisfactionScore = Math.max(0, Math.min(newSatisfactionScore, Math.min(categorisedBuildings.get(BuildingCategory.ACCOMMODATION).size() * 0.1f, 1f)));
        gameState.satisfactionScore = newSatisfactionScore;
    }
}

package com.spacecomplexity.longboilife.game.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Vector;

import com.spacecomplexity.longboilife.game.building.Building;
import com.spacecomplexity.longboilife.game.building.BuildingCategory;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.MainCamera;
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
    
    private static Map<BuildingType, List<Double>> getBuildingTypeList(List<BuildingDistance> buildingDistances) {
        Map<BuildingType, List<Double>> buildingTypes = new HashMap<BuildingType, List<Double>>();
        
        for (BuildingDistance bd : buildingDistances) {
            BuildingType type = bd.building.getType();
            
            if (!buildingTypes.containsKey(type)) {
                buildingTypes.put(type, new ArrayList<Double>());
            }
            buildingTypes.get(type).add(bd.distance);
        }
        
        return buildingTypes;
    }
    
    private static double calculateBuildingSatisfaction(BuildingType building, List<Double> distances) {
        BuildingCategory category = building.getCategory();
        
        
        // The maximum distance a building can be to award satisfaction
        // Anything under half of this value is awarded the full amount, and anything above is awarded an amount that decreases linearly with distance
        double range = 50;
        
        // The amount each building contributes to the satisfaction score
        double contriubtion = 0;
        
        // The maximum amount of satisfaction this type of building can give
        double cap = 0;
        
        
        switch (category) {
            case EDUCATIONAL:
                switch (building) {
                    case OFFICE:
                        contriubtion = 20;
                        cap = 30;
                        break;
                        
                    case LIBRARY:
                        contriubtion = 40;
                        cap = 40;
                        break;
                        
                    default:
                        break;
                }
                break;
                
            case FOOD:
                switch (building) {
                    case CAFETERIA:
                        contriubtion = 15;
                        cap = 30;
                        break;
                        
                    case FOODSTORE:
                        contriubtion = 20;
                        cap = 20;
                        break;
                        
                    default:
                        break;
                }
                break;
                
            case RECREATIONAL:
                switch (building) {
                    case OUTDOORGYM:
                        contriubtion = 15;
                        cap = 20;
                        break;
                        
                    case PARK:
                        contriubtion = 20;
                        cap = 20;
                        break;
                        
                    case STATIONERYSTORE:
                        contriubtion = 10;
                        cap = 20;
                        break;
                        
                    default:
                        break;
                }
                break;
            
            default:
                break;
        }
        
        
        double satisfaction = 0;
        for (Double distance : distances) {
            
            // 1 if below range/2, 0 if above range, otherwise between 0 and 1 dependant on distance
            double rangeMultiplier = 1;
            if (distance > range) {
                rangeMultiplier = 0;
            }
            else if (distance > 0.5 * range) {
                rangeMultiplier = Math.max(0, 1 - (distance - 0.5 * range) / (0.5 * range));
            }
                
            satisfaction += contriubtion * rangeMultiplier;
        }
        return Math.min(cap, satisfaction);
    }
    
    private static double getSatisfactionScore(List<BuildingDistance> buildingDistances) {
        // A dictionary of each type of building, and its distances from the accommodation building
        Map<BuildingType, List<Double>> buildingTypes = getBuildingTypeList(buildingDistances);
        
        // A list of the types of buildings that have contriubted to satisfaction score
        List<BuildingType> buildingContributors = new ArrayList<BuildingType>();
        
        
        double satisfaction = 0;
        for (BuildingType type : buildingTypes.keySet()) {
            // Gets the satisfaction score associated with this amount of this type of buildings at these specific distances
            satisfaction += calculateBuildingSatisfaction(type, buildingTypes.get(type));
            
            if (satisfaction > 0) {
                // If a type of building contributed to the satisfaction score then it is added to this list so that the variety bonus can be calculated
                buildingContributors.add(type);
            }
        }
        
        if (buildingContributors.size() >= 4) {
            // Variety bonus
            satisfaction += 20;
        }
        
        return satisfaction;
    }
    
    public static double getContribution(BuildingType building, List<Double> distances) {
        return calculateBuildingSatisfaction(building, distances);
    }
    
    /**
     * (See explanation within the function)
     *
     * @param world the world reference for buildings.
     */
    public static void updateSatisfactionScore(World world) {
        // Implementation of satisfaction score:
        
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
         * (with a little bit more complicated formula)
         */


        pathfinder = new AStar();
        Vector<Building> buildings = world.getBuildings();

        // Gets a list of all the accommodation buildings
        List<Building> accommodationBuildings = getAccommodationBuildings(buildings);
        // Gets a list of all non-accommodation and non-road buildings
        List<Building> utilityBuildings = getUtilityBuildings(buildings);
        
        
        double totalSatisfaction = 0;
        for (Building accommodation : accommodationBuildings) {
            // Gets the distances from the accommodation building to each utility building
            List<BuildingDistance> buildingDistances = getBuildingDistances(accommodation, utilityBuildings);
            
            // Calculates the satisfaction score based on the number, type, and distances of each building
            double satisfaction = getSatisfactionScore(buildingDistances);
            
            // Adds (or subtracts) the cumulative bonuses given by any active events
            satisfaction += getSatisfactionEventModifiers(accommodation);
            
            // Clamp satisfaction between 0 and 100%
            satisfaction = Math.min(100, Math.max(0, satisfaction));
            
            totalSatisfaction += satisfaction;
        }
        
        // SatisfactionScore = total satisfaction / (0.8 * accommodation building count + 1)
        // This formula makes it easier to score higher satisfaction scores with more accommodation buildings
        // And requires you to have at least 4 accommodation buildings to get maximum score (100%)
        double averageSatisfaction = totalSatisfaction / (0.75 * accommodationBuildings.size() + 1);
        
        // Clamp the final satisfaction score to 100%
        averageSatisfaction = Math.min(100, averageSatisfaction);
        
        // Round to 2 decimal places
        averageSatisfaction = Math.round(averageSatisfaction * 100.0) / 100.0;
        
        // Update game state with the new satisfaction score!
        GameState.getState().satisfactionScore = averageSatisfaction;
    }
    
    private static double getSatisfactionEventModifiers(Building accommodation) {
        return accommodation.getSatisfactionModifier();
    }
    
    public static void halfPriceEvent() {
        Vector<Building> buildings = world.getBuildings();
        
        // Gets a list of all the accommodation buildings
        List<Building> accommodationBuildings = getAccommodationBuildings(buildings);
        
        for (Building accommodation : accommodationBuildings) {
            // If the building doesn't already have the event triggered
            if (!accommodation.getSatisfactionInfo().contains("Half-price sausage rolls")) {
                // Adds a satisfaction modifier of 10% to the building
                accommodation.setSatisfactionModifier(accommodation.getSatisfactionModifier() + 10);
                accommodation.setSatisfactionInfo(accommodation.getSatisfactionInfo() + "\nHalf-price sausage rolls: +10%");
            }
        }
    }
}

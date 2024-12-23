package com.spacecomplexity.longboilife.game.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.spacecomplexity.longboilife.game.building.Building;
import com.spacecomplexity.longboilife.game.building.BuildingCategory;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.globals.Constants;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.pathways.PathwayPositions;
import com.spacecomplexity.longboilife.game.world.World;

/**
 * A class to handle calculation of satisfaction score.
 */
public class Satisfaction {
    private static World world;
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
        private PriorityQueue<Vector2Int> queue;
        private int minDistance;
        private PathwayPositions[][] map;
        
        
        public AStar(PathwayPositions[][] Paths) {
            this.map = Paths;
        }
        
        public int pathfind(Vector2Int location, Vector2Int goal) {
            // TODO: Figure out how the heck to implement the A* pathfinding algorithm
            
            // Check each neighbour, calculate the heuristic for it
            // Visit the neighbour (of all neighbours) with the lowest heuristic (check it is valid to travel on first)
            
            
            return -1;
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

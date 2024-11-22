package com.spacecomplexity.longboilife.game.world;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.spacecomplexity.longboilife.game.building.Building;
import com.spacecomplexity.longboilife.game.building.BuildingCategory;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.pathways.PathwayPositions;
import com.spacecomplexity.longboilife.game.tile.InvalidSaveMapException;
import com.spacecomplexity.longboilife.game.tile.Tile;
import com.spacecomplexity.longboilife.game.utils.Vector2Int;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

/**
 * Represents the game world containing the base tiles and buildings.
 */
public class World {
    private Tile[][] world;
    public Vector<Building> buildings;
    private PathwayPositions[][] pathways;

    /**
     * Creates a new world loaded from a map JSON file.
     *
     * @param mapFile the handle to the JSON file containing map.
     * @throws FileNotFoundException   if the specified file does not exist.
     * @throws InvalidSaveMapException if the map contains invalid tile names.
     */
    public World(FileHandle mapFile) throws FileNotFoundException, InvalidSaveMapException {
        // If the file does not exist throw an exception
        if (!mapFile.exists()) {
            throw new FileNotFoundException("File does not exist: \"" + mapFile.name() + "\"");
        }

        Json json = new Json();

        try {
            // Deserialize the JSON data into the SaveMap object
            SaveMap saveMap = json.fromJson(SaveMap.class, mapFile.readString());

            // Get the Tile[][] from this object
            world = saveMap.getWorld();

            // Build the buildings onto this world
            buildings = new Vector<>();
            pathways = new PathwayPositions[getWidth()][getHeight()];
            saveMap.buildBuildings(this);
        } catch (SerializationException e) {
            // If there is an issue in deserialising throw an exception
            throw new InvalidSaveMapException("Issue deserialising map save file \"" + mapFile.name() + "\": " + e.getMessage());
        }
    }

    /**
     * Retrieves the tile at the specified coordinates.
     *
     * @param coordinate the coordinate.
     * @return the {@link Tile} at the specified coordinates.
     */
    public Tile getTile(Vector2Int coordinate) {
        return world[coordinate.x][coordinate.y];
    }

    /**
     * Retrieves the pathway position at the specified coordinates.
     *
     * @param coordinate the coordinate.
     * @return the pathway position the specified coordinates.
     */
    public PathwayPositions getPathwayPosition(Vector2Int coordinate) {
        return pathways[coordinate.x][coordinate.y];
    }

    public int getHeight() {
        return world[0].length;
    }

    public int getWidth() {
        return world.length;
    }

    /**
     * Check if a building can be placed at a specific location in the world.
     *
     * @param building   the building we wish to place.
     * @param coordinate the coordinate of the building.
     * @return whether this is a valid position to build the building.
     */
    public boolean canBuild(BuildingType building, Vector2Int coordinate) {
        Vector2Int buildingTop = coordinate.add(building.getSize());

        // If the building goes off the edge of the map it is not valid
        if (!isInWorld(coordinate) || !isInWorld(buildingTop.subtract(new Vector2Int(1, 1)))) {
            return false;
        }

        // Check if every tile underneath this building is buildable
        for (int xi = coordinate.x; xi < buildingTop.x; xi++) {
            for (int yi = coordinate.y; yi < buildingTop.y; yi++) {
                if (!getTile(new Vector2Int(xi, yi)).isBuildable()) {
                    // If a single tile is not then the building placement is invalid
                    return false;
                }
            }
        }

        // If all tiles are then the building placement is valid
        return true;
    }

    /**
     * Build a building at a specific location in the world.
     *
     * @param buildingType the building we wish to place.
     * @param coordinate   the coordinate of the building.
     * @throws IllegalStateException if the building cannot be built.
     */
    public void build(BuildingType buildingType, Vector2Int coordinate) throws IllegalStateException {
        // Create the building instance
        Building building = new Building(buildingType, new Vector2Int(coordinate.x, coordinate.y));

        // Try to build this
        build(building);
    }

    /**
     * Build a building at a specific location in the world.
     *
     * @param building   the building to build.
     * @param coordinate the coordinate of the building.
     * @throws IllegalStateException if the building cannot be built.
     */
    public void build(Building building, Vector2Int coordinate) throws IllegalStateException {
        // Update the building instance
        building.setPosition(new Vector2Int(coordinate.x, coordinate.y));

        // Try to build this
        build(building);
    }

    /**
     * Build a building in the world.
     *
     * @param building the building to build.
     * @throws IllegalStateException if the building cannot be built.
     */
    public void build(Building building) throws IllegalStateException {
        // Get the coordinates of where to build the building
        Vector2Int buildingPosition = building.getPosition();

        // If we cannot build then throw an exception
        if (!canBuild(building.getType(), buildingPosition)) {
            throw new IllegalStateException("Building \"" + building.getType().name() + "\" cannot build at (" + buildingPosition.x + ", " + buildingPosition.y + ")");
        }

        // Set every tile underneath this building to un-buildable and assign the reference
        Vector2Int buildingSize = building.getType().getSize();
        for (int xi = buildingPosition.x; xi < buildingPosition.x + buildingSize.x; xi++) {
            for (int yi = buildingPosition.y; yi < buildingPosition.y + buildingSize.y; yi++) {
                Tile tile = getTile(new Vector2Int(xi, yi));
                tile.setBuildingRef(building);
                tile.setBuildable(false);

            }
        }

        buildings.add(building);

        // If building is a pathway then calculate and add the type to the pathways grid
        if (building.getType().getCategory() == BuildingCategory.PATHWAY) {
            updatePathwayPosition(buildingPosition);
        }

        // Update the game state counter with the new building
        GameState.getState().changeBuildingCount(building.getType(), 1);
    }

    /**
     * Remove a building from the world.
     *
     * @param building the building to remove.
     */
    public void demolish(Building building) {
        // Set every tile underneath this building to its original buildable state and remove the building reference
        Vector2Int buildingSize = building.getType().getSize();
        Vector2Int buildingPosition = building.getPosition();
        for (int xi = buildingPosition.x; xi < buildingPosition.x + buildingSize.x; xi++) {
            for (int yi = buildingPosition.y; yi < buildingPosition.y + buildingSize.y; yi++) {
                Tile tile = getTile(new Vector2Int(xi, yi));
                tile.setBuildingRef(null);
                tile.setBuildable(tile.getType().isNaturallyBuildable());
            }
        }

        buildings.remove(building);

        if (building.getType().getCategory() == BuildingCategory.PATHWAY) {
            pathways[buildingPosition.x][buildingPosition.y] = null;

            BuildingType thisBuildingType = building.getType();

            // Get neighbouring pathways
            Vector2Int[] neighbours = new Vector2Int[]{
                new Vector2Int(buildingPosition.x, buildingPosition.y + 1),
                new Vector2Int(buildingPosition.x + 1, buildingPosition.y),
                new Vector2Int(buildingPosition.x, buildingPosition.y - 1),
                new Vector2Int(buildingPosition.x - 1, buildingPosition.y)
            };
            for (Vector2Int neighbour : neighbours) {
                if (isOurPathway(neighbour, thisBuildingType)) {
                    updatePathwayPosition(neighbour);

                }
            }
        }

        // Update the game state counter with the removal of the building
        GameState.getState().changeBuildingCount(building.getType(), -1);
    }


    /**
     * Map of encoded neighbouring paths and the respective layout of this path.
     * <p>
     * The key is represented by a 4-bit binary number 0b(top)(right)(bottom)(left
     */
    private static final HashMap<Integer, PathwayPositions> pathwayPositionsMap = new HashMap<>() {
        {
            put(0b1000, PathwayPositions.TOP_BOTTOM);
            put(0b0010, PathwayPositions.TOP_BOTTOM);
            put(0b0100, PathwayPositions.LEFT_RIGHT);
            put(0b0001, PathwayPositions.LEFT_RIGHT);
            put(0b1010, PathwayPositions.TOP_BOTTOM);
            put(0b0101, PathwayPositions.LEFT_RIGHT);
            put(0b1001, PathwayPositions.LEFT_TOP);
            put(0b1100, PathwayPositions.TOP_RIGHT);
            put(0b0110, PathwayPositions.RIGHT_BOTTOM);
            put(0b0011, PathwayPositions.BOTTOM_LEFT);
            put(0b1101, PathwayPositions.LEFT_TOP_RIGHT);
            put(0b1110, PathwayPositions.TOP_RIGHT_BOTTOM);
            put(0b0111, PathwayPositions.RIGHT_BOTTOM_LEFT);
            put(0b1011, PathwayPositions.BOTTOM_LEFT_TOP);
            put(0b1111, PathwayPositions.TOP_LEFT_BOTTOM_RIGHT);
        }
    };

    /**
     * Update the pathways on the board recursively starting at the coordinates given.
     *
     * @param coordinate the coordinate of the pathway.
     */
    private void updatePathwayPosition(Vector2Int coordinate) {
        // If there is no building on this tile then do nothing.
        Building thisBuilding = getTile(coordinate).getBuildingRef();
        if (thisBuilding == null) {
            return;
        }
        // If the building is not a pathway then ignore
        BuildingType thisBuildingType = thisBuilding.getType();
        if (thisBuildingType.getCategory() != BuildingCategory.PATHWAY) {
            return;
        }

        // Call recursive function with an empty vector as no pathways have been set
        updatePathwayPosition(coordinate, new Vector<>());
    }

    /**
     * Update the pathways on the board recursively starting at the coordinates given.
     * <p>
     * This should only be called recursive from this function and the starter function.
     *
     * @param coordinate      the coordinate of the pathway.
     * @param updatedPathways a list of all pathways which have already been updated.
     */
    private void updatePathwayPosition(Vector2Int coordinate, Vector<Vector2Int> updatedPathways) {
        // If this pathway has already been done then ignore
        if (updatedPathways.contains(coordinate)) {
            return;
        }

        BuildingType thisBuildingType = getTile(coordinate).getBuildingRef().getType();

        // Get neighbouring pathways
        boolean top = isOurPathway(new Vector2Int(coordinate.x, coordinate.y + 1), thisBuildingType);
        boolean right = isOurPathway(new Vector2Int(coordinate.x + 1, coordinate.y), thisBuildingType);
        boolean bottom = isOurPathway(new Vector2Int(coordinate.x, coordinate.y - 1), thisBuildingType);
        boolean left = isOurPathway(new Vector2Int(coordinate.x - 1, coordinate.y), thisBuildingType);

        // Encode our neighboring paths to query the map for the layout
        int code = (top ? 1 : 0) << 3 | (right ? 1 : 0) << 2 | (bottom ? 1 : 0) << 1 | (left ? 1 : 0);
        PathwayPositions position = pathwayPositionsMap.get(code);
        // If this is not specified default to straight
        if (position == null) {
            position = PathwayPositions.TOP_BOTTOM;
        }

        pathways[coordinate.x][coordinate.y] = position;

        // Recursively set nearby pathways positions
        // The initial checks have already been done as `isOurPathway` will only return true if these passed
        updatedPathways.add(new Vector2Int(coordinate.x, coordinate.y));

        if (top) {
            updatePathwayPosition(new Vector2Int(coordinate.x, coordinate.y + 1), updatedPathways);
        }
        if (right) {
            updatePathwayPosition(new Vector2Int(coordinate.x + 1, coordinate.y), updatedPathways);
        }
        if (bottom) {
            updatePathwayPosition(new Vector2Int(coordinate.x, coordinate.y - 1), updatedPathways);
        }
        if (left) {
            updatePathwayPosition(new Vector2Int(coordinate.x - 1, coordinate.y), updatedPathways);
        }
    }

    /**
     * Returns if the coordinate supplied it outside the bounds of the world.
     *
     * @param coordinate the coordinate.
     * @return true if the coordinate is in the world bounds.
     */
    private boolean isInWorld(Vector2Int coordinate) {
        return coordinate.x >= 0 && coordinate.x < getWidth() && coordinate.y >= 0 && coordinate.y < getHeight();
    }

    /**
     * Check if a pathway is the same as a specified one.
     *
     * @param coordinate  the coordinate of the pathway
     * @param thisPathway the pathway to check/match too.
     * @return if the pathway at (x, y) is the same as the pathway specified.
     */
    private boolean isOurPathway(Vector2Int coordinate, BuildingType thisPathway) {
        // Check this is in the worlds bounds
        if (!isInWorld(coordinate)) {
            return false;
        }

        // Get the building on this tile
        Building ref = getTile(coordinate).getBuildingRef();
        // If there is no building return false
        // If there is a building only return true if it is the same pathway as this
        return ref != null && ref.getType() == thisPathway;
    }

    /**
     * Breadth first search along pathways to calculate the distance between two positions.
     *
     * @param from coordinates of the location to search from.
     * @param to   coordinates of the location to search for.
     * @return the distance in tiles to travel, -1 if there is no path.
     */
    public int getPathDistance(Vector2Int from, Vector2Int to) {
        if (pathways[from.x][from.y] == null) {
            return -1;
        }
        if (pathways[to.x][to.y] == null) {
            return -1;
        }

        Queue<Vector2Int> queue = new LinkedList<>() {{
            add(from);
        }};
        Vector<Vector2Int> visited = new Vector<>();

        // Distance counter
        int distance = 0;

        // While there are still tiles to search
        while (!queue.isEmpty()) {
            int size = queue.size();

            // Process each level in the queue
            for (int i = 0; i < size; i++) {
                Vector2Int current = queue.poll();

                // If we reached the target position return this distance
                if (current.equals(to)) {
                    return distance;
                }

                // Explore neighbors
                Vector2Int[] possibleNext = new Vector2Int[]{
                    new Vector2Int(current.x, current.y + 1),
                    new Vector2Int(current.x + 1, current.y),
                    new Vector2Int(current.x, current.y - 1),
                    new Vector2Int(current.x - 1, current.y),
                };
                for (Vector2Int next : possibleNext) {

                    // If this has been visited before don't search it again
                    if (visited.contains(next)) {
                        continue;
                    }
                    // If this is outside the worlds bounds don't search it
                    if (!isInWorld(next)) {
                        continue;
                    }
                    // If this is not a pathway don't search it
                    if (pathways[next.x][next.y] == null) {
                        continue;
                    }

                    // Add this neighbour to be searched
                    queue.add(next);
                }

                // Mark the current position as visited
                visited.add(current);

            }
            // Increment distance after processing the current level
            distance++;
        }

        // Return -1 if there is no path
        return -1;
    }

    public Vector<Building> getBuildings() {
        return buildings;
    }
}

package com.spacecomplexity.longboilife.game.utils;

import java.util.function.Function;
import java.util.Random;
import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;

import com.spacecomplexity.longboilife.Main;
import com.spacecomplexity.longboilife.achievements.AchievementManager;
import com.spacecomplexity.longboilife.game.building.Building;
import com.spacecomplexity.longboilife.game.building.BuildingCategory;
import com.spacecomplexity.longboilife.game.building.BuildingType;
import com.spacecomplexity.longboilife.game.globals.Constants;
import com.spacecomplexity.longboilife.game.globals.GameState;
import com.spacecomplexity.longboilife.game.globals.MainTimer;
import com.spacecomplexity.longboilife.game.tile.Tile;
import com.spacecomplexity.longboilife.game.utils.Timer.SEASON;
import com.spacecomplexity.longboilife.game.world.World;

public class Events {
    private GameState gameState = GameState.getState();
    private Main game;
    private World world;
    
    private static Random rng = new Random();
    
    public Events(Main Game, World World) {
        this.game = Game;
        this.world = World;
    }
    
    
    public enum Event {
        BUILD,
        SELECT_BUILDING,
        CANCEL_OPERATIONS,
        SELL_BUILDING,
        MOVE_BUILDING,
        PAUSE_GAME,
        RESUME_GAME,
        OPEN_SELECTED_MENU,
        CLOSE_SELECTED_MENU,
        CLOSE_BUILD_MENU,
        GAME_END,
        RETURN_MENU,
        OPEN_SETTINGS,
        CLOSE_EVENT_POPUP
        ;

        private Function<Object[], Object> callback;

        public void setCallback(Function<Object[], Object> callback) {
            this.callback = callback;
        }

        public Function<Object[], Object> getCallback() {
            return callback;
        }
    }
    
    public enum GameEvent {
        FIRE,
        WEATHER,
        DIRTY_BUILDING,
        HALF_PRICE,
        BUDGET_CUT
        ;

        private Function<Object[], Object> callback;

        public void setCallback(Function<Object[], Object> callback) {
            this.callback = callback;
        }

        public Function<Object[], Object> getCallback() {
            return callback;
        }
        
        private Function<Object[], Object> probabilityCalc;

        public void setProbabilityCalc(Function<Object[], Object> probabilityCalc) {
            this.probabilityCalc = probabilityCalc;
        }

        public Function<Object[], Object> getProbabilityCalc() {
            return probabilityCalc;
        }
    }
    
    
    public static void pollEventTriggers() {
        // Get the probabilities of each event occuring at this time
        Map<GameEvent, Double> eventProbabilities = getGameEventProbabilities();
        
        // Shuffle the event list so that there is no bias to event probability due to list ordering
        List<Object> events = Arrays.asList(eventProbabilities.keySet().toArray());
        Collections.shuffle(events);
        
        for (Object event : events) {
            // Get the probability of the event
            GameEvent gameEvent = (GameEvent)event;
            double eventProbability = eventProbabilities.get(gameEvent);
            
            double randomValue = rng.nextDouble(0, 1024 / Constants.EVENT_FREQUENCY);
            
            // If the randomly generated value is within the probability range of the event, call the event
            // Essentially the event will have a eventProbability * EVENT_FREQUENCY / 1024 chance of occuring each poll
            if (randomValue < eventProbability) {
                EventHandler.getEventHandler().callEvent(gameEvent);
                GameState.getState().incrementEventCount();
                
                // Return after calling the event, as calling two events in one poll makes no sense
                return;
            }
        }
    }
    
    private static Map<GameEvent, Double> getGameEventProbabilities() {
        Map<GameEvent, Double> probabilities = new HashMap<GameEvent,Double>();
        
        for (GameEvent event : GameEvent.values()) {
            Function<Object[], Object> calc = event.getProbabilityCalc();
            
            Object[] params = new Object[0]; // We don't need any parameters
            double probability = (double)calc.apply(params);
            
            probabilities.put(event, probability);
        }
        
        return probabilities;
    }
    
    
    private void initialiseEventProbabilities() {
        GameEvent.FIRE.setProbabilityCalc((params) -> {
            // Get the amount of buildings in the world (excluding roads)
            Vector<Building> buildings = getBuildings();
            int buildingCount = buildings.size();
            
            if (buildingCount == 0) {
                // No buildings to set on fire :(
                return 0.0;
            }
            // Else return c * the square root of the building count
            // Allows the probability of a fire to increase with building count but not get out of hand
            return 0.75 * Math.sqrt(buildingCount);
        });
        
        GameEvent.WEATHER.setProbabilityCalc((params) -> {
            SEASON season = MainTimer.getTimerManager().getTimer().getSeason();
            if (season == SEASON.WINTER || season == SEASON.SUMMER) {
                return 1.5;
            }
            
            return 0.0;
        });
        
        GameEvent.DIRTY_BUILDING.setProbabilityCalc((params) -> {
            Vector<Building> buildings = getBuildings();
            
            // Check if the world currently has any accommodation buildings placed down
            int accommodationCount = 0;
            for (Building building : buildings) {
                if (building.getType().getCategory() == BuildingCategory.ACCOMMODATION) {
                    accommodationCount++;
                }
            }
            
            if (accommodationCount > 0) {
                return 1.0 * accommodationCount;
            }
            
            return 0.0;
        });
        
        GameEvent.HALF_PRICE.setProbabilityCalc((params) -> {
            Vector<Building> buildings = getBuildings();
            
            // Check if the world currently has any food buildings placed down
            int foodCount = 0;
            int accommodationCount = 0;
            for (Building building : buildings) {
                if (building.getType().getCategory() == BuildingCategory.FOOD) {
                    foodCount++;
                }
                if (building.getType().getCategory() == BuildingCategory.ACCOMMODATION) {
                    accommodationCount++;
                }
            }
            
            if (accommodationCount > 0 && foodCount > 0) {
                return 2.0 * foodCount;
            }
            
            return 0.0;
        });
        
        GameEvent.BUDGET_CUT.setProbabilityCalc((params) -> {
            // The remaining time left in the game, in milliseconds
            long timeLeft = MainTimer.getTimerManager().getTimer().getTimeLeft();
            
            // Budget cuts start off very unlikely and increase in likelyhood as the game progresses
            // Assumes a game lasts 5 minutes (300000 milliseconds)
            if (timeLeft < 150000) {
                return 1.5;
            }
            else if (timeLeft < 225000) {
                return 0.75;
            }
            return 0.2;
        });
    }
    
    public void initialiseEvents() {
        EventHandler eventHandler = EventHandler.getEventHandler();
        
        
        // Build the selected building
        eventHandler.createEvent(Event.BUILD, (params) -> {
            BuildingType toBuild = gameState.placingBuilding;

            // If there is no selected building do nothing
            if (toBuild == null) {
                return null;
            }

            // If the building is in an invalid location then don't built
            Vector2Int mouse = GameUtils.getMouseOnGrid(world);
            if (!world.canBuild(toBuild, mouse)) {
                return null;
            }

            // If there is no moving building then this is a new build
            if (gameState.movingBuilding == null) {
                // If the user doesn't have enough money to buy the building then don't build
                float cost = toBuild.getCost();
                if (gameState.money < cost) {
                    return null;
                }

                // Build the building at the mouse location and charge the player accordingly
                world.build(toBuild, mouse);
                gameState.money -= cost;

                // Check for achievements
                AchievementManager.getInstance().checkAchievements();

                // Remove the selected building if shift is not held
                if (!gameState.shiftHeld) {
                    gameState.placingBuilding = null;
                }
            }
            // If there is a moving building then this is a moved building.
            else {
                // If the user doesn't have enough money to buy the building then don't build
                float cost = toBuild.getCost() * Constants.moveCostRecovery;
                if (gameState.money < cost) {
                    return null;
                }

                // Build the building at the mouse location and charge the player accordingly
                world.build(gameState.movingBuilding, mouse);
                gameState.money -= cost;

                // Remove the old moving building and selected building
                gameState.movingBuilding = null;
                gameState.placingBuilding = null;
            }
            
            Satisfaction.updateSatisfactionScore(world);

            return null;
        });

        // Select a previously built building
        eventHandler.createEvent(Event.SELECT_BUILDING, (params) -> {
            // Get the tile at the mouse coordinates
            Tile tile = world.getTile(GameUtils.getMouseOnGrid(world));
            // If there is no tile here then do nothing
            if (tile == null) {
                return null;
            }
            // Get the building on the tile
            Building selectedBuilding = tile.getBuildingRef();
        
            // If there is no building here then do nothing
            if (selectedBuilding == null) {
                return null;
            }
        
            // Set the selected building
            gameState.selectedBuilding = selectedBuilding;
        
            // Open the selected building menu
            eventHandler.callEvent(Event.OPEN_SELECTED_MENU);
        
            return null;
        });

        // Cancel all actions
        eventHandler.createEvent(Event.CANCEL_OPERATIONS, (params) -> {
            // Close menus and deselect any buildings
            eventHandler.callEvent(Event.CLOSE_BUILD_MENU);
            gameState.placingBuilding = null;
            eventHandler.callEvent(Event.CLOSE_SELECTED_MENU);
            gameState.selectedBuilding = null;

            // If there is a building move in progress cancel this
            if (gameState.movingBuilding != null) {
                world.build(gameState.movingBuilding);
                gameState.movingBuilding = null;
            }

            return null;
        });

        // Sell the selected building
        eventHandler.createEvent(Event.SELL_BUILDING, (params) -> {
            // Get the refund amount before nulling the reference
            float refund = gameState.selectedBuilding.getType().getCost() * Constants.sellCostRecovery;
            
            // Delete the building
            world.demolish(gameState.selectedBuilding);
            
            // Add the refund
            gameState.money += refund;
            
            // Update satisfaction score
            Satisfaction.updateSatisfactionScore(world);
            
            // Finally, deselect the building
            gameState.selectedBuilding = null;
            
            return null;
        });

        // Start the move of the selected building
        eventHandler.createEvent(Event.MOVE_BUILDING, (params) -> {
            float cost = gameState.selectedBuilding.getType().getCost() * Constants.moveCostRecovery;
            // If we don't have enough money then don't allow the move
            if (gameState.money < cost) {
                return null;
            }

            // Delete the original building
            world.demolish(gameState.selectedBuilding);
            // Select the same type of building to be placed again
            gameState.placingBuilding = gameState.selectedBuilding.getType();
            // Deselect the removed building and set it to the building to be moved
            gameState.movingBuilding = gameState.selectedBuilding;
            gameState.selectedBuilding = null;

            // Close the menu
            eventHandler.callEvent(Event.CLOSE_SELECTED_MENU);
            
            Satisfaction.updateSatisfactionScore(world);

            return null;
        });

        // Return to the menu
        eventHandler.createEvent(Event.RETURN_MENU, (params) -> {
            // If the game is over, go to leaderboard instead of menu
            if (gameState.gameOver) {
                game.openLeaderboard(Main.ScreenType.GAME);
            } else {
                game.switchScreen(Main.ScreenType.MENU);
            }

            return null;
        });

        // Open settings menu
        eventHandler.createEvent(Event.OPEN_SETTINGS, (params) -> {
            game.openSettings(Main.ScreenType.GAME);
            return null;
        });
        
        
        
        
        // Arson :)
        eventHandler.createEvent(GameEvent.FIRE, (params) -> {
            Vector<Building> buildings = getBuildings();
            
            if (buildings.isEmpty()) { 
                // No buildings to set on fire :(
                return null;
            }
            
            // Gets a random building (to be set on fire)
            Building randomBuilding = buildings.get(rng.nextInt(0, buildings.size()));
            
            String message = "Oh no!" +
                "\nA chemistry student set fire to a building (accidentally?)";
            
            // Checks if there are any roads adjacent to the building
            // If there are then gives an advantage to the chance of firefighters saving the building
            Boolean advantage = GameUtils.roadAdjacent(world, randomBuilding);
            Boolean advantageRoll = false;
            if (advantage) {
                advantageRoll = rng.nextBoolean();
            }
            
            // With advantage, this is essentially flipping two coins and winning if either is heads (true)
            // Without advantage, its just a 50/50
            Boolean buildingSaved = rng.nextBoolean() || advantageRoll;
            
            if (buildingSaved) {
                message += "\n\nThankfully firefighters managed to arrive on the scene quick enough to put out the fire before any major damage occured." +
                    "\nYay!";
                game.getUIManager().getEventPopup().showEvent(message);
                return null;
            }
            else if (!buildingSaved && advantage) {
                message += "\n\nDespite the best efforts of the firefighters, the fire moved too quickly, and the building was destroyed." +
                    "\n\nYou lost a " + randomBuilding.getType().getDisplayName() + ".";
            }
            else {
                message += "\n\nUnfortunately, the firefighters got really lost, and the building was destroyed before they could arrive." +
                    "\n(Maybe they would have arrived quicker if the building was easier to access?)" +
                    "\n\nYou lost a " + randomBuilding.getType().getDisplayName() + ".";
            }
            
            // Show event popup
            game.getUIManager().getEventPopup().showEvent(message);
            
            // Destroy building
            world.demolish(randomBuilding);
            
            // Update satisfaction score
            Satisfaction.updateSatisfactionScore(world);
            
            return null;
        });
        
        eventHandler.createEvent(GameEvent.WEATHER, (params) -> {
            String message = "";
            SEASON season = MainTimer.getTimerManager().getTimer().getSeason();
            
            if (season == SEASON.WINTER) {
                double averageDistance = Satisfaction.snowEventDistance();
                
                if (averageDistance <= 25) {
                    message = "A light blanket of snow has fallen upon the campus." +
                        "\nStudents are enjoying the picturesque view on short walks." +
                        "\n\n+5% Student satisfaction.";
                    
                    Satisfaction.lightSnowEvent();
                    // Update satisfaction score
                    Satisfaction.updateSatisfactionScore(world);
                }
                else if (averageDistance > 50) {
                    message = "A heavy blanket of snow has descended upon the campus." +
                        "\nStudents are freezing cold during their long trip to the lecture halls." +
                        "\n\n-5% Student satisfaction.";
                    
                    Satisfaction.heavySnowEvent();
                    // Update satisfaction score
                    Satisfaction.updateSatisfactionScore(world);
                }
                else {
                    // The students have to travel pretty far in the snow but its not far enough for them to be upset, but nor is it short enough for them to be happy
                    return null;
                }
            }
            
            else if (season == SEASON.SUMMER) {
                message = "A record breaking heatwave is sweeping across Britain!" + 
                    "\nAuthorities advise everyone to stay inside and in the shade as much as possible." +
                    "\nUnfortunately, lectures still need to be attended in-person." +
                    "\n\n-5% Student satisfaction.";
                
                Satisfaction.heatwaveEvent();
                // Update satisfaction score
                Satisfaction.updateSatisfactionScore(world);
            }
            
            // Show event popup
            game.getUIManager().getEventPopup().showEvent(message);
            
            return null;
        });
        
        eventHandler.createEvent(GameEvent.DIRTY_BUILDING, (params) -> {
            String message = "The local students have been out partying *way* too much." +
                "\nThey keep leaving their empty bottles of vodka all over the place!" +
                "\nThe campus has never looked so unclean..." +
                "\n\n-10% Satisfaction to one accommodation block.";
            
            // Show event popup
            game.getUIManager().getEventPopup().showEvent(message);
            
            // Adds a satisfaction reduction of 10% to any existing accommodation buildings that don't already have the reduction
            Satisfaction.dirtyBuildingEvent();
            
            // Update satisfaction score
            Satisfaction.updateSatisfactionScore(world);
            
            return null;
        });
        
        // Greggs sausage rolls my beloved <3
        eventHandler.createEvent(GameEvent.HALF_PRICE, (params) -> {
            String message = "Greggs™ have started a sale in York." +
                "\nSausage rolls are now half-price!" +
                "\nThe students are *very* happy." +
                "\n\n+10% Student satisfaction.";
            
            // Show event popup
            game.getUIManager().getEventPopup().showEvent(message);
            
            // Adds a satisfaction bonus of 10% to any existing accommodation buildings that don't already have the bonus
            Satisfaction.halfPriceEvent();
            
            // Update satisfaction score
            Satisfaction.updateSatisfactionScore(world);
            
            return null;
        });
        
        
        // All my homies hate the government budget cuts
        eventHandler.createEvent(GameEvent.BUDGET_CUT, (params) -> {
            // Randomly picks between 50k, 75, and 100k to be lost
            double amountCut = rng.nextInt(2, 5) * 25000;
            
            String message = 
                "The government have cut funding from the education sector" +
                "\n...again." +
                "\n" +
                "\n You have lost £" + String.format("%,.2f", amountCut) + ".";
            
            // Show event popup
            game.getUIManager().getEventPopup().showEvent(message);
            
            gameState.money -= amountCut;
            
            return null;
        });
        
        
        initialiseEventProbabilities();
    }
    
    /**
     * Retrieves the list of buildings currently placed in the world.
     * @return all buildings- excluding roads.
     */
    private Vector<Building> getBuildings() {
        // Get all buildings (a new copy to ensure no pointer shenanigans)
        Vector<Building> buildings = new Vector<Building>(world.getBuildings());
        
        // Remove roads
        buildings.removeIf(building -> building.getType().getCategory() == BuildingCategory.PATHWAY);
        
        return buildings;
    }
}

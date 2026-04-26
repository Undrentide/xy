package domain.service;

import domain.model.impl.Dot;
import domain.model.impl.RouteHistory;

import java.util.List;

/**
 * Service for navigation management and routing logic.
 * Handles application initialization, pathfinding algorithms,
 * and persistent storage of user travel history.
 */
public interface RouteService {

    /**
     * Initializes the application, generates dots on first run,
     * and sets the user's initial random location.
     */
    void initializeApp();

    /**
     * Retrieves the current location of the user.
     *
     * @return the Dot where the user is currently located.
     */
    Dot getUserLocation();

    /**
     * Selects a random destination dot from the database.
     *
     * @return a random Dot entity.
     */
    List<List<Dot>> buildBestThreeRoutes(Dot target);

    /**
     * Calculates the top 3 shortest paths to the target using a graph of nearby neighbors.
     * param target - the destination point.
     * return a list containing up to three alternative routes (as lists of Dots).
     */
    void selectAndSaveRoute(List<Dot> route);

    /**
     * Persists the selected route to history and updates the user's current location to the final dot.
     * param route the chosen list of dots to be saved.
     */
    List<RouteHistory> getRouteHistory();

    /**
     * Retrieves the most recent navigation records.
     * return a list of RouteHistory entries based on the established limit.
     */
    Dot getRandomDot();
}
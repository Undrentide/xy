package api;

import domain.model.impl.Dot;
import domain.model.impl.RouteHistory;
import domain.service.RouteService;

import java.util.List;

public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    public Dot getCurrentLocation() {
        return routeService.getUserLocation();
    }

    public List<List<Dot>> getBestThreeRoutes(Dot target) {
        return routeService.buildBestThreeRoutes(target);
    }

    public void selectRoute(List<Dot> route) {
        routeService.selectAndSaveRoute(route);
    }

    public List<RouteHistory> getHistory() {
        return routeService.getRouteHistory();
    }
}
package domain.service.impl;

import domain.dal.DotDao;
import domain.dal.RouteHistoryDao;
import domain.dal.RouteHistoryStepDao;
import domain.model.impl.Dot;
import domain.model.impl.RouteHistory;
import domain.model.impl.RouteHistoryStep;
import domain.service.RouteService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RouteServiceImpl implements RouteService {
    private static final int INITIAL_DOTS_COUNT = 100;
    private static final int HISTORY_LIMIT = 10;
    private static final double FIELD_SIZE = 100.0;
    private static final int NEIGHBORS_COUNT = 5;

    private final DotDao dotDao;
    private final RouteHistoryDao routeHistoryDao;
    private final RouteHistoryStepDao routeHistoryStepDao;
    private Dot currentUserLocation;

    public RouteServiceImpl(DotDao dotDao, RouteHistoryDao routeHistoryDao, RouteHistoryStepDao routeHistoryStepDao) {
        this.dotDao = dotDao;
        this.routeHistoryDao = routeHistoryDao;
        this.routeHistoryStepDao = routeHistoryStepDao;
    }

    @Override
    public Dot getUserLocation() {
        ensureUserIsLocated();
        return currentUserLocation;
    }

    @Override
    public List<List<Dot>> buildBestThreeRoutes(Dot target) {
        List<Dot> allDots = dotDao.findAll();
        Map<UUID, List<Dot>> graph = buildGraph(allDots);
        return findKShortestPaths(currentUserLocation, target, graph);
    }

    @Override
    public void selectAndSaveRoute(List<Dot> route) {
        if (route != null && !route.isEmpty()) {
            Dot start = route.getFirst();
            Dot end = route.getLast();
            RouteHistory history = new RouteHistory(start.getId(), end.getId());
            routeHistoryDao.save(history);
            for (int i = 0; i < route.size(); i++) {
                routeHistoryStepDao.save(new RouteHistoryStep(history.getId(), route.get(i).getId(), i));
            }
            currentUserLocation = end;
        }
    }

    @Override
    public List<RouteHistory> getRouteHistory() {
        return routeHistoryDao.findLastRoutes(HISTORY_LIMIT);
    }

    private Map<UUID, List<Dot>> buildGraph(List<Dot> allDots) {
        Map<UUID, List<Dot>> graph = new HashMap<>();
        for (Dot current : allDots) {
            List<Dot> neighbors = allDots.stream()
                    .filter(dot -> !dot.getId().equals(current.getId()))
                    .sorted(Comparator.comparingDouble(dot -> calculateDistance(current, dot)))
                    .limit(NEIGHBORS_COUNT)
                    .collect(Collectors.toList());
            graph.put(current.getId(), neighbors);
        }
        return graph;
    }

    private List<List<Dot>> findKShortestPaths(Dot start, Dot end, Map<UUID, List<Dot>> graph) {
        List<List<Dot>> rootPaths = new ArrayList<>();
        Queue<List<Dot>> potentialPaths = new PriorityQueue<>(Comparator.comparingDouble(this::calculatePathDistance));
        List<Dot> firstPath = dijkstra(start, end, graph, Collections.emptySet(), Collections.emptySet());
        if (firstPath.isEmpty()) {
            return rootPaths;
        }
        rootPaths.add(firstPath);
        for (int i = 1; i < 3; i++) {
            List<Dot> previousPath = rootPaths.get(i - 1);
            for (int j = 0; j < previousPath.size() - 1; j++) {
                Dot spurNode = previousPath.get(j);
                List<Dot> rootPathNode = previousPath.subList(0, j + 1);
                Set<UUID> ignoredNodes = new HashSet<>();
                Set<String> ignoredEdges = new HashSet<>();
                for (List<Dot> path : rootPaths) {
                    if (path.size() > j && path.subList(0, j + 1).equals(rootPathNode)) {
                        ignoredEdges.add(path.get(j).getId().toString() + "-" + path.get(j + 1).getId().toString());
                    }
                }
                for (int m = 0; m < rootPathNode.size() - 1; m++) {
                    ignoredNodes.add(rootPathNode.get(m).getId());
                }
                List<Dot> spurPath = dijkstra(spurNode, end, graph, ignoredNodes, ignoredEdges);
                if (!spurPath.isEmpty()) {
                    List<Dot> totalPath = new ArrayList<>(rootPathNode);
                    totalPath.addAll(spurPath.subList(1, spurPath.size()));
                    if (!potentialPaths.contains(totalPath)) {
                        potentialPaths.add(totalPath);
                    }
                }
            }
            if (potentialPaths.isEmpty()) break;
            rootPaths.add(potentialPaths.poll());
        }
        return rootPaths;
    }

    private List<Dot> dijkstra(Dot start, Dot end, Map<UUID, List<Dot>> graph, Set<UUID> ignoredNodes, Set<String> ignoredEdges) {
        Map<UUID, Double> distances = new HashMap<>();
        Map<UUID, Dot> previous = new HashMap<>();
        Queue<Dot> queue = new PriorityQueue<>(Comparator.comparingDouble(d -> distances.getOrDefault(d.getId(), Double.MAX_VALUE)));
        distances.put(start.getId(), 0.0);
        queue.add(start);
        while (!queue.isEmpty()) {
            Dot u = queue.poll();
            if (u.getId().equals(end.getId())) {
                break;
            }
            for (Dot v : graph.getOrDefault(u.getId(), Collections.emptyList())) {
                if (ignoredNodes.contains(v.getId())) {
                    continue;
                }
                if (ignoredEdges.contains(u.getId().toString() + "-" + v.getId().toString())) {
                    continue;
                }
                double alt = distances.get(u.getId()) + calculateDistance(u, v);
                if (alt < distances.getOrDefault(v.getId(), Double.MAX_VALUE)) {
                    distances.put(v.getId(), alt);
                    previous.put(v.getId(), u);
                    queue.add(v);
                }
            }
        }
        List<Dot> path = new LinkedList<>();
        for (Dot at = end; at != null; at = previous.get(at.getId())) {
            path.addFirst(at);
        }
        return path.size() > 1 && path.getFirst().equals(start) ? path : Collections.emptyList();
    }

    private void ensureUserIsLocated() {
        if (currentUserLocation == null) {
            List<Dot> dots = dotDao.findAll();
            if (dots.isEmpty()) {
                generateInitialDots();
                dots = dotDao.findAll();
            }
            this.currentUserLocation = dots.get(new Random().nextInt(dots.size()));
        }
    }

    private void generateInitialDots() {
        Random random = new Random();
        for (int i = 0; i < INITIAL_DOTS_COUNT; i++) {
            dotDao.save(new Dot(
                    "Point_" + i,
                    random.nextDouble() * FIELD_SIZE,
                    random.nextDouble() * FIELD_SIZE
            ));
        }
    }

    private double calculateDistance(Dot a, Dot b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }

    private double calculatePathDistance(List<Dot> path) {
        double distance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            distance += calculateDistance(path.get(i), path.get(i + 1));
        }
        return distance;
    }
}
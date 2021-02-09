package bearmaps.utils.graph;

import bearmaps.utils.pq.MinHeapPQ;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {
    private SolverOutcome outcome;
    private double timeSpan; // Time elapsed
    private double solutionWeight;
    private LinkedList<Vertex> solution = new LinkedList<>();
    private HashMap<Vertex, AStarData> visited = new HashMap<>();

    public AStarSolver(AStarGraph<Vertex> input, Vertex start, Vertex end, double timeout) {
        Stopwatch beep = new Stopwatch();
        MinHeapPQ<Vertex> pq = new MinHeapPQ<>();
        pq.insert(start, input.estimatedDistanceToGoal(start, end));
        visited.put(start, new AStarData(null, 0));
        while (pq.size() > 0) {
            Vertex polled = pq.poll();
            timeSpan = beep.elapsedTime();
            // If time out
            if (timeSpan > timeout) {
                outcome = SolverOutcome.TIMEOUT;
                return;
            }
            // If found solution
            else if (polled.equals(end)) {
                outcome = SolverOutcome.SOLVED;
                Vertex curr = end;
                solution.addLast(end);
                while (visited.get(curr).vertFrom != null) {
                    curr = visited.get(curr).vertFrom;
                    solution.addFirst(curr);
                }
                return;
            } else {
                for (WeightedEdge<Vertex> e : input.neighbors(polled)) {
                    if (!visited.containsKey(e.to())) {
                        visited.put(e.to(), new AStarData(polled,
                                visited.get(polled).bestWeight + e.weight()));
                        pq.insert(e.to(), visited.get(e.to()).bestWeight
                                + input.estimatedDistanceToGoal(e.to(), end));
                    } else if (visited.get(e.to()).bestWeight >
                            e.weight() + visited.get(polled).bestWeight) {
                        visited.put(e.to(), new AStarData(polled,
                                visited.get(polled).bestWeight + e.weight()));
                        if (pq.contains(e.to())) {
                            pq.changePriority(e.to(), visited.get(e.to()).bestWeight
                                    + input.estimatedDistanceToGoal(e.to(), end));
                        } else {
                            pq.insert(e.to(), visited.get(polled).bestWeight
                                    + input.estimatedDistanceToGoal(e.to(), end));
                        }
                    }
                }
            }
        }
        outcome = SolverOutcome.UNSOLVABLE;
    }


    public SolverOutcome outcome() {
        return outcome;
    }

    public List<Vertex> solution() {
        return solution;
    }

    public double solutionWeight() {
        return solutionWeight;
    }

    public int numStatesExplored() {
        return visited.size();
    }

    public double explorationTime() {
        return timeSpan;
    }

    private class AStarData {
        public Vertex vertFrom;
        public double bestWeight;

        public AStarData(Vertex v, double bw) {
            vertFrom = v;
            bestWeight = bw;
        }
    }

}
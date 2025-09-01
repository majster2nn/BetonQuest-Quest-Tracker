package majster2nn.dev.betonQuestQT.tracker.gps.utils;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;

public class PathFinder {
    private final Location startLocation;
    private Location endLocation;

    private final Node startNode;
    private Node endNode;

    private boolean pathFound = false;
    private final ArrayList<Node> checkedNodes = new ArrayList<>();
    private final ArrayList<Node> uncheckedNodes = new ArrayList<>();

    private final int maxNodeTests;
    private final boolean canClimbLadders;
    private final double maxFallDistance;

    // ---
    // CONSTRUCTORS
    // ---

    public PathFinder(Location start, Location end, int maxNodeTests, boolean canClimbLadders, double maxFallDistance) {
        this.startLocation = start;
        this.endLocation = end;

        startNode = new Node(startLocation, 0, null);
        endNode = new Node(endLocation, 0, null);

        this.maxNodeTests = maxNodeTests;
        this.canClimbLadders = canClimbLadders;
        this.maxFallDistance = maxFallDistance;
    }

    public Location[] findPath(){
        uncheckedNodes.clear();
        checkedNodes.clear();

        if(!(canStandAt(startLocation) && canStandAt(endLocation))) {
            return new Location[0];
        }

        uncheckedNodes.add(startNode);

        // cycle through untested nodes until a exit condition is fulfilled
        while(checkedNodes.size() < maxNodeTests && !pathFound && !uncheckedNodes.isEmpty())
        {
            Node n = uncheckedNodes.getFirst();
            for(Node nt : uncheckedNodes)
                if(nt.getEstimatedFinalExpense() < n.getEstimatedFinalExpense())
                    n = nt;

            if(n.estimatedExpenseLeft < 1)
            {
                pathFound = true;
                endNode = n;

//                Bukkit.broadcastMessage(uncheckedNodes.size() + "uc " + checkedNodes.size() + "c " + n.expense + "cne " + n.getEstimatedFinalExpense() + "cnee ");

                break;
            }

            n.getReachableLocations();
            uncheckedNodes.remove(n);
            checkedNodes.add(n);
        }

        // returning if no path has been found
        if(!pathFound)
        {
            return new Location[0];
        }

        // get length of path to create array, 1 because of start
        int length = 1;
        Node n = endNode;
        while(n.origin != null)
        {
            n = n.origin;
            length++;
        }

        Location[] locations = new Location[length];

        //fill Array
        n = endNode;
        for(int i = length - 1; i > 0; i --)
        {
            locations[i] = n.getLocation();
            n = n.origin;
        }

        locations[0] = startNode.getLocation();
        return locations;
    }

    private Node getNode(Location loc)
    {
        Node test = new Node(loc, 0, null);

        for(Node n : checkedNodes)
            if(n.id == test.id)
                return n;

        return test;
    }

    public class Node {
        private final Location location;
        public double id;

        public Node origin;

        public double expense;
        private double estimatedExpenseLeft = -1;

        // ---
        // CONSTRUCTORS
        // ---

        public Node(Location loc, double expense, Node origin)
        {
            location = loc;
            id = (long) loc.getBlockX() * 31_000_000_000L +
                    (long) loc.getBlockY() * 1_000_000L +
                    (long) loc.getBlockZ();


            this.origin = origin;

            this.expense = expense;
        }

        // ---
        // GETTERS
        // ---

        public Location getLocation()
        {
            return location;
        }

        public double getEstimatedFinalExpense()
        {
            if(estimatedExpenseLeft == -1)
                estimatedExpenseLeft = location.distance(endLocation);

            return expense + estimatedExpenseLeft * 3;
        }

        // ---
        // PATHFINDING
        // ---

        public void getReachableLocations()
        {
            //trying to get all possibly walkable blocks
            for(int z = -1; z <= 1; z++) {
                for (int x = -1; x <= 1; x++) {
                    if(!(x == 0 && z == 0)) {

                        if (x != 0 && z != 0) {
                            // Check if horizontal or vertical block is obstructed
                            Location horizontal = location.clone().add(x, 0, 0);
                            Location vertical = location.clone().add(0, 0, z);

                            if (isObstructed(horizontal) || isObstructed(vertical)) {
                                continue; // Skip this diagonal move
                            }
                        }

                        Location loc = new Location(location.getWorld(), location.getBlockX() + x, location.getBlockY(), location.getBlockZ() + z);

                        // usual unchanged y
                        if (canStandAt(loc)) {
                            reachNode(loc, expense + 1);
                        }

                        // one block up
                        if (!isObstructed(loc.clone().add(-x, 2, -z))) {
                            Location nLoc = loc.clone().add(0, 1, 0);
                            if (canStandAt(nLoc)) {
                                reachNode(nLoc, expense + 10);
                            }
                        }

                        // one block down or falling multiple blocks down
                        if (!isObstructed(loc.clone().add(0, 1, 0))) {
                            Location nLoc = loc.clone().add(0, -1, 0);
                            if (canStandAt(nLoc)) {
                                reachNode(nLoc, expense + 10);
                            } else if (!isObstructed(nLoc) && !isObstructed(nLoc.clone().add(0, 1, 0))) {
                                int drop = 1;
                                while (drop <= maxFallDistance && !isObstructed(loc.clone().add(0, -drop, 0))) {
                                    Location locF = loc.clone().add(0, -drop, 0);
                                    if (canStandAt(locF)) {
                                        Node fallNode = addFallNode(loc, expense + 1);

                                        fallNode.reachNode(locF, expense + drop * 2);
                                    }

                                    drop++;
                                }
                            }
                        }

                        //ladder
                        if (canClimbLadders) {
                            if (loc.clone().add(-x, 0, -z).getBlock().getType() == Material.LADDER) {
                                Location nLoc = loc.clone().add(-x, 0, -z);
                                int up = 1;
                                while (nLoc.clone().add(0, up, 0).getBlock().getType() == Material.LADDER)
                                    up++;

                                reachNode(nLoc.clone().add(0, up, 0), expense + up * 2);
                            }
                        }
                    }
                }
            }
        }

        public void reachNode(Location locThere, double expenseThere) {
            Node nt = getNode(locThere);

            if (nt.origin == null && nt != startNode) {
                nt.expense = expenseThere;
                nt.origin = this;
                uncheckedNodes.add(nt);
                return;
            }

            if (nt.expense > expenseThere) {
                nt.expense = expenseThere;
                nt.origin = this;

                // Force reprocessing with updated cost
                uncheckedNodes.remove(nt);
                uncheckedNodes.add(nt);
            }
        }




        public Node addFallNode(Location loc, double expense)
        {
            return new Node(loc, expense, this);
        }

    }

    public boolean isObstructed(Location loc)
    {
        if(loc == null || loc.getWorld() == null){
            return true;
        }
        return loc.getBlock().getType().isSolid();
    }

    public boolean canStandAt(Location loc)
    {
        return !(isObstructed(loc) || isObstructed(loc.clone().add(0, 1, 0)) || !isObstructed(loc.clone().add(0, -1, 0)));
    }
}

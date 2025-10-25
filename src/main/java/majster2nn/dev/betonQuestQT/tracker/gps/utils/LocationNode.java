package majster2nn.dev.betonQuestQT.tracker.gps.utils;

import org.bukkit.Location;

public class LocationNode {
    private Location location;
    private LocationNode previousNode;
    private LocationNode nextNode;

    public LocationNode getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(LocationNode previousNode) {
        this.previousNode = previousNode;
    }

    public LocationNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(LocationNode nextNode) {
        this.nextNode = nextNode;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

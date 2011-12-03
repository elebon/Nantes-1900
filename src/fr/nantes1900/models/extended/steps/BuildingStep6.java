package fr.nantes1900.models.extended.steps;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import fr.nantes1900.models.extended.Ground;
import fr.nantes1900.models.extended.Roof;
import fr.nantes1900.models.extended.Wall;

/**
 * Implements a building step : a state of the building. This step is after the
 * determination of the neighbours and before the sort of neighbours.
 * @author Daniel Lefevre
 */
public class BuildingStep6 extends AbstractBuildingStep {

    /**
     * The list of walls.
     */
    private List<Wall> walls = new ArrayList<>();

    /**
     * The list of roofs.
     */
    private List<Roof> roofs = new ArrayList<>();

    /**
     * Constructor.
     * @param wallsIn
     *            the list of walls
     * @param roofsIn
     *            the list of roofs
     */
    public BuildingStep6(final List<Wall> wallsIn, final List<Roof> roofsIn) {
        this.walls = wallsIn;
        this.roofs = roofsIn;
    }

    /**
     * Getter.
     * @return the list of roofs
     */
    public final List<Roof> getRoofs() {
        return this.roofs;
    }

    /**
     * Getter.
     * @return the list of walls
     */
    public final List<Wall> getWalls() {
        return this.walls;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.nantes1900.models.extended.steps.AbstractBuildingStep#launchProcess ()
     */
    @Override
    public final AbstractBuildingStep launchProcess() {
        // No more processs for now : this method do nothing.
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.nantes1900.models.extended.steps.AbstractBuildingStep#returnNode()
     */
    @Override
    public final DefaultMutableTreeNode returnNode(final int counter) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Building"
                + counter);

        int counterWall = 0;
        for (Wall w : this.walls) {
            w.setNodeString("Wall " + counterWall);
            root.add(new DefaultMutableTreeNode(w.returnNode()));
            counterWall++;
        }

        int counterRoof = 0;
        for (Roof r : this.roofs) {
            r.setNodeString("Wall " + counterRoof);
            root.add(new DefaultMutableTreeNode(r.returnNode()));
            counterRoof++;
        }

        return root;
    }

    /**
     * Setter.
     * @param groundIn
     *            the grounds
     */
    public final void setArguments(final Ground groundIn) {
    }
}

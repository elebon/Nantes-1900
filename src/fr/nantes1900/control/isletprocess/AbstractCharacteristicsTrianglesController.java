package fr.nantes1900.control.isletprocess;

import java.util.ArrayList;
import java.util.List;

import fr.nantes1900.models.basis.Triangle;

/**
 * Abstract class for triangles selection conrollers.
 * @author Camille Bouquet
 */
public abstract class AbstractCharacteristicsTrianglesController extends
        CharacteristicsController {

    /**
     * List of selected triangles.
     */
    protected ArrayList<Triangle> trianglesList;

    /**
     * Creates a new basic controller with the list of selected triangles.
     * @param parentControllerIn
     *            the parent controller
     * @param trianglesSelected
     *            the list of selected triangles
     */
    public AbstractCharacteristicsTrianglesController(
            final IsletProcessController parentControllerIn,
            final List<Triangle> trianglesSelected) {
        super(parentControllerIn);
        this.trianglesList = (ArrayList<Triangle>) trianglesSelected;
    }

    /**
     * Modifies the characteristics panel when the list is updated.
     */
    public abstract void modifyViewCharacteristics();

    /**
     * Gets the list of selected triangles.
     * @return the list of selected triangles.
     */
    public final ArrayList<Triangle> getTriangles() {
        return this.trianglesList;
    }
}

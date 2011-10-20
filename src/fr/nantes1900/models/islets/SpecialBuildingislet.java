package fr.nantes1900.models.islets;

import fr.nantes1900.models.middle.TriangleMesh;

/**
 * Implements a special building object as a container of a mesh.
 * 
 * @author Daniel Lefevre
 */
public class SpecialBuildingislet extends AbstractIslet {

    /**
     * The mesh describing the surface of the special building.
     */
    private TriangleMesh triangleMesh = new TriangleMesh();

    /**
     * Constructor. TODO : redo
     */
    public SpecialBuildingislet(TriangleMesh m) {
	super(m);
    }

    /**
     * Builds a special building from a mesh. Not implemented.
     * 
     * @param m
     *            the special building as a mesh
     */
    public final void buildFromMesh(final TriangleMesh m) {
	this.triangleMesh = m;
	// TODO : implement this method.
    }

    /**
     * Getter.
     * 
     * @return the mesh
     */
    public final TriangleMesh getMesh() {
	return this.triangleMesh;
    }

    /**
     * Treats the files of special buildings which are in the directory. Puts
     * them as meshes in the specialBuilding list.
     * 
     * @param directoryName
     *            the name of the directory where are the files
     */
    public void treatSpecialBuildings(final String directoryName) {
	// TODO
    }
}

package fr.nantes1900.models.extended;

import fr.nantes1900.constants.SeparationFloorBuilding;
import fr.nantes1900.models.Mesh;
import fr.nantes1900.models.basis.Edge.MoreThanTwoTrianglesPerEdgeException;
import fr.nantes1900.utils.Algos;
import fr.nantes1900.utils.Algos.NoFloorException;
import fr.nantes1900.utils.MatrixMethod;
import fr.nantes1900.utils.MatrixMethod.SingularMatrixException;
import fr.nantes1900.utils.ParserSTL;
import fr.nantes1900.utils.ParserSTL.BadFormedFileException;
import fr.nantes1900.utils.WriterCityGML;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.vecmath.Vector3d;

/**
 * Implements a class of town describing all the algorithms to parse and build a
 * town, using the building, mesh, floor, and other classes. Allows to write a
 * CityGML file containing this town.
 * 
 * @author Daniel Lefevre
 */
public class Town {

    private List<Building> industrials = new ArrayList<Building>();
    private List<Building> residentials = new ArrayList<Building>();
    private List<Floor> floors = new ArrayList<Floor>();
    private List<Floor> wateries = new ArrayList<Floor>();
    private List<SpecialBuilding> specialBuildings =
        new ArrayList<SpecialBuilding>();

    private double[][] matrix = new double[3][3];

    private static final Logger LOG = Logger.getLogger("logger");

    /**
     * Constructor.
     */
    public Town() {
        Town.LOG.setLevel(Level.FINEST);
        final Handler handler =
            new StreamHandler(System.out, new SimpleFormatter());
        handler.setLevel(Level.FINEST);
        Town.LOG.addHandler(handler);
        Town.LOG.setUseParentHandlers(false);
    }

    /**
     * Extract buildings by extracting the blocks after the floor extraction.
     * 
     * @param mesh
     *            the mesh to extract building in
     * @param noise
     *            the noise mesh to stock the noise
     * @return a list of buildings as meshes
     */
    private List<Mesh> buildingsExtraction(final Mesh mesh, final Mesh noise) {

        final List<Mesh> buildingList = new ArrayList<Mesh>();
        final List<Mesh> thingsList = new ArrayList<Mesh>();

        // Extraction of the buildings
        List<Mesh> formsList;
        try {
            formsList = Algos.blockExtract(mesh);

            // Separation of the little noises
            for (Mesh m : formsList) {
                if (m.size() > 1) {
                    thingsList.add(m);
                } else {
                    noise.addAll(m);
                }
            }
        } catch (MoreThanTwoTrianglesPerEdgeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Algorithm : detection of buildings considering their size
        int number = 0;
        for (Mesh m : thingsList) {
            number += m.size();
        }

        for (Mesh m : thingsList) {
            if (m.size() >= SeparationFloorBuilding.BLOCK_SIZE_ERROR
                * (double) number / (double) thingsList.size()) {

                buildingList.add(m);

            } else {

                noise.addAll(m);

            }
        }

        if (buildingList.size() == 0) {
            Town.LOG.severe("Error !");
        }

        return buildingList;
    }

    private void cleanResultDirectory(final String directoryName) {
        if (new File(directoryName + "/results/").exists()) {
            final File[] fileList =
                new File(directoryName + "/results/").listFiles();
            for (int i = 0; i < fileList.length; i++) {

                // Delete only the files, and the empty directories. Then it's
                // safe to put something in a directory Archives for example.
                fileList[i].delete();
            }
        } else {
            // Create the directory results if not already existing.
            try {
                new File(directoryName + "/results/").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create a change base matrix with the normal to the floor. See the
     * MatrixMethod for more informations.
     * 
     * @param normalFloor
     *            the vector to build the matrix.
     */
    private void createChangeBaseMatrix(final Vector3d normalFloor) {

        try {
            // Base change
            this.matrix = MatrixMethod.createOrthoBase(normalFloor);
            MatrixMethod.changeBase(normalFloor, this.matrix);

        } catch (SingularMatrixException e) {
            LOG.severe("Error : the matrix is badly formed !");
            System.exit(1);
        }
    }

    /**
     * Read the floor file and return the average normal as floor normal.
     * 
     * @param fileName
     *            the name of the floor file
     * @return the normal to the floor as Vector3d
     */
    private Vector3d extractFloorNormal(final String fileName) {

        try {
            ParserSTL parser = new ParserSTL(fileName);
            final Mesh floorBrut = parser.read();

            // Extract of the normal of the floor
            return floorBrut.averageNormal();

        } catch (BadFormedFileException e) {
            LOG.severe("Error : the file is badly formed !");
            System.exit(1);
            return null;

        } catch (IOException e) {
            LOG.severe("Error : file not found or unreadable !");
            System.exit(1);
            return null;
        }
    }

    /**
     * Extract the floors, using the floorExtract method.
     * 
     * @param mesh
     *            the mesh to extract from
     * @param normalFloor
     *            the normal to the floor
     * @return a mesh containing the floor
     * @throws NoFloorException
     *             if there is no floor-oriented triangle
     */
    private Mesh floorExtraction(final Mesh mesh, final Vector3d normalFloor) {
        // TODO? Return an exception if there is no floor in the mesh, such as :
        // please get some floors during the previous cut !

        // Searching for floor-oriented triangles with an error :
        // angleNormalErrorFactor
        final Mesh meshOriented =
            mesh.orientedAs(normalFloor,
                SeparationFloorBuilding.ANGLE_FLOOR_ERROR);

        // Floor-search algorithm
        // Select the lowest Triangle : it belongs to the floor
        // Take all of its neighbours
        // Select another Triangle which is not too high and repeat the above
        // step
        Mesh floors;
        try {
            floors =
                Algos.floorExtract(meshOriented,
                    SeparationFloorBuilding.ALTITUDE_ERROR);

            mesh.remove(floors);

            return floors;
        } catch (MoreThanTwoTrianglesPerEdgeException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Add the maximum of noise on floors to complete them. See block extract
     * method in Algos. After the completion of the floors, triangles are
     * removed from noise.
     * 
     * @param floors
     *            the floor
     * @param noise
     *            the noise mesh computed by former algorithms
     * @return a list of floors completed with noise
     */
    private List<Mesh> noiseTreatment(final Mesh floors, final Mesh noise) {

        final Mesh floorsAndNoise = new Mesh(floors);
        floorsAndNoise.addAll(noise);
        List<Mesh> floorsList;
        try {
            floorsList = Algos.blockExtract(floorsAndNoise);

            floors.clear();
            for (Mesh e : floorsList) {
                floors.addAll(e);
                noise.remove(e);
            }

            return floorsList;
        } catch (MoreThanTwoTrianglesPerEdgeException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    /**
     * Parse a STL file. Use the ParserSTL methods.
     * 
     * @param fileName
     *            the name of the file
     * @return the mesh parsed
     */
    private Mesh parseFile(final String fileName) {

        try {
            ParserSTL parser = new ParserSTL(fileName);
            return parser.read();

        } catch (BadFormedFileException e) {
            LOG.severe("Error : the file is badly formed !");
            System.exit(1);
            return null;

        } catch (IOException e) {
            LOG.severe("Error : file not found or unreadable !");
            System.exit(1);
            return null;
        }
    }

    /**
     * Treat the files of floors which are in the directory. Create Floor
     * objects for each files, put an attribute, and call the buildFromMesh
     * method of Floor. Then add it to the list of floors.
     * 
     * @param directoryName
     *            the directory name to find the floors.
     */
    private void treatFloors(final String directoryName) {
        int counterFloors = 1;

        while (new File(directoryName + "floor - " + counterFloors + ".stl")
            .exists()) {

            final Floor floor = new Floor("floor");

            floor.buildFromMesh(this.parseFile(directoryName + "floor - "
                + counterFloors + ".stl"));

            floor.getMesh().changeBase(this.matrix);

            this.addFloor(floor);

            counterFloors++;
        }
    }

    /**
     * Treat the files of industrial zones which are in the directory. Separate
     * floors and buildings, build a Floor object and call buildFromMesh method,
     * build Building objects, put the buildings in and call the buildFromMesh
     * methods.
     * 
     * @param directoryName
     *            the name of the directory where are the files
     * @param normalFloor
     *            the normal to the floor
     */
    private void treatIndustrials(final String directoryName,
        final Vector3d normalFloor) {
        // FIXME : maybe create the same private method for things common with
        // treat residentials...
        // TODO? different coefficients than residentials.
    }

    /**
     * Treat the files of residential zones which are in the directory. Separate
     * floors and buildings, build a Floor object and call buildFromMesh method,
     * build Building objects, put the buildings in and call the buildFromMesh
     * methods.
     * 
     * @param directoryName
     *            the name of the directory where are the files
     * @param normalGravityOriented
     *            the normal oriented as the gravity vector oriented to the sky
     */
    private void treatResidentials(final String directoryName,
        final Vector3d normalGravityOriented) {
        int counterResidentials = 1;

        while (new File(directoryName + "residential - " + counterResidentials
            + ".stl").exists()) {

            final Mesh mesh =
                this.parseFile((directoryName + "residential - "
                    + counterResidentials + ".stl"));

            Vector3d realNormalToTheFloor;
            if (new File(directoryName + "floor - " + counterResidentials
                + ".stl").exists()) {
                realNormalToTheFloor =
                    this.extractFloorNormal(directoryName + "floor - "
                        + counterResidentials + ".stl");
                MatrixMethod.changeBase(realNormalToTheFloor, this.matrix);
            } else {
                realNormalToTheFloor = normalGravityOriented;
            }

            // FIXME FIXME FIXME : after extracting the
            // normalGravityOriented, then make the change base on each mesh
            // and assume that this normal is (0, 0, 1).
            // Then the real normalFloor will not be confonded.
            // Make it on all the source codes !

            // FIXME FIXME FIXME : the zMinPoint method is not adapted to
            // extract the floor... It should be a new realNormalToTheFloor z...
            mesh.changeBase(this.matrix);

            final Mesh noise = new Mesh();

            Mesh wholeFloor = this.floorExtraction(mesh, realNormalToTheFloor);
            final List<Mesh> buildings = this.buildingsExtraction(mesh, noise);

            final List<Mesh> floors = this.noiseTreatment(wholeFloor, noise);

            // FIXME : code this method !
            // ArrayList<Mesh> formsList =
            // this.carveRealBuildings(buildings);

            for (Mesh m : buildings) {

                final Building e = new Building();
                e.buildFromMesh(m, wholeFloor, normalGravityOriented);
                this.addResidential(e);
            }

            wholeFloor = new Mesh();
            for (Mesh m : floors) {
                wholeFloor.addAll(m);
            }

            int counterFloor = 1;
            for (Mesh m : floors) {
                m.writeSTL("Files/floor - " + counterFloor + ".stl");
                final Floor floor = new Floor("street");
                floor.buildFromMesh(m);
                this.addFloor(floor);
                counterFloor++;
            }

            // FIXME : I don't know what to do with the formsList : little
            // walls, forms on the ground...

            counterResidentials++;
        }
    }

    /**
     * Treat the files of special buildings which are in the directory. Put them
     * as meshes in the specialBuilding list.
     * 
     * @param directoryName
     *            the name of the directory where are the files
     */
    private void treatSpecialBuildings(final String directoryName,
        final Vector3d normalFloor) {
        int counterSpecialBuildings = 1;

        while (new File(directoryName + "special_building - "
            + counterSpecialBuildings + ".stl").exists()) {

            final Mesh meshSpecialBuilding =
                this.parseFile((directoryName + "special_building - "
                    + counterSpecialBuildings + ".stl"));

            meshSpecialBuilding.changeBase(this.matrix);

            // TODO : improve this by extracting floors.

            final SpecialBuilding specialBuilding = new SpecialBuilding();

            specialBuilding.buildFromMesh(meshSpecialBuilding);

            this.addSpecialBuilding(specialBuilding);

            counterSpecialBuildings++;
        }
    }

    /**
     * Treat the files of wateries which are in the directory. Create Floor
     * objects for each files, put an attribute : Water, and call the
     * buildFromMesh method of Floor. Then add it to the list of wateries.
     * 
     * @param directoryName
     *            the directory name to find the wateries.
     */
    private void treatWateries(final String directoryName) {
        // FIXME : see in treatFloors, it's the same. Maybe create a private
        // common shared method.
    }

    /**
     * Add a floor to the attribute list of floors.
     * 
     * @param floor
     *            the floor to add
     */
    public final void addFloor(final Floor floor) {
        if (!this.floors.contains(floor)) {
            this.floors.add(floor);
        }
    }

    /**
     * Add an industrial building to the attribute list of industrials.
     * 
     * @param building
     *            the industrial to add
     */
    public final void addIndustrial(final Building building) {
        if (!this.industrials.contains(building)) {
            this.industrials.add(building);
        }
    }

    /**
     * Add a residential building to the attribute list of residentials.
     * 
     * @param building
     *            the residential to add
     */
    public final void addResidential(final Building building) {
        if (!this.residentials.contains(building)) {
            this.residentials.add(building);
        }
    }

    /**
     * Add a special building to the attribute list of special buildings.
     * 
     * @param specialBuilding
     *            the special building to add
     */
    public final void addSpecialBuilding(final SpecialBuilding specialBuilding) {
        if (!this.specialBuildings.contains(specialBuilding)) {
            this.specialBuildings.add(specialBuilding);
        }
    }

    /**
     * Add a watery to the attribute list of wateries.
     * 
     * @param watery
     *            the watery to add
     */
    public final void addWatery(final Floor watery) {
        if (!this.floors.contains(watery)) {
            this.floors.add(watery);
        }
    }

    /**
     * Build a town by computing all the files in the directory. Search in the
     * directory name the fives directories : inductrials, residentials, floors,
     * wateries, and specialBuildings, and treat each files and put them in the
     * lists.
     * 
     * @param directoryName
     *            the directory name where are the five directories
     */
    public final void buildFromMesh(final String directoryName) {

        final long time = System.nanoTime();

        // Create or clean the directory : directoryName + "/results/" to put
        // the datas in.
        this.cleanResultDirectory(directoryName);

        final Vector3d normalFloor =
            this.extractFloorNormal(directoryName + "/floor.stl");
        this.createChangeBaseMatrix(normalFloor);

        this.treatFloors(directoryName + "/floors/");
        LOG.info("Numbers of floors : " + this.floors.size());

        this.treatWateries(directoryName + "/wateries/");
        LOG.info("Numbers of wateries : " + this.wateries.size());

        this.treatSpecialBuildings(directoryName + "/special_buildings/",
            normalFloor);
        LOG.info("Numbers of special buildings : "
            + this.specialBuildings.size());

        this.treatIndustrials(directoryName + "/industrials/", normalFloor);
        LOG.info("Numbers of industrials zones : " + this.industrials.size());

        this.treatResidentials(directoryName + "/residentials/", normalFloor);
        LOG.info("Numbers of residentials zones : " + this.residentials.size());

        LOG.info("Temps total écoulé : " + (System.nanoTime() - time));
    }

    /**
     * Write the town in a CityGML file. Use the WriterCityGML.
     * 
     * @param fileName
     *            the name of the file to write in
     */
    public final void writeCityGML(final String fileName) {
        final WriterCityGML writer = new WriterCityGML(fileName);

        writer.addBuildings(this.residentials);
        writer.addBuildings(this.industrials);
        writer.addFloors(this.floors);
        writer.addFloors(this.wateries);
        writer.addSpecialBuildings(this.specialBuildings);

        writer.write();
    }

    public final void writeSTL(final String directoryName) {
        this.writeSTLFloors(directoryName);
        this.writeSTLIndustrials(directoryName);
        this.writeSTLResidentials(directoryName);
        this.writeSTLSpecialBuildings(directoryName);
        this.writeSTLWateries(directoryName);
    }

    /**
     * Write the floors as STL files. Use for debugging.
     */
    public final void writeSTLFloors(final String directoryName) {
        int counterFloor = 1;

        for (Floor f : this.floors) {
            f.writeSTL(directoryName + "floor - " + counterFloor + ".stl");
            counterFloor++;
        }
    }

    /**
     * Write the industrial buildings as STL files. Use for debugging.
     */
    public final void writeSTLIndustrials(final String directoryName) {
        int buildingCounter = 1;

        for (Building m : this.industrials) {
            m.writeSTL(directoryName + "building - " + buildingCounter + ".stl");
            buildingCounter++;
        }
    }

    /**
     * Write the residential buildings as STL files. Use for debugging.
     */
    public final void writeSTLResidentials(final String directoryName) {
        int buildingCounter = 1;

        for (Building m : this.residentials) {
            m.writeSTL(directoryName + "/building - " + buildingCounter
                + ".stl");
            buildingCounter++;
        }
    }

    /**
     * Write the special buildings as STL files. Use for debugging.
     */
    public final void writeSTLSpecialBuildings(final String directoryName) {
        int buildingCounter = 1;

        for (SpecialBuilding m : this.specialBuildings) {
            m.getMesh().writeSTL(
                directoryName + "special_building - " + buildingCounter
                    + ".stl");
            buildingCounter++;
        }
    }

    /**
     * Write the wateries as STL files. Use for debugging.
     */
    public final void writeSTLWateries(final String directoryName) {
        int counterWateries = 1;

        for (Floor f : this.wateries) {
            f.writeSTL(directoryName + "/watery - " + counterWateries + ".stl");
            counterWateries++;
        }
    }
}

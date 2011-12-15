package fr.nantes1900.view.display3d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

import fr.nantes1900.control.display3d.NewMouseRotate;
import fr.nantes1900.control.display3d.Universe3DController;
import fr.nantes1900.models.basis.Point;
import fr.nantes1900.models.extended.Surface;

/**
 * The Universe3DView generated by a U3DController.
 * @author Siju Wu & Nicolas Bouillon.
 */

public class Universe3DView extends JPanel {

    /**
     * The toolbar to interact with the 3DView.
     */
    private JToolBar toolbar;
    /**
     * Version ID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The list to SAVE all the surfaceView.
     */
    private ArrayList<SurfaceView> surfaceViewList = new ArrayList<>();
    /**
     * The Universe3DController attached.
     */
    private Universe3DController u3DController;
    /**
     * The universe.
     */
    private SimpleUniverse simpleUniverse;
    /**
     * Constant defining the drawing BACK of the camera when initializing the
     * 3DView.
     */
    public static final double TRANSLATION_CAMERA_Z_DIRECTION = 300;
    /**
     * Constant defining the 3DView panel height.
     */
    public static final int PANEL_HEIGHT = 450;
    /**
     * Constant defining the 3DView panel width.
     */
    public static final int PANEL_WIDTH = 800;
    /**
     * Constant defining the range where the lights have an effect.
     */
    public static final int LIGHT_BOUND_RADIUS = 100000;
    /**
     * Constant defining the range where the transformations (rotation,
     * translation, zoom) have an effect.
     */
    public static final int BOUNDING_RADIUS = 100000;
    /**
     * Constant defining the range where objects displayed are visible.
     */
    public static final int BACKCLIP_DISTANCE = 100000;
    /**
     * Constant defining the sensitivity of the zoom transformation.
     */
    public static final double ZOOM_FACTOR = 2;

    /**
     * Constant defining the sensitivity of the wheelzoom transformation.
     */
    public static final double WHEEL_ZOOM_FACTOR = 3;
    /**
     * Constant defining the sensitivity of the zoom transformation.
     */
    public static final double TRANSLATION_FACTOR = 1.2;

    /**
     * Creates a new universe. Sets the Canvas3D and the panel size.
     * @param u3DControllerIn
     *            the controller that generates this view.
     */
    public Universe3DView(final Universe3DController u3DControllerIn) {
        this.u3DController = u3DControllerIn;
        this.setLayout(new BorderLayout());

        Canvas3D c = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        this.add(c, BorderLayout.CENTER);

        // Setups the SimpleUniverse, attachss the Canvas3D
        this.simpleUniverse = new SimpleUniverse(c);
        this.getSimpleUniverse().getCanvas().addMouseListener(u3DControllerIn);
        this.getSimpleUniverse().getCanvas()
                .addMouseMotionListener(u3DControllerIn);

        // Size to show the panel while there is nothing to show
        this.setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
    }

    /**
     * Adds some surfaces to the surfaces already displayed.
     * @param surfaces
     *            the list of surfaces to add
     */
    public final void addSurfaces(final List<Surface> surfaces) {
        this.u3DController.setSurfaceLocked(null);
        this.u3DController.setSurfaceLockedNeighbours(null);
        this.u3DController.setLockMode(false);
        
        if (this.u3DController.getDisplayMode() == Universe3DController.DISPLAY_MESH_MODE)
        {
            this.displayMeshes(surfaces);
        } else if (this.u3DController.getDisplayMode() == Universe3DController.DISPLAY_POLYGON_MODE)
        {
            this.displayPolygons(surfaces);
        } else
        {
            // If the display mode is not well initialized.
            this.u3DController
                    .setDisplayMode(Universe3DController.DISPLAY_MESH_MODE);
            this.displayMeshes(surfaces);
        }

        TransformGroup transformGroup = createTransformGroup(this.surfaceViewList);
        this.simpleUniverse.addBranchGraph(this
                .createSceneGraph(transformGroup));

        // Computes the centroid of the first surface.
        Point centroid = surfaces.get(0).getMesh().getCentroid();

        // Translates the camera.
        this.translateCamera(centroid.getX(), centroid.getY(), centroid.getZ()
                + TRANSLATION_CAMERA_Z_DIRECTION);

        // Changes the rotation center
        this.u3DController.getMouseRotate().setCenter(centroid);

    }

    /**
     * Removes everything displayed.
     */
    public final void clearAll() {
        Canvas3D c = this.simpleUniverse.getCanvas();
        this.simpleUniverse.cleanup();
        this.simpleUniverse = new SimpleUniverse(c);
        this.surfaceViewList.clear();
        c.getView().setBackClipDistance(BACKCLIP_DISTANCE);
    }

    /**
     * Creates a new branchGroup containing the lights the pickCanvas.
     * @param transformGroup
     *            The transformGroup attached to the universe.
     * @return The generated branchGroup.
     */
    private BranchGroup createSceneGraph(final TransformGroup transformGroup) {
        BranchGroup objRoot = new BranchGroup();
        objRoot.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        objRoot.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        objRoot.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        objRoot.setCapability(Group.ALLOW_CHILDREN_WRITE);

        this.u3DController.setPickCanvas(objRoot);

        this.createLights(objRoot);
        objRoot.addChild(transformGroup);

        objRoot.compile();
        return objRoot;
    }

    /**
     * Generates the different lights to put in the universe. These lights
     * combined to materials and textures of the objects to display make them
     * visible.
     * @param objRoot
     *            The branchGroup to put the lights in.
     */

    private void createLights(BranchGroup objRoot) {

        // Light bound
        BoundingSphere lightBounds = new BoundingSphere(new Point3d(0.0, 0.0,
                0.0), LIGHT_BOUND_RADIUS);
        // Ambient light
        AmbientLight ambLight = new AmbientLight(true, new Color3f(1.0f, 1.0f,
                1.0f));
        ambLight.setInfluencingBounds(lightBounds);
        // Directional light
        DirectionalLight headLight = new DirectionalLight(new Color3f(
                Color.white), new Vector3f(1.0f, -1.0f, -1.0f));
        headLight.setInfluencingBounds(lightBounds);

        objRoot.addChild(ambLight);
        objRoot.addChild(headLight);

    }

    /**
     * Creates a transformGroup containing the transforamtions (translation,
     * rotation, zoom) and the objects to display (in a specific branchGroup).
     * @param surfaceView
     *            The list containing the surfaceViews to display.
     * @return The transformGroup created.
     */

    private TransformGroup createTransformGroup(
            final ArrayList<SurfaceView> surfaceView) {
        BoundingSphere boundingSphere = new BoundingSphere(new Point3d(0.0,
                0.0, 0.0), BOUNDING_RADIUS);

        TransformGroup transformGroup = new TransformGroup();
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        TransformGroup translationGroup1 = new TransformGroup();
        translationGroup1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        translationGroup1.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        transformGroup.addChild(translationGroup1);

        TransformGroup rotationGroup = new TransformGroup();
        rotationGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        rotationGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        translationGroup1.addChild(rotationGroup);

        TransformGroup translationGroup2 = new TransformGroup();
        translationGroup2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        translationGroup2.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        rotationGroup.addChild(translationGroup2);

        BranchGroup sceneRoot = new BranchGroup();

        for (SurfaceView surface : this.surfaceViewList)
        {
            sceneRoot.addChild(surface);
        }
        translationGroup2.addChild(sceneRoot);

        // Links the left button of the mouse with a rotation transformation
        NewMouseRotate mouseRotate = new NewMouseRotate(translationGroup1,
                rotationGroup, translationGroup2);
        mouseRotate.setSchedulingBounds(boundingSphere);
        translationGroup2.addChild(mouseRotate);
        this.u3DController.setMouseRotate(mouseRotate);

        // Links the middle button of the mouse with a zoom transformation
        MouseZoom mouseZoom = new MouseZoom();
        mouseZoom.setFactor(ZOOM_FACTOR);
        mouseZoom.setTransformGroup(transformGroup);
        transformGroup.addChild(mouseZoom);
        mouseZoom.setSchedulingBounds(boundingSphere);

        // Links the wheel of the mouse with a zoom transformation
        MouseWheelZoom mouseWheelZoom = new MouseWheelZoom();
        mouseWheelZoom.setFactor(WHEEL_ZOOM_FACTOR);
        mouseWheelZoom.setTransformGroup(transformGroup);
        transformGroup.addChild(mouseWheelZoom);
        mouseWheelZoom.setSchedulingBounds(boundingSphere);

        // Links the right button of the mouse with a translation transformation
        MouseTranslate mouseTranslate = new MouseTranslate();
        mouseTranslate.setFactor(TRANSLATION_FACTOR);
        mouseTranslate.setTransformGroup(transformGroup);
        transformGroup.addChild(mouseTranslate);
        mouseTranslate.setSchedulingBounds(boundingSphere);

        return transformGroup;
    }

    /**
     * Generates the set of surfaceViews corresponding to the set of surfaces in
     * parameter. Also sets the meshView attributes in the surfaceViews just
     * created.
     * @param surfacesList
     *            The list of surfaces containing the MESHES to display.
     */
    private void displayMeshes(final List<Surface> surfacesList) {
       
        for (Surface surface : surfacesList)
        {   
            
            
            SurfaceView surfaceView = new SurfaceView(surface);

            MeshView meshView = new MeshView(surface.getMesh());
            surfaceView.setMeshView(meshView);


            surfaceView.showMeshView();
            this.surfaceViewList.add(surfaceView);
        }
        
    }

    /**
     * Generates the set of surfaceViews corresponding to the set of surfaces in
     * parameter. Also sets the polygonView attributes in the surfaceViews just
     * created.
     * @param surfacesList
     *            The list of surfaces containing the MESHES to display.
     */
    private void displayPolygons(final List<Surface> surfacesList) {
       
        for (Surface surface : surfacesList)
        {
           
            SurfaceView surfaceView = new SurfaceView(surface);
            if (surface.getPolygon() != null)
            {
                PolygonView polygonView = new PolygonView(surface.getPolygon());
                surfaceView.setPolygonView(polygonView);
               
            }
            surfaceView.showPolygonView();
            this.surfaceViewList.add(surfaceView);
        }
       
    }

    /**
     * Getter.
     * @return the simple universe
     */
    public final SimpleUniverse getSimpleUniverse() {
        return this.simpleUniverse;
    }

    /**
     * Getter.
     * @return the list of the SurfaceViews
     */
    public final ArrayList<SurfaceView> getSurfaceViewList() {
        return this.surfaceViewList;
    }

    /**
     * Getter.
     * @return the Toolbar
     */
    public final JToolBar getToolbar() {
        return this.toolbar;
    }

    /**
     * Sets the new toolbar to control the 3d universe.
     * @param newToolbar
     *            The new toolbar.
     */
    public final void setToolbar(final JToolBar newToolbar) {
        if (this.toolbar != null)
        {
            this.remove(this.toolbar);
        }
        this.toolbar = newToolbar;
        this.add(newToolbar, BorderLayout.SOUTH);
    }

    /**
     * Translate the position of the camera.
     * @param x
     *            The x coordinate of the camera.
     * @param y
     *            The y coordinate of the camera.
     * @param z
     *            The z coordinate of the camera.
     */
    private void
            translateCamera(final double x, final double y, final double z) {
        // Gets the camera.
        ViewingPlatform camera = this.simpleUniverse.getViewingPlatform();
        TransformGroup cameraTransformGroup = camera.getMultiTransformGroup()
                .getTransformGroup(0);

        Transform3D cameraTranslation = new Transform3D();
        cameraTransformGroup.getTransform(cameraTranslation);

        // Sets the position of the camera.
        cameraTranslation.setTranslation(new Vector3d(x, y, z));
        cameraTransformGroup.setTransform(cameraTranslation);
    }

}

package fr.nantes1900.models.islets.buildings.steps;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.vecmath.Vector3d;

import fr.nantes1900.models.extended.Building;
import fr.nantes1900.models.extended.Ground;
import fr.nantes1900.models.extended.Surface;

public class BuildingsIsletStep4 extends AbstractBuildingsIsletStep
{

    private List<Building> buildings;
    private Ground         grounds;
    private Surface        groundForAlgorithm;
    private Vector3d       groundNormal;

    public BuildingsIsletStep4(List<Building> cutBuildings, Ground groundsIn)
    {
        this.buildings = cutBuildings;
        this.grounds = groundsIn;
    }

    public List<Building> getBuildings()
    {
        return buildings;
    }

    public Ground getGrounds()
    {
        return grounds;
    }

    /**
     * TODO. SeparationWallsAndSeparationRoofs
     * @return TODO.
     */
    @Override
    public final BuildingsIsletStep5 launchTreatment()
    {
        this.groundForAlgorithm = new Surface(this.grounds);

        for (Building b : this.buildings)
        {
            b.launchTreatment();
        }

        return new BuildingsIsletStep5(this.buildings, this.grounds);
    }

    @Override
    public DefaultMutableTreeNode returnNode()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(this);
        for (Building b : this.buildings)
        {
            root.add(b.returnNode());
        }
        DefaultMutableTreeNode nodeG = new DefaultMutableTreeNode(this.grounds);

        root.add(nodeG);

        return root;
    }

    public void setArguments(Vector3d groundNormalIn)
    {
        this.groundNormal = groundNormalIn;
    }
}

package fr.nantes1900.models.extended;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.vecmath.Vector3d;

import fr.nantes1900.models.basis.Mesh;
import fr.nantes1900.models.extended.steps.BuildingStep3;
import fr.nantes1900.models.extended.steps.BuildingStep4;
import fr.nantes1900.models.extended.steps.BuildingStep5;
import fr.nantes1900.models.extended.steps.BuildingStep6;
import fr.nantes1900.models.extended.steps.BuildingStep7;
import fr.nantes1900.models.extended.steps.BuildingStep8;
import fr.nantes1900.models.islets.buildings.exceptions.NullArgumentException;

/**
 * Implements a building as containing 6 steps representing the state of the
 * building during the treatments 3 to 8.
 * @author Daniel Lefevre
 */
public class Building
{

    /**
     * The third building step.
     */
    private BuildingStep3 bStep3;
    /**
     * The fourth building step.
     */
    private BuildingStep4 bStep4;
    /**
     * The fifth building step.
     */
    private BuildingStep5 bStep5;
    /**
     * The sixth building step.
     */
    private BuildingStep6 bStep6;
    /**
     * The seventh building step.
     */
    private BuildingStep7 bStep7;
    /**
     * The eighth building step.
     */
    private BuildingStep8 bStep8;

    /**
     * The gravity normal.
     */
    private Vector3d      gravityNormal;

    /**
     * The ground.
     */
    private Ground        grounds;
    /**
     * The normal to the ground.
     */
    private Vector3d      groundNormal;
    /**
     * The noise.
     */
    private Mesh          noise;

    /**
     * Constructor.
     * @param mesh
     *            the mesh representing the building
     */
    public Building(final Mesh mesh)
    {
        this.bStep3 = new BuildingStep3(mesh);
    }

    /**
     * Getter.
     * @return the third step
     */
    public final BuildingStep3 getbStep3()
    {
        return this.bStep3;
    }

    /**
     * Getter.
     * @return the fourth step
     */
    public final BuildingStep4 getbStep4()
    {
        return this.bStep4;
    }

    /**
     * Getter.
     * @return the fifth step
     */
    public final BuildingStep5 getbStep5()
    {
        return this.bStep5;
    }

    /**
     * Getter.
     * @return the sixth step
     */
    public final BuildingStep6 getbStep6()
    {
        return this.bStep6;
    }

    /**
     * Getter.
     * @return the seventh step
     */
    public final BuildingStep7 getbStep7()
    {
        return this.bStep7;
    }

    /**
     * Getter.
     * @return the eighth step
     */
    public final BuildingStep8 getbStep8()
    {
        return this.bStep8;
    }

    /**
     * TODO.
     * @throws NullArgumentException
     */
    public final void launchTreatment3() throws NullArgumentException
    {
        this.bStep3.setArguments(this.gravityNormal);
        this.bStep4 = this.bStep3.launchTreatment();
    }

    public final void launchTreatment4() throws NullArgumentException
    {
        this.bStep4.setArguments(this.groundNormal, this.grounds, this.noise);
        this.bStep5 = this.bStep4.launchTreatment();
    }

    public final void launchTreatment5() throws NullArgumentException
    {
        this.bStep5.setArguments(this.noise, this.grounds);
        this.bStep6 = this.bStep5.launchTreatment();
    }

    public final void launchTreatment6() throws NullArgumentException
    {
        this.bStep6.setArguments(this.grounds);
        this.bStep7 = this.bStep6.launchTreatment();
    }

    public final void launchTreatment7() throws NullArgumentException
    {
        this.bStep7.setArguments(this.groundNormal);
        this.bStep8 = this.bStep7.launchTreatment();
    }

    /**
     * TODO.
     * @return the mutable tree node
     */
    public final DefaultMutableTreeNode returnNode3()
    {
        return this.bStep3.returnNode();
    }

    public final DefaultMutableTreeNode returnNode4()
    {
        return this.bStep4.returnNode();
    }

    public final DefaultMutableTreeNode returnNode5()
    {
        return this.bStep5.returnNode();
    }

    public final DefaultMutableTreeNode returnNode6()
    {
        return this.bStep6.returnNode();
    }

    public final DefaultMutableTreeNode returnNode7()
    {
        return this.bStep7.returnNode();
    }

    public final DefaultMutableTreeNode returnNode8()
    {
        return this.bStep8.returnNode();
    }

    /**
     * Setter.
     * @param groundNormalIn
     *            the normal to the ground
     * @param gravityNormalIn
     *            the gravity normal
     * @param groundsIn
     *            the grounds
     * @param noiseIn
     *            the noise
     */
    public final void setArguments(final Vector3d groundNormalIn,
            final Vector3d gravityNormalIn,
            final Ground groundsIn,
            final Mesh noiseIn)
    {
        this.groundNormal = groundNormalIn;
        this.gravityNormal = gravityNormalIn;
        this.grounds = groundsIn;
        this.noise = noiseIn;
    }
}

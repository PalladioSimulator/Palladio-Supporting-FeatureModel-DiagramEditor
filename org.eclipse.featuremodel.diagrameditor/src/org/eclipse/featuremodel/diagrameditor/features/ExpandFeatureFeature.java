package org.eclipse.featuremodel.diagrameditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.diagrameditor.utilities.Properties;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IMoveShapeFeature;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

/**
 * Feature handle expanding of Feature Diagram elements. A Features with child Features can be
 * collapsed (see {@link CollapseFeatureFeature}) to hide the child elements and expanded (see
 * {@link ExpandFeatureFeature}) to show this.
 * 
 * @author Alexander Moor
 */
public class ExpandFeatureFeature extends AbstractCustomFeature {

    /**
     * Creates an instance of {@link ExpandFeatureFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public ExpandFeatureFeature(IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Gets the name of this function feature.
     * 
     * @return The name.
     */
    @Override
    public String getName() {
        return "Expand";
    }

    /**
     * Gets the description of this function feature.
     * 
     * @return The description.
     */
    @Override
    public String getDescription() {
        return "Expand child Features";
    }

    /**
     * Decides if the expand feature is available with the given context. This implementation
     * returns <code>true</code> if the selected pictogram element represents a Feature.
     * 
     * @param context
     *            the context.
     * @return true if collapse feature is available, false if not
     */
    @Override
    public boolean isAvailable(IContext context) {
        if (context instanceof ICustomContext) {
            PictogramElement[] pes = ((ICustomContext) context).getPictogramElements();
            if (pes != null && pes.length == 1) {
                Object bo = getBusinessObjectForPictogramElement(pes[0]);
                if (bo instanceof Feature) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the current pictogram element of the given context can be expanded. This
     * implementation returns <code>true</code> if the selected pictogram element represents a
     * Feature, it has child Features and is of the type
     * {@link Properties#PROP_VAL_CONTAINER_TYPE_COLLAPSED}.
     * 
     * @param context
     *            The context.
     * @return true if the selected pictogram element represents a Feature, it has child Features
     *         and is of the type {@link Properties#PROP_VAL_CONTAINER_TYPE_COLLAPSED}.
     */
    @Override
    public boolean canExecute(ICustomContext context) {
        if (context instanceof ICustomContext) {
            PictogramElement[] pes = ((ICustomContext) context).getPictogramElements();
            // allow to expand of Feature if exactly one pictogram element is selected
            if (pes != null && pes.length == 1) {
                Object bo = getBusinessObjectForPictogramElement(pes[0]);
                // allow to expand only Feature objects with children
                if (bo instanceof Feature && !((Feature) bo).getChildren().isEmpty()) {
                    String value = Graphiti.getPeService().getPropertyValue(pes[0], Properties.PROP_KEY_CONTAINER_TYPE);
                    // allow to expand only Feature of type collapsed
                    if (value != null && Properties.PROP_VAL_CONTAINER_TYPE_COLLAPSED.equals(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Collapse the current Feature.
     * 
     * @param context
     *            the context
     */
    @Override
    public void execute(ICustomContext context) {
        // get current pictogram element
        PictogramElement selectedPE = context.getPictogramElements()[0];
        Feature feature = (Feature) getBusinessObjectForPictogramElement(selectedPE);

        // get the current position of the Feature to expand
        int x = selectedPE.getGraphicsAlgorithm().getX();
        int y = selectedPE.getGraphicsAlgorithm().getY();
        // get the initial position of the Feature was set by collapsing
        String oldX = Graphiti.getPeService().getPropertyValue(selectedPE, Properties.PROP_KEY_CONTAINER_INIT_X);
        String oldY = Graphiti.getPeService().getPropertyValue(selectedPE, Properties.PROP_KEY_CONTAINER_INIT_Y);
        // delta to move child Features
        int deltaX = x - Integer.valueOf(oldX);
        int deltaY = y - Integer.valueOf(oldY);

        // collapse all child elements and update child positions by the given deltas.
        expand(feature, deltaX, deltaY);

        // set the type of the given Feature pictogram element to expanded
        Graphiti.getPeService().setPropertyValue(selectedPE, Properties.PROP_KEY_CONTAINER_TYPE,
                Properties.PROP_VAL_CONTAINER_TYPE_EXPANDED);

        // disable expand sign
        disableExpandSign(feature);
    }

    /**
     * Disable the expand sign.
     * 
     * @param feature
     *            the Feature
     */
    private void disableExpandSign(Feature feature) {
        PictogramElement[] peList = getFeatureProvider().getAllPictogramElementsForBusinessObject(feature);

        // look for a container of the type expand sign and disable it
        for (PictogramElement pe : peList) {
            String value = Graphiti.getPeService().getPropertyValue(pe, Properties.PROP_KEY_CONTAINER_TYPE);

            if (value != null && Properties.PROP_VAL_CONTAINER_TYPE_EXPANDSIGN.equals(value)) {
                pe.setVisible(false);
                break;
            }
        }
    }

    /**
     * Expand recursively all child elements of the given Feature and update child positions by the
     * given deltas.
     * 
     * @param parent
     *            the parent Feature
     * @param deltaX
     *            delta in X direction
     * @param deltaY
     *            delta in Y direction
     */
    private void expand(Feature parent, int deltaX, int deltaY) {
        // expand all child Groups
        for (Group grp : parent.getChildren()) {
            enableGroupPictogramElements(grp);
            // expand all child Features
            for (Feature child : grp.getFeatures()) {
                enableFeaturePictogramElements(child);
                moveFeature(child, deltaX, deltaY);
                // if a child Feature was already collapsed don't expand his child Features
                if (!isCollapsed(child)) {
                    expand(child, deltaX, deltaY);
                }
            }
        }
    }

    /**
     * Moves the given Feature by the given deltas.
     * 
     * @param feature
     *            the Feature to move
     * @param deltaX
     *            delta in X direction
     * @param deltaY
     *            delta in Y direction
     */
    private void moveFeature(Feature feature, int deltaX, int deltaY) {
        PictogramElement pe = getFeatureProvider().getPictogramElementForBusinessObject(feature);

        // get the current position of the Feature pictogram element
        int x = pe.getGraphicsAlgorithm().getX();
        int y = pe.getGraphicsAlgorithm().getY();

        // create context to move the Featrue pictogram element
        MoveShapeContext msc = new MoveShapeContext((Shape) pe);
        msc.setX(x + deltaX);
        msc.setY(y + deltaY);
        msc.setTargetContainer(getDiagram());

        // get for the created context suitable move feature and execute it
        IMoveShapeFeature moveFeature = getFeatureProvider().getMoveShapeFeature(msc);
        moveFeature.moveShape(msc);
    }

    /**
     * Checks whether the pictogram element of the given Feature is collapsed.
     * 
     * @param feature
     *            the Feature
     * @return <code>true</code> if the pictogram element of the given Feature is collapsed.
     */
    private boolean isCollapsed(Feature feature) {
        PictogramElement pe = getFeatureProvider().getPictogramElementForBusinessObject(feature);
        String value = Graphiti.getPeService().getPropertyValue(pe, Properties.PROP_KEY_CONTAINER_TYPE);

        if (value != null && Properties.PROP_VAL_CONTAINER_TYPE_COLLAPSED.equals(value)) {
            return true;
        }

        return false;
    }

    /**
     * Enable all pictogram elements of the given Group.
     * 
     * @param group
     *            the Group
     */
    private void enableGroupPictogramElements(Group group) {
        PictogramElement[] pes = getFeatureProvider().getAllPictogramElementsForBusinessObject(group);

        for (PictogramElement pe : pes) {
            pe.setVisible(true);
        }
    }

    /**
     * Enable pictogram elements of the given Feature.
     * 
     * @param feature
     *            the Feature
     */
    private void enableFeaturePictogramElements(Feature feature) {
        PictogramElement pe = getFeatureProvider().getPictogramElementForBusinessObject(feature);
        pe.setVisible(true);
    }
}

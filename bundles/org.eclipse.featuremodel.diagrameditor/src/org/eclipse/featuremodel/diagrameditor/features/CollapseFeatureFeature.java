package org.eclipse.featuremodel.diagrameditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.diagrameditor.utilities.Properties;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;

/**
 * Feature handle collapsing of Feature Diagram elements. A Features with child Features can be
 * collapsed (see {@link CollapseFeatureFeature}) to hide the child elements and expanded (see
 * {@link ExpandFeatureFeature}) to show this.
 * 
 */
public class CollapseFeatureFeature extends AbstractCustomFeature {

    /**
     * Creates an instance of {@link CollapseFeatureFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public CollapseFeatureFeature(IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Gets the name of this function feature.
     * 
     * @return The name.
     */
    @Override
    public String getName() {
        return "Collapse";
    }

    /**
     * Gets the description of this function feature.
     * 
     * @return The description.
     */
    @Override
    public String getDescription() {
        return "Collapse child Features";
    }

    /**
     * Decides if the collapse feature is available with the given context. This implementation
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
     * Checks whether the current pictogram element of the given context can be collapsed. This
     * implementation returns <code>true</code> if the selected pictogram element represents a
     * Feature, it has child Features and is of the type
     * {@link Properties#PROP_VAL_CONTAINER_TYPE_EXPANDED}.
     * 
     * @param context
     *            The context.
     * @return true if the selected pictogram element represents a Feature, it has child Features
     *         and is of the type {@link Properties#PROP_VAL_CONTAINER_TYPE_EXPANDED}.
     */
    @Override
    public boolean canExecute(ICustomContext context) {
        if (context instanceof ICustomContext) {
            PictogramElement[] pes = ((ICustomContext) context).getPictogramElements();
            // allow to collapse Feature if exactly one pictogram element is selected
            if (pes != null && pes.length == 1) {
                Object bo = getBusinessObjectForPictogramElement(pes[0]);
                // allow to collapse only Feature objects with children
                if (bo instanceof Feature && !((Feature) bo).getChildren().isEmpty()) {
                    String value = Graphiti.getPeService().getPropertyValue(pes[0], Properties.PROP_KEY_CONTAINER_TYPE);
                    // allow to collapse only Feature of type expanded
                    if (value != null && Properties.PROP_VAL_CONTAINER_TYPE_EXPANDED.equals(value)) {
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

        // set current X/Y coordinates of the given Feature pictogram element
        // this coordinates are used to restore positions of the child Features
        Graphiti.getPeService().setPropertyValue(selectedPE, Properties.PROP_KEY_CONTAINER_INIT_X,
                String.valueOf(selectedPE.getGraphicsAlgorithm().getX()));
        Graphiti.getPeService().setPropertyValue(selectedPE, Properties.PROP_KEY_CONTAINER_INIT_Y,
                String.valueOf(selectedPE.getGraphicsAlgorithm().getY()));

        // collapse all child elements
        collapse(feature);

        // set type of the given Feature pictogram element to collapsed
        Graphiti.getPeService().setPropertyValue(selectedPE, Properties.PROP_KEY_CONTAINER_TYPE,
                Properties.PROP_VAL_CONTAINER_TYPE_COLLAPSED);

        // enable expand sign
        enableExpandSign(feature);
    }

    /**
     * Enable the expand sign.
     * 
     * @param feature
     *            the Feature
     */
    private void enableExpandSign(Feature feature) {
        PictogramElement[] peList = getFeatureProvider().getAllPictogramElementsForBusinessObject(feature);

        for (PictogramElement pe : peList) {
            String value = Graphiti.getPeService().getPropertyValue(pe, Properties.PROP_KEY_CONTAINER_TYPE);

            if (value != null && Properties.PROP_VAL_CONTAINER_TYPE_EXPANDSIGN.equals(value)) {
                pe.setVisible(true);
                break;
            }
        }
    }

    /**
     * Collapse recursively all child elements of the given Feature.
     * 
     * @param parent
     *            the parent Feature
     */
    private void collapse(Feature parent) {
        // collapse all child Groups
        for (Group grp : parent.getChildren()) {
            disableGroupPictogramElements(grp);
            // collapse all child Features
            for (Feature child : grp.getFeatures()) {
                disableFeaturePictogramElements(child);
                collapse(child);
            }
        }
    }

    /**
     * Disable all pictogram elements of the given Group.
     * 
     * @param group
     *            the Group
     */
    private void disableGroupPictogramElements(Group group) {
        PictogramElement[] pes = getFeatureProvider().getAllPictogramElementsForBusinessObject(group);

        for (PictogramElement pe : pes) {
            pe.setVisible(false);
        }
    }

    /**
     * Disable pictogram elements of the given Feature.
     * 
     * @param feature
     *            the Feature
     */
    private void disableFeaturePictogramElements(Feature feature) {
        PictogramElement pe = getFeatureProvider().getPictogramElementForBusinessObject(feature);
        pe.setVisible(false);
    }
}

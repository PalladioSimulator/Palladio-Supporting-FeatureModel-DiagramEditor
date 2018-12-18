package org.eclipse.featuremodel.diagrameditor.features;

import org.eclipse.featuremodel.FeatureModel;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

/**
 * Context menu action to layout the tree structure of the Feature Diagram.
 */
public class LayoutDiagramActionFeature extends AbstractCustomFeature {

    /**
     * Creates an instance of {@link SetMandatoryRelationTypeFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public LayoutDiagramActionFeature(IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Gets the name of this function feature.
     * 
     * @return The name.
     */
    @Override
    public String getName() {
        return "Layout";
    }

    /**
     * Gets the description of this function feature.
     * 
     * @return The description.
     */
    @Override
    public String getDescription() {
        return "Layout the diagram";
    }

    /**
     * Checks whether the current pictogram element of the given context can be modified. This
     * implementation returns <code>true</code> if the pictogram element represents a not empty
     * Feature Model with more than one Feature.
     * 
     * @param context
     *            The context.
     * @return true if the pictogram element represents a not empty Feature Model with more than one
     *         Feature.
     */
    @Override
    public boolean canExecute(ICustomContext context) {
        // allow change relation type if exactly one pictogram element
        // representing a Group is selected
        boolean result = false;
        PictogramElement[] pes = context.getPictogramElements();
        if (pes != null && pes.length == 1) {
            Object bo = getBusinessObjectForPictogramElement(pes[0]);
            if (bo instanceof FeatureModel) {
                FeatureModel fm = (FeatureModel) bo;
                if (fm.getRoot() != null && fm.getRoot().getChildren().size() != 0) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Layouts the tree structure of the Feature Diagram.
     * 
     * @param context
     *            The context.
     */
    @Override
    public void execute(ICustomContext context) {
        layoutPictogramElement(this.getDiagram());
    }
}

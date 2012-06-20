package de.fzi.se.eclipse.featuremodel.treeeditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.AbstractText;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;

/**
 * Feature handle updating any changes made to a Feature model object.
 * 
 * @author Alexander Moor
 */
public class UpdateFeatureFeature extends AbstractUpdateFeature {

    /**
     * Creates an instance of {@link UpdateFeatureFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public UpdateFeatureFeature(final IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Checks whether the values of the current pictogram element of the given context can be
     * updated.
     * 
     * @param context
     *            The context.
     * @return true if update is possible
     */
    @Override
    public boolean canUpdate(final IUpdateContext context) {
        Object obj = getBusinessObjectForPictogramElement(context.getPictogramElement());
        return (obj instanceof Feature);
    }

    /**
     * Updates the Feature name field. It copies the latest name value from the Feature model object
     * to the graphics algorithm of name text field.
     * 
     * @param context
     *            The context.
     * @return true, if update process was successful.
     */
    @Override
    public boolean update(final IUpdateContext context) {
        PictogramElement pictogramElement = context.getPictogramElement();
        Object obj = getBusinessObjectForPictogramElement(pictogramElement);
        if (obj instanceof Feature) {
            String featureName = ((Feature) obj).getName();

            // updating the name text field of the pictogram element
            if (pictogramElement instanceof ContainerShape) {
                ContainerShape cs = (ContainerShape) pictogramElement;
                for (Shape shape : cs.getChildren()) {
                    if (shape.getGraphicsAlgorithm() instanceof AbstractText) {
                        AbstractText text = (AbstractText) shape.getGraphicsAlgorithm();
                        text.setValue(featureName);
                        return true;
                    }
                }
            }

        }

        return false;
    }

    /**
     * Check whether the Feature name in the name text field of the pictogram element is up to date,
     * that means whether the graphics algorithm of this name text field contain the latest values
     * from the Feature model object.
     * 
     * @param context
     *            The context.
     * @return true if name field of the pictogram model needs to be updated with the latest values.
     *         from the Feature model object.
     */
    @Override
    public IReason updateNeeded(final IUpdateContext context) {
        String pictogramName = null;
        boolean updateRequired = false;
        PictogramElement pictogramElement = context.getPictogramElement();
        if (pictogramElement instanceof ContainerShape) {
            ContainerShape cs = (ContainerShape) pictogramElement;
            for (Shape shape : cs.getChildren()) {
                if (shape.getGraphicsAlgorithm() instanceof AbstractText) {
                    AbstractText text = (AbstractText) shape.getGraphicsAlgorithm();
                    pictogramName = text.getValue();
                }
            }

            String featureName = null;
            Object obj = getBusinessObjectForPictogramElement(pictogramElement);
            if (obj instanceof Feature) {
                featureName = ((Feature) obj).getName();
            }

            if (featureName == null || featureName.isEmpty()) {
                updateRequired = pictogramName != null && !featureName.isEmpty();
            } else {
                updateRequired = !featureName.equals(pictogramName);
            }

        }
        // show the reason to the user
        if (updateRequired) {
            return Reason.createTrueReason("Name is out of Date");
        } else {
            return Reason.createFalseReason();
        }
    }

}

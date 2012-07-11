package org.eclipse.featuremodel.diagrameditor.features;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.diagrameditor.utilities.BOUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

/**
 * Feature handle resizing of the pictogram element represents a Feature.
 * 
 * @author Alexander Moor
 */
public class ResizeFeatureFeature extends DefaultResizeShapeFeature {

    /**
     * Creates an instance of {@link ResizeFeatureFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public ResizeFeatureFeature(IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Checks whether the current pictogram element of the given context can be resized. This
     * implementation returns <code>true</code> if the current pictogram element represents a
     * Feature.
     * 
     * @param context
     *            the context.
     * @return <code>true</code> if the current pictogram element represents a Feature
     */
    @Override
    public boolean canResizeShape(IResizeShapeContext context) {
        Object bo = getBusinessObjectForPictogramElement(context.getShape());

        if (bo instanceof Feature) {
            return true;
        }
        return false;
    }

    /**
     * Resizes the current Feature element.
     * 
     * @param context
     *            the context
     */
    @Override
    public void resizeShape(IResizeShapeContext context) {
        Shape shape = context.getShape();
        int x = context.getX();
        int y = context.getY();
        int width = context.getWidth();
        int height = context.getHeight();

        if (shape.getGraphicsAlgorithm() != null) {
            Graphiti.getGaService().setLocationAndSize(shape.getGraphicsAlgorithm(), x, y, width, height);
        }

        Feature movedFeature = (Feature) this.getFeatureProvider().getBusinessObjectForPictogramElement(
                context.getShape());
        
        // update the inner text label to the new size
        EList<EObject> contents = shape.eContents();
        for (EObject eObject : contents) {
            if (eObject instanceof PictogramElement) {
                PictogramElement innerPictogram = (PictogramElement) eObject;
                if (innerPictogram.getGraphicsAlgorithm() instanceof Text) {
                    Graphiti.getGaService()
                            .setLocationAndSize(innerPictogram.getGraphicsAlgorithm(), 10, 10, shape.getGraphicsAlgorithm().getWidth() - 20, shape.getGraphicsAlgorithm().getHeight() - 20);
                }
                this.updatePictogramElement(innerPictogram);
            }
            
        }

        // update the parent Group if exists
        if (movedFeature.getParentGroup() != null) {
            Connection c = BOUtil.getPictogramElementForBusinessObject(movedFeature.getParentGroup(), Connection.class,
                    getFeatureProvider());
            this.updatePictogramElement(c);
        }

        // update all child Groups if exist
        for (Group gr : movedFeature.getChildren()) {
            Connection c = BOUtil.getPictogramElementForBusinessObject(gr, Connection.class, getFeatureProvider());
            this.updatePictogramElement(c);
        }
    }
}

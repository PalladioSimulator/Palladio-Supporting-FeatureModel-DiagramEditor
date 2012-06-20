package org.eclipse.featuremodel.treeeditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.treeeditor.utilities.BOUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Connection;


/**
 * Feature handle moving of the pictogram element represents Feature.
 * 
 * @author Alexander Moor
 */
public class MoveFeatureFeature extends DefaultMoveShapeFeature {

    /**
     * Creates an instance of {@link MoveFeatureFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public MoveFeatureFeature(IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Checks whether the current pictogram element of the given context can be moved. This
     * implementation returns <code>true</code> if the pictogram element represents a Feature.
     * 
     * @param context
     *            The context.
     * @return true if the pictogram element represents a Feature
     */
    @Override
    public boolean canMoveShape(IMoveShapeContext context) {
        // boolean result = super.canMoveShape(context);

        Object objToMove = getBusinessObjectForPictogramElement(context.getShape());
        if (objToMove instanceof Feature) {
            // result &= true;

            // Shape targetContainer = context.getTargetContainer();
            // Connection targetConnection = context.getTargetConnection();
            // Object target;
            // check whether the new Feature is wanted to be added to a connection.
            // if (context.getTargetContainer().equals(getDiagram())) {
            // return true;
            // } else if (context.getTargetConnection() != null) {
            // return true;
            // // target =
            // // this.getFeatureProvider().getBusinessObjectForPictogramElement(targetConnection);
            // } else {
            // Object target = this.getFeatureProvider().getBusinessObjectForPictogramElement(
            // context.getTargetContainer());
            // if (target instanceof Feature && !target.equals(objToMove)) {
            // return true;
            // }
            // }
            return super.canMoveShape(context);

        }

        return false;
    }

    /**
     * Update all associated Groups after moving the Feature pictogram element. A Feature is
     * associated with the parent Group and child Groups.
     * 
     * @param context
     *            The context.
     */
    @Override
    protected void postMoveShape(IMoveShapeContext context) {
        Feature feature = (Feature) this.getFeatureProvider().getBusinessObjectForPictogramElement(context.getShape());

        // update the parent Group if exists
        if (feature.getParentGroup() != null) {
            Connection c = BOUtil.getPictogramElementForBusinessObject(feature.getParentGroup(), Connection.class,
                    getFeatureProvider());
            this.updatePictogramElement(c);
        }

        // update all child Groups if exist
        for (Group gr : feature.getChildren()) {
            Connection c = BOUtil.getPictogramElementForBusinessObject(gr, Connection.class, getFeatureProvider());
            this.updatePictogramElement(c);
        }

    }
}

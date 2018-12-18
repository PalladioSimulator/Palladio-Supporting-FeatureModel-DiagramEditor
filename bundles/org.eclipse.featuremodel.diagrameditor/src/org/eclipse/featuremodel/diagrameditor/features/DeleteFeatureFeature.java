package org.eclipse.featuremodel.diagrameditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.diagrameditor.utilities.BOUtil;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.impl.DeleteContext;
import org.eclipse.graphiti.features.context.impl.MultiDeleteInfo;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;

/**
 * Feature handle deleting of a Feature model object. All child Features and Groups are deleted
 * before a Feature object is deleted.
 * 
 */
public class DeleteFeatureFeature extends DefaultDeleteFeature {

    /**
     * The parent Group of the Feature to delete.
     */
    private Group parentGroup;

    /**
     * Creates an instance of {@link DeleteFeatureFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public DeleteFeatureFeature(IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Checks whether the current pictogram element of the given context can be deleted.
     * 
     * @param context
     *            The context.
     * @return true if delete is possible
     */
    @Override
    public boolean canDelete(IDeleteContext context) {
        Object obj = getBusinessObjectForPictogramElement(context.getPictogramElement());
        return (obj instanceof Feature);
    }

    /**
     * Delete all child Features and Groups before a Feature object is deleted.
     * 
     * @param context
     *            The delete context.
     */
    @Override
    public void preDelete(IDeleteContext context) {
        Object obj = this.getBusinessObjectForPictogramElement(context.getPictogramElement());

        if (obj instanceof Feature) {
            Feature feature = (Feature) obj;
            feature.getParentGroup();

            // if child Groups exist delete all
            if (!feature.getChildren().isEmpty()) {

                // delete all child Groups and Features
                Group[] groups = feature.getChildren().toArray(new Group[0]);
                for (Group group : groups) {
                    // delete all child Features
                    Feature[] features = group.getFeatures().toArray(new Feature[0]);
                    for (Feature childFeature : features) {
                        PictogramElement pe = this.getFeatureProvider().getPictogramElementForBusinessObject(
                                childFeature);
                        DeleteContext deleteContext = new DeleteContext(pe);
                        deleteContext.setMultiDeleteInfo(new MultiDeleteInfo(false, false, 0));
                        IDeleteFeature deleteFeature = getFeatureProvider().getDeleteFeature(deleteContext);
                        deleteFeature.execute(deleteContext);
                    }
                }
            }

            this.parentGroup = feature.getParentGroup();
        }
    }

    /**
     * Delete the parent Group if this was the last Feature in the Group.
     * 
     * @param context
     *            The delete context.
     */
    @Override
    public void postDelete(IDeleteContext context) {
        if (this.parentGroup != null) {
            // Delete the parent Group if this was the last Feature in Group
            if (this.parentGroup.getFeatures().size() < 1) {
                deleteBusinessObject(this.parentGroup);
            } else { // otherwise update Group
                Connection c = BOUtil.getPictogramElementForBusinessObject(this.parentGroup, Connection.class,
                        getFeatureProvider());
                this.updatePictogramElement(c);
            }
        }
    }
}

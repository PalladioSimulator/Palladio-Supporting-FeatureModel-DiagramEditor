package org.eclipse.featuremodel.treeeditor.features;

import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.treeeditor.utilities.BOUtil;
import org.eclipse.featuremodel.treeeditor.utilities.BOUtil.RelationType;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;


/**
 * Feature handle changing the single relation type of a Group model object from the
 * {@link RelationType#Optional} to the {@link RelationType#Mandatory}.
 * 
 * @author Alexander Moor
 */
public class SetMandatoryRelationTypeFeature extends AbstractCustomFeature {

    /**
     * Creates an instance of {@link SetMandatoryRelationTypeFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public SetMandatoryRelationTypeFeature(IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Gets the name of this function feature.
     * 
     * @return The name.
     */
    @Override
    public String getName() {
        return "Mandatory";
    }

    /**
     * Gets the description of this function feature.
     * 
     * @return The description.
     */
    @Override
    public String getDescription() {
        return "Relation type";
    }

    /**
     * Decides if the change relation type feature is available with the given context. This
     * implementation returns <code>true</code> if the pictogram element represents a Group and the
     * current Group relation type is the {@link RelationType#Optional} or
     * {@link RelationType#Mandatory}.
     * 
     * @param context
     *            The context.
     * @return true if it is available, false if not
     */
    @Override
    public boolean isAvailable(IContext context) {
        boolean result = false;
        if (context instanceof ICustomContext) {
            PictogramElement[] pes = ((ICustomContext) context).getPictogramElements();
            if (pes != null && pes.length == 1) {
                Object bo = getBusinessObjectForPictogramElement(pes[0]);
                if (bo instanceof Group) {
                    RelationType relationType = BOUtil.getRelationType((Group) bo);
                    if (RelationType.Optional.equals(relationType) || RelationType.Mandatory.equals(relationType)) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Checks whether the current pictogram element of the given context can be modified. This
     * implementation returns <code>true</code> if the pictogram element represents a Group and the
     * current Group relation type is the {@link RelationType#Optional}.
     * 
     * @param context
     *            The context.
     * @return true if the pictogram element represents a Group and the current Group relation type
     *         is the {@link RelationType#Optional}
     */
    @Override
    public boolean canExecute(ICustomContext context) {
        // allow change relation type if exactly one pictogram element
        // representing a Group is selected
        boolean result = false;
        PictogramElement[] pes = context.getPictogramElements();
        if (pes != null && pes.length == 1) {
            Object bo = getBusinessObjectForPictogramElement(pes[0]);
            if (bo instanceof Group) {
                RelationType relationType = BOUtil.getRelationType((Group) bo);
                if (RelationType.Optional.equals(relationType)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Changes the Group relation type to the {@link RelationType#Mandatory}.
     * 
     * @param context
     *            The context.
     */
    @Override
    public void execute(ICustomContext context) {
        PictogramElement[] pes = context.getPictogramElements();
        Group group = (Group) getBusinessObjectForPictogramElement(pes[0]);

        BOUtil.setRelationType(group, RelationType.Mandatory);

        Connection connection = BOUtil.getPictogramElementForBusinessObject(group, Connection.class,
                getFeatureProvider());
        updatePictogramElement(connection);
    }
}

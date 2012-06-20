package de.fzi.se.eclipse.featuremodel.treeeditor.features;

import org.eclipse.featuremodel.Feature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.impl.AbstractDirectEditingFeature;
import org.eclipse.graphiti.func.IDirectEditing;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

/**
 * Feature for enabling direct editing of the name of a Feature.
 * 
 * @author Alexander Moor
 */
public class DirectEditFeatureFeature extends AbstractDirectEditingFeature {

    /**
     * Creates an instance of {@link DirectEditFeatureFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public DirectEditFeatureFeature(final IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Checks whether the current pictogram element of the given context can be direct edited.
     * 
     * @param context
     *            The context.
     * @return true if direct editing is possible
     */
    @Override
    public boolean canDirectEdit(final IDirectEditingContext context) {
        Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
        return bo instanceof Feature;
    }

    /**
     * Gets the type of editing. The name text field is wanted for editing.
     * 
     * @return The type of editing.
     */
    @Override
    public int getEditingType() {
        return IDirectEditing.TYPE_TEXT;
    }

    /**
     * Gets the initial name value of the Feature model object.
     * 
     * @param context
     *            The context.
     * @return The initial name value.
     */
    @Override
    public String getInitialValue(final IDirectEditingContext context) {
        Object object = getBusinessObjectForPictogramElement(context.getPictogramElement());
        if (object instanceof Feature) {
            return ((Feature) object).getName();
        } else {
            return null;
        }
    }

    /**
     * Sets the new value of the Feature model object and initiate the update of the referenced
     * pictogram element.
     * 
     * @param value
     *            The new name value.
     * @param context
     *            The context.
     */
    @Override
    public void setValue(String value, IDirectEditingContext context) {
        PictogramElement pe = context.getPictogramElement();
        Object obj = this.getBusinessObjectForPictogramElement(pe);
        if (obj instanceof Feature) {
            Feature feature = (Feature) obj;
            feature.setName(value);
            // Explicitly update the main shape to display the new value in the diagram.
            // The main shape is the container of the Feature.
            this.updatePictogramElement(this.getFeatureProvider().getPictogramElementForBusinessObject(feature));
        }
    }
}

package org.eclipse.featuremodel.treeeditor.features;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;

/**
 * Feature handle resizing of the pictogram element represents set relation of a Group.
 * 
 * @author Alexander Moor
 */
public class ResizeSetRelationFeature extends DefaultResizeShapeFeature {

    /**
     * Creates an instance of {@link ResizeSetRelationFeature}.
     * 
     * @param fp
     *            The feature provider.
     */
    public ResizeSetRelationFeature(IFeatureProvider fp) {
        super(fp);
    }

    /**
     * Checks whether the current pictogram element of the given context can be resized. This
     * implementation returns <code>false</code> and tells the framework to do not resize the the
     * pictogram element represents set relation of a Group.
     * 
     * @param context
     *            The context.
     * @return <code>false</code>
     */
    @Override
    public boolean canResizeShape(IResizeShapeContext context) {
        return false;
    }
}

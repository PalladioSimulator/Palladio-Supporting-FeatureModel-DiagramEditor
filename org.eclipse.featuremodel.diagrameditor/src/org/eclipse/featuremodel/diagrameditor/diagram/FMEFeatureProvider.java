package org.eclipse.featuremodel.diagrameditor.diagram;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.featuremodel.Feature;
import org.eclipse.featuremodel.FeatureModel;
import org.eclipse.featuremodel.Group;
import org.eclipse.featuremodel.diagrameditor.features.AddFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.AddFeatureModelFeature;
import org.eclipse.featuremodel.diagrameditor.features.AddGroupFeature;
import org.eclipse.featuremodel.diagrameditor.features.CollapseFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.CreateFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.DeleteFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.DirectEditFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.ExpandFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.LayoutDiagramActionFeature;
import org.eclipse.featuremodel.diagrameditor.features.LayoutDiagramFeature;
import org.eclipse.featuremodel.diagrameditor.features.LayoutFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.MoveFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.RemovFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.ResizeFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.SetMandatoryRelationTypeFeature;
import org.eclipse.featuremodel.diagrameditor.features.SetORRelationTypeFeature;
import org.eclipse.featuremodel.diagrameditor.features.SetOptionalRelationTypeFeature;
import org.eclipse.featuremodel.diagrameditor.features.SetXORRelationTypeFeature;
import org.eclipse.featuremodel.diagrameditor.features.UpdateFeatureFeature;
import org.eclipse.featuremodel.diagrameditor.features.UpdateRelationshipFeature;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IDirectEditingFeature;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.IMoveShapeFeature;
import org.eclipse.graphiti.features.IReconnectionFeature;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.IResizeShapeFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.context.IReconnectionContext;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;

/**
 * {@link FMEFeatureProvider} is used by the Graphiti framework to find out which operations are
 * supported by this editor in the current situation.
 * 
 * @author Alexander Moor
 */
public class FMEFeatureProvider extends DefaultFeatureProvider {

    /**
     * Creates an instance of {@link FMEFeatureProvider}.
     * 
     * @param diagramTypeProvider
     *            The diagram type provider
     */
    public FMEFeatureProvider(IDiagramTypeProvider diagramTypeProvider) {
        super(diagramTypeProvider);
    }

    /**
     * Create features create Feature Model objects and their graphical representations. In the
     * graphics framework they will be visualized in an editor as create tools.
     * 
     * @return all create features.
     */
    @Override
    public ICreateFeature[] getCreateFeatures() {
        return new ICreateFeature[] { new CreateFeatureFeature(this) };
    }

    /**
     * Add features create graphical representations of Feature Model objects.
     * 
     * @param context
     *            The context.
     * @return add feature according to the given context.
     */
    @Override
    public IAddFeature getAddFeature(IAddContext context) {
        if (context.getNewObject() instanceof FeatureModel) {
            return new AddFeatureModelFeature(this);
        } else if (context.getNewObject() instanceof Feature) {
            return new AddFeatureFeature(this);
        } else if (context.getNewObject() instanceof Group) {
            return new AddGroupFeature(this);
        }
        return super.getAddFeature(context);
    }

    /**
     * Layout features do the layouting work (sizes and dimensions) inside (and/or) outside a
     * pictogram element.
     * 
     * @param context
     *            The context.
     * @return layout feature according to the given context.
     */
    @Override
    public ILayoutFeature getLayoutFeature(final ILayoutContext context) {
        PictogramElement pe = context.getPictogramElement();
        Object obj = getBusinessObjectForPictogramElement(pe);
        if (obj instanceof FeatureModel) {
            return new LayoutDiagramFeature(this);
        } else if (obj instanceof Feature) {
            return new LayoutFeatureFeature(this);
        }
        return super.getLayoutFeature(context);
    }

    /**
     * Direct editing features handle direct editing functionality (including drop down lists and
     * text completion).
     * 
     * @param context
     *            The context.
     * @return direct editing feature according to the given context.
     */
    @Override
    public IDirectEditingFeature getDirectEditingFeature(final IDirectEditingContext context) {
        PictogramElement pe = context.getPictogramElement();
        Object obj = getBusinessObjectForPictogramElement(pe);

        if (obj instanceof Feature) {
            return new DirectEditFeatureFeature(this);
        }
        return null;
    }

    /**
     * Update features do the synchronization work and transport data from Feature Model to
     * pictograms model elements.
     * 
     * @param context
     *            The context.
     * @return update feature according to the given context.
     */
    @Override
    public IUpdateFeature getUpdateFeature(IUpdateContext context) {
        PictogramElement pictogramElement = context.getPictogramElement();

        Object obj = getBusinessObjectForPictogramElement(pictogramElement);
        if (obj instanceof Feature) {
            return new UpdateFeatureFeature(this);
        } else if (obj instanceof Group) {
            return new UpdateRelationshipFeature(this);
        }

        return null;
    }

    /**
     * Delete features remove the graphical representations of Feature Model objects as well as the
     * Feature Model objects itself.
     * 
     * @param context
     *            The context.
     * @return delete feature according to the given context.
     */
    @Override
    public IDeleteFeature getDeleteFeature(IDeleteContext context) {
        Object obj = getBusinessObjectForPictogramElement(context.getPictogramElement());
        if (obj instanceof Feature) {
            return new DeleteFeatureFeature(this);
        }

        return null;
    }

    /**
     * Gets custom features. Custom features can do anything (e.g. changing Group relation type).
     * 
     * @param context
     *            The context.
     * @return custom feature according to the given context
     */
    @Override
    public ICustomFeature[] getCustomFeatures(ICustomContext context) {
        List<ICustomFeature> result = new ArrayList<ICustomFeature>();
        // context menu to layout the diagram
        result.add(new LayoutDiagramActionFeature(this));

        // context menus to set/change Group relation
        result.add(new SetOptionalRelationTypeFeature(this));
        result.add(new SetMandatoryRelationTypeFeature(this));
        result.add(new SetORRelationTypeFeature(this));
        result.add(new SetXORRelationTypeFeature(this));

        // context menu to collapse/expand Feature children
        result.add(new CollapseFeatureFeature(this));
        result.add(new ExpandFeatureFeature(this));

        return result.toArray(new ICustomFeature[result.size()]);
    }

    /**
     * Called in case an existing object on the diagram is moved. Needs to return a feature that can
     * handle the move operation or <code>null</code> to prevent the operation.
     * 
     * @param context
     *            The context.
     * @return The feature that handle the move.
     */
    @Override
    public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
        // Retrieve the domain object for the moved pictogram element
        Object obj = getBusinessObjectForPictogramElement(context.getShape());

        if (obj instanceof Feature) {
            return new MoveFeatureFeature(this);
        }

        return null;
    }

    /**
     * Called in case a pictogram element on the diagram is resized. Needs to return a feature that
     * can handle the resize operation.
     * 
     * @param context
     *            The context.
     * @return The feature that handle the move.
     */
    @Override
    public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
        Object bo = getBusinessObjectForPictogramElement(context.getShape());

        if (bo instanceof Feature) {
            return new ResizeFeatureFeature(this);
        }

        return null;
    }

    /**
     * Reconnection features handle the change of a connection's start or end anchor. This
     * implementation forbid the reconnection.
     * 
     * @param context
     *            the context
     * @return <code>null</code> to forbid the reconnection
     */
    @Override
    public IReconnectionFeature getReconnectionFeature(IReconnectionContext context) {
        return null;
    }

    /**
     * Remove features remove the graphical representations of Feature Model objects.
     * 
     * @param context
     *            the context
     * @return remove feature according to the given context
     */
    @Override
    public IRemoveFeature getRemoveFeature(IRemoveContext context) {
        Object obj = getBusinessObjectForPictogramElement(context.getPictogramElement());
        if (obj instanceof Feature) {
            return new RemovFeatureFeature(this);
        }

        return null;
    }
}

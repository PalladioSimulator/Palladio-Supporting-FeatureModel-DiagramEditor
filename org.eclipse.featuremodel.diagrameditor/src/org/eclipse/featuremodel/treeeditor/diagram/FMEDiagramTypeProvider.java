package org.eclipse.featuremodel.treeeditor.diagram;

import org.eclipse.graphiti.dt.AbstractDiagramTypeProvider;
import org.eclipse.graphiti.tb.IToolBehaviorProvider;

/**
 * Defines the diagram type provider.
 * 
 * @author Alexander Moor
 */
public class FMEDiagramTypeProvider extends AbstractDiagramTypeProvider {

    /**
     * Array of all registered tool behavior providers.
     */
    private IToolBehaviorProvider[] toolBehaviorProviders = null;

    /**
     * Creates an instance of {@link FMEDiagramTypeProvider}.
     */
    public FMEDiagramTypeProvider() {
        super();

        // The diagram type provider needs to know its feature provider, so the
        // Graphiti framework can ask which operations are supported.
        setFeatureProvider(new FMEFeatureProvider(this));
    }

    /**
     * Returns all available tool behavior providers.
     * 
     * @return An array of all registered tool behavior providers
     */
    @Override
    public IToolBehaviorProvider[] getAvailableToolBehaviorProviders() {
        if (this.toolBehaviorProviders == null) {
            this.toolBehaviorProviders = new IToolBehaviorProvider[] { new FMEToolBehaviourProvider(this) };
        }
        return this.toolBehaviorProviders;
    }
}

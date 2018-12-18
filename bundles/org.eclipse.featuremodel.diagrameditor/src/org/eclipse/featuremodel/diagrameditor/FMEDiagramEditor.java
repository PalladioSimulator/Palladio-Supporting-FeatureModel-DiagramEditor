package org.eclipse.featuremodel.diagrameditor;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.swt.SWT;

/**
 * A diagram editor for Feature Diagrams.
 * 
 */
public class FMEDiagramEditor extends DiagramEditor {

    /** the editor identifier. */
    public static final String DIAGRAM_EDITOR_ID = "org.eclipse.featuremodel.diagrameditor.diagrameditor";
    /** the diagram type name. */
    public static final String DIAGRAM_TYPE_NAME = "Feature Diagram";
    /** the diagram type id. */
    public static final String DIAGRAM_TYPE_ID = "org.eclipse.featuremodel.diagrameditor.diagramtype";
    /** the diagram type provider id. */
    public static final String DIAGRAM_TYPE_PROVIDER_ID = "org.eclipse.featuremodel.diagrameditor.diagramtypeprovider";
    /** The file extension for Feature Diagram files. */
    public static final String DIAGRAM_FILE_EXTENSION = "featurediagram";
    /** The file extension for Feature Model files. */
    public static final String MODEL_FILE_EXTENSION = "featuremodel";

    /**
     * Called to configure the editor, before it receives its content. The default-implementation is
     * for example doing the following: configure the ZoomManager, registering Actions... This
     * implementation adds support for mouse wheel zooming in the editor.
     */
    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        // add zooming action with "CTRL + Mouse Wheel"
        GraphicalViewer viewer = getGraphicalViewer();
        viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1), MouseWheelZoomHandler.SINGLETON);
    }
}

package org.eclipse.featuremodel.treeeditor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditorPlugin;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * The wizard for creation of new Feature Diagrams. A new Feature Model is created or an existing
 * loaded.
 * 
 * @author Alexander Moor
 */
public class FMENewWizard extends Wizard implements INewWizard {

    /** The wizard name for a new Feature Diagram. */
    private static final String WIZARD_NAME = "New Feature Diagram";

    /** The wizard page for handling Feature Model files. */
    private FMEWizardPage modelFilePage;
    /** The current selection. */
    private IStructuredSelection selection;
    /** The Feature Diagram. */
    private Resource featureDiagram;

    /**
     * Creates an instance of {@link FMENewWizard}.
     */
    public FMENewWizard() {
        super();
    }

    /**
     * Initializes this creation wizard using the passed workbench and object selection. This method
     * is called after the no argument constructor and before other methods are called.
     * 
     * @param workbench
     *            The current workbench.
     * @param selection
     *            The current object selection.
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.setWindowTitle(WIZARD_NAME);
        this.selection = selection;
        this.setNeedsProgressMonitor(true);
    }

    /**
     * Add pages to this wizard.
     */
    @Override
    public final void addPages() {
        this.modelFilePage = new FMEWizardPage("FeatureModelPage", selection);
        addPage(this.modelFilePage);
    }

    /**
     * Processing the creating and opening a new Feature Diagram.
     * 
     * @return true if a new Feature Diagram was created and open.
     */
    @Override
    public boolean performFinish() {
        IRunnableWithProgress op = new WorkspaceModifyOperation(null) {
            @Override
            protected void execute(IProgressMonitor monitor) throws CoreException, InterruptedException {

                // create Feature Diagram
                FMENewWizard.this.featureDiagram = FMEDiagramEditorUtil.createFeatureDiagram(
                        FMENewWizard.this.modelFilePage.getFeatureDiagramURI(),
                        FMENewWizard.this.modelFilePage.getFeatureModelURI(), monitor);

                if (featureDiagram != null) {
                    try {
                        // open the created Feature Diagram
                        FMENewWizard.this.openDiagram(FMENewWizard.this.featureDiagram);
                    } catch (PartInitException e) {
                        ErrorDialog.openError(getContainer().getShell(), "Error opening Feature Diagram editor", null,
                                e.getStatus());
                    }
                }
            }
        };
        try {
            // execute the creating and opening a new Feature Diagram
            getContainer().run(false, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof CoreException) {
                ErrorDialog.openError(getContainer().getShell(), "Creation Problems", null,
                        ((CoreException) e.getTargetException()).getStatus());
            } else {
                EcoreDiagramEditorPlugin.getInstance().logError("Error opening diagram editor", e.getTargetException());
            }
            return false;
        }
        return featureDiagram != null;
    }

    /**
     * Open the Feature Diagram from the given resource.
     * 
     * @param diagramResource
     *            THe Feature Diagram resource.
     * @throws PartInitException
     *             if the diagram could not be created or initialized
     */
    private void openDiagram(Resource diagramResource) throws PartInitException {
        DiagramEditorInput editorInput = new DiagramEditorInput(this.featureDiagram.getURI(),
                FMEDiagramEditor.DIAGRAM_TYPE_PROVIDER_ID);
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .openEditor(editorInput, FMEDiagramEditor.DIAGRAM_EDITOR_ID);
    }

}

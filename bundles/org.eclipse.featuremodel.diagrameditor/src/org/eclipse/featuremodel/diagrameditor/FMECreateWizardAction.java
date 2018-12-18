package org.eclipse.featuremodel.diagrameditor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This class handle the context menu action in the project explorer to create a new Feature
 * Diagram.
 * 
 * 
 */
public class FMECreateWizardAction implements IObjectActionDelegate {

    /** The current workbench part . */
    private IWorkbenchPart tartgetPart;
    /** The current selection. */
    private ISelection selection;

    /**
     * Performs the create Feature Diagram wizard action.
     * 
     * @param action
     *            the action proxy that handles the presentation portion of the action
     */
    @Override
    public void run(IAction action) {
        if (this.selection instanceof IStructuredSelection) {
            // initializes the wizard
            FMENewWizard wizard = new FMENewWizard();
            wizard.init(this.tartgetPart.getSite().getWorkbenchWindow().getWorkbench(),
                    (IStructuredSelection) this.selection);

            // Instantiates the wizard container with the wizard and opens it
            WizardDialog dialog = new WizardDialog(this.tartgetPart.getSite().getShell(), wizard);
            dialog.create();
            dialog.open();
        }
    }

    /**
     * Notifies this action delegate that the selection in the workbench has changed.
     * 
     * When the selection changes, the action enablement state is updated based on the criteria
     * specified in the plugin.xml file. Then the delegate is notified of the selection change
     * regardless of whether the enablement criteria in the plugin.xml file is met.
     * 
     * @param action
     *            the action proxy that handles presentation portion of the action
     * @param selection
     *            the current selection, or null if there is no selection.
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

    /**
     * This method will be called every time the action appears in a popup menu. The targetPart may
     * change with each invocation.
     * 
     * @param action
     *            the action proxy that handles presentation portion of the action; must not be
     *            null.
     * @param targetPart
     *            the new part target; must not be null.
     */
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.tartgetPart = targetPart;
    }

}

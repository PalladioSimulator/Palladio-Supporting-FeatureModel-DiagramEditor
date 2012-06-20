package de.fzi.se.eclipse.featuremodel.treeeditor;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;

/**
 * The wizard page to get informations for creating a new Feature Diagram. It includes dialogs to
 * create a new Feature Model or load an existing.
 * 
 * @author Alexander Moor
 */
public class FMEWizardPage extends WizardPage implements Listener {

    /** The default name for Feature Models and Feature Diagrams. */
    public static final String DEFAULT_MODEL_FILE_NAME = "My";

    /** The page title. */
    public static final String PAGE_TITLE = "Create Feature Diagram";
    /** The default page message. */
    public static final String PAGE_DESCRIPTION = "Create a new Feature Model file or select an existing.";

    /** Button to activate section to select an existing Feature Model. */
    private Button existingModelSectionButton;
    /** Button to activate section to create a new Feature Model. */
    private Button newModelSectionButton;

    /** Section to create a new Feature Model. */
    private Group newModelSection;
    /** Text field for directory of a new Feature Model. */
    private Text newModelDirectory;
    /** Button to activate dialog to select a directory for a new Feature Model. */
    private Button newModelDirectoryButton;
    /** Text field for name of a new Feature Model. */
    private Text newModelName;

    /** Section to select an existing Feature Model. */
    private Group existingModelSection;
    /** Text field for file path of an existing Feature Model. */
    private Text existingModel;
    /** Button to activate dialog to select an existing Feature Model. */
    private Button existingModelButton;

    /** The current selection. */
    private IStructuredSelection selection;

    /**
     * Creates an instance of {@link FMEWizardPage}.
     * 
     * @param pageName
     *            the internal name of this page
     * @param selection
     *            the current selection
     */
    protected FMEWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName);
        setTitle(PAGE_TITLE);
        setDescription(PAGE_DESCRIPTION);
        setPageComplete(false);

        this.selection = selection;
    }

    /**
     * Return the Feature Model file URI.
     * 
     * @return the URI
     */
    public URI getFeatureModelURI() {
        return URI.createPlatformResourceURI(this.getModelFilePath().toString(), false);
    }

    /**
     * Return the Feature Diagram file URI.
     * 
     * @return the URI
     */
    public URI getFeatureDiagramURI() {
        // replace Feature Model file extension with Feature Diagram file extension
        IPath path = this.getModelFilePath().removeFileExtension();
        path = path.addFileExtension(FMEDiagramEditor.DIAGRAM_FILE_EXTENSION);

        return URI.createPlatformResourceURI(path.toString(), false);
    }

    /**
     * Creates the top level control for this dialog page under the given parent composite.
     * 
     * @param parent
     *            the parent container
     */
    @Override
    public void createControl(Composite parent) {
        // create page widgets
        Composite widgetsContainer = createWizardContent(parent);

        // initialize widgets
        init();

        // add listeners to observe widget changes
        addListeners();

        setControl(widgetsContainer);
        // validate required input from the user before it can be considered complete
        setPageComplete(validatePage());
    }

    /**
     * Initializes page widgets. According to selected resource initial Feature Model file directory
     * is set.
     */
    private void init() {
        IResource selectedResource;
        Object obj = this.selection.getFirstElement();

        // determine the selected resource
        if (obj instanceof IResource) {
            // check whether selected resource is a file or directory
            if (obj instanceof IFile) {
                IFile file = (IFile) obj;
                // if file has not the Feature Model extension, get its directory
                if (FMEDiagramEditor.MODEL_FILE_EXTENSION.equals(file.getFileExtension())) {
                    selectedResource = file;
                } else {
                    selectedResource = file.getParent();
                }
            } else {
                selectedResource = (IResource) obj;
            }
        } else {
            // get workspace root directory
            selectedResource = ResourcesPlugin.getWorkspace().getRoot();
        }

        // check whether selected resource is a Feature Model file or directory and initialize
        // widgets
        if (selectedResource instanceof IFile) {
            // set initial values into section for an existing Feature Model
            setNewModelSectionEnabled(false);
            existingModelSectionButton.setSelection(true);

            newModelDirectory.setText(selectedResource.getParent().getFullPath().toString());
            newModelName.setText(getUniqueFileName(selectedResource.getParent().getFullPath()));
            existingModel.setText(selectedResource.getFullPath().toString());
            existingModelButton.setFocus();
        } else {
            // set initial values into section for a new Feature Model
            setExistingModelSectionEnabled(false);
            newModelSectionButton.setSelection(true);

            newModelDirectory.setText(selectedResource.getFullPath().toString());
            newModelName.setText(this.getUniqueFileName(selectedResource.getFullPath()));
            newModelName.setFocus();
        }
    }

    /**
     * Create widgets of this wizard page.
     * 
     * @param parent
     *            The parent container.
     * @return the container holds widgets.
     */
    private Composite createWizardContent(Composite parent) {
        // create the composite to hold the widgets
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setFont(parent.getFont());

        // button activates the section to create a new Feature Model file
        newModelSectionButton = new Button(composite, SWT.RADIO);
        newModelSectionButton.setText("Create a new model");
        newModelSectionButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // create section for creating a new Feature Model file
        createNewDomainModelSection(composite);

        // button activates the section to load an existing Feature Model file
        existingModelSectionButton = new Button(composite, SWT.RADIO);
        existingModelSectionButton.setText("Create from an existing model");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.verticalIndent = 20;
        existingModelSectionButton.setLayoutData(gd);

        // create section for opening an existing Feature Model file
        createExistingDomainModelSection(composite);

        return composite;
    }

    /**
     * Creates the section for creating a new Feature Model file.
     * 
     * @param parent
     *            the parent container
     */
    private void createNewDomainModelSection(Composite parent) {
        newModelSection = new Group(parent, SWT.NONE);
        newModelSection.setLayout(new GridLayout(3, false));
        newModelSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // the destination directory
        Label directoryLbl = new Label(newModelSection, SWT.NONE);
        directoryLbl.setText("Directory:");
        // text field for directory of new Feature Model file
        newModelDirectory = new Text(newModelSection, SWT.BORDER);
        newModelDirectory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        newModelDirectory.setEnabled(false);
        // directory choice button
        newModelDirectoryButton = new Button(newModelSection, SWT.PUSH);
        newModelDirectoryButton.setText("Browse...");

        // label
        Label nameLbl = new Label(newModelSection, SWT.NONE);
        nameLbl.setText("Feature Model name:");
        // text field for new name of the Feature Model file to create
        newModelName = new Text(newModelSection, SWT.BORDER);
        GridData layoutNameFd = new GridData(GridData.FILL_HORIZONTAL);
        layoutNameFd.horizontalSpan = 2;
        newModelName.setLayoutData(layoutNameFd);
    }

    /**
     * Creates the section for opening an existing Feature Model file.
     * 
     * @param parent
     *            the parent container
     */
    private void createExistingDomainModelSection(Composite parent) {
        existingModelSection = new Group(parent, SWT.NONE);
        existingModelSection.setLayout(new GridLayout(3, false));
        existingModelSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // label
        Label modelLbl = new Label(existingModelSection, SWT.NONE);
        modelLbl.setText("Feature Model:");
        // text field for file path of an existing Feature Model
        existingModel = new Text(existingModelSection, SWT.BORDER);
        existingModel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        existingModel.setEnabled(false);
        // file choice button
        existingModelButton = new Button(existingModelSection, SWT.PUSH);
        existingModelButton.setText("Browse...");
    }

    /**
     * Adds listeners to the page widgets to observe changes.
     */
    private void addListeners() {
        newModelSectionButton.addListener(SWT.Selection, this);
        newModelDirectory.addListener(SWT.Modify, this);
        newModelDirectoryButton.addListener(SWT.Selection, this);
        newModelName.addListener(SWT.Modify, this);

        existingModelSectionButton.addListener(SWT.Selection, this);
        existingModel.addListener(SWT.Modify, this);
        existingModelButton.addListener(SWT.Selection, this);
    }

    /**
     * Handle user actions and input informations changes.
     * 
     * @param event
     *            the event which occurred
     */
    @Override
    public void handleEvent(Event event) {
        if (event.widget == newModelSectionButton) {
            // if new model section radio button is activated enable the new model section and
            // disable existing model section
            if (newModelSectionButton.getSelection()) {
                this.setNewModelSectionEnabled(true);
                this.setExistingModelSectionEnabled(false);
            }
            setPageComplete(validatePage());
        } else if (event.widget == existingModelSectionButton) {
            if (existingModelSectionButton.getSelection()) {
                // if the existing model section radio button is activated, enable the existing
                // model section and disable new model section
                this.setNewModelSectionEnabled(false);
                this.setExistingModelSectionEnabled(true);
            }
            setPageComplete(validatePage());
        } else if (event.widget == newModelDirectoryButton) {
            // if the Feature Model directory choice button activated show dialog
            chooseNewModelDirectory();
        } else if (event.widget == newModelDirectory) {
            // if the new Feature Model directory field is changed validate input
            setPageComplete(validatePage());
        } else if (event.widget == newModelName) {
            // if the new Feature Model name field is changed validate input
            setPageComplete(validatePage());
        } else if (event.widget == existingModelButton) {
            // if the Feature Model file choice button activated show dialog
            chooseExistingModel();
        } else if (event.widget == existingModel) {
            // if the existing Feature Model directory field is changed validate input
            setPageComplete(validatePage());
        }
    }

    /**
     * Handle the Feature Model directory choice button action. A dialog to choose a directory for
     * the new Feature Model file is enabled.
     */
    private void chooseNewModelDirectory() {
        // create and open the dialog
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), (IContainer) ResourcesPlugin
                .getWorkspace().getRoot().findMember(newModelDirectory.getText()), false,
                "Select the destination directory:");
        if (dialog.open() == Window.OK) {
            Object[] selectedItems = dialog.getResult();
            if (selectedItems.length == 1 && selectedItems[0] instanceof IPath) {
                IPath modelPath = (IPath) selectedItems[0];
                this.newModelDirectory.setText(modelPath.toString());
            }
        }
    }

    /**
     * Handle the Feature Model file choice button action. A dialog to choose an existing Feature
     * Model file is enabled.
     */
    private void chooseExistingModel() {
        // the initial directory
        String initialDirectory = ResourcesPlugin.getWorkspace().getRoot().getFullPath().toString();

        // create and open the dialog
        ResourceSelectionDialog dialog = new ResourceSelectionDialog(getShell(), ResourcesPlugin.getWorkspace()
                .getRoot().findMember(initialDirectory), "Select an existing Feature Model file:");
        if (dialog.open() == Window.OK) {
            Object[] selectedItems = dialog.getResult();
            if (selectedItems.length == 1 && selectedItems[0] instanceof IFile) {
                IPath modelPath = ((IFile) selectedItems[0]).getFullPath();
                this.existingModel.setText(modelPath.toString());
            }
        }
    }

    /**
     * Sets the section for selecting an existing Feature Model file enabled/disabled.
     * 
     * @param enabled
     *            the new enabled state
     */
    private void setNewModelSectionEnabled(boolean enabled) {
        this.newModelDirectory.setEnabled(enabled);
        this.newModelName.setEnabled(enabled);
    }

    /**
     * Sets the section for creating a new Feature Model file enabled/disabled.
     * 
     * @param enabled
     *            the new enabled state
     */
    private void setExistingModelSectionEnabled(boolean enabled) {
        this.existingModelButton.setEnabled(enabled);
    }

    /**
     * Return whether Feature Model is new.
     * 
     * @return true if the Feature Model file is not created yet
     */
    private boolean isNewModel() {
        return this.newModelSectionButton.getSelection();
    }

    /**
     * Gets Feature Model file path. It includes path to the file and the file name with extension.
     * 
     * @return the path to the Feature Model file or empty path if no file is set.
     */
    private IPath getModelFilePath() {
        String path = "";
        if (this.isNewModel() && this.newModelDirectory != null) {
            path = this.newModelDirectory.getText();
            if (this.newModelName != null) {
                path += File.separator + this.newModelName.getText();
            }
        } else if (this.existingModel != null) {
            path = this.existingModel.getText();
        }

        return new Path(path);
    }

    /**
     * Returns the Feature Model file name (including file extension).
     * 
     * @return the Feature Model file name or empty string if no name is set.
     */
    private String getModelFileName() {
        IPath path = getModelFilePath();
        if (path == null || path.isEmpty() || path.hasTrailingSeparator()) {
            return "";
        }
        return path.lastSegment();
    }

    /**
     * Gets an unique file name. This method checks whether files with a same name already exist in
     * the given directory (path). The default name {@link FMEWizardPage#DEFAULT_MODEL_FILE_NAME} is
     * used for initialization.
     * 
     * @param containerFullPath
     *            The path
     * @return the unique file name
     */
    private String getUniqueFileName(IPath containerFullPath) {
        if (containerFullPath == null) {
            containerFullPath = new Path("");
        }
        // add the default name and Feature Model file extension
        IPath filePath = containerFullPath.append(FMEWizardPage.DEFAULT_MODEL_FILE_NAME);
        filePath = filePath.addFileExtension(FMEDiagramEditor.MODEL_FILE_EXTENSION);

        // look for files with a same name in the given directory
        int i = 1;
        while (ResourcesPlugin.getWorkspace().getRoot().exists(filePath)) {
            i++;
            filePath = containerFullPath.append(FMEWizardPage.DEFAULT_MODEL_FILE_NAME + i);
            filePath = filePath.addFileExtension(FMEDiagramEditor.MODEL_FILE_EXTENSION);
        }
        return filePath.lastSegment();
    }

    /**
     * Validate the page input informations.
     * 
     * @return true if data is valid
     */
    private boolean validatePage() {
        // remove all previous messages
        this.setMessage(null);
        this.setErrorMessage(null);

        // check if the path is empty
        if (getModelFilePath().removeLastSegments(1).isEmpty()) {
            setErrorMessage(PAGE_DESCRIPTION);
            return false;
        } else if (getModelFileName().isEmpty()) {
            // check if the name is empty
            setErrorMessage("Feature Model file name cannot be empty.");
            return false;
        } else if (!FMEDiagramEditor.MODEL_FILE_EXTENSION.equals(getModelFilePath().getFileExtension())) {
            // check if the files extension wrong
            setErrorMessage("File name should have " + FMEDiagramEditor.MODEL_FILE_EXTENSION + " extension.");
            return false;
        }

        // if it is a new Feature Model, check if the files does not already exist
        if (isNewModel()) {
            // check if the Feature Model file does not already exist
            if (isModelFileExist()) {
                setErrorMessage("The Feature Model file with the same name already exists in this folder.");
                return false;
            } else if (isDiagramFileExist()) {
                // check if the Feature Diagram file does not already exist
                setErrorMessage("The Feature Diagram file with the same name already exists in this folder.");
                return false;
            }
        } else {
            // if Feature Model file already exist check if the Feature Diagram file does not
            // already exist
            if (isDiagramFileExist()) {
                setMessage(
                        "The Feature Diagram file with the same name already exists in this folder. It will be overwritten!",
                        IMessageProvider.WARNING);
            }
        }

        return true;
    }

    /**
     * Returns whether Feature Model file exists in the workspace.
     * 
     * @return true if the file exists, otherwise false
     */
    private boolean isModelFileExist() {
        return FMEDiagramEditorUtil.getResource(this.getModelFilePath().toString()) != null;
    }

    /**
     * Returns whether Feature Diagram file exists in the workspace.
     * 
     * @return true if the file exists, otherwise false
     */
    private boolean isDiagramFileExist() {
        IPath path = this.getModelFilePath().removeFileExtension();
        path = path.addFileExtension(FMEDiagramEditor.DIAGRAM_FILE_EXTENSION);
        return FMEDiagramEditorUtil.getResource(path.toString()) != null;
    }
}

package org.eclipse.featuremodel.diagrameditor.utilities;

import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * <code>IdGen</code> supports the generating of unique ids.
 * 
 * 
 */
public class IdGen {
    /**
     * Generates a unique id.
     * 
     * @return The unique id.
     */
    public static String generate() {
        return EcoreUtil.generateUUID();
    }
}

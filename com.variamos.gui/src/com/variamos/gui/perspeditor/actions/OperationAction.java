package com.variamos.gui.perspeditor.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;

import com.mxgraph.util.mxResources;
import com.variamos.dynsup.instance.InstElement;
import com.variamos.dynsup.types.OpersOpType;
import com.variamos.gui.core.mxgraph.editor.DefaultFileFilter;
import com.variamos.gui.core.viewcontrollers.AbstractVariamoGUIAction;
import com.variamos.gui.maineditor.MainFrame;
import com.variamos.gui.maineditor.VariamosGraphComponent;
import com.variamos.gui.maineditor.VariamosGraphEditor;

@SuppressWarnings("serial")
public class OperationAction extends AbstractVariamoGUIAction {

	protected String lastDir = null;

	public OperationAction() {
	}

	/**
		 * 
		 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Component editor = getComponentEditor(e);
		VariamosGraphEditor variamosEditor = null;
		int perspective = ((MainFrame) editor).getPerspective();
		if (editor instanceof VariamosGraphEditor)
			variamosEditor = (VariamosGraphEditor) editor;
		if (editor instanceof MainFrame) {

			variamosEditor = ((MainFrame) editor).getEditor(perspective);
		}
		String operation = ((JMenuItem) e.getSource()).getName();
		List<String> operations = new ArrayList<String>();

		// FIXME review why this is needed for verification operations
		if (!operation.startsWith("N:"))
			variamosEditor.updateObjects();

		if (operation.startsWith("exec-all-ver-")) {
			// FIXME get all operations to execute
			// for ()
			// operations.add (oper);
		} else
			operations.add(operation);

		if (perspective == 2)
			((VariamosGraphComponent) variamosEditor.getGraphComponent())
					.setSimulationStarted(false);
		else
			((VariamosGraphComponent) variamosEditor.getGraphComponent())
					.setSimulationStarted(true);

		String filename = null;
		InstElement operationObj = variamosEditor.getDynamicBehaviorDTO().getRefas2hlcl().getRefas()
				.getSyntaxModel().getOperationalModel().getElement(operation);

		if (operationObj != null
				&& operationObj.getInstAttributeValue("operType").equals(
						OpersOpType.Export.toString())) {

			// boolean dialogShown = false;

			// if (vg.getCurrentFile() == null) {
			String wd;

			if (lastDir != null) {
				wd = lastDir;
			} else if (variamosEditor.getCurrentFile() != null) {
				wd = variamosEditor.getCurrentFile().getParent();
			} else {
				wd = System.getProperty("user.dir");
			}

			JFileChooser fc = new JFileChooser(wd);

			// Adds the default file format
			FileFilter defaultFilter = new DefaultFileFilter(".xls",
					"Excel File Format (.xls)");

			fc.setFileFilter(defaultFilter);
			int rc = fc.showDialog(null, mxResources.get("save"));
			// dialogShown = true;

			if (rc != JFileChooser.APPROVE_OPTION) {
				return;
			} else {
				lastDir = fc.getSelectedFile().getParent();
			}

			filename = fc.getSelectedFile().getAbsolutePath();

			if (!filename.endsWith(".xls"))
				filename += ".xls";
			// }
		}
		variamosEditor.callOperations(operations, filename);

		if (operation.startsWith("N:")) {
			variamosEditor.editPropertiesRefas();
			variamosEditor.updateDashBoard(true, false, true);
		}

		variamosEditor.updateSimulResults();
	}
}

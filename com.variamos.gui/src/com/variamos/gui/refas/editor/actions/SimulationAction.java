package com.variamos.gui.refas.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import com.mxgraph.util.mxResources;
import com.variamos.gui.maineditor.BasicGraphEditor;
import com.variamos.gui.maineditor.AbstractEditorAction;
import com.variamos.gui.maineditor.VariamosGraphEditor;
import com.variamos.gui.refas.editor.RefasGraph;
import com.variamos.refas.core.simulationmodel.Refas2Hlcl;

@SuppressWarnings("serial")
public class SimulationAction extends AbstractEditorAction {

	private boolean first;
	private boolean clean;

	public SimulationAction(boolean clean, boolean first) {
		this.first = first;
		this.clean = clean;
	}

	/**
		 * 
		 */
	public void actionPerformed(ActionEvent e) {
		VariamosGraphEditor editor = getEditor(e);
		editor.clearNotificationBar();
		if (clean)
			editor.cleanSimulation();
		else
		{
		//	editor.executeSimulation(first, Refas2Hlcl.DESIGN_EXEC);
			editor.executeSimulation(first, Refas2Hlcl.SIMUL_EXEC, true, "Simul");
		}
	}
}
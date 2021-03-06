package com.variamos.gui.perspeditor.actions;

import java.awt.event.ActionEvent;

import javax.swing.JTextArea;

import com.variamos.gui.core.viewcontrollers.AbstractVariamoGUIAction;
import com.variamos.gui.maineditor.VariamosGraphEditor;

@SuppressWarnings("serial")
public class VerifyFalseProductLineModelAction extends
		AbstractVariamoGUIAction {
	/**
		 * 
		 */
	public void actionPerformed(ActionEvent e) {
		
	//	ProductLine pl = null;
	//	SolverEditorType prologEditorType = SolverEditorType.GNU_PROLOG;

		VariamosGraphEditor editor = getEditor(e);
		
		JTextArea messagesArea = editor.getMessagesArea();
		StringBuilder outputMessage = new StringBuilder();
		
		editor.bringUpTab("Messages");
		
		/*mxGraphComponent graphComponent = editor.getGraphComponent();
		mxGraph graph = graphComponent.getGraph();
		ProductLineGraph plGraph = (ProductLineGraph) graph;
		pl = plGraph.getProductLine();*/
		
		
	//	pl = (ProductLine)editor.getEditedModel();
		
	//	pl.printDebug(System.out);

		// Start verification operations
		// VOID MODEL
		// Start verification operations
		// VOID MODEL
		/*try {
			
			DefectsVerifier verifier = DefectAnalyzerUtil
					.createVerifierClass(pl, prologEditorType);
			
			Defect falseProductLine = verifier.isFalsePLM();
			if (falseProductLine != null) {
				outputMessage.append("Model is a false product line");
			} else {
				outputMessage.append("Model is not a false product line");
			}

		} catch (FunctionalException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			outputMessage.append(e1.getMessage());
		}*/

		// Set the end messages
		messagesArea.setText(outputMessage.toString());
	}
}
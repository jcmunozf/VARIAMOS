package com.variamos.gui.core.viewcontrollers;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import com.variamos.gui.maineditor.MainFrame;
import com.variamos.gui.maineditor.VariamosGraphEditor;

/**
 *  Common behaviors for each action listener inside the main gui view of VariaMos
 *  @author Juan C. Munoz Fernandez <jcmunoz@gmail.com>
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractVariamoGUIAction extends AbstractAction {

	public static final VariamosGraphEditor getEditor(ActionEvent e) {
		if (e.getSource() instanceof Component) {
			Component component = (Component) e.getSource();
			Component childComponent = (Component) e.getSource();

			while (true) {
				if (component == null)
					if (childComponent instanceof JPopupMenu
							&& ((JPopupMenu) childComponent).getInvoker() != null) {

						component = ((JPopupMenu) childComponent).getInvoker();

					} else
						break;
				else {
					if (component instanceof VariamosGraphEditor)
						break;

					if (component instanceof MainFrame)
						break;
					childComponent = component;
					component = component.getParent();
				}
			}

			return (VariamosGraphEditor) component;
		}

		return null;
	}

	public static final Component getComponentEditor(ActionEvent e) {
		if (e.getSource() instanceof Component) {
			Component component = (Component) e.getSource();
			Component childComponent = (Component) e.getSource();

			while (true) {
				if (component == null)
					if (childComponent instanceof JPopupMenu
							&& ((JPopupMenu) childComponent).getInvoker() != null) {

						component = ((JPopupMenu) childComponent).getInvoker();

					} else
						break;
				else {
					if (component instanceof VariamosGraphEditor)
						break;

					if (component instanceof MainFrame)
						break;
					childComponent = component;
					component = component.getParent();
				}
			}

			return component;
		}

		return null;
	}

}


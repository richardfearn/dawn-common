package org.dawb.common.ui.plot;

import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * Class used to fool plotting system that there is a
 * part which is active. This provides then the IAdaptable
 * for tools to work.
 * 
 * @author fcp94556
 *
 */
public class EmptyWorkbenchPart extends WorkbenchPart {

	private IPlottingSystem system;

	public EmptyWorkbenchPart(IPlottingSystem system) {
		this.system = system;
	}
	
	@Override
    public Object getAdapter(final Class clazz) {
		
		 if (clazz == IToolPageSystem.class) {
			return system;
		}
		
		return null;
	}

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}

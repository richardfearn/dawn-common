package org.dawb.common.ui.plot.tool;

import org.dawb.common.ui.plot.IPlottingSystem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * This class represents a page in a page book view which
 * is associated with a specific tool in the plotting system. The tool
 * is a 1D tool meaning it is linked to a 1D plot. It itself does
 * not show a 1D plot normally and if it does, no tools menu will be
 * shown. This prevents recursion which it was decided is too complex
 * for users.
 * 
 * The tool this page connects to currently has some limitations which
 * should be adhered to for the over all UI design to be coherent:
 * 
 * 1. Implement getToolRole() to provide information as to where the tool
 *    page should appear. For instance ToolPageRole.ROLE_2D would have a 1D plot
 *    with tool pages available if their type is ToolPageRole.ROLE_1D, normally.
 *    
 * 2. Do not plot things in an IToolPage with a 1D role directly. These are 1D
 * tools and should plot back to the original 1D view if they have 1D data to plot.
 * This avoids recursion.
 * 
 * If you have to plot on a 1D tool, by default the tools menu will not be shown so you cannot
 * do recursive tools. (This can be overrided by implementing getAdpater(...) for
 * IToolPageSystem if you really need to, but you are breaking the rules a little and
 * some things may not work.)
 * 
 * 
 *
 * 
 * @author fcp94556
 *
 */
public interface IToolPage extends IPageBookViewPage {
	
	public enum ToolPageRole {
		ROLE_2D("org.dawb.common.ui.plot.tool.ROLE_2D", false, true),  // For instance LineProfile, Profile
		ROLE_1D("org.dawb.common.ui.plot.tool.ROLE_1D", true, false),  // 1D only
		ROLE_1D_AND_2D("org.dawb.common.ui.plot.tool.ROLE_1D_AND_2D", true, true); // Measure, derivative, peak fitting
		
		private boolean is1D;
		private boolean is2D;
		private String  id;

		ToolPageRole(String id, boolean is1D, boolean is2D) {
			this.id   = id;
			this.is1D = is1D;
			this.is2D = is2D;
		}
		
		public boolean is1D() {
			return is1D;
		}
		public boolean is2D() {
			return is2D;
		}

		public String getId() {
			return id;
		}
	}
	
	public ToolPageRole getToolPageRole();

	/**
	 * the title for the tool.
	 * @return
	 */
	public String getTitle();
	
	/**
	 * This title will be show in the part displaying this tool
	 * @param title
	 */
	public void setTitle(final String title);
	
	/**
	 * Called when the tool is read from extension for a given 
	 * plotting system instance, used internally.
	 * 
	 * @param system
	 */
	public void setPlottingSystem(IPlottingSystem system);
	
	/**
	 * returns the main plotting system that the tool is
	 * acting on - not the plotting system that this tool
	 * may be showing.
	 * 
	 * @return
	 */
	public IPlottingSystem getPlottingSystem();
	
	/**
	 * The tool system that this page is active within.
	 * @return
	 */
	public IToolPageSystem getToolSystem();
	
	/**
	 * Set the IToolPageSystem, used internally
	 * @param system
	 */
	public void setToolSystem(IToolPageSystem system);
	
	/**
	 * Return true if this is the active tool.
	 * @return
	 */
	public boolean isActive();
	
	/**
	 * Called if the tool is chosen when it already exists.
	 */
	public void activate();
	
	/**
	 * Called if the tool swapped for another but it not disposed.
	 */
	public void deactivate();

	/**
	 * The page on which this tool is used. Normally the page containing the
	 * plotting system which we are currently a tool of.
	 * @return
	 */
	public IWorkbenchPart getPart();
	
	/**
	 * Sets the part this page is linked to.
	 * @param part
	 */
	public void setPart(IWorkbenchPart part);

	/**
	 * The ImageDescriptor for the page
	 * @param des
	 */
	public void setImageDescriptor(ImageDescriptor des);
	
	/**
	 * The ImageDescriptor for the page
	 * @return
	 */
	public ImageDescriptor getImageDescriptor();
}
package org.dawb.common.ui.plot.tool;

import org.dawb.common.ui.plot.region.IRegion;

/**
 * Interface designed to hide special tool pages.
 * @author fcp94556
 *
 */
public interface IProfileToolPage extends IToolPage {

	/**
	 * Line type for Box Line Profiles
	 * @param lineType either SWT.VERTICAL, or SWT.HORIZONTAL
	 */
	void setLineType(int lineType);

	/**
	 * Set the plotAverageProfile flag for BoxLineProfile
	 * @param b
	 */
	void setPlotAverageProfile(boolean b);

	/**
	 * Set the plotEdgeProfile flag for BoxLineProfile
	 * @param b
	 */
	void setPlotEdgeProfile(boolean b);

	/**
	 * Update Profile given an IRegion
	 * @param region
	 */
	void update(IRegion region);

}

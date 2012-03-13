package org.dawb.common.ui.plot.region;

import java.util.Collection;
import java.util.HashSet;


public abstract class AbstractRegion implements IRegion {

	private Collection<IRegionBoundsListener> regionBoundsListeners;
	private boolean regionEventsActive = true;

	/**
	 * Add a listener which is notified when this region is resized or
	 * moved.
	 * 
	 * @param l
	 */
	public boolean addRegionBoundsListener(final IRegionBoundsListener l) {
		if (regionBoundsListeners==null) regionBoundsListeners = new HashSet<IRegionBoundsListener>(11);
		return regionBoundsListeners.add(l);
	}
	
	/**
	 * Remove a RegionBoundsListener
	 * @param l
	 */
	public boolean removeRegionBoundsListener(final IRegionBoundsListener l) {
		if (regionBoundsListeners==null) return false;
		return regionBoundsListeners.remove(l);
	}
	
	protected void clearListeners() {
		if (regionBoundsListeners==null) return;
		regionBoundsListeners.clear();
	}
	
	protected void fireRegionBoundsDragged(RegionBounds bounds) {
		
		if (regionBoundsListeners==null) return;
		if (!regionEventsActive) return;
		
		final RegionBoundsEvent evt = new RegionBoundsEvent(this, bounds);
		for (IRegionBoundsListener l : regionBoundsListeners) {
			l.regionBoundsDragged(evt);
		}
	}
	
	protected void fireRegionBoundsChanged(RegionBounds bounds) {
		
		if (regionBoundsListeners==null) return;
		if (!regionEventsActive) return;
		
		final RegionBoundsEvent evt = new RegionBoundsEvent(this, bounds);
		for (IRegionBoundsListener l : regionBoundsListeners) {
			l.regionBoundsChanged(evt);
		}
	}
	
	protected RegionBounds regionBounds;
	
	public RegionBounds getRegionBounds() {
		return regionBounds;
	}

	public void setRegionBounds(RegionBounds bounds) {
		this.regionBounds = bounds;
		updateRegionBounds();
		fireRegionBoundsChanged(bounds);
	}
	
	/**
	 * Implement to return the real position.
	 * @param recordResult if true this calculation changes the
	 *        recorded absolute position.
	 */
	protected abstract RegionBounds createRegionBounds(boolean recordResult);

	/**
	 * Updates the position of the region, usually called
	 * when items have been created and the position of the 
	 * region should be updated. Does not fire events.
	 */
	protected void updateRegionBounds() {
		if (regionBounds!=null) {
			try {
				this.regionEventsActive = false;
				updateRegionBounds(regionBounds);
			} finally {
				this.regionEventsActive = true;
			}
		}
	}
	
	/**
	 * Implement this method to redraw the figure to the 
	 * axis coordinates (only).
	 * 
	 * @param bounds
	 */
	protected abstract void updateRegionBounds(RegionBounds bounds);
}
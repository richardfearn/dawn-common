/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.preferences.ViewConstants;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.slicing.api.system.ISliceGallery;
import org.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;

/**
 * A part similar to ImageMonitorView but it does not monitor.
 * Instead it navigates a h5 file.
 * 
 * @author gerring
 *
 */
public class H5GalleryView extends ViewPart implements MouseListener, SelectionListener, ISliceGallery {

	public static final String ID = "org.dawb.workbench.views.h5GalleryView"; //$NON-NLS-1$
    
	private static Logger  logger = LoggerFactory.getLogger(H5GalleryView.class);
	
	private H5GalleryInfo            info;
	private MenuAction               dimensionList;
	private GalleryDelegate          galleryDelegate;

	
	public H5GalleryView() {
		this.galleryDelegate = new GalleryDelegate();
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new FillLayout());
		galleryDelegate.createContent("Please choose a directory to monitor...", parent);
		
		createActions();
		initializeToolBar();
		initializeMenu();
		
		//getSite().setSelectionProvider(new GalleryTreeViewer(gallery));
		
		galleryDelegate.addMouseListener(this);
		galleryDelegate.addSelectionListener(this);
	}
	
	private void createImageGallery(H5GalleryInfo info) {
		this.info = info;
		galleryDelegate.setData(info);
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		
		super.init(site);

		try {
			if (memento==null || memento.getString("DIR")==null) return;
			//this.directoryPath = memento.getString("DIR");
		} catch (Exception ne) {
			throw new PartInitException(ne.getMessage());
		}
	}

	@Override
	public void saveState(IMemento memento) {
		try {
			//memento.putString("DIR", directoryPath);
		} catch (Exception e) {
			logger.error("Cannot save plot bean", e);
		}
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		final MenuManager menuManager = new MenuManager();
		galleryDelegate.setMenu(menuManager);
		getSite().registerContextMenu(menuManager, null);
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		
		dimensionList = new MenuAction("Slice dimension");
		dimensionList.setImageDescriptor(Activator.getImageDescriptor("icons/slice_dimension.gif"));
		toolbarManager.add(dimensionList);
		
		Action prefs = new Action("Preferences...", Activator.getImageDescriptor("icons/data.gif")) {
			@Override
			public void run() {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), ViewConstants.PAGE_ID, null, null);
				if (pref != null) pref.open();
			}
		};
		toolbarManager.add(prefs);
		
		getViewSite().getActionBars().getMenuManager().add(prefs);

	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		galleryDelegate.setFocus();
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		//System.out.println("Open slice!");
	}
	
	@Override
	public void mouseDown(MouseEvent e) {
        //updateSelection();
	}
	@Override
	public void widgetSelected(SelectionEvent e) {
		updateSelection();
	}
	
	private void updateSelection() {
		
		final GalleryItem[] items = galleryDelegate.getSelection();
		if (items==null || items.length<1) return;
		
		final IEditorPart part = EclipseUtils.getActiveEditor();
		if (part != null) {
			
			final ISliceSystem sliceComponent = (ISliceSystem)part.getAdapter(ISliceSystem.class);
			if (sliceComponent!=null) {
				sliceComponent.setSliceIndex(info.getSliceDimension(), items[0].getItemCount(), items.length<=1);
			}
			if (items.length<=1) return;
			
			List<IDataset> ys = galleryDelegate.getSelectionData(items);
			final IPlottingSystem system = (IPlottingSystem)part.getAdapter(IPlottingSystem.class);
			system.clear();

			if (ys.get(0).getShape().length==1) {
				system.createPlot1D(null, ys, null);
			} else if (ys.get(0).getShape().length==2) {
				// Average the images, then plot
			    AbstractDataset added = Maths.add(Arrays.asList(ys.toArray(new IDataset[ys.size()])), false);
			    AbstractDataset mean  = Maths.divide(added, ys.size());
			    system.createPlot2D(mean, null, null);
			}
		}
	}
	
	public void dispose() {
		
		galleryDelegate.removeSelectionListener(this);
		galleryDelegate.removeMouseListener(this);
		galleryDelegate.dispose();
		info=null;
		
		super.dispose();

	}
	
	@Override
	public void mouseUp(MouseEvent e) {
		//System.out.println(e);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// Auto-generated method stub
		
	}

	@Override
	public void updateSlice(final ILazyDataset lazySet, final SliceObject slice) {

		final H5GalleryInfo info = new H5GalleryInfo(lazySet);
		info.setShape(lazySet.getShape());		
		info.setSlice(slice);
		info.createDefaultSliceDimension();
		createImageGallery(info);

		dimensionList.clear();
		
		final CheckableActionGroup grp = new CheckableActionGroup();
		final List<Integer> dims = info.getSliceableDimensions();
		for (final int dim : dims) {
			final IAction dimAction = new Action(""+(dim+1), IAction.AS_CHECK_BOX) {
				public void run() {
					info.setSliceDimension(dim);
					galleryDelegate.refreshAll();
				}
			};
			if (info.getSliceDimension()==dim) dimAction.setChecked(true);
			dimAction.setToolTipText("Slice using the dimension "+dim+" of the data.");
			dimensionList.add(dimAction);
			grp.add(dimAction);
		}
		
		getViewSite().getActionBars().getToolBarManager().update(true);
	}

}

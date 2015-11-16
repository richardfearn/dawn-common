/*-
 * Copyright © 2010 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.dawnsci.nexus;

import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.TestUtils;

/**
 * A collection of tests that causes the JVM to crash
 */
public class NexusFileStressTest {
	static String testScratchDirectoryName;
	static String filename;

	/**
	 * Creates an empty directory for use by test code.
	 * 
	 * @throws Exception if setup fails
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(NexusFileStressTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
		filename = testScratchDirectoryName + "file.nxs";
	}

	@Before
	public void deleteFile() {
		File handle = new File(filename);
		if (handle.isFile()) {
			if (!(handle.delete()))
				fail("Unable to delete file " + filename);
		}
	}

	/**
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	@Test(expected=NullPointerException.class)
	public void testConstructor() throws Exception {
		NexusFile file = NexusUtils.openNexusFileReadOnly(null);
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testMakeGroupNPE1() throws Exception {
		NexusFile file = null;
		try {
			file = NexusUtils.createNexusFile(filename);
			file.getGroup(null, true);
			}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testMakeGroupNPE2() throws Exception {
		NexusFile file = null;
		try {
			file = NexusUtils.createNexusFile(filename);
			file.getGroup(null, "entry1", null, true);
		}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testOpenGroupNPE1() throws Exception {
		NexusFile file = null;
		try {
			file = NexusUtils.createNexusFile(filename);
			file.getGroup(null, false);
		}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testOpenGroupNPE2() throws Exception {
		NexusFile file = null;
		try {
			file = NexusUtils.createNexusFile(filename);
			file.getGroup(null, "entry1", null, false);
		}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testMakeDataNPE1() throws Exception {
		NexusFile file = null;
		try {
			file = NexusUtils.createNexusFile(filename);
			file.createData("/entry1:NXentry", DatasetFactory.createFromObject(2.).reshape(1), true);
		}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testMakeDataNPE2() throws Exception {
		NexusFile file = null;
		try {
			file = NexusUtils.createNexusFile(filename);
			file.createData("/entry1:NXentry", DatasetFactory.createFromObject(2.), true);
		}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testMultiDimPutSlab() throws Exception {
		//**// configure here if you like //**//
		// data dimension to write on each slab
		int[] dim = new int[] { 512, 512 };
		// number of slabs
		int numberOfPoints = 2;
		
			// setup data etc. according to configuration
			int rank = dim.length+1;
			int[] dimArray = new int[rank];
			dimArray[0] = ILazyWriteableDataset.UNLIMITED;
			for (int i = 0; i < dim.length; i++) {
				dimArray[i+1] = dim[i];
			}
			int[] startPos = new int[rank];
			int[] stop = dimArray.clone();
			stop[0] = 1;
			Dataset data = DatasetFactory.zeros(dim, Dataset.FLOAT64).reshape(stop);

			// create the file
			NexusFile file = null;
			try {
				file = NexusUtils.createNexusFile(filename);
				ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(null, Dataset.FLOAT64, stop, dimArray, stop);
				file.createData("/entry1:NXentry", lazy, true);
	
				// write the slabs
				dimArray[0] = 1;
				for (int i = 0; i < numberOfPoints; i++) {
					startPos[0] = i;
					stop[0] = startPos[i] + 1;
					lazy.setSlice(null, data, SliceND.createSlice(lazy, startPos, stop));
				}
			}
			finally {
				if (file != null) file.close();
			}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NexusException.class)
	public void testFileNotThere() throws Exception {
			deleteFile();
			NexusFile file = NexusUtils.openNexusFileReadOnly(filename);
			file.getGroup("entry1:NXentry", false);
	}

	public static void main(String[] args) {
	     try {
	    	NexusFileStressTest.setUpBeforeClass();
			NexusFileStressTest nfsst = new NexusFileStressTest();
			nfsst.testFileNotThere();
			System.err.println("not good - no exception");
		} catch (NexusException e) {
			System.out.println("all good - expected behaviour");
		} catch (Exception e) {
			System.err.println("not so good - unexpected exception seen: "+e.getMessage());
		}
	}
}
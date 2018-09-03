ImageJ Plugin for PURE-LET Image Deconvolution
=============
This set of codes is a Java implementation of PURE-LET deconvolution algorithms for an ImageJ plugin. 

Authors: Jizhou Li, Florian Luisier and Thierry Blu

References:
    [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, IEEE Trans. Image Process., 
        vol. 27, no. 1, pp. 92-105, 2018.
    [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
        2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP'16), Phoenix, Arizona, USA, 2016, pp.2708-2712.
    [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
        2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI'17), Melbourne, Australia, 2017, pp. 723-727.
   
Installation
-----------
# Dependencies

<dependencies>
		<dependency>
			<groupId>net.imagej</groupId>
			<artifactId>ij</artifactId>
		</dependency>
		<dependency>
			<groupId>net.sourceforge.csparsej</groupId>
			<artifactId>csparsej</artifactId>
			<version>1.1.1</version>
		</dependency>
		<dependency>
			<groupId>com.github.rwl</groupId>
			<artifactId>optimization</artifactId>
			<version>1.3</version>
		</dependency>
		<dependency>
			<groupId>com.github.rwl</groupId>
			<artifactId>JKLU</artifactId>
			<version>1.0.0</version>
		</dependency>
		<dependency>
			<groupId>com.github.rwl</groupId>
			<artifactId>jplasma</artifactId>
			<version>1.2.0</version>
		</dependency>
		<dependency>
			<groupId>com.googlecode.netlib-java</groupId>
			<artifactId>netlib-java</artifactId>
			<version>1.1</version>
		</dependency>
		<dependency>
			<groupId>com.github.wendykierp</groupId>
			<artifactId>JTransforms</artifactId>
			<version>3.1</version>
		</dependency>
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-math3</artifactId>
			<version>3.6.1</version>
		</dependency>
	</dependencies>

# Installing the plugin

Drag and drop the unzipped plugin .jar file (from the Explorer, Finder or any file browser) into the plugins directory. See https://imagej.net/Installing_3rd_party_plugins for details of the installation process in different systems.

Usage:
-----------

# Demo
This module provides the simulation evaluation. Given the original image and degradation parameters (PSF size, noise levels), the deconvolution performance can be evaluated by the peak-signal-to-noise ratio (PSNR). 

# Run
- Given the blurred noisy image, the parameters of the noise model are automatically estimated or manually adjusted.  
- Click on the "Start" button to launch the deconvolution task.
- Click on the "Stop" button to abort the current deconvolution task.

To-do:
-----------

- The automatical estimation of the PSF size based on the algorithm described in Li et al. "Gaussian blur estimation for photon-limited images", ICIP'17.
- The 3D deconvolution module.


Contact: Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.
Last updated: 23 Aug, 2018
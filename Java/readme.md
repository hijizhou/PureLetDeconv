ImageJ Plugin for PURE-LET Image Deconvolution
=============
This set of codes is a Java implementation of PURE-LET deconvolution algorithms for an ImageJ plugin. 

Authors: Jizhou Li, Florian Luisier and Thierry Blu

References:
- [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, IEEE Trans. Image Process., 
        vol. 27, no. 1, pp. 92-105, 2018.
- [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
        2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP'16), Phoenix, Arizona, USA, 2016, pp.2708-2712.
- [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
        2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI'17), Melbourne, Australia, 2017, pp. 723-727.
   
### Installation
-----------
#### Dependencies

```
+ net.imagj - ij
+ net.sourceforge.csparsej - 1.1.1
+ com.github.rwl - optimization - 1.3
+ com.github.rwl - JKLU - 1.0.0
+ com.github.rwl - jplasma - 1.2.0
+ com.googlecode.netlib-java - netlib-java - 1.1
+ com.github.wendykierp - Jtransforms - 3.1
+ org.apache.commons - commons-math3 - 3.6.1
+ org.jblas - jblas - 1.2.4
```

#### Installing the plugin

#### Using ImageJ2/Fiji

The PURE-LET algorithm is embeded in the AoEtools plugin, which is distributed using an ImageJ2/Fiji update site.

To install the plugins using Fiji (an ImageJ distribution) just follow the instructions How_to_follow_a_3rd_party_update_site (https://imagej.net/Following_an_update_site) and add the AoEtools update site (http://sites.imagej.net/AoEtools).

All the plugins will appear under the 'Plugins > AoEtools > PureLetDeconv2D' menu.

#### Using ImageJ (version 1)

If you do not want to use ImageJ2/Fiji to get the plugins then you can download the Jar file and put them in your ImageJ plugins folder. The Jars can be found here: http://www.ee.cuhk.edu.hk/~tblu/monsite/phps/pureletdeconv.php. See https://imagej.net/Installing_3rd_party_plugins for details of the installation process in different systems.

Note that the jblas jar file is needed to be put into the ImageJ jars folder.

### Usage:
-----------

#### Demo
This module provides the simulation evaluation. Given the original image and degradation parameters (PSF size, noise levels), the deconvolution performance can be evaluated by the peak-signal-to-noise ratio (PSNR). 

#### Run
- Given the blurred noisy image, the parameters of the noise model are automatically calculated (based on some physical parameters) or manually adjusted.  
- Clic on the "Estimate" under the Noise section to estimate the Poisson noise level, you may need to tune it a little bit.
- Click on the "Start Deconvolution" button to launch the deconvolution task.
- Click on the "Stop" button to abort the current deconvolution task.

### To-do:
-----------

- The automatical estimation of the PSF size based on the algorithm described in Li et al. "Gaussian blur estimation for photon-limited images", ICIP'17.
- The 3D deconvolution module.
- Image sequence processing supports.

### Updates:
-----------

- Add multi-channel image processing support.

Contact: Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.

Last updated: 25 Nov, 2018

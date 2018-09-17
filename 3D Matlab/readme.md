3D PURE-LET Deconvolution for Fluorescence Microscopy Images
=============
This set of codes is a Matlab implementation of PURE-LET deconvolution algorithms for 3D fluorescence microscopy images. 

Authors: Jizhou Li, Florian Luisier and Thierry Blu

References:
- [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, IEEE Trans. Image Process., 
        vol. 27, no. 1, pp. 92-105, 2018.
- [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
        2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP'16), Phoenix, Arizona, USA, 2016, pp.2708-2712.
- [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
        2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI'17), Melbourne, Australia, 2017, pp. 723-727.
   
Usage
-----------
This version implements the 3D PURE-LET non-blind deconvolution algorithm, both the image and PSF data are needed.

Example:
-----------
```
addpath('Utilities/');
addpath('Data/');
addpath('Funs');

% load the original image
I0 = aux_stackread('Pollen.tif'); 

% load the PSF, which has the same size as the original image
PSF = aux_stackread('PSF.tif'); 

% noise settings: 
%   [alpha] scaling factor of Poisson noise; 
%   [nsigma^2] variance of Gaussian noise;  
options.alpha = 0.2; options.nsigma = 0.2;

% generate the measurement
[input, options] = aux_acquisition(I0, PSF, options);

% PURE-LET image deconvolution
output = PURE_LET_3D(input, PSF, options);

% visualize the measurement and deconvolved image
aux_sliceViewer(input,output);
```
-----------
Or directly run 'demo.m' (more details inside).

Visualization
-----------
Three ways for visualization are supported.
* Slice view (default): the slice comparisons are displayed. See 'aux_sliceViewer.m' for details.
```
aux_sliceViewer(input,output);
```
* Maximum intensity projection. 
* Use Icy (http://icy.bioimageanalysis.org) for 3D rendering. 
    For the interaction between Matlab and Icy, the plugin "Matlab communicator"(http://icy.bioimageanalysis.org/plugin/Matlab_communicator) is needed.
```
icy_im3show(output);
```

Contact: Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.

Last updated: 23 Aug, 2018

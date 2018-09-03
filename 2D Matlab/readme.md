PURE-LET Image Deconvolution
=============
This set of codes is a Matlab implementation of PURE-LET image deconvolution algorithms. 

Authors: Jizhou Li, Florian Luisier and Thierry Blu

References:
    [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, IEEE Trans. Image Process., 
        vol. 27, no. 1, pp. 92-105, 2018.
    [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
        2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP'16), Phoenix, Arizona, USA, 2016, pp.2708-2712.
    [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
        2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI'17), Melbourne, Australia, 2017, pp. 723-727.
  
Model
-----------  
Acquisition model: input = alpha*Poisson(H*original/alpha) + Gaussian(0, nsigma^2);
Point-spread function (PSF): H
Noise levels:
    alpha : scaling factor of Poisson noise
    nsigma: noise std of additive Gaussian noise
 
Usage
-----------
This version implements the PURE-LET non-blind deconvolution algorithm, both the image and PSF data are needed.

Example:
-----------
directly run 'demo.m' (more details inside)

Contact:
-----------
Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.

Last updated: 23 Aug, 2018
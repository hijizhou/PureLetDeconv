function rse = aux_RSE(psf, gt)
%AUX_RSE Compute the relative squared error (RSE) of two images
%   rse = AUX_RSE(psf, gt) calculates the RSE for the estimated psf, with
%   the ground truth gt as the reference.
%
%  Example:
%       rse = aux_RSE(psf, gt);
%
%   See also aux_PSNR
%
% AUTHORS: Jizhou Li, Florian Luisier and Thierry Blu
%
% REFERENCES:
%     [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, 
%           IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
%     [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
%           2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
%     [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
%           2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
%     [4] J. Li, F. Xue and T. Blu, Fast and accurate three-dimensional point spread function computation
%           for fluorescence microscopy, J. Opt. Soc. Am. A, vol. 34, no. 6, pp. 1029-1034, 2017.
%
% CONTACT: Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.
%
% Last updated: 08 Nov, 2017

rse = sum((psf(:)-gt(:)).^2)./sum(gt(:).^2)*100;
end
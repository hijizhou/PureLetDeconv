function [input, options] = aux_acquisition(I0, PSF, options)
%%AUX_ACQUISITION: Simulation of the measurement acquisition. Mixed
%%Poisson-Gaussian noise model is considered.
%
% USAGE: [input, options] = aux_acquisition(I0, PSF, options)
%
% Model
% -----------  
%   Acquisition model: input = alpha*Poisson(H*original/alpha) + Gaussian(0, nsigma^2);
%
% INPUT:
%        'I0'           -  the original image (ground truth)
%        'PSF'          -  the point-spread function (PSF), same size as I0
%        'options'      -  algorithm settings
%           'alpha'     -  scaling factor of Poisson noise (default 0.2)
%           'nsigma'    -  noise std of additive Gaussian noise (default 0)
%
% OUTPUT:
%         'input'       -  the blurred noisy image
%         'options'     -  the acquisition settings
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
%
% CONTACT: Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.
%
% Last updated: 08 Nov, 2017

if ~isfield(options, 'nsigma')
    options.nsigma = 0;
end
if ~isfield(options, 'alpha')
    options.nsigma = 0.2;
end
%%%%%%%%%%%%%%   add blurring     %%%%%%%%%%%%%
PSF = PSF./sum(PSF(:));
H = fftn(fftshift(PSF));
blurred = abs(ifftn(H.*fftn(I0)));   % generate the blurred image

%%%%%%%%%%%%%%   add noise (Poisson+Gaussian)    %%%%%%%%%%%%%?8
s = RandStream('mt19937ar','seed',0);
RandStream.setGlobalStream(s);
noise = randn(size(I0));
noise = options.nsigma*noise/std(noise(:));
input = options.alpha*poissrnd(blurred/options.alpha) + noise;

end
function y = aux_awgn(x,sigma,seed)
% FUNCTION: generate the degradated image by additive Gaussian noise
%
% Authors: Jizhou Li, Florian Luisier and Thierry Blu
% References:
%     [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, 
%             IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
%     [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
%               2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
%     [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
%               2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
%
% Contact: Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.
%   
% Last updated: 23 Aug, 2018

if(~exist('seed','var'))
    seed = 0;
end
RandStream.setGlobalStream(RandStream.create('mt19937ar','seed',seed));
n = randn(size(x));
if(numel(n)>1)
    n = n/std(n(:));
end
y = x+sigma*n;
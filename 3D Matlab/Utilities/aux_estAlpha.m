function alpha = aux_estAlpha(input)
%%AUX_ESTALPHA: Estimate the Poisson noise level, alpha
%    based on Laplacian filter (it works perfectly when only this noise parameter needs to be estimated)
% 
% Noise acquisition model: input = alpha*Poisson(original/alpha);
%
% INPUT:  input -- the noisy (or blurred noisy) image
% OUTPUT: alpha -- the noise parameter
%
% AUTHORS: Jizhou Li and Thierry Blu
%         The Chinese University of Hong Kong
%
% REFERENCES:
%     [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, 
%           IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
%     [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
%           2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
%     [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
%           2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
%
% CONTACT:
%   Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.
%
% Last updated: 08 Nov, 2017

[~,~,nz] = size(input);

if nz==1
    % two dimensional
    LPfilter = [0 1 0;1 -4 1;0 1 0];
else
    % three dimensional
    LPfilter(:,:,1) = [0 0 0; 0 1 0; 0 0 0];
    LPfilter(:,:,2) = [0 1 0; 1 -6 1; 0 1 0];
    LPfilter(:,:,3) = [0 0 0; 0 1 0; 0 0 0];
end
    
LPfilter = LPfilter/sqrt(sum(LPfilter(:).^2));

J0 = abs(imfilter(input,LPfilter)).^2;
alpha = mean(J0(:))/mean(input(:));
end
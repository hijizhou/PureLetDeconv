function [alpha, sigma] = aux_estNoisePar(input)
%% AUX_ESTNOISEPAR: Estimate the noise parameter based on a robust linear regression performed on a
%  collection of local estimates of the sample mean and sample variance.
%
% Noise acquisition model: input = alpha*Poisson(original/alpha) + Gaussian(0, sigma^2);
%
% USAGE: [alpha, sigma] = aux_estNoisePar(input)
%
% INPUT:  input -- the noisy (or blurred noisy) image
% OUTPUT: alpha, sigma -- the noise parameters
%
% AUTHORS: Jizhou Li, Florian Luisier and Thierry Blu
%         
% REFERENCES:
%     [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, 
%           IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
%     [2] F. Luisier, T. Blu, and M. Unser, Image denoising in mixed Poisson- Gaussian noise,
%           IEEE Trans. Image Process., vol. 20, no. 3, pp. 696? 708, 2011.
%
% CONTACT:
%   Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.
%
% Last updated: 08 Nov, 2017

warning off;
% check the dimension of input
if ismatrix(input)
    [alpha, sigma] = est_slice_noise(input);
else
    % 3D image, currently use the median values for the whole stack
    nz = size(input,3);
    disp('--------   Estimating the noise level slice by slice   --------');
    disp('          (alpha: the scaling factor of Poisson noise)         ');
    disp('          (sigma^2: variance of Gaussian noise)         ');
    for zi = 1:nz
        disp(['Slice: ' num2str(zi) '/' num2str(nz)]);
        [tmpA, tmpB] = est_slice_noise(input(:,:,zi));
        Alpha(zi) = tmpA;
        Sigma(zi) = tmpB;
    end
    alpha = median(Alpha);
    sigma = median(Sigma);
end

end

function [alpha, sigma] = est_slice_noise(slice)

wsize = 4;
meanKernel = ones(wsize,wsize); meanKernel = meanKernel./sum(meanKernel(:));
Mean = imfilter(slice, meanKernel);
Mean2 = imfilter(slice.^2, meanKernel);
Var = Mean2 - Mean.^2;
[Xpix, index] = sort(Mean(:), 'ascend');
Ypix = Var(:);
Ypix = Ypix(index);
brob = robustfit(Xpix, Ypix,'bisquare', 3);
alpha = brob(2);
sigma = real(sqrt(brob(1)));

end

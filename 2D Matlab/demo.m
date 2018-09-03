%% Demo for PURE-LET Image Deconvolution
% This set of codes is a Matlab implementation of PURE-LET deconvolution algorithms. 
% The mixed Poisson-Gaussian noise (including pure Poisson noise) is assumed.
%
% Model
% -----------  
%   Acquisition model: input = alpha*Poisson(H*original/alpha) + Gaussian(0, nsigma^2);
%   Point-spread function (PSF): H
%   Noise levels:
%           options.alpha : scaling factor of Poisson noise
%           options.nsigma: noise std of additive Gaussian noise
%
% Example (high noise case):
% -------------------------------------------------------------------------
% >> demo  % see MW_PURE_LET.m for more details
% -------------------------------------------------------------------------
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
clear; clc;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
addpath('Utilities/');
addpath('Funs/');
addpath('Data/');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Load the original noise-free image
%-----------------------------------
Filename = {
    'fluocells.tif',
    %      'cameraman.tif'
    };
F  = length(Filename);
NR = 1; % number of noise realizations

%%%%%%%%%%%%%%%%%%%%  Image Degredation %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% PSF
params.type = 'gaussian';
params.var  = 5;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
params.oracle = 0; % MSE-LET or PURE-LET

for f = 1:F
    filename = Filename{f};
    original = double(imread(filename));
    
    Mx       = max(original(:));
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    params.size = size(original);
    params = aux_blur_operator(params);
    params = aux_reg_operator(params);
    blurred = abs(ifft2(params.H.*fft2(original)));
    Mblurred2 = mean(blurred(:).^2);
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    Sigma = [
         50;
        ];
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    Alpha = [
        50;
        ];
    Imax = Mx*ones(size(Alpha));
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    NS = numel(Alpha);
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    SNR0  = zeros(NS,NR);
    PSNR0 = zeros(NS,NR);
    SNR   = zeros(NS,NR);
    PSNR  = zeros(NS,NR);
    Time  = zeros(NS,NR);
    for S = 1:NS
        blurred  = Imax(S)*blurred/Mx;
        original = Imax(S)*original/Mx;
        params.original = original;
        for R = 1:NR
            params.alpha = Alpha(S);
            params.sigma = Sigma(S);
            s = RandStream('mt19937ar','seed',R-1);
            RandStream.setGlobalStream(s);
            input = params.alpha*poissrnd(blurred/params.alpha);
            input = aux_awgn(input,params.sigma,R-1);
         
            params.input = input;
            start = tic;
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            % Multi-Wiener PURE-LET
            %-----------------------------------
            output = MW_PURE_LET(params);
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            Time(S,R) = toc(start);
            PSNR0(S,R) = aux_PSNR(input,original);
            PSNR(S,R) = aux_PSNR(output,original);
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            fprintf('*');
        end
    end
    fprintf(['\n' filename '\n']);
    avgPSNR0 = mean(PSNR0,2);
    avgPSNR  = mean(PSNR,2);
    avgTime  = mean(Time,2);
end

if(NR==1&&NS==1) 
    %display results
    display(['Input PSNR: ' num2str(avgPSNR0) ' dB']);
    display(['Output PSNR: ' num2str(avgPSNR) ' dB']);
    display(['Running time: ' num2str(avgTime) ' s']);
    subplot(1,2,1); imshow(input, []);
    subplot(1,2,2); imshow(output, []);
end

%% Demo for 3D PURE-LET Deconvolution
% This set of codes is a Matlab implementation of PURE-LET deconvolution algorithms
%   for 3D fluorescence microscopy images. The mixed Poisson-Gaussian noise is assumed.
% 
% The demo includes:
%         (1) Simulation: the measurement is generated based on the
%              loaded original image and PSF, and the noise levels.
%         (2) Deconvolution: the measurement is deconvolved by PURE-LET
%             deconvolution algorithm.
%         (3) Analysis: the Peak Signal-to-noise-ratio (PSNR) is
%             computed and the slices of measurement and deconvolved image
%             are compared visually.
%
% Model
% -----------  
%   Acquisition model: input = alpha*Poisson(H*original/alpha) + Gaussian(0, nsigma^2);
%   Noise levels:
%           options.alpha : scaling factor of Poisson noise
%           options.nsigma: noise std of additive Gaussian noise
%
% Example:
% -------------------------------------------------------------------------
% % load the original image
% I0 = aux_stackread('Pollen.tif'); 
% 
% % load the PSF, which has the same size as the original image
% PSF = aux_stackread('PSF.tif'); 
% 
% % noise settings: 
% %   [alpha] scaling factor of Poisson noise; 
% %   [nsigma^2] variance of Gaussian noise;  
% options.alpha = 0.2; options.nsigma = 0.2;
% 
% % generate the measurement
% [input, options] = aux_acquisition(I0, PSF, options);
% 
% % PURE-LET image deconvolution
% output = PURE_LET_3D(input, PSF, options);
% 
% % visualize the measurement and deconvolved image
% aux_sliceViewer(input,output);
% -------------------------------------------------------------------------
%
% Visualization
% -----------
% Three ways for visualization are supported.
% + Slice view (default): the slice comparisons are displayed. See 'aux_sliceViewer.m' for details.
% + Maximum intensity projection. 
% + Use Icy (http://icy.bioimageanalysis.org) for 3D rendering. 
%     For the interaction between Matlab and Icy, the plugin "Matlab communicator"
%       (http://icy.bioimageanalysis.org/plugin/Matlab_communicator) is needed.
%    >> icy_im3show(output);
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
% Last updated: 08 Nov, 2017

clear; clc;
addpath('Utilities/');
addpath('Data/');
addpath('Funs');

%%
%%%%%%%%%%%%%%%%%%%%%%%%%%   Simulation Settings    %%%%%%%%%%%%%%%%%%%%%%%
% disp('--------------------        Simulation         --------------------');
I0 = aux_stackread('Pollen.tif');  % load the original ground-truth image
PSF = aux_stackread('PSF.tif');   % load the PSF
options.nsigma = 0.2;  % noise std of additive Gaussian noise
options.alpha = 0.2; % scaling factor of Poisson noise
[input, options] = aux_acquisition(I0, PSF, options); % generate the measurement

%%
%%%%%%%%%%%%%%%%%%%%%%%%%% 3D PURE-LET Deconvolution %%%%%%%%%%%%%%%%%%%%%%
[output, time] = PURE_LET_3D(input, PSF, options);

inputPSNR = aux_PSNR(input,I0);
outputPSNR = aux_PSNR(output,I0);

%%
%%%%%%%%%%%%%%%%%%%%%%%%%%   Quantitative results    %%%%%%%%%%%%%%%%%%%%%%
disp('-------------------------------------------------------------------');
disp(['Input   PSNR : ' num2str(inputPSNR,4) ' dB']);
disp(['Output  PSNR : ' num2str(outputPSNR,4) ' dB']);
disp(['Running Time : ' num2str(time,4) ' s'])

%%
%%%%%%%%%%%%%%%%%%%%%%%%%%     Visualization         %%%%%%%%%%%%%%%%%%%%%%
% 1 - SliceView in Matlab, see aux_sliceViewer
% 2 - Maximum Intensity Projection (MIP) in Matlab
% 3 - 3D rendering in Icy (http://icy.bioimageanalysis.org).
vis_type = 1;
switch vis_type
    case 1
        aux_sliceViewer(input,output);
    case 2
        inputT = permute(input, [3 2 1]);
        outputT = permute(output, [3 2 1]);
        inputMIP = aux_MIP(input);
        outputMIP = aux_MIP(output);
        inputTMIP = aux_MIP(inputT);
        outputTMIP = aux_MIP(outputT);
        inputMIP = aux_imscale(inputMIP, [0, max(outputMIP)]);
        inputTMIP = aux_imscale(inputTMIP, [0, max(outputTMIP)]);
        
        imshow([inputMIP outputMIP; inputTMIP outputTMIP],[])       
    case 3
        icy_im3show(input);
        icy_im3show(output);
end
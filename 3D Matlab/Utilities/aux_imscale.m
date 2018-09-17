function imgt = aux_imscale(img, range)
%% AUX_IMSCALE: Scale an image to fit the range
%       This function scales the values of an image to fit the range.
%
% INPUT:
%       img   = The image.
%       range  = The min target value. (def=[0,1])
% OUTPUT: 
%       imgt  = The image scaled.
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

% Check params:
if nargin<2
    range=[0,1];
end

% Transform the image type:
imgt = double(img);

% Obtain and appling the offset:
imgt = imgt-min(min(min(imgt)));

% Obtain and appling the scale:
scale = max(max(max(imgt)));
if scale>0
    imgt = imgt/scale;
end

% Generate the required image:
if not(abs(range(2)-range(1))==1)
    imgt = imgt*abs(range(2)-range(1));
end
if not(range(1)==0)
    imgt = imgt+range(1);
end
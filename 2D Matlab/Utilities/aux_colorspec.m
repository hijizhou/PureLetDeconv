function aux_colorspec(color)
%%AUX_COLORSPEC: specify a colormap to current figure,  without background 
%
% USAGE: aux_colorspec(color)
%
% INPUT:
%        'color'   -  the color to be assigned.
%              'red', 'green' (default), and 'blue'     
%
% AUTHOR: Jizhou Li
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

t = linspace(0,1,256)';
switch color
    case 'red'
        cm = t*[1 0 0];
    case 'green'
        cm = t*[0 1 0];
    case 'blue'
        cm = t*[0 0 1];
end

colormap(cm);

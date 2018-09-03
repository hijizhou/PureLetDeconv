function Y = aux_PSNR(I,I0,reference)
%%AUX_PSNR: Compute the PSNR between I and I0
%   This function also supports signal, energy or contrast
%
% USAGE: Y = aux_PSNR(I,I0,reference)
% 
% INPUT: 
%       I           -  the target 
%       I0          -  the reference
%       reference   -  the type of these signals
%           'image' (default), 'signal', 'energy', 'contrast'
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

if nargin<=2
    reference='image';
end
I=I(:);
I0=I0(:);

switch reference
    case 'image'
        Y=-20*log10(norm(I-I0)/max(abs(I0))/sqrt(length(I0)));
    case 'signal'
        Y=-20*log10(norm(I-I0)/max(abs(I0))/sqrt(length(I0)));
    case 'energy'
        Y=-20*log10(norm(I-I0)/norm(I0));
    case 'contrast'
        A=[I0(:)'*I0(:) sum(I0(:));sum(I0(:)) length(I0(:))];
        B=[I0(:)'*I(:);sum(I(:))];
        a=A\B;
        Y=-20*log10(norm(I-(a(1)*I0+a(2)))/norm(a(1)*I0+a(2)));
    otherwise
        Y=-20*log10(norm(I0-I)/reference/sqrt(length(I0)));
end
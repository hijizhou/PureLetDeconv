function [stack,info] = aux_stackread(filename)
%AUX_STACKREAD: Read a stack of images and convert it to a double 3D
%   matrix.
%
% USAGE:  [stack,info] = aux_stackread(filename) 
%       It reads the stack named 'filename'
%   and returns a 3D double matrix 'stack' and the stack info 'info'.
%
% INPUT: 
%       filename : the name of the stack file
%
% OUTPUT:
%       stack: the extracted double 3D matrix.
%       info : informations about the stack.
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


info = imfinfo(filename);
nz   = size(info,1);
nx   = info(1).Height;
ny   = info(1).Width;
type = info(1).ColorType;
if(strcmp(type,'truecolor'))
    C = 3;
else
    C = 1;
end
stack = zeros(nx,ny,C*nz);
for z=1:nz
    stack(:,:,(z-1)*C+1:z*C) = double(imread(filename,z));
end
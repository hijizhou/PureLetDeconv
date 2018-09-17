function aux_stackwrite(stack, filename)
%AUX_STACKREAD: Read a stack of images and convert it to a double 3D
%   matrix.
% 
% USEAGE: aux_stackwrite(stack, filename)
%
% INPUT:
%   stack: the 3D image
%   filename: string containing the name of the stack.
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

% info = imfinfo(filename);
% nz = size(info,1);
% nx = info(1).Height;
% ny = info(1).Width;
% type = info(1).ColorType;
% if(strcmp(type,'truecolor'))
%     C = 3;
% else
%     C = 1;
% end
% stack = zeros(nx,ny,C*nz);

[nx,ny,nz] = size(stack);
for z=1:nz
    imwrite(uint8(stack(:,:,z)), filename, 'WriteMode', 'append');
end

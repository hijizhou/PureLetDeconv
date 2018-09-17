function S = aux_reg(nx, ny, nz)
% AUX_REG: regulization operator used in the Wiener filter
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

x = linspace(-1,1,nx);
y = linspace(-1,1,ny);
z = linspace(-1,1,nz);

[X,Y,Z] = meshgrid(x,y,z);
S = X.*X + Y.*Y + Z.*Z;
S = max(S(:))-S;
if(mod(nx,2)==0)&&(mod(ny,2)==0)&&(mod(nz,2)==0)
    S = S(1:nx/2+1,1:ny/2+1,1:nz/2+1);
    S = padarray(S,[nx,ny,nz]/2,'symmetric','post');
    S(nx/2+1,:,:) = [];
    S(:,ny/2+1,:) = [];
    S(:,:,nz/2+1) = [];
end
if(mod(nx,2)==0)&&(mod(ny,2)==1)&&(mod(nz,2)==0)
    S = S(1:nx/2+1,1:(ny+1)/2,1:nz/2+1);
    S = padarray(S,[nx,(ny-1),nz]/2,'symmetric','post');
    S(nx/2+1,:,:) = [];
    S(:,:,nz/2+1) = [];
end
if(mod(nx,2)==0)&&(mod(ny,2)==1)&&(mod(nz,2)==1)
    S = S(1:nx/2+1,1:(ny+1)/2,1:(nz+1)/2);
    S = padarray(S,[nx,(ny-1),(nz-1)]/2,'symmetric','post');
    S(nx/2+1,:,:) = [];
end

if(mod(nx,2)==1)&&(mod(ny,2)==0)&&(mod(nz,2)==0)
    S = S(1:(nx+1)/2,1:ny/2+1,1:nz/2+1);
    S = padarray(S,[(nx-1),ny,nz]/2,'symmetric','post');
    S(:,ny/2+1,:) = [];
    S(:,:,nz/2+1) = [];
end
if(mod(nx,2)==1)&&(mod(ny,2)==0)&&(mod(nz,2)==1)
    S = S(1:(nx+1)/2,1:ny/2+1,1:(nz+1)/2);
    S = padarray(S,[(nx-1),ny,(nz-1)]/2,'symmetric','post');
    S(:,ny/2+1,:) = [];
end
if(mod(nx,2)==1)&&(mod(ny,2)==1)&&(mod(nz,2)==0)
    S = S(1:(nx+1)/2,1:(ny+1)/2,1:nz/2+1);
    S = padarray(S,([nx-1,ny-1,nz])/2,'symmetric','post');
    S(:,:,nz/2+1) = [];
end
if(mod(nx,2)==1)&&(mod(ny,2)==1)&&(mod(nz,2)==1)
    S = S(1:(nx+1)/2,1:(ny+1)/2,1:(nz+1)/2);
    S = padarray(S,([nx-1,ny-1,nz-1])/2,'symmetric','post');
end
if(mod(nx,2)==0)&&(mod(ny,2)==0)&&(mod(nz,2)==1)
    S = S(1:nx/2+1,1:ny/2+1,1:(nz+1)/2);
    S = padarray(S,[nx,ny,nz-1]/2,'symmetric','post');
    S(nx/2+1,:,:) = [];
    S(:,ny/2+1,:) = [];
end

S = single(S);

end
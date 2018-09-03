function params = aux_reg_operator(params)
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
params.reg = 'paper';
nx = params.size(1);
ny = params.size(2);
s  = zeros(nx,ny);
c  = [1,1]; 
switch params.reg
    case 'paper'
        x = linspace(-1,1,nx);
        y = linspace(-1,1,ny);
        [X,Y] = meshgrid(x,y);
        S = X.*X+Y.*Y;
        S = max(S(:))-S;   
        if(mod(nx,2)==0)&&(mod(ny,2)==0)
            S = S(1:nx/2+1,1:ny/2+1);
            S = padarray(S,[nx,ny]/2,'symmetric','post');
            S(nx/2+1,:) = [];
            S(:,ny/2+1) = [];
        end
        if(mod(nx,2)==0)&&(mod(ny,2)==1)
            S = S(1:nx/2+1,1:(ny+1)/2);
            S = padarray(S,[nx,(ny-1)]/2,'symmetric','post');
            S(nx/2+1,:) = [];
        end
        if(mod(nx,2)==1)&&(mod(ny,2)==0)
            S = S(1:(nx+1)/2,1:ny/2+1);
            S = padarray(S,[(nx-1),ny]/2,'symmetric','post');
            S(:,ny/2+1) = [];
        end
        if(mod(nx,2)==1)&&(mod(ny,2)==1)
            S = S(1:(nx+1)/2,1:(ny+1)/2);
            S = padarray(S,([nx,ny]-1)/2,'symmetric','post');
        end
        params.S = S;    
    case 'laplacian'
        wx = pi*linspace(-1,1,nx);
        wy = pi*linspace(-1,1,ny);
        [Wx,Wy] = meshgrid(wx,wy);
        S = -(Wx.*Wx+Wy.*Wy);
        params.S = fftshift(S);
    case 'laplacian4'
        s(1:3,1:3) = [0 1 0;1 -4 1;0 1 0]; 
        params.S = fft2(circshift(s,-c));
    case 'laplacian6'
        s(1:3,1:3) = [1/2 1 1/2;1 -6 1;1/2 1 1/2];  
        params.S = fft2(circshift(s,-c));
    case 'laplacian8'
        s(1:3,1:3) = [1 1 1;1 -8 1;1 1 1];  
        params.S = fft2(circshift(s,-c));
    case 'gradient'
        s(1:2,1:2) = kron([1,-1]',[1,-1])/2;
        params.S = fft2(circshift(s,-c));
    case 'identity'
        s(1,1) = 1;
        c = [0,0];
        params.S = fft2(circshift(s,-c));
end
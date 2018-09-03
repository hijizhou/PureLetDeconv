function params = aux_blur_operator(params)
% AUX_REG: blurring operator
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

nx = params.size(1);
ny = params.size(2);
params.fname = [params.type num2str(nx) 'x' num2str(ny)];
switch params.type
    case 'separable'
        h = [1 4 6 4 1]'*[1 4 6 4 1]./256;
        lx = 5; ly = 5;
        center = ceil(([ly,lx]-1)/2);
        params.fname = [params.fname '_' num2str(lx) 'x' num2str(ly)];
    case 'uniform'
        h = zeros(nx,ny);
        lx = params.length(1);
        ly = params.length(2);
        h(1:lx,1:ly) = 1/(lx*ly);
        center = ceil(([ly,lx]-1)/2);
        params.fname = [params.fname '_' num2str(lx) 'x' num2str(ly)];
    case 'radial'
        h = zeros(nx,ny);
        lx = params.length(1);
        ly = params.length(2);
        [x,y] = meshgrid(-lx:lx,-ly:ly);
        h(1:2*lx+1,1:2*ly+1) = 1./(1+x.^2+y.^2);
        h = h/sum(h(:));
        center = [ly,lx];
        params.fname = [params.fname '_' num2str(lx) 'x' num2str(ly)];
    case 'gaussian'
        [x,y] = meshgrid(-ny/2:ny/2-1,-nx/2:nx/2-1);
        h = exp(-(x.^2+y.^2)/(2*params.var));
        h = h/sum(h(:));
        center = [nx,ny]/2;
        params.fname = [params.fname '_' num2str(params.var,'%.1f')];
    case 'real'
        load h;
        M = nx; N = ny; [s1,s2] = size(h);
        % From kernel h to frequency response T
        T = zeros(M,N);
        s12 = ceil(s1/2);
        s22 = ceil(s2/2);
        T(1:s12,1:s22) = h(s12:s1,s22:s2);
        T(M-s12+2:M,1:s22) = h(1:s12-1,s22:s2);
        T(1:s12,N-s22+2:N) = h(s12:s1,1:s22-1);
        T(M-s12+2:M,N-s22+2:N) = h(1:s12-1,1:s22-1);
        params.psf = T;
        params.H = fft2(T);
        return;
        center = [nx,ny]/2;
        params.fname = [params.fname '_' num2str(params.var,'%.1f')];
        
    case 'other'
        h = zeros(nx,ny);
        [lx,ly] = size(params.psf);
        h(1:lx,1:ly) = params.psf;
        center = ceil(([ly,lx]-1)/2);
        params.fname = [params.fname '_' num2str(lx) 'x' num2str(ly)];
end
params.psf = fftshift(h);
params.H = fft2(circshift(h,-center),nx,ny);